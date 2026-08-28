package com.kriptobr.placar.core

/**
 * Estatisticas derivadas do log de eventos.
 *
 * Nao existe estrutura paralela: tudo abaixo e recalculado a partir da
 * mesma lista de eventos que produz o placar.
 *
 * Observacao importante sobre o formato tradicional: todo ponto e marcado
 * por quem esta sacando. Entao "pontos ganhos sacando" nao diferencia nada.
 * O que diferencia e quantos rallies cada dupla ganhou recebendo, quantos
 * turnos de saque renderam zero e o tamanho das sequencias.
 */
data class Estatisticas(
    val pontosEsquerda: Int,
    val pontosDireita: Int,
    val ralliesEsquerda: Int,
    val ralliesDireita: Int,
    val ralliesRecebendoEsquerda: Int,
    val ralliesRecebendoDireita: Int,
    val turnosSecosEsquerda: Int,
    val turnosSecosDireita: Int,
    val maiorSequenciaEsquerda: Int,
    val maiorSequenciaDireita: Int,
    val viradas: Int,
    val totalRallies: Int
)

object Analise {

    fun calcular(eventos: List<Evento>, primeiroSaque: Lado): Estatisticas {
        var estado = Regras.estadoInicial(primeiroSaque)

        var ralliesEsq = 0
        var ralliesDir = 0
        var recebendoEsq = 0
        var recebendoDir = 0
        var secosEsq = 0
        var secosDir = 0
        var seqEsq = 0
        var seqDir = 0
        var maiorEsq = 0
        var maiorDir = 0
        var viradas = 0

        var pontosNoTurno = 0
        var liderAnterior: Lado? = null

        for (evento in eventos) {
            val sacandoAntes = estado.sacando

            if (evento.vencedor == Lado.ESQUERDA) ralliesEsq++ else ralliesDir++

            if (evento.vencedor != sacandoAntes) {
                if (evento.vencedor == Lado.ESQUERDA) recebendoEsq++ else recebendoDir++
            }

            val depois = Regras.aplicar(estado, evento)

            val pontuou = depois.pontosDe(sacandoAntes) > estado.pontosDe(sacandoAntes)
            if (pontuou) {
                pontosNoTurno++
                if (sacandoAntes == Lado.ESQUERDA) {
                    seqEsq++
                    seqDir = 0
                    if (seqEsq > maiorEsq) maiorEsq = seqEsq
                } else {
                    seqDir++
                    seqEsq = 0
                    if (seqDir > maiorDir) maiorDir = seqDir
                }
            }

            // turno de saque encerrado sem render ponto nenhum
            if (depois.sacando != sacandoAntes) {
                if (pontosNoTurno == 0) {
                    if (sacandoAntes == Lado.ESQUERDA) secosEsq++ else secosDir++
                }
                pontosNoTurno = 0
            }

            val lider = when {
                depois.pontosEsquerda > depois.pontosDireita -> Lado.ESQUERDA
                depois.pontosDireita > depois.pontosEsquerda -> Lado.DIREITA
                else -> null
            }
            if (lider != null && liderAnterior != null && lider != liderAnterior) viradas++
            if (lider != null) liderAnterior = lider

            estado = depois
        }

        return Estatisticas(
            pontosEsquerda = estado.pontosEsquerda,
            pontosDireita = estado.pontosDireita,
            ralliesEsquerda = ralliesEsq,
            ralliesDireita = ralliesDir,
            ralliesRecebendoEsquerda = recebendoEsq,
            ralliesRecebendoDireita = recebendoDir,
            turnosSecosEsquerda = secosEsq,
            turnosSecosDireita = secosDir,
            maiorSequenciaEsquerda = maiorEsq,
            maiorSequenciaDireita = maiorDir,
            viradas = viradas,
            totalRallies = eventos.size
        )
    }
}
