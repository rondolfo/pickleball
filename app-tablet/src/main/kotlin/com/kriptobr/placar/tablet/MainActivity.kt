package com.kriptobr.placar.tablet

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kriptobr.placar.core.EstadoJogo
import com.kriptobr.placar.core.Evento
import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.Origem
import com.kriptobr.placar.core.Regras
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.UUID

class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS = "placar"
        private const val CHAVE_IDIOMA_UI = "idioma_ui"
        private const val CHAVE_IDIOMA_VOZ = "idioma_voz"
        private const val SEGUNDOS_DESTRAVADO = 8_000L
    }

    private val eventos: SnapshotStateList<Evento> = mutableStateListOf()
    private val idsVistos = mutableSetOf<String>()
    private val relogioDeTrava = Handler(Looper.getMainLooper())
    private var tarefaDeTrava: Runnable? = null

    private var primeiroSaque by mutableStateOf(Lado.ESQUERDA)
    private var clientes by mutableStateOf(0)
    private var enderecoLocal by mutableStateOf("")
    private var travado by mutableStateOf(true)
    private var menuAberto by mutableStateOf(false)
    private var confirmandoNovoGame by mutableStateOf(false)
    private var escolhendoPrimeiroSaque by mutableStateOf(false)
    private var idiomaUi by mutableStateOf(Textos.EN)
    private var idiomaVoz by mutableStateOf(Textos.EN)
    private var vozIndisponivel by mutableStateOf(false)

    private var servidor: ServidorPlacar? = null
    private lateinit var voz: Voz

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        carregarPreferencias()
        enderecoLocal = ipLocal()

        voz = Voz(this).also { motor ->
            motor.iniciar {
                motor.definirIdioma(idiomaVoz)
                vozIndisponivel = !motor.disponivel(idiomaVoz)
            }
        }

        servidor = ServidorPlacar(
            contexto = this,
            aoReceberRally = { id, vencedor -> registrarRally(vencedor, Origem.RELOGIO, id) },
            aoReceberDesfazer = { runOnUiThread { desfazer() } },
            estadoJson = { estadoJson() },
            aoMudarClientes = { total -> runOnUiThread { clientes = total } }
        ).also { it.iniciar() }

        setContent {
            val estado = Regras.derivar(eventos, primeiroSaque)

            TelaPlacar(
                estado = estado,
                idiomaUi = idiomaUi,
                idiomaVoz = idiomaVoz,
                conectado = clientes > 0,
                endereco = enderecoLocal,
                travado = travado,
                onPonto = { lado ->
                    registrarRally(lado, Origem.TOQUE)
                    adiarTrava()
                },
                onDesfazer = { desfazer(); adiarTrava() },
                onDestravar = { destravar() },
                onAbrirMenu = { menuAberto = true },
                onTrocarIdiomaVoz = { trocarIdiomaVoz() },
                onRepetir = { voz.repetir() }
            )

            if (menuAberto) {
                MenuAjustes(
                    idiomaUi = idiomaUi,
                    idiomaVoz = idiomaVoz,
                    vozIndisponivel = vozIndisponivel,
                    onNovoGame = { menuAberto = false; confirmandoNovoGame = true },
                    onInverter = { inverterLados(); menuAberto = false },
                    onTrocarIdiomaVoz = { trocarIdiomaVoz() },
                    onTrocarIdiomaTela = { trocarIdiomaTela() },
                    onRepetir = { voz.repetir(); menuAberto = false },
                    onFechar = { menuAberto = false }
                )
            }

            if (confirmandoNovoGame) {
                DialogoConfirmar(
                    mensagem = Textos.get("confirmar_novo", idiomaUi),
                    textoSim = Textos.get("sim", idiomaUi),
                    textoNao = Textos.get("nao", idiomaUi),
                    onSim = { confirmandoNovoGame = false; escolhendoPrimeiroSaque = true },
                    onNao = { confirmandoNovoGame = false }
                )
            }

            if (escolhendoPrimeiroSaque) {
                DialogoPrimeiroSaque(idiomaUi = idiomaUi) { lado ->
                    novoGame(lado)
                    escolhendoPrimeiroSaque = false
                }
            }
        }
    }

    override fun onDestroy() {
        tarefaDeTrava?.let { relogioDeTrava.removeCallbacks(it) }
        voz.liberar()
        servidor?.parar()
        servidor = null
        super.onDestroy()
    }

    // ---------- jogo ----------

    private fun registrarRally(
        vencedor: Lado,
        origem: Origem,
        id: String = UUID.randomUUID().toString()
    ) {
        runOnUiThread {
            if (!idsVistos.add(id)) return@runOnUiThread

            val agora = System.currentTimeMillis()
            val ultimo = eventos.lastOrNull()
            if (ultimo != null && agora - ultimo.ts < 1000L) return@runOnUiThread

            val anterior = Regras.derivar(eventos, primeiroSaque)
            eventos.add(Evento(id = id, vencedor = vencedor, origem = origem, ts = agora))
            val novo = Regras.derivar(eventos, primeiroSaque)

            voz.anunciar(anterior, novo)
            servidor?.transmitir(estadoJson())
        }
    }

    private fun desfazer() {
        if (eventos.isEmpty()) return
        voz.cancelarPendente()
        val removido = eventos.removeAt(eventos.lastIndex)
        idsVistos.remove(removido.id)
        servidor?.transmitir(estadoJson())
    }

    private fun novoGame(saqueInicial: Lado) {
        voz.cancelarPendente()
        eventos.clear()
        idsVistos.clear()
        primeiroSaque = saqueInicial
        travado = true
        servidor?.transmitir(estadoJson())
    }

    /**
     * Inverter lados espelha o log inteiro. O placar, o saque e o numero do
     * sacador acompanham, entao a tela continua correta depois da troca.
     */
    private fun inverterLados() {
        val espelhados = eventos.map { it.copy(vencedor = it.vencedor.oposto()) }
        eventos.clear()
        eventos.addAll(espelhados)
        primeiroSaque = primeiroSaque.oposto()
        servidor?.transmitir(estadoJson())
    }

    // ---------- travamento ----------

    private fun destravar() {
        travado = false
        adiarTrava()
    }

    private fun adiarTrava() {
        tarefaDeTrava?.let { relogioDeTrava.removeCallbacks(it) }
        val tarefa = Runnable { travado = true }
        tarefaDeTrava = tarefa
        relogioDeTrava.postDelayed(tarefa, SEGUNDOS_DESTRAVADO)
    }

    // ---------- idiomas ----------

    private fun trocarIdiomaVoz() {
        idiomaVoz = if (idiomaVoz == Textos.EN) Textos.PT else Textos.EN
        voz.definirIdioma(idiomaVoz)
        vozIndisponivel = !voz.disponivel(idiomaVoz)
        salvarPreferencias()
    }

    private fun trocarIdiomaTela() {
        idiomaUi = if (idiomaUi == Textos.EN) Textos.PT else Textos.EN
        salvarPreferencias()
    }

    private fun carregarPreferencias() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        idiomaUi = prefs.getString(CHAVE_IDIOMA_UI, Textos.EN) ?: Textos.EN
        idiomaVoz = prefs.getString(CHAVE_IDIOMA_VOZ, Textos.EN) ?: Textos.EN
    }

    private fun salvarPreferencias() {
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(CHAVE_IDIOMA_UI, idiomaUi)
            .putString(CHAVE_IDIOMA_VOZ, idiomaVoz)
            .apply()
    }

    // ---------- rede ----------

    private fun estadoJson(): String {
        val estado: EstadoJogo = Regras.derivar(eventos, primeiroSaque)
        return JSONObject().apply {
            put("tipo", "ESTADO")
            put("esq", estado.pontosEsquerda)
            put("dir", estado.pontosDireita)
            put("sacando", estado.sacando.name)
            put("sacador", estado.sacador)
            put("chamada", estado.chamada)
            put("encerrado", estado.encerrado)
            put("pontoDeJogo", estado.pontoDeJogo)
        }.toString()
    }

    private fun ipLocal(): String = runCatching {
        NetworkInterface.getNetworkInterfaces()
            .toList()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
            ?.hostAddress
            ?: "sem rede"
    }.getOrDefault("sem rede")
}
