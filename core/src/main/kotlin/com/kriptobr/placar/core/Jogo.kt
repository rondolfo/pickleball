package com.kriptobr.placar.core

/**
 * Regras do pickleball em duplas tradicional.
 *
 * Este modulo e Kotlin puro, sem dependencia de Android.
 * Tablet e relogio usam exatamente o mesmo codigo, entao os dois
 * nunca discordam sobre o placar.
 */

enum class Lado {
    ESQUERDA,
    DIREITA;

    fun oposto(): Lado = if (this == ESQUERDA) DIREITA else ESQUERDA
}

enum class Origem {
    RELOGIO,
    TOQUE,
    CONTROLE,
    CAMERA
}

data class Evento(
    val id: String,
    val vencedor: Lado,
    val origem: Origem,
    val ts: Long
)

/**
 * Alem do placar, o estado guarda a posicao dos jogadores em quadra.
 *
 * Isso e o que permite dizer QUEM esta sacando, e nao apenas qual dupla.
 * Tambem corrige o lado do saque: a regra simplificada de par pela direita
 * so vale para o primeiro sacador. Quando entra o segundo, os parceiros nao
 * trocam de lado, entao ele saca do lado onde ja esta.
 *
 * naDireitaX guarda qual jogador da dupla, 0 ou 1, esta na quadra da direita.
 * indiceSacador guarda qual jogador da dupla que saca esta com a bola.
 */
data class EstadoJogo(
    val pontosEsquerda: Int = 0,
    val pontosDireita: Int = 0,
    val sacando: Lado = Lado.ESQUERDA,
    val sacador: Int = 2,
    val encerrado: Boolean = false,
    val vencedor: Lado? = null,
    val naDireitaEsquerda: Int = 0,
    val naDireitaDireita: Int = 0,
    val indiceSacador: Int = 0
) {
    fun pontosDe(lado: Lado): Int =
        if (lado == Lado.ESQUERDA) pontosEsquerda else pontosDireita

    fun naDireitaDe(lado: Lado): Int =
        if (lado == Lado.ESQUERDA) naDireitaEsquerda else naDireitaDireita

    val pontosSacador: Int get() = pontosDe(sacando)
    val pontosRecebedor: Int get() = pontosDe(sacando.oposto())

    /** Direita quando quem saca esta na quadra da direita. */
    val ladoDoSaque: String
        get() = if (indiceSacador == naDireitaDe(sacando)) "direita" else "esquerda"

    /** Chamada oficial: pontos de quem saca, pontos de quem recebe, numero do sacador. */
    val chamada: String get() = "$pontosSacador-$pontosRecebedor-$sacador"

    val pontoDeJogo: Boolean
        get() = !encerrado &&
            pontosSacador >= Regras.PONTOS_PARA_VENCER - 1 &&
            pontosSacador - pontosRecebedor >= Regras.VANTAGEM_MINIMA - 1
}

object Regras {

    const val PONTOS_PARA_VENCER = 11
    const val VANTAGEM_MINIMA = 2

    /**
     * O game comeca em 0-0-2: a primeira dupla a sacar tem apenas um sacador
     * nesse primeiro turno, entao perder o rally gera troca de saque imediata.
     *
     * Por convencao, o primeiro jogador da dupla comeca na quadra da direita.
     */
    fun estadoInicial(primeiroSaque: Lado = Lado.ESQUERDA): EstadoJogo =
        EstadoJogo(
            sacando = primeiroSaque,
            sacador = 2,
            naDireitaEsquerda = 0,
            naDireitaDireita = 0,
            indiceSacador = 0
        )

    fun aplicar(estado: EstadoJogo, evento: Evento): EstadoJogo {
        if (estado.encerrado) return estado
        return if (evento.vencedor == estado.sacando) pontuar(estado) else perderSaque(estado)
    }

    /** Ponto para quem saca: os parceiros trocam de lado e o mesmo jogador continua sacando. */
    private fun pontuar(e: EstadoJogo): EstadoJogo {
        val esquerda = if (e.sacando == Lado.ESQUERDA) e.pontosEsquerda + 1 else e.pontosEsquerda
        val direita = if (e.sacando == Lado.DIREITA) e.pontosDireita + 1 else e.pontosDireita

        val novo = e.copy(
            pontosEsquerda = esquerda,
            pontosDireita = direita,
            naDireitaEsquerda = if (e.sacando == Lado.ESQUERDA) 1 - e.naDireitaEsquerda
            else e.naDireitaEsquerda,
            naDireitaDireita = if (e.sacando == Lado.DIREITA) 1 - e.naDireitaDireita
            else e.naDireitaDireita
        )

        val meus = novo.pontosDe(e.sacando)
        val deles = novo.pontosDe(e.sacando.oposto())

        return if (meus >= PONTOS_PARA_VENCER && meus - deles >= VANTAGEM_MINIMA) {
            novo.copy(encerrado = true, vencedor = e.sacando)
        } else {
            novo
        }
    }

    /**
     * Quem saca perde o rally.
     *
     * Sacador 1: a bola passa para o parceiro, que vira sacador 2. Ninguem
     * troca de lado, entao o parceiro saca de onde ja esta.
     *
     * Sacador 2: troca de saque. Na outra dupla, saca quem estiver na quadra
     * da direita, e essa pessoa vira o sacador 1.
     */
    private fun perderSaque(e: EstadoJogo): EstadoJogo =
        if (e.sacador == 1) {
            e.copy(sacador = 2, indiceSacador = 1 - e.indiceSacador)
        } else {
            val entrando = e.sacando.oposto()
            e.copy(
                sacando = entrando,
                sacador = 1,
                indiceSacador = e.naDireitaDe(entrando)
            )
        }

    /**
     * Estado de uma correcao manual do placar.
     *
     * Ninguem precisa dizer de que lado cada um esta. Um parceiro so troca de
     * lado quando a propria dupla marca ponto sacando, entao quem comecou na
     * direita esta na direita sempre que o placar da dupla e par. O que nao da
     * para deduzir do placar e quem esta com a bola, e isso vem de fora.
     *
     * Antes a correcao guardava as posicoes de antes dela. Se a correcao mudava
     * o placar de uma dupla ou a dupla que saca, essas posicoes deixavam de
     * bater com o jogo, e o aplicativo passava a apontar o sacador errado.
     */
    fun estadoCorrigido(
        pontosEsquerda: Int,
        pontosDireita: Int,
        sacando: Lado,
        sacador: Int,
        indiceSacador: Int
    ): EstadoJogo = EstadoJogo(
        pontosEsquerda = pontosEsquerda,
        pontosDireita = pontosDireita,
        sacando = sacando,
        sacador = sacador,
        naDireitaEsquerda = pontosEsquerda % 2,
        naDireitaDireita = pontosDireita % 2,
        indiceSacador = indiceSacador
    )

    /** Indice, 0 ou 1, de quem esta na quadra da direita na dupla, pelo placar. */
    fun naDireitaPeloPlacar(pontos: Int): Int = pontos % 2

    /**
     * O mesmo estado visto com as duplas trocadas de ponta.
     * Placar, saque e posicoes trocam juntos, como num espelho.
     */
    fun espelhar(e: EstadoJogo): EstadoJogo = e.copy(
        pontosEsquerda = e.pontosDireita,
        pontosDireita = e.pontosEsquerda,
        sacando = e.sacando.oposto(),
        vencedor = e.vencedor?.oposto(),
        naDireitaEsquerda = e.naDireitaDireita,
        naDireitaDireita = e.naDireitaEsquerda
    )

    fun derivar(eventos: List<Evento>, primeiroSaque: Lado = Lado.ESQUERDA): EstadoJogo =
        derivarDe(eventos, estadoInicial(primeiroSaque))

    /**
     * Deriva a partir de um estado base em vez do zero.
     *
     * Serve para a correcao manual: em vez de inventar eventos falsos para
     * chegar num placar, o placar corrigido vira o novo ponto de partida.
     */
    fun derivarDe(eventos: List<Evento>, base: EstadoJogo): EstadoJogo =
        eventos.fold(base) { acumulado, evento -> aplicar(acumulado, evento) }
}
