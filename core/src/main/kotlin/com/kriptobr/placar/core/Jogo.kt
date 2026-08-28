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

/**
 * Evento imutavel. A unica coisa que qualquer entrada produz.
 * O id serve para o tablet ignorar reenvio duplicado.
 */
data class Evento(
    val id: String,
    val vencedor: Lado,
    val origem: Origem,
    val ts: Long
)

data class EstadoJogo(
    val pontosEsquerda: Int = 0,
    val pontosDireita: Int = 0,
    val sacando: Lado = Lado.ESQUERDA,
    val sacador: Int = 2,
    val encerrado: Boolean = false,
    val vencedor: Lado? = null
) {
    fun pontosDe(lado: Lado): Int =
        if (lado == Lado.ESQUERDA) pontosEsquerda else pontosDireita

    val pontosSacador: Int get() = pontosDe(sacando)
    val pontosRecebedor: Int get() = pontosDe(sacando.oposto())

    /** Pontuacao par saca pela direita, impar pela esquerda. */
    val ladoDoSaque: String get() = if (pontosSacador % 2 == 0) "direita" else "esquerda"

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
     */
    fun estadoInicial(primeiroSaque: Lado = Lado.ESQUERDA): EstadoJogo =
        EstadoJogo(sacando = primeiroSaque, sacador = 2)

    fun aplicar(estado: EstadoJogo, evento: Evento): EstadoJogo {
        if (estado.encerrado) return estado
        return if (evento.vencedor == estado.sacando) pontuar(estado) else perderSaque(estado)
    }

    private fun pontuar(e: EstadoJogo): EstadoJogo {
        val esquerda = if (e.sacando == Lado.ESQUERDA) e.pontosEsquerda + 1 else e.pontosEsquerda
        val direita = if (e.sacando == Lado.DIREITA) e.pontosDireita + 1 else e.pontosDireita
        val novo = e.copy(pontosEsquerda = esquerda, pontosDireita = direita)

        val meus = novo.pontosDe(e.sacando)
        val deles = novo.pontosDe(e.sacando.oposto())

        return if (meus >= PONTOS_PARA_VENCER && meus - deles >= VANTAGEM_MINIMA) {
            novo.copy(encerrado = true, vencedor = e.sacando)
        } else {
            novo
        }
    }

    private fun perderSaque(e: EstadoJogo): EstadoJogo =
        if (e.sacador == 1) {
            e.copy(sacador = 2)
        } else {
            e.copy(sacando = e.sacando.oposto(), sacador = 1)
        }

    /**
     * O estado nunca e editado, e sempre recalculado a partir do log.
     * Desfazer e apenas remover o ultimo evento e chamar isto de novo.
     */
    fun derivar(eventos: List<Evento>, primeiroSaque: Lado = Lado.ESQUERDA): EstadoJogo =
        eventos.fold(estadoInicial(primeiroSaque)) { acumulado, evento ->
            aplicar(acumulado, evento)
        }
}
