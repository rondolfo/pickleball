package com.kriptobr.placar.tablet

import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.Regras
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

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
    val pontosSofridos: Int = 0,
    val maiorSequencia: Int = 0
) {
    val saldo: Int get() = pontosFeitos - pontosSofridos

    val aproveitamento: Int
        get() = if (partidas == 0) 0 else (vitorias * 100) / partidas

    val pontosPorJogo: Double
        get() = if (partidas == 0) 0.0 else pontosFeitos.toDouble() / partidas
}

data class DuplaResumo(val a: String, val b: String, val jogos: Int, val vitorias: Int) {
    val aproveitamento: Int get() = if (jogos == 0) 0 else (vitorias * 100) / jogos
}

data class Confronto(val jogos: Int, val vitorias: Int)

/**
 * Estatistica acumulada por jogador, calculada a partir das partidas salvas.
 *
 * Quem entrou por substituicao conta como tendo jogado a partida, assim
 * como quem saiu. Nenhum dos dois some do historico.
 */
object EstatisticasJogador {

    /** Abaixo disso o jogador entra como visitante, fora da classificacao. */
    const val MINIMO_REGULAR = 15
    const val MINIMO_REGULAR_DIA = 2
    const val MINIMO_DUPLA = 5

    /**
     * Uma partida so entra na estatistica quando descreve um jogo real:
     * dois contra dois, ninguem repetido, e com vencedor legitimo.
     *
     * A marcacao "completa" nao serve como criterio. Ela se perdia quando um
     * game novo comecava logo apos o anterior terminar, entao partidas
     * perfeitamente validas ficavam marcadas como incompletas.
     */
    fun partidaValida(partida: Partida): Boolean {
        val esquerda = partida.duplaEsquerda.ids.distinct()
        val direita = partida.duplaDireita.ids.distinct()
        if (esquerda.size != 2 || direita.size != 2) return false
        if (esquerda.any { direita.contains(it) }) return false

        val stats = partida.estatisticas()
        val maior = maxOf(stats.pontosEsquerda, stats.pontosDireita)
        val diferenca = abs(stats.pontosEsquerda - stats.pontosDireita)
        return maior >= Regras.PONTOS_PARA_VENCER && diferenca >= Regras.VANTAGEM_MINIMA
    }

    fun validas(partidas: List<Partida>): List<Partida> = partidas.filter { partidaValida(it) }

    fun calcular(partidas: List<Partida>, idJogador: String): ResumoJogador {
        var jogos = 0
        var vitorias = 0
        var feitos = 0
        var sofridos = 0
        var maiorSeq = 0
        val contagemParceiro = mutableMapOf<String, Int>()
        val vitoriasParceiro = mutableMapOf<String, Int>()

        validas(partidas).filter { it.participantes().contains(idJogador) }
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
                    maiorSeq = maxOf(maiorSeq, stats.maiorSequenciaEsquerda)
                } else {
                    feitos += stats.pontosDireita
                    sofridos += stats.pontosEsquerda
                    maiorSeq = maxOf(maiorSeq, stats.maiorSequenciaDireita)
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
            pontosSofridos = sofridos,
            maiorSequencia = maiorSeq
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

    private fun ordenado(lista: List<ResumoJogador>) = lista.sortedWith(
        compareByDescending<ResumoJogador> { it.aproveitamento }
            .thenByDescending { it.saldo }
            .thenByDescending { it.partidas }
    )

    fun ranking(partidas: List<Partida>, jogadores: List<Jogador>): List<ResumoJogador> =
        ordenado(jogadores.map { calcular(partidas, it.id) }.filter { it.partidas > 0 })

    /**
     * Separa quem tem partidas suficientes de quem passou pontualmente.
     *
     * Sem esse corte, alguem que jogou cinco vezes num unico dia aparece na
     * frente de quem joga toda semana, e a classificacao deixa de significar
     * o que as pessoas esperam dela.
     */
    fun rankingDividido(
        partidas: List<Partida>,
        jogadores: List<Jogador>,
        minimo: Int
    ): Pair<List<ResumoJogador>, List<ResumoJogador>> {
        val todos = jogadores.map { calcular(partidas, it.id) }.filter { it.partidas > 0 }
        return ordenado(todos.filter { it.partidas >= minimo }) to
            ordenado(todos.filter { it.partidas < minimo })
    }

    /** Duplas que jogaram juntas pelo menos o minimo de partidas. */
    fun duplas(partidas: List<Partida>, minimo: Int = MINIMO_DUPLA): List<DuplaResumo> {
        val contagem = mutableMapOf<Pair<String, String>, IntArray>()

        validas(partidas).forEach { partida ->
            val stats = partida.estatisticas()
            val venceuEsquerda = stats.pontosEsquerda > stats.pontosDireita
            listOf(partida.duplaEsquerda.ids to venceuEsquerda,
                   partida.duplaDireita.ids to !venceuEsquerda).forEach { (ids, venceu) ->
                if (ids.size == 2) {
                    val chave = if (ids[0] <= ids[1]) ids[0] to ids[1] else ids[1] to ids[0]
                    val acumulado = contagem.getOrPut(chave) { intArrayOf(0, 0) }
                    acumulado[0] += 1
                    if (venceu) acumulado[1] += 1
                }
            }
        }

        return contagem
            .filter { it.value[0] >= minimo }
            .map { (chave, v) -> DuplaResumo(chave.first, chave.second, v[0], v[1]) }
            .sortedWith(
                compareByDescending<DuplaResumo> { it.aproveitamento }.thenByDescending { it.jogos }
            )
    }

    /** Vitorias de a contra b, contando apenas jogos em lados opostos. */
    fun confrontos(partidas: List<Partida>): Map<Pair<String, String>, Confronto> {
        val acumulado = mutableMapOf<Pair<String, String>, IntArray>()

        validas(partidas).forEach { partida ->
            val stats = partida.estatisticas()
            val venceuEsquerda = stats.pontosEsquerda > stats.pontosDireita
            val vencedores = if (venceuEsquerda) partida.duplaEsquerda.ids else partida.duplaDireita.ids
            val perdedores = if (venceuEsquerda) partida.duplaDireita.ids else partida.duplaEsquerda.ids

            vencedores.forEach { a ->
                perdedores.forEach { b ->
                    acumulado.getOrPut(a to b) { intArrayOf(0, 0) }.let { it[0] += 1; it[1] += 1 }
                    acumulado.getOrPut(b to a) { intArrayOf(0, 0) }[0] += 1
                }
            }
        }
        return acumulado.mapValues { Confronto(it.value[0], it.value[1]) }
    }

    /** Quantidade de partidas por dia de jogo, em ordem cronologica. */
    fun porSessao(partidas: List<Partida>): List<Pair<String, Int>> {
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return validas(partidas)
            .groupingBy { formato.format(java.util.Date(it.inicio)) }
            .eachCount()
            .toList()
            .sortedBy { it.first }
    }

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
