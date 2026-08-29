package com.kriptobr.placar.tablet

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
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
import com.kriptobr.placar.core.Formacao
import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.MemoriaRodizio
import com.kriptobr.placar.core.Origem
import com.kriptobr.placar.core.Regras
import com.kriptobr.placar.core.Rodizio
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    private val escalacoes: SnapshotStateList<Escalacao> = mutableStateListOf()
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
    private var estadoBase by mutableStateOf<EstadoJogo?>(null)

    private var presentes by mutableStateOf<Set<String>>(emptySet())
    private var memoriaRodizio by mutableStateOf(MemoriaRodizio())
    private var sugestao by mutableStateOf<Formacao?>(null)

    private var clientes by mutableStateOf(0)
    private var ultimoContato by mutableStateOf<Long?>(null)
    private var enderecoLocal by mutableStateOf("")
    private var travado by mutableStateOf(true)
    private var idiomaUi by mutableStateOf(Textos.EN)
    private var idiomaVoz by mutableStateOf(Textos.EN)
    private var vozIndisponivel by mutableStateOf(false)
    private var versaoFotos by mutableStateOf(0)
    private var ultimaTecla by mutableStateOf("")
    private var codigoUltimaTecla by mutableStateOf<Int?>(null)
    private var tempoIdaEVolta by mutableStateOf<Long?>(null)

    private var menuAberto by mutableStateOf(false)
    private var telaNovoJogo by mutableStateOf(false)
    private var telaSubstituicao by mutableStateOf(false)
    private var telaJogadores by mutableStateOf(false)
    private var telaRodizio by mutableStateOf(false)
    private var telaHistorico by mutableStateOf(false)
    private var telaEstatisticas by mutableStateOf(false)
    private var telaStatus by mutableStateOf(false)
    private var telaTeste by mutableStateOf(false)
    private var telaCorrecao by mutableStateOf(false)
    private var confirmandoNovoGame by mutableStateOf(false)
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
        val (memoria, presentesSalvos) = repositorio.carregarRodizio()
        memoriaRodizio = memoria
        presentes = presentesSalvos
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
            aoMudarClientes = { total -> runOnUiThread { clientes = total } },
            aoReceberEco = { enviado ->
                val agora = System.currentTimeMillis()
                runOnUiThread { tempoIdaEVolta = agora - enviado }
            },
            aoTerContato = {
                val agora = System.currentTimeMillis()
                runOnUiThread { ultimoContato = agora }
            }
        ).also { it.iniciar() }

        setContent {
            val estado = estadoAtual()

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
                    onDuplas = { menuAberto = false; telaNovoJogo = true },
                    onSubstituir = { menuAberto = false; telaSubstituicao = true },
                    onRodizio = {
                        sugestao = calcularSugestao(false)
                        menuAberto = false
                        telaRodizio = true
                    },
                    onJogadores = { menuAberto = false; telaJogadores = true },
                    onHistorico = {
                        historico = repositorio.listarPartidas()
                        menuAberto = false
                        telaHistorico = true
                    },
                    onEstatisticas = {
                        historico = repositorio.listarPartidas()
                        menuAberto = false
                        telaEstatisticas = true
                    },
                    onCorrigir = { menuAberto = false; telaCorrecao = true },
                    onStatus = {
                        historico = repositorio.listarPartidas(1)
                        menuAberto = false
                        telaStatus = true
                    },
                    onTeste = { menuAberto = false; telaTeste = true },
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
                    onSim = { confirmandoNovoGame = false; telaNovoJogo = true },
                    onNao = { confirmandoNovoGame = false }
                )
            }

            if (telaNovoJogo) {
                TelaNovoJogo(
                    jogadores = jogadores.toList(),
                    duplaEsquerda = duplaEsquerda,
                    duplaDireita = duplaDireita,
                    idiomaUi = idiomaUi,
                    versaoFotos = versaoFotos,
                    onMudar = { lado, dupla ->
                        if (lado == Lado.ESQUERDA) duplaEsquerda = dupla else duplaDireita = dupla
                        salvarAndamento()
                    },
                    onComecar = { saque ->
                        novoGame(saque)
                        telaNovoJogo = false
                    },
                    onFechar = { telaNovoJogo = false }
                )
            }

            if (telaSubstituicao) {
                TelaSubstituicao(
                    jogadores = jogadores.toList(),
                    duplaEsquerda = duplaEsquerda,
                    duplaDireita = duplaDireita,
                    idiomaUi = idiomaUi,
                    versaoFotos = versaoFotos,
                    onSubstituir = { lado, posicao, entrando ->
                        substituir(lado, posicao, entrando)
                        telaSubstituicao = false
                    },
                    onFechar = { telaSubstituicao = false }
                )
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

            if (telaRodizio) {
                TelaRodizio(
                    jogadores = jogadores.toList(),
                    presentes = presentes,
                    memoria = memoriaRodizio,
                    sugestao = sugestao,
                    idiomaUi = idiomaUi,
                    versaoFotos = versaoFotos,
                    onAlternarPresenca = { id ->
                        presentes = if (presentes.contains(id)) presentes - id else presentes + id
                        repositorio.salvarRodizio(memoriaRodizio, presentes)
                        sugestao = calcularSugestao(false)
                    },
                    onSortear = { sugestao = calcularSugestao(true) },
                    onAceitar = { formacao ->
                        aceitarFormacao(formacao)
                        telaRodizio = false
                        telaNovoJogo = true
                    },
                    onFechar = { telaRodizio = false }
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

            if (telaEstatisticas) {
                TelaEstatisticas(
                    jogadores = jogadores.toList(),
                    partidas = historico,
                    idiomaUi = idiomaUi,
                    versaoFotos = versaoFotos,
                    onExportar = { exportarCsv() },
                    onFechar = { telaEstatisticas = false }
                )
            }

            if (telaCorrecao) {
                TelaCorrecao(
                    estadoAtual = estadoAtual(),
                    idiomaUi = idiomaUi,
                    onAplicar = { corrigido ->
                        aplicarCorrecao(corrigido)
                        telaCorrecao = false
                    },
                    onFechar = { telaCorrecao = false }
                )
            }

            if (telaStatus) {
                TelaStatus(
                    idiomaUi = idiomaUi,
                    relogioConectado = clientes > 0,
                    segundosDesdeContato = ultimoContato?.let {
                        (System.currentTimeMillis() - it) / 1000
                    },
                    ultimaTecla = ultimaTecla,
                    vozEnOk = voz.disponivel(Textos.EN),
                    vozPtOk = voz.disponivel(Textos.PT),
                    saidaAudio = saidaDeAudio(),
                    ultimaPartida = historico.firstOrNull()?.let {
                        SimpleDateFormat("dd/MM HH:mm", Locale.US).format(Date(it.inicio))
                    } ?: "",
                    endereco = enderecoLocal,
                    onFechar = { telaStatus = false }
                )
            }

            if (telaTeste) {
                TelaTeste(
                    idiomaUi = idiomaUi,
                    ultimaTecla = ultimaTecla,
                    codigoUltimaTecla = codigoUltimaTecla,
                    tempoIdaEVolta = tempoIdaEVolta,
                    onFalarEn = { voz.falarExemplo(Textos.EN) },
                    onFalarPt = { voz.falarExemplo(Textos.PT) },
                    onMapear = { acao ->
                        codigoUltimaTecla?.let { ControleBluetooth.mapear(this, it, acao) }
                    },
                    onMedir = { tempoIdaEVolta = null; servidor?.medirIdaEVolta() },
                    onDerrubar = { servidor?.derrubarSessoes() },
                    onFechar = { telaTeste = false }
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
                        telaNovoJogo = true
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

    /**
     * Controle Bluetooth: esses aparelhos se apresentam como teclado.
     * O evento e consumido, senao o botao mexe no volume do sistema junto.
     */
    override fun onKeyDown(codigo: Int, evento: KeyEvent?): Boolean {
        ultimaTecla = ControleBluetooth.nomeDaTecla(codigo)
        codigoUltimaTecla = codigo

        if (telaTeste) return true

        return when (ControleBluetooth.acaoDe(this, codigo)) {
            ControleBluetooth.ACAO_ESQUERDA -> {
                registrarRally(Lado.ESQUERDA, Origem.CONTROLE); true
            }
            ControleBluetooth.ACAO_DIREITA -> {
                registrarRally(Lado.DIREITA, Origem.CONTROLE); true
            }
            ControleBluetooth.ACAO_DESFAZER -> {
                desfazer(); true
            }
            else -> super.onKeyDown(codigo, evento)
        }
    }

    // ---------- jogo ----------

    private fun estadoAtual(): EstadoJogo {
        val base = estadoBase
        return if (base != null) Regras.derivarDe(eventos, base)
        else Regras.derivar(eventos, primeiroSaque)
    }

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

            val anterior = estadoAtual()
            if (anterior.encerrado) return@runOnUiThread

            eventos.add(Evento(id = id, vencedor = vencedor, origem = origem, ts = agora))
            val novo = estadoAtual()

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

    private fun aplicarCorrecao(corrigido: EstadoJogo) {
        voz.cancelarPendente()
        estadoBase = corrigido
        eventos.clear()
        idsVistos.clear()
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
        escalacoes.clear()
        estadoBase = null
        partidaEncerrada = null
        avisoResultado = ""
        idPartida = UUID.randomUUID().toString()
        inicioPartida = System.currentTimeMillis()
        primeiroSaque = saqueInicial
        travado = true
        escalacoes.add(Escalacao(inicioPartida, duplaEsquerda, duplaDireita))
        repositorio.limparAtual()
        servidor?.transmitir(estadoJson())
        salvarAndamento()
    }

    /** Substituicao no meio do jogo. O placar nao muda. */
    private fun substituir(lado: Lado, posicao: Int, entrando: Jogador) {
        if (lado == Lado.ESQUERDA) {
            duplaEsquerda = if (posicao == 0) duplaEsquerda.copy(a = entrando.id)
            else duplaEsquerda.copy(b = entrando.id)
        } else {
            duplaDireita = if (posicao == 0) duplaDireita.copy(a = entrando.id)
            else duplaDireita.copy(b = entrando.id)
        }
        escalacoes.add(Escalacao(System.currentTimeMillis(), duplaEsquerda, duplaDireita))
        salvarAndamento()
    }

    private fun inverterLados() {
        val espelhados = eventos.map { it.copy(vencedor = it.vencedor.oposto()) }
        eventos.clear()
        eventos.addAll(espelhados)
        primeiroSaque = primeiroSaque.oposto()
        estadoBase = estadoBase?.let { base ->
            base.copy(
                pontosEsquerda = base.pontosDireita,
                pontosDireita = base.pontosEsquerda,
                sacando = base.sacando.oposto()
            )
        }
        val guardada = duplaEsquerda
        duplaEsquerda = duplaDireita
        duplaDireita = guardada
        escalacoes.add(Escalacao(System.currentTimeMillis(), duplaEsquerda, duplaDireita))
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
        escalacoes = escalacoes.toList(),
        eventos = eventos.toList(),
        base = estadoBase,
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
        estadoBase = salva.base
        escalacoes.addAll(salva.escalacoes)
        eventos.addAll(salva.eventos)
        salva.eventos.forEach { idsVistos.add(it.id) }
    }

    // ---------- rodizio ----------

    private fun calcularSugestao(embaralhar: Boolean): Formacao? =
        Rodizio.sugerir(presentes.toList(), memoriaRodizio, embaralhar)

    private fun aceitarFormacao(formacao: Formacao) {
        duplaEsquerda = Dupla(formacao.esquerda.getOrNull(0), formacao.esquerda.getOrNull(1))
        duplaDireita = Dupla(formacao.direita.getOrNull(0), formacao.direita.getOrNull(1))
        memoriaRodizio = Rodizio.registrar(memoriaRodizio, formacao)
        repositorio.salvarRodizio(memoriaRodizio, presentes)
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
        presentes = presentes - jogador.id
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

    // ---------- saidas ----------

    private fun enviarEmail(partida: Partida) {
        val lista = jogadores.toList()
        if (ResumoEmail.destinatarios(partida, lista).isEmpty()) {
            avisoResultado = Textos.get("sem_email", idiomaUi)
            return
        }
        val abriu = ResumoEmail.abrir(this, partida, lista, idiomaUi)
        avisoResultado = if (abriu) "" else Textos.get("sem_app_email", idiomaUi)
    }

    private fun exportarCsv() {
        runCatching {
            val arquivo = Exportacao.gerarCsv(this, historico, jogadores.toList())
            Exportacao.compartilhar(this, arquivo)
        }
    }

    private fun saidaDeAudio(): String {
        val audio = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return Textos.get("alto_falante", idiomaUi)
        @Suppress("DEPRECATION")
        val bluetooth = audio.isBluetoothA2dpOn
        return if (bluetooth) Textos.get("caixa_bluetooth", idiomaUi)
        else Textos.get("alto_falante", idiomaUi)
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
        servidor?.transmitir(estadoJson())
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
        val estado = estadoAtual()
        return JSONObject().apply {
            put("tipo", "ESTADO")
            put("esq", estado.pontosEsquerda)
            put("dir", estado.pontosDireita)
            put("sacando", estado.sacando.name)
            put("sacador", estado.sacador)
            put("chamada", estado.chamada)
            put("encerrado", estado.encerrado)
            put("pontoDeJogo", estado.pontoDeJogo)
            put("idioma", idiomaUi)
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
