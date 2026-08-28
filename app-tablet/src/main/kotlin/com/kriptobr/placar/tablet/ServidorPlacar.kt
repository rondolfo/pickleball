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
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Collections

/**
 * Servidor do tablet.
 *
 * O tablet e a unica fonte de verdade. Ele escuta eventos de rally de
 * qualquer cliente e devolve o estado calculado para todos.
 *
 * Anuncia o servico na rede local, para o relogio achar sozinho.
 * Digitar endereco na mao em quadra nao e uma opcao viavel.
 */
class ServidorPlacar(
    private val contexto: Context,
    private val aoReceberRally: (id: String, vencedor: Lado) -> Unit,
    private val aoReceberDesfazer: () -> Unit,
    private val estadoJson: () -> String,
    private val aoMudarClientes: (Int) -> Unit
) {

    companion object {
        const val PORTA = Protocolo.PORTA
        const val CAMINHO = Protocolo.CAMINHO
        const val TIPO_SERVICO = Protocolo.TIPO_SERVICO
        const val NOME_SERVICO = Protocolo.NOME_SERVICO
        private const val TAG = "ServidorPlacar"
    }

    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sessoes = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketServerSession>())

    private var pararServidor: (() -> Unit)? = null
    private var nsdManager: NsdManager? = null
    private var listenerRegistro: NsdManager.RegistrationListener? = null

    fun iniciar() {
        val motor = embeddedServer(CIO, port = PORTA, host = "0.0.0.0") {
            install(WebSockets)
            routing {
                webSocket(CAMINHO) {
                    sessoes.add(this)
                    notificarClientes()
                    runCatching { send(Frame.Text(estadoJson())) }
                    try {
                        for (quadro in incoming) {
                            if (quadro is Frame.Text) tratarMensagem(quadro.readText())
                        }
                    } catch (erro: Exception) {
                        Log.w(TAG, "sessao encerrada: ${erro.message}")
                    } finally {
                        sessoes.remove(this)
                        notificarClientes()
                    }
                }
            }
        }
        motor.start(wait = false)
        pararServidor = { runCatching { motor.stop(500, 1000) } }
        anunciarNaRede()
        Log.i(TAG, "servidor no ar na porta $PORTA")
    }

    fun parar() {
        removerAnuncio()
        pararServidor?.invoke()
        pararServidor = null
    }

    /** Envia o estado atual para todos os clientes conectados. */
    fun transmitir(json: String) {
        escopo.launch {
            val copia = synchronized(sessoes) { sessoes.toList() }
            copia.forEach { sessao ->
                runCatching { sessao.send(Frame.Text(json)) }
            }
        }
    }

    private fun tratarMensagem(texto: String) {
        runCatching {
            val objeto = JSONObject(texto)
            when (objeto.optString("tipo")) {
                Protocolo.TIPO_RALLY -> {
                    val id = objeto.optString("id")
                    val vencedor = Lado.valueOf(objeto.optString("vencedor"))
                    if (id.isNotEmpty()) aoReceberRally(id, vencedor)
                }
                Protocolo.TIPO_DESFAZER -> aoReceberDesfazer()
                Protocolo.TIPO_PING -> transmitir(estadoJson())
            }
        }.onFailure { Log.w(TAG, "mensagem invalida: $texto") }
    }

    private fun notificarClientes() {
        aoMudarClientes(sessoes.size)
    }

    private fun anunciarNaRede() {
        val gerenciador = contexto.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
        nsdManager = gerenciador

        val info = NsdServiceInfo().apply {
            serviceName = NOME_SERVICO
            serviceType = TIPO_SERVICO
            port = PORTA
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
