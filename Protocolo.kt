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
    // Identificadores do transporte Bluetooth de baixa energia.
    // O tablet anuncia este servico e o relogio procura por ele, sem
    // depender de rede sem fio nenhuma.
    const val BLE_SERVICO = "6b1d0001-1f7a-4c3e-9a2b-5d8e7c4f1a20"
    const val BLE_ENVIO = "6b1d0002-1f7a-4c3e-9a2b-5d8e7c4f1a20"
    const val BLE_RETORNO = "6b1d0003-1f7a-4c3e-9a2b-5d8e7c4f1a20"
    const val BLE_CCCD = "00002902-0000-1000-8000-00805f9b34fb"

    const val TIPO_ECO = "ECO"
    const val TIPO_ECO_RESP = "ECO_RESP"
    const val TIPO_INVERTER = "INVERTER"
}
