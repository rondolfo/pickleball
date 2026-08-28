package com.kriptobr.placar.tablet

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
}

data class Partida(
    val id: String,
    val inicio: Long,
    val fim: Long? = null,
    val primeiroSaque: Lado = Lado.ESQUERDA,
    val duplaEsquerda: Dupla = Dupla(),
    val duplaDireita: Dupla = Dupla(),
    val eventos: List<Evento> = emptyList(),
    val completa: Boolean = false
)
