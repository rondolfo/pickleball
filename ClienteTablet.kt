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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

/**
 * Cliente do relogio.
 *
 * Acha o tablet sozinho na rede local, conecta e reconecta em silencio.
 * O relogio nunca decide placar, ele apenas informa quem ganhou o rally.
 */
class ClienteTablet(
    private val contexto: Context,
    private val aoReceberEstado: (JSONObject) -> Unit,
    private val aoMudarConexao: (Boolean) -> Unit
) {

    companion object {
        private const val TAG = "ClienteTablet"
    }

    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fila = Channel<String>(Channel.UNLIMITED)

    private val cliente = HttpClient(CIO) {
        install(WebSockets)
    }

    @Volatile
    private var host: String? = null

    @Volatile
    private var porta: Int = Protocolo.PORTA

    private var nsdManager: NsdManager? = null
    private var listenerBusca: NsdManager.DiscoveryListener? = null

    fun iniciar() {
        procurarTablet()
        escopo.launch {
            while (isActive) {
                val alvo = host
                if (alvo == null) {
                    delay(1000)
                    continue
                }
                conectar(alvo, porta)
                aoMudarConexao(false)
                delay(1500)
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
        val mensagem = JSONObject().apply {
            put("tipo", Protocolo.TIPO_RALLY)
            put("id", UUID.randomUUID().toString())
            put("vencedor", vencedor.name)
            put("ts", System.currentTimeMillis())
        }.toString()
        fila.trySend(mensagem)
    }

    fun enviarDesfazer() {
        val mensagem = JSONObject().apply {
            put("tipo", Protocolo.TIPO_DESFAZER)
        }.toString()
        fila.trySend(mensagem)
    }

    private suspend fun conectar(alvo: String, portaAlvo: Int) {
        runCatching {
            cliente.webSocket("ws://$alvo:$portaAlvo${Protocolo.CAMINHO}") {
                aoMudarConexao(true)
                Log.i(TAG, "conectado em $alvo:$portaAlvo")

                val enviador = launch {
                    for (mensagem in fila) {
                        send(Frame.Text(mensagem))
                    }
                }

                try {
                    for (quadro in incoming) {
                        if (quadro is Frame.Text) {
                            runCatching { aoReceberEstado(JSONObject(quadro.readText())) }
                        }
                    }
                } finally {
                    enviador.cancel()
                }
            }
        }.onFailure { Log.w(TAG, "conexao caiu: ${it.message}") }
    }

    private fun procurarTablet() {
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

            override fun onServiceLost(info: NsdServiceInfo) {
                Log.w(TAG, "servico sumiu da rede")
            }

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
            override fun onResolveFailed(info: NsdServiceInfo, codigo: Int) {
                Log.w(TAG, "falha ao resolver, codigo $codigo")
            }

            override fun onServiceResolved(info: NsdServiceInfo) {
                val endereco = info.host?.hostAddress ?: return
                host = endereco
                porta = info.port
                Log.i(TAG, "tablet encontrado em $endereco:${info.port}")
            }
        }
        runCatching { gerenciador.resolveService(info, resolvedor) }
    }
}
