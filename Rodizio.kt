package com.kriptobr.placar.core

/**
 * Sugestao de proxima formacao.
 *
 * Tres prioridades, nesta ordem:
 *   1. quem esperou mais tempo entra primeiro
 *   2. evitar repetir parceiro
 *   3. evitar repetir os mesmos adversarios da rodada anterior
 *
 * Sorteio puro nao serve: sempre sobra alguem de fora tres rodadas
 * seguidas enquanto outro joga direto. Por isso ate a opcao aleatoria
 * respeita quem esta esperando, variando apenas a montagem.
 */
data class MemoriaRodizio(
    val rodada: Int = 0,
    val ultimaRodadaPorJogador: Map<String, Int> = emptyMap(),
    val jogosPorJogador: Map<String, Int> = emptyMap(),
    val ultimoParceiro: Map<String, String> = emptyMap(),
    val ultimosAdversarios: Map<String, Set<String>> = emptyMap()
)

data class Formacao(
    val esquerda: List<String>,
    val direita: List<String>
) {
    val todos: List<String> get() = esquerda + direita
}

object Rodizio {

    /**
     * @param presentes ids de quem esta na quadra hoje
     * @param embaralhar variar a montagem dentro de quem tem prioridade
     */
    fun sugerir(
        presentes: List<String>,
        memoria: MemoriaRodizio,
        embaralhar: Boolean = false
    ): Formacao? {
        if (presentes.size < 4) return null

        val nunca = -1
        // o total de jogos e essencial no desempate: sem ele, os mesmos
        // jogadores ganham a ordem fixa e nunca saem de quadra
        val ordenados = presentes.sortedWith(
            compareBy<String> { memoria.ultimaRodadaPorJogador[it] ?: nunca }
                .thenBy { memoria.jogosPorJogador[it] ?: 0 }
                .thenBy { if (embaralhar) (Math.random() * 100000).toInt() else it.hashCode() }
        )

        // corte de prioridade: quem esperou mais entra, empate resolvido acima
        val quatro = ordenados.take(4)

        val montagens = listOf(
            Pair(listOf(quatro[0], quatro[1]), listOf(quatro[2], quatro[3])),
            Pair(listOf(quatro[0], quatro[2]), listOf(quatro[1], quatro[3])),
            Pair(listOf(quatro[0], quatro[3]), listOf(quatro[1], quatro[2]))
        )

        val avaliadas = montagens.map { (esq, dir) ->
            var custo = 0
            if (memoria.ultimoParceiro[esq[0]] == esq[1]) custo += 10
            if (memoria.ultimoParceiro[dir[0]] == dir[1]) custo += 10
            esq.forEach { jogador ->
                val antigos = memoria.ultimosAdversarios[jogador].orEmpty()
                custo += dir.count { antigos.contains(it) }
            }
            Triple(esq, dir, custo)
        }

        val menorCusto = avaliadas.minOf { it.third }
        val candidatas = avaliadas.filter { it.third == menorCusto }
        val escolhida = if (embaralhar) candidatas.random() else candidatas.first()

        return Formacao(escolhida.first, escolhida.second)
    }

    /** Registra a formacao que de fato entrou em quadra. */
    fun registrar(memoria: MemoriaRodizio, formacao: Formacao): MemoriaRodizio {
        val rodada = memoria.rodada + 1

        val ultimaRodada = memoria.ultimaRodadaPorJogador.toMutableMap()
        val jogos = memoria.jogosPorJogador.toMutableMap()
        formacao.todos.forEach {
            ultimaRodada[it] = rodada
            jogos[it] = (jogos[it] ?: 0) + 1
        }

        val parceiros = memoria.ultimoParceiro.toMutableMap()
        if (formacao.esquerda.size == 2) {
            parceiros[formacao.esquerda[0]] = formacao.esquerda[1]
            parceiros[formacao.esquerda[1]] = formacao.esquerda[0]
        }
        if (formacao.direita.size == 2) {
            parceiros[formacao.direita[0]] = formacao.direita[1]
            parceiros[formacao.direita[1]] = formacao.direita[0]
        }

        val adversarios = memoria.ultimosAdversarios.toMutableMap()
        formacao.esquerda.forEach { adversarios[it] = formacao.direita.toSet() }
        formacao.direita.forEach { adversarios[it] = formacao.esquerda.toSet() }

        return MemoriaRodizio(rodada, ultimaRodada, jogos, parceiros, adversarios)
    }

    /** Quantas rodadas cada presente esta esperando. */
    fun esperaDe(presentes: List<String>, memoria: MemoriaRodizio): Map<String, Int> =
        presentes.associateWith { jogador ->
            val ultima = memoria.ultimaRodadaPorJogador[jogador] ?: 0
            (memoria.rodada - ultima).coerceAtLeast(0)
        }
}
