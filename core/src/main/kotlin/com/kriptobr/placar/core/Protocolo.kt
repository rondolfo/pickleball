package com.kriptobr.placar.core

/**
 * Constantes compartilhadas entre tablet e relogio.
 * Ficam aqui para os dois lados nunca sairem de sincronia.
 */
object Protocolo {
    const val PORTA = 8765
    const val CAMINHO = "/placar"
    const val TIPO_SERVICO = "_placarpb._tcp"
    const val NOME_SERVICO = "PlacarPickleball"

    const val TIPO_RALLY = "RALLY"
    const val TIPO_DESFAZER = "DESFAZER"
    const val TIPO_ESTADO = "ESTADO"
    const val TIPO_PING = "PING"
    const val TIPO_ACK = "ACK"
    const val TIPO_ECO = "ECO"
    const val TIPO_ECO_RESP = "ECO_RESP"
}
