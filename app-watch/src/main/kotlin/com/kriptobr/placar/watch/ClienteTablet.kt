package com.kriptobr.placar.watch

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.Protocolo
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections
import java.util.UUID

/**
 * Cliente do relogio.
 *
 * Acha o tablet por tres caminhos, em cascata:
 *   1. endereco salvo da ultima vez que funcionou
 *   2. anuncio do servico na rede local
 *   3. varredura da faixa de enderecos, como ultimo recurso
 *
 * Assim a conexao deixa de depender de um unico mecanismo, e o jogador
 * nunca precisa digitar endereco em quadra.
 *
 * Pontos ficam numa fila ate o tablet confirmar. Reenvio e seguro porque
 * cada evento tem identificador proprio e o tablet ignora repetidos.
 */
class ClienteTablet(
    private val contexto: Context,
    private val aoReceberEstado: (JSONObject) -> Unit,
    private val aoMudarConexao: (Boolean) -> Unit,
    private val aoConfirmarPonto: () -> Unit,
    private val aoMudarProcura: (String) -> Unit
) {

    companion object {
        private const val TAG = "ClienteTablet"
        private const val PREFS = "placar"
        private const val CHAVE_HOST = "ultimo_host"
        private const val INTERVALO_REENVIO = 700L
        private const val TENTATIVAS_ANTES_DE_REDESCOBRIR = 3
    }

    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val imediatas = Channel<String>(Channel.UNLIMITED)
    private val pendentes = Collections.synchronizedMap(LinkedHashMap<String, String>())

    private val cliente = HttpClient(CIO) { install(WebSockets) }

    @Volatile private var host: String? = null
    @Volatile private var porta: Int = Protocolo.PORTA
    private var falhasSeguidas = 0

    private var nsdManager: NsdManager? = null
    private var listenerBusca: NsdManager.DiscoveryListener? = null

    fun iniciar() {
        procurarNaRede()
        escopo.launch {
            while (isActive) {
                val alvo = host ?: resolverHost()
                if (alvo == null) {
                    delay(1000)
                    continue
                }
                host = alvo
                conectar(alvo, porta)
                aoMudarConexao(false)
                falhasSeguidas++
                if (falhasSeguidas >= TENTATIVAS_ANTES_DE_REDESCOBRIR) {
                    host = null
                    falhasSeguidas = 0
                }
                delay(1200)
            }
        }
    }

    fun parar() {
        runCatching {
            val gerenciador = nsdManager
            val listener = listenerBusca
            if (gerenciador != null && listener != null) gerenciador.stopServiceDiscovery(listener)
        }
        runCatching { cliente.close() }
        escopo.cancel()
    }

    fun enviarRally(vencedor: Lado) {
        val id = UUID.randomUUID().toString()
        val mensagem = JSONObject().apply {
            put("tipo", Protocolo.TIPO_RALLY)
            put("id", id)
            put("vencedor", vencedor.name)
            put("ts", System.currentTimeMillis())
        }.toString()
        pendentes[id] = mensagem
    }

    fun enviarDesfazer() {
        val mensagem = JSONObject().apply { put("tipo", Protocolo.TIPO_DESFAZER) }.toString()
        imediatas.trySend(mensagem)
    }

    fun pontosNaFila(): Int = pendentes.size

    // ---------- conexao ----------

    private suspend fun conectar(alvo: String, portaAlvo: Int) {
        runCatching {
            cliente.webSocket("ws://$alvo:$portaAlvo${Protocolo.CAMINHO}") {
                aoMudarConexao(true)
                aoMudarProcura("")
                falhasSeguidas = 0
                salvarHost(alvo)
                Log.i(TAG, "conectado em $alvo:$portaAlvo")

                val reenviador = launch {
                    while (isActive) {
                        val copia = synchronized(pendentes) { pendentes.values.toList() }
                        copia.forEach { runCatching { send(Frame.Text(it)) } }
                        delay(INTERVALO_REENVIO)
                    }
                }

                val avulsas = launch {
                    for (mensagem in imediatas) send(Frame.Text(mensagem))
                }

                try {
                    for (quadro in incoming) {
                        if (quadro is Frame.Text) tratar(quadro.readText())
                    }
                } finally {
                    reenviador.cancel()
                    avulsas.cancel()
                }
            }
        }.onFailure { Log.w(TAG, "conexao caiu: ${it.message}") }
    }

    private fun tratar(texto: String) {
        runCatching {
            val objeto = JSONObject(texto)
            when (objeto.optString("tipo")) {
                Protocolo.TIPO_ACK -> {
                    val id = objeto.optString("id")
                    if (pendentes.remove(id) != null) aoConfirmarPonto()
                }
                Protocolo.TIPO_ECO -> {
                    // devolve o mesmo horario para o tablet medir a ida e volta
                    val resposta = JSONObject().apply {
                        put("tipo", Protocolo.TIPO_ECO_RESP)
                        put("ts", objeto.optLong("ts"))
                    }.toString()
                    imediatas.trySend(resposta)
                }
                else -> aoReceberEstado(objeto)
            }
        }
    }

    // ---------- descoberta em cascata ----------

    private suspend fun resolverHost(): String? {
        val salvo = lerHostSalvo()
        if (salvo != null) {
            aoMudarProcura("...")
            if (portaAberta(salvo, porta, 600)) return salvo
        }

        aoMudarProcura("...")
        repeat(12) {
            host?.let { return it }
            delay(500)
        }

        aoMudarProcura("scan")
        return varrerSubrede()
    }

    /**
     * Ultimo recurso: testa a porta do placar em toda a faixa da rede local.
     * Com concorrencia limitada, leva poucos segundos e evita digitar endereco.
     */
    private suspend fun varrerSubrede(): String? {
        val meu = ipDoRelogio() ?: return null
        val prefixo = meu.substringBeforeLast(".")
        val limite = Semaphore(40)

        return coroutineScope {
            val tentativas = (1..254).map { final ->
                async {
                    val alvo = "$prefixo.$final"
                    limite.withPermit {
                        if (portaAberta(alvo, porta, 350)) alvo else null
                    }
                }
            }
            tentativas.awaitAll().firstOrNull { it != null }
        }
    }

    private suspend fun portaAberta(alvo: String, portaAlvo: Int, tempoLimite: Int): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                Socket().use { conexao ->
                    conexao.connect(InetSocketAddress(alvo, portaAlvo), tempoLimite)
                    true
                }
            }.getOrDefault(false)
        }

    private fun ipDoRelogio(): String? = runCatching {
        NetworkInterface.getNetworkInterfaces()
            .toList()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
            ?.hostAddress
    }.getOrNull()

    private fun procurarNaRede() {
        val gerenciador = contexto.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
        nsdManager = gerenciador

        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(tipo: String) {
                Log.i(TAG, "procurando o tablet na rede")
            }

            override fun onServiceFound(info: NsdServiceInfo) {
                if (!info.serviceType.contains("placarpb")) return
                resolver(gerenciador, info)
            }

            override fun onServiceLost(info: NsdServiceInfo) {}
            override fun onDiscoveryStopped(tipo: String) {}
            override fun onStartDiscoveryFailed(tipo: String, codigo: Int) {
                Log.e(TAG, "falha ao iniciar busca, codigo $codigo")
            }
            override fun onStopDiscoveryFailed(tipo: String, codigo: Int) {}
        }

        listenerBusca = listener
        runCatching {
            gerenciador.discoverServices(
                Protocolo.TIPO_SERVICO,
                NsdManager.PROTOCOL_DNS_SD,
                listener
            )
        }
    }

    private fun resolver(gerenciador: NsdManager, info: NsdServiceInfo) {
        val resolvedor = object : NsdManager.ResolveListener {
            override fun onResolveFailed(info: NsdServiceInfo, codigo: Int) {}
            override fun onServiceResolved(info: NsdServiceInfo) {
                val endereco = info.host?.hostAddress ?: return
                host = endereco
                porta = info.port
                Log.i(TAG, "tablet encontrado em $endereco:${info.port}")
            }
        }
        runCatching { gerenciador.resolveService(info, resolvedor) }
    }

    private fun lerHostSalvo(): String? =
        contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CHAVE_HOST, null)

    private fun salvarHost(endereco: String) {
        contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(CHAVE_HOST, endereco)
            .apply()
    }
}
