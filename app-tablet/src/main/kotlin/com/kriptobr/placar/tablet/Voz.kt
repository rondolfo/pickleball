package com.kriptobr.placar.tablet

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.LANG_MISSING_DATA
import android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED
import com.kriptobr.placar.core.EstadoJogo
import java.util.Locale

/**
 * Chamada falada do placar.
 *
 * Os termos ficam em ingles nos dois idiomas, porque e o vocabulario que os
 * jogadores usam. Apenas os numeros mudam de idioma. Como o motor de voz em
 * portugues pronuncia termos em ingles letra por letra, eles sao escritos
 * foneticamente aqui. Ajuste ouvindo em quadra, e so mexer nestas linhas.
 */
class Voz(private val contexto: Context) {

    companion object {
        private const val ATRASO_MS = 800L
    }

    private val executor = Handler(Looper.getMainLooper())
    private var motor: TextToSpeech? = null
    private var pronto = false
    private var idioma = Textos.EN
    private var pendente: Runnable? = null
    private var ultimaFala: String = ""

    fun iniciar(aoFicarPronto: () -> Unit = {}) {
        motor = TextToSpeech(contexto) { status ->
            pronto = status == TextToSpeech.SUCCESS
            if (pronto) {
                aplicarIdioma()
                aoFicarPronto()
            }
        }
    }

    fun definirIdioma(novo: String) {
        idioma = novo
        aplicarIdioma()
    }

    /** Retorna false quando a voz do idioma nao esta instalada no aparelho. */
    fun disponivel(alvo: String): Boolean {
        val maquina = motor ?: return false
        val resultado = maquina.isLanguageAvailable(localeDe(alvo))
        return resultado != LANG_MISSING_DATA && resultado != LANG_NOT_SUPPORTED
    }

    /**
     * A fala sai com atraso. Se o ponto for desfeito nesse intervalo,
     * o placar errado nunca chega a ser falado.
     */
    fun anunciar(anterior: EstadoJogo, novo: EstadoJogo) {
        val texto = montarFrase(anterior, novo)
        ultimaFala = texto
        agendar(texto)
    }

    fun repetir() {
        if (ultimaFala.isNotEmpty()) falarAgora(ultimaFala)
    }

    fun cancelarPendente() {
        pendente?.let { executor.removeCallbacks(it) }
        pendente = null
    }

    fun liberar() {
        cancelarPendente()
        motor?.stop()
        motor?.shutdown()
        motor = null
    }

    private fun agendar(texto: String) {
        cancelarPendente()
        val tarefa = Runnable { falarAgora(texto) }
        pendente = tarefa
        executor.postDelayed(tarefa, ATRASO_MS)
    }

    private fun falarAgora(texto: String) {
        if (!pronto) return
        motor?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "placar")
    }

    private fun aplicarIdioma() {
        if (!pronto) return
        motor?.language = localeDe(idioma)
    }

    private fun localeDe(alvo: String): Locale =
        if (alvo == Textos.PT) Locale("pt", "BR") else Locale.US

    private fun montarFrase(anterior: EstadoJogo, novo: EstadoJogo): String {
        val partes = mutableListOf<String>()

        if (novo.encerrado) {
            partes.add(termo("game"))
            partes.add(chamadaFalada(novo))
            return partes.joinToString(", ")
        }

        if (novo.sacando != anterior.sacando) partes.add(termo("side_out"))

        partes.add(chamadaFalada(novo))

        if (novo.pontoDeJogo) partes.add(termo("game_point"))

        return partes.joinToString(", ")
    }

    /** Numeros separados por virgula, que e o que cria a pausa entre eles. */
    private fun chamadaFalada(estado: EstadoJogo): String =
        "${estado.pontosSacador}, ${estado.pontosRecebedor}, ${estado.sacador}"

    private fun termo(chave: String): String = when (idioma) {
        Textos.PT -> when (chave) {
            "side_out" -> "sáid aut"
            "game_point" -> "guêimi point"
            "game" -> "guêimi"
            else -> chave
        }
        else -> when (chave) {
            "side_out" -> "side out"
            "game_point" -> "game point"
            "game" -> "game"
            else -> chave
        }
    }
}
