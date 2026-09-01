package com.kriptobr.placar.tablet

import com.kriptobr.placar.core.Lado

import java.util.Calendar

data class ResumoJogador(
    val id: String,
    val partidas: Int,
    val vitorias: Int,
    val derrotas: Int,
    val parceiroMaisFrequente: String?,
    val vezesComParceiro: Int,
    val melhorParceiro: String?,
    val vitoriasComMelhorParceiro: Int,
    val jogosComMelhorParceiro: Int,
    val pontosFeitos: Int = 0,
    val pontosSofridos: Int = 0
) {
    val saldo: Int get() = pontosFeitos - pontosSofridos

    val aproveitamento: Int
        get() = if (partidas == 0) 0 else (vitorias * 100) / partidas
}

/**
 * Estatistica acumulada por jogador, calculada a partir das partidas salvas.
 *
 * Quem entrou por substituicao conta como tendo jogado a partida, assim
 * como quem saiu. Nenhum dos dois some do historico.
 */
object EstatisticasJogador {

    fun calcular(partidas: List<Partida>, idJogador: String): ResumoJogador {
        var jogos = 0
        var vitorias = 0
        var feitos = 0
        var sofridos = 0
        val contagemParceiro = mutableMapOf<String, Int>()
        val vitoriasParceiro = mutableMapOf<String, Int>()

        partidas.filter { it.completa && it.participantes().contains(idJogador) }
            .forEach { partida ->
                val lado = partida.ladoDe(idJogador) ?: return@forEach
                jogos++

                val stats = partida.estatisticas()
                val venceuEsquerda = stats.pontosEsquerda > stats.pontosDireita
                val venceu = (lado == Lado.ESQUERDA && venceuEsquerda) ||
                    (lado == Lado.DIREITA && !venceuEsquerda)
                if (venceu) vitorias++

                if (lado == Lado.ESQUERDA) {
                    feitos += stats.pontosEsquerda
                    sofridos += stats.pontosDireita
                } else {
                    feitos += stats.pontosDireita
                    sofridos += stats.pontosEsquerda
                }

                parceirosNaPartida(partida, idJogador, lado).forEach { parceiro ->
                    contagemParceiro[parceiro] = (contagemParceiro[parceiro] ?: 0) + 1
                    if (venceu) vitoriasParceiro[parceiro] = (vitoriasParceiro[parceiro] ?: 0) + 1
                }
            }

        val maisFrequente = contagemParceiro.maxByOrNull { it.value }
        val melhor = vitoriasParceiro
            .filter { (contagemParceiro[it.key] ?: 0) >= 2 }
            .maxByOrNull { it.value }

        return ResumoJogador(
            id = idJogador,
            partidas = jogos,
            vitorias = vitorias,
            derrotas = jogos - vitorias,
            parceiroMaisFrequente = maisFrequente?.key,
            vezesComParceiro = maisFrequente?.value ?: 0,
            melhorParceiro = melhor?.key,
            vitoriasComMelhorParceiro = melhor?.value ?: 0,
            jogosComMelhorParceiro = melhor?.let { contagemParceiro[it.key] } ?: 0,
            pontosFeitos = feitos,
            pontosSofridos = sofridos
        )
    }

    /** Partidas do dia corrente, que e o recorte de uma noite de jogo. */
    fun doDia(partidas: List<Partida>, quando: Long = System.currentTimeMillis()): List<Partida> {
        val referencia = Calendar.getInstance().apply { timeInMillis = quando }
        return partidas.filter { partida ->
            val dela = Calendar.getInstance().apply { timeInMillis = partida.inicio }
            dela.get(Calendar.YEAR) == referencia.get(Calendar.YEAR) &&
                dela.get(Calendar.DAY_OF_YEAR) == referencia.get(Calendar.DAY_OF_YEAR)
        }
    }

    /** Ranking pronto, ja ordenado. Empate resolvido por saldo e depois por jogos. */
    fun ranking(partidas: List<Partida>, jogadores: List<Jogador>): List<ResumoJogador> =
        jogadores
            .map { calcular(partidas, it.id) }
            .filter { it.partidas > 0 }
            .sortedWith(
                compareByDescending<ResumoJogador> { it.aproveitamento }
                    .thenByDescending { it.saldo }
                    .thenByDescending { it.partidas }
            )

    private fun parceirosNaPartida(
        partida: Partida,
        idJogador: String,
        lado: Lado
    ): Set<String> {
        val duplas = partida.escalacoes.map {
            if (lado == Lado.ESQUERDA) it.esquerda else it.direita
        } + listOf(if (lado == Lado.ESQUERDA) partida.duplaEsquerda else partida.duplaDireita)

        return duplas
            .filter { it.ids.contains(idJogador) }
            .flatMap { it.ids }
            .filter { it != idJogador }
            .toSet()
    }
}
