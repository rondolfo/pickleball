package com.kriptobr.placar.tablet

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.Protocolo
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Collections

/**
 * Servidor do tablet. O tablet e a unica fonte de verdade.
 *
 * Cada evento recebido gera uma confirmacao individual de volta, para o
 * relogio saber que o ponto entrou e poder vibrar. Sem isso, com o tablet
 * longe, o jogador nao tem como saber se o toque valeu.
 */
class ServidorPlacar(
    private val contexto: Context,
    private val aoReceberRally: (id: String, vencedor: Lado) -> Unit,
    private val aoReceberDesfazer: () -> Unit,
    private val aoReceberInverter: () -> Unit = {},
    private val estadoJson: () -> String,
    private val aoMudarClientes: (Int) -> Unit,
    private val aoReceberEco: (Long) -> Unit = {},
    private val aoTerContato: () -> Unit = {}
) {

    companion object {
        private const val TAG = "ServidorPlacar"
    }

    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sessoes = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketServerSession>())

    private var pararServidor: (() -> Unit)? = null
    private var nsdManager: NsdManager? = null
    private var listenerRegistro: NsdManager.RegistrationListener? = null

    fun iniciar() {
        val motor = embeddedServer(CIO, port = Protocolo.PORTA, host = "0.0.0.0") {
            install(WebSockets)
            routing {
                webSocket(Protocolo.CAMINHO) {
                    sessoes.add(this)
                    aoMudarClientes(sessoes.size)
                    runCatching { send(Frame.Text(estadoJson())) }
                    try {
                        for (quadro in incoming) {
                            if (quadro is Frame.Text) {
                                val resposta = tratarMensagem(quadro.readText())
                                if (resposta != null) runCatching { send(Frame.Text(resposta)) }
                            }
                        }
                    } catch (erro: Exception) {
                        Log.w(TAG, "sessao encerrada: ${erro.message}")
                    } finally {
                        sessoes.remove(this)
                        aoMudarClientes(sessoes.size)
                    }
                }
            }
        }
        motor.start(wait = false)
        pararServidor = { runCatching { motor.stop(500, 1000) } }
        anunciarNaRede()
        Log.i(TAG, "servidor no ar na porta ${Protocolo.PORTA}")
    }

    fun parar() {
        removerAnuncio()
        pararServidor?.invoke()
        pararServidor = null
    }

    /** Dispara um eco para medir o tempo de ida e volta ate o relogio. */
    fun medirIdaEVolta() {
        val mensagem = JSONObject().apply {
            put("tipo", Protocolo.TIPO_ECO)
            put("ts", System.currentTimeMillis())
        }.toString()
        transmitir(mensagem)
    }

    /** Derruba as sessoes abertas, para testar a reconexao do relogio. */
    fun derrubarSessoes() {
        escopo.launch {
            val copia = synchronized(sessoes) { sessoes.toList() }
            copia.forEach { sessao ->
                runCatching { sessao.close() }
            }
        }
    }

    fun transmitir(json: String) {
        escopo.launch {
            val copia = synchronized(sessoes) { sessoes.toList() }
            copia.forEach { sessao -> runCatching { sessao.send(Frame.Text(json)) } }
        }
    }

    /** Devolve a confirmacao a ser enviada de volta, ou null quando nao ha. */
    private fun tratarMensagem(texto: String): String? {
        aoTerContato()
        return runCatching {
            val objeto = JSONObject(texto)
            when (objeto.optString("tipo")) {
                Protocolo.TIPO_RALLY -> {
                    val id = objeto.optString("id")
                    val vencedor = Lado.valueOf(objeto.optString("vencedor"))
                    if (id.isEmpty()) return@runCatching null
                    aoReceberRally(id, vencedor)
                    JSONObject().apply {
                        put("tipo", Protocolo.TIPO_ACK)
                        put("id", id)
                    }.toString()
                }
                Protocolo.TIPO_DESFAZER -> {
                    aoReceberDesfazer()
                    null
                }
                Protocolo.TIPO_INVERTER -> {
                    aoReceberInverter()
                    null
                }
                Protocolo.TIPO_PING -> estadoJson()
                Protocolo.TIPO_ECO_RESP -> {
                    aoReceberEco(objeto.optLong("ts"))
                    null
                }
                else -> null
            }
        }.getOrElse {
            Log.w(TAG, "mensagem invalida: $texto")
            null
        }
    }

    private fun anunciarNaRede() {
        val gerenciador = contexto.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
        nsdManager = gerenciador

        val info = NsdServiceInfo().apply {
            serviceName = Protocolo.NOME_SERVICO
            serviceType = Protocolo.TIPO_SERVICO
            port = Protocolo.PORTA
        }

        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                Log.i(TAG, "servico anunciado como ${info.serviceName}")
            }

            override fun onRegistrationFailed(info: NsdServiceInfo, codigo: Int) {
                Log.e(TAG, "falha ao anunciar servico, codigo $codigo")
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) {}
            override fun onUnregistrationFailed(info: NsdServiceInfo, codigo: Int) {}
        }

        listenerRegistro = listener
        runCatching { gerenciador.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener) }
    }

    private fun removerAnuncio() {
        val gerenciador = nsdManager ?: return
        val listener = listenerRegistro ?: return
        runCatching { gerenciador.unregisterService(listener) }
        listenerRegistro = null
    }
}
