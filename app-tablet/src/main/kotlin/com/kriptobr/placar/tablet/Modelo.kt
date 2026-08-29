package com.kriptobr.placar.tablet

import com.kriptobr.placar.core.EstadoJogo
import com.kriptobr.placar.core.Evento
import com.kriptobr.placar.core.Lado

data class Jogador(
    val id: String,
    val nome: String,
    val apelido: String,
    val email: String = "",
    val sexo: String = "",
    val temFoto: Boolean = false
) {
    val iniciais: String
        get() = nome.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    val curto: String get() = apelido.ifBlank { nome.trim().split(" ").first() }
}

data class Dupla(val a: String? = null, val b: String? = null) {
    val vazia: Boolean get() = a == null && b == null
    val ids: List<String> get() = listOfNotNull(a, b)
    val completa: Boolean get() = a != null && b != null
}

/** Quem estava em quadra a partir de determinado momento. */
data class Escalacao(
    val ts: Long,
    val esquerda: Dupla,
    val direita: Dupla
)

data class Partida(
    val id: String,
    val inicio: Long,
    val fim: Long? = null,
    val primeiroSaque: Lado = Lado.ESQUERDA,
    val duplaEsquerda: Dupla = Dupla(),
    val duplaDireita: Dupla = Dupla(),
    val escalacoes: List<Escalacao> = emptyList(),
    val eventos: List<Evento> = emptyList(),
    val base: EstadoJogo? = null,
    val completa: Boolean = false
) {
    /**
     * Todo mundo que passou pela quadra nesta partida, incluindo quem
     * saiu e quem entrou no lugar. Substituicao nao apaga participacao.
     */
    fun participantes(): Set<String> {
        val doHistorico = escalacoes.flatMap { it.esquerda.ids + it.direita.ids }
        return (doHistorico + duplaEsquerda.ids + duplaDireita.ids).toSet()
    }

    fun ladoDe(idJogador: String): Lado? = when {
        escalacoes.any { it.esquerda.ids.contains(idJogador) } -> Lado.ESQUERDA
        escalacoes.any { it.direita.ids.contains(idJogador) } -> Lado.DIREITA
        duplaEsquerda.ids.contains(idJogador) -> Lado.ESQUERDA
        duplaDireita.ids.contains(idJogador) -> Lado.DIREITA
        else -> null
    }
}

/**
 * Estatisticas desta partida, respeitando o estado base quando houve
 * correcao manual do placar.
 */
fun Partida.estatisticas(): com.kriptobr.placar.core.Estatisticas =
    com.kriptobr.placar.core.Analise.calcular(
        eventos,
        base ?: com.kriptobr.placar.core.Regras.estadoInicial(primeiroSaque)
    )
