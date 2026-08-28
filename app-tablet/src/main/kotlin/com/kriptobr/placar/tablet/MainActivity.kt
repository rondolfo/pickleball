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
        private const val MS_DESTRAVADO = 8_000L
    }

    private val eventos: SnapshotStateList<Evento> = mutableStateListOf()
    private val jogadores: SnapshotStateList<Jogador> = mutableStateListOf()
    private val idsVistos = mutableSetOf<String>()
    private val relogioDeTrava = Handler(Looper.getMainLooper())
    private var tarefaDeTrava: Runnable? = null

    private lateinit var repositorio: Repositorio
    private lateinit var voz: Voz
    private var servidor: ServidorPlacar? = null

    private var idPartida by mutableStateOf(UUID.randomUUID().toString())
    private var inicioPartida by mutableStateOf(System.currentTimeMillis())
    private var duplaEsquerda by mutableStateOf(Dupla())
    private var duplaDireita by mutableStateOf(Dupla())
    private var primeiroSaque by mutableStateOf(Lado.ESQUERDA)

    private var clientes by mutableStateOf(0)
    private var enderecoLocal by mutableStateOf("")
    private var travado by mutableStateOf(true)
    private var idiomaUi by mutableStateOf(Textos.EN)
    private var idiomaVoz by mutableStateOf(Textos.EN)
    private var vozIndisponivel by mutableStateOf(false)
    private var versaoFotos by mutableStateOf(0)

    private var menuAberto by mutableStateOf(false)
    private var telaJogadores by mutableStateOf(false)
    private var telaDuplas by mutableStateOf(false)
    private var telaHistorico by mutableStateOf(false)
    private var confirmandoNovoGame by mutableStateOf(false)
    private var escolhendoPrimeiroSaque by mutableStateOf(false)
    private var partidaExibida by mutableStateOf<Partida?>(null)
    private var partidaEncerrada by mutableStateOf<Partida?>(null)
    private var avisoResultado by mutableStateOf("")
    private var historico by mutableStateOf<List<Partida>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        repositorio = Repositorio(this)
        carregarPreferencias()
        jogadores.addAll(repositorio.carregarJogadores())
        enderecoLocal = ipLocal()
        restaurarPartidaEmAndamento()

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
                nomeEsquerda = nomeDoLado(Lado.ESQUERDA),
                nomeDireita = nomeDoLado(Lado.DIREITA),
                idiomaUi = idiomaUi,
                idiomaVoz = idiomaVoz,
                conectado = clientes > 0,
                endereco = enderecoLocal,
                travado = travado,
                onPonto = { lado -> registrarRally(lado, Origem.TOQUE); adiarTrava() },
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
                    onDuplas = { menuAberto = false; telaDuplas = true },
                    onJogadores = { menuAberto = false; telaJogadores = true },
                    onHistorico = {
                        historico = repositorio.listarPartidas()
                        menuAberto = false
                        telaHistorico = true
                    },
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

            if (telaJogadores) {
                TelaJogadores(
                    jogadores = jogadores.toList(),
                    idiomaUi = idiomaUi,
                    versaoFotos = versaoFotos,
                    onSalvar = { salvarJogador(it) },
                    onExcluir = { excluirJogador(it) },
                    onFechar = { telaJogadores = false }
                )
            }

            if (telaDuplas) {
                TelaDuplas(
                    jogadores = jogadores.toList(),
                    duplaEsquerda = duplaEsquerda,
                    duplaDireita = duplaDireita,
                    idiomaUi = idiomaUi,
                    versaoFotos = versaoFotos,
                    onMudar = { lado, dupla ->
                        if (lado == Lado.ESQUERDA) duplaEsquerda = dupla else duplaDireita = dupla
                        salvarAndamento()
                    },
                    onFechar = { telaDuplas = false }
                )
            }

            if (telaHistorico) {
                TelaHistorico(
                    partidas = historico,
                    jogadores = jogadores.toList(),
                    idiomaUi = idiomaUi,
                    onAbrir = { partidaExibida = it },
                    onFechar = { telaHistorico = false }
                )
            }

            val paraMostrar = partidaEncerrada ?: partidaExibida
            if (paraMostrar != null) {
                TelaResultado(
                    partida = paraMostrar,
                    jogadores = jogadores.toList(),
                    idiomaUi = idiomaUi,
                    aviso = avisoResultado,
                    mostrarNovoGame = partidaEncerrada != null,
                    onEmail = { enviarEmail(paraMostrar) },
                    onNovoGame = {
                        partidaEncerrada = null
                        avisoResultado = ""
                        escolhendoPrimeiroSaque = true
                    },
                    onFechar = {
                        partidaEncerrada = null
                        partidaExibida = null
                        avisoResultado = ""
                    }
                )
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
            if (anterior.encerrado) return@runOnUiThread

            eventos.add(Evento(id = id, vencedor = vencedor, origem = origem, ts = agora))
            val novo = Regras.derivar(eventos, primeiroSaque)

            voz.anunciar(anterior, novo)
            servidor?.transmitir(estadoJson())
            salvarAndamento()

            if (novo.encerrado) encerrarPartida()
        }
    }

    private fun desfazer() {
        if (eventos.isEmpty()) return
        voz.cancelarPendente()
        val removido = eventos.removeAt(eventos.lastIndex)
        idsVistos.remove(removido.id)
        partidaEncerrada = null
        servidor?.transmitir(estadoJson())
        salvarAndamento()
    }

    private fun encerrarPartida() {
        val partida = montarPartida(fim = System.currentTimeMillis(), completa = true)
        repositorio.salvarPartida(partida)
        repositorio.limparAtual()
        partidaEncerrada = partida
        avisoResultado = ""
    }

    private fun novoGame(saqueInicial: Lado) {
        val emAndamento = eventos.isNotEmpty() && partidaEncerrada == null
        if (emAndamento) {
            repositorio.salvarPartida(montarPartida(fim = null, completa = false))
        }
        voz.cancelarPendente()
        eventos.clear()
        idsVistos.clear()
        partidaEncerrada = null
        avisoResultado = ""
        idPartida = UUID.randomUUID().toString()
        inicioPartida = System.currentTimeMillis()
        primeiroSaque = saqueInicial
        travado = true
        repositorio.limparAtual()
        servidor?.transmitir(estadoJson())
    }

    private fun inverterLados() {
        val espelhados = eventos.map { it.copy(vencedor = it.vencedor.oposto()) }
        eventos.clear()
        eventos.addAll(espelhados)
        primeiroSaque = primeiroSaque.oposto()
        val guardada = duplaEsquerda
        duplaEsquerda = duplaDireita
        duplaDireita = guardada
        servidor?.transmitir(estadoJson())
        salvarAndamento()
    }

    private fun montarPartida(fim: Long?, completa: Boolean) = Partida(
        id = idPartida,
        inicio = inicioPartida,
        fim = fim,
        primeiroSaque = primeiroSaque,
        duplaEsquerda = duplaEsquerda,
        duplaDireita = duplaDireita,
        eventos = eventos.toList(),
        completa = completa
    )

    private fun salvarAndamento() {
        repositorio.salvarAtual(montarPartida(fim = null, completa = false))
    }

    private fun restaurarPartidaEmAndamento() {
        val salva = repositorio.carregarAtual() ?: return
        idPartida = salva.id
        inicioPartida = salva.inicio
        primeiroSaque = salva.primeiroSaque
        duplaEsquerda = salva.duplaEsquerda
        duplaDireita = salva.duplaDireita
        eventos.addAll(salva.eventos)
        salva.eventos.forEach { idsVistos.add(it.id) }
    }

    // ---------- jogadores ----------

    private fun salvarJogador(jogador: Jogador) {
        val indice = jogadores.indexOfFirst { it.id == jogador.id }
        if (indice >= 0) jogadores[indice] = jogador else jogadores.add(jogador)
        jogadores.sortBy { it.nome.lowercase() }
        repositorio.salvarJogadores(jogadores.toList())
        versaoFotos += 1
    }

    private fun excluirJogador(jogador: Jogador) {
        jogadores.removeAll { it.id == jogador.id }
        runCatching { repositorio.arquivoFoto(jogador.id).delete() }
        repositorio.salvarJogadores(jogadores.toList())
        if (duplaEsquerda.a == jogador.id) duplaEsquerda = duplaEsquerda.copy(a = null)
        if (duplaEsquerda.b == jogador.id) duplaEsquerda = duplaEsquerda.copy(b = null)
        if (duplaDireita.a == jogador.id) duplaDireita = duplaDireita.copy(a = null)
        if (duplaDireita.b == jogador.id) duplaDireita = duplaDireita.copy(b = null)
        versaoFotos += 1
    }

    private fun nomeDoLado(lado: Lado): String {
        val dupla = if (lado == Lado.ESQUERDA) duplaEsquerda else duplaDireita
        if (dupla.vazia) {
            return Textos.get(if (lado == Lado.ESQUERDA) "esquerda" else "direita", idiomaUi)
        }
        return ResumoEmail.nomeDupla(dupla, jogadores.toList(), idiomaUi)
    }

    // ---------- email ----------

    private fun enviarEmail(partida: Partida) {
        val lista = jogadores.toList()
        if (ResumoEmail.destinatarios(partida, lista).isEmpty()) {
            avisoResultado = Textos.get("sem_email", idiomaUi)
            return
        }
        val abriu = ResumoEmail.abrir(this, partida, lista, idiomaUi)
        avisoResultado = if (abriu) "" else Textos.get("sem_app_email", idiomaUi)
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
        relogioDeTrava.postDelayed(tarefa, MS_DESTRAVADO)
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
