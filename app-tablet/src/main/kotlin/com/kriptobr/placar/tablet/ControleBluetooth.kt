package com.kriptobr.placar.tablet

import android.content.Context
import android.view.KeyEvent

/**
 * Controle Bluetooth de disparo.
 *
 * Esses controles se apresentam ao Android como teclado. O aplicativo
 * captura o evento de tecla e o consome, senao o botao mexe no volume
 * do sistema junto.
 *
 * O mapeamento e configuravel porque cada modelo envia teclas diferentes.
 * A captura de tecla do modo de teste mostra qual codigo chegou.
 */
object ControleBluetooth {

    const val ACAO_NENHUMA = ""
    const val ACAO_ESQUERDA = "ESQUERDA"
    const val ACAO_DIREITA = "DIREITA"
    const val ACAO_DESFAZER = "DESFAZER"

    private const val PREFS = "controle"

    private val padrao = mapOf(
        KeyEvent.KEYCODE_VOLUME_UP to ACAO_ESQUERDA,
        KeyEvent.KEYCODE_VOLUME_DOWN to ACAO_DIREITA,
        KeyEvent.KEYCODE_DPAD_LEFT to ACAO_ESQUERDA,
        KeyEvent.KEYCODE_DPAD_RIGHT to ACAO_DIREITA,
        KeyEvent.KEYCODE_PAGE_UP to ACAO_ESQUERDA,
        KeyEvent.KEYCODE_PAGE_DOWN to ACAO_DIREITA,
        KeyEvent.KEYCODE_DPAD_UP to ACAO_ESQUERDA,
        KeyEvent.KEYCODE_DPAD_DOWN to ACAO_DIREITA,
        KeyEvent.KEYCODE_ENTER to ACAO_DESFAZER,
        KeyEvent.KEYCODE_DPAD_CENTER to ACAO_DESFAZER
    )

    fun acaoDe(contexto: Context, codigo: Int): String {
        val prefs = contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val personalizada = prefs.getString("tecla_$codigo", null)
        if (personalizada != null) return personalizada
        return padrao[codigo] ?: ACAO_NENHUMA
    }

    fun mapear(contexto: Context, codigo: Int, acao: String) {
        contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("tecla_$codigo", acao)
            .apply()
    }

    fun nomeDaTecla(codigo: Int): String = runCatching {
        KeyEvent.keyCodeToString(codigo).removePrefix("KEYCODE_")
    }.getOrDefault(codigo.toString())
}
