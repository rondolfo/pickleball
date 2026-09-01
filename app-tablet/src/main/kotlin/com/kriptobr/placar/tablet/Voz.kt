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
    private var anunciarSideOut = false
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

    /**
     * "Side out" e o termo oficial e significa apenas que o saque mudou de
     * dupla, nao que a bola saiu. Como isso confunde quem esta jogando,
     * o padrao passou a ser anunciar quem assume o saque, que e inequivoco.
     */
    fun definirEstiloDeTroca(usarSideOut: Boolean) {
        anunciarSideOut = usarSideOut
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
    fun anunciar(anterior: EstadoJogo, novo: EstadoJogo, nomeSacador: String = "") {
        val texto = montarFrase(anterior, novo, nomeSacador)
        ultimaFala = texto
        agendar(texto)
    }

    fun repetir() {
        if (ultimaFala.isNotEmpty()) falarAgora(ultimaFala)
    }

    /**
     * Fala um placar de exemplo no idioma pedido, sem alterar o idioma
     * configurado. Usado pelo modo de teste, para ajustar volume e entonacao.
     */
    fun falarExemplo(idiomaAlvo: String) {
        if (!pronto) return
        val guardado = idioma
        idioma = idiomaAlvo
        aplicarIdioma()
        val exemplo = listOf(termo("side_out"), "4, 2, 1", termo("game_point")).joinToString(", ")
        falarAgora(exemplo)
        idioma = guardado
        executor.postDelayed({ aplicarIdioma() }, 3000L)
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

    /**
     * O nome do sacador so entra quando o sacador muda.
     *
     * Falar o nome em todo ponto deixa a chamada lenta e cansa. Falar na
     * troca e exatamente quando a duvida existe.
     */
    private fun montarFrase(
        anterior: EstadoJogo,
        novo: EstadoJogo,
        nomeSacador: String
    ): String {
        val partes = mutableListOf<String>()

        if (novo.encerrado) {
            partes.add(termo("game"))
            partes.add(chamadaFalada(novo))
            return partes.joinToString(", ")
        }

        if (novo.sacando != anterior.sacando && anunciarSideOut) partes.add(termo("side_out"))

        partes.add(chamadaFalada(novo))

        val trocouSacador = novo.sacando != anterior.sacando ||
            novo.indiceSacador != anterior.indiceSacador
        if (trocouSacador && nomeSacador.isNotBlank()) {
            partes.add(anuncioDeSacador(nomeSacador))
        }

        if (novo.pontoDeJogo) partes.add(termo("game_point"))

        return partes.joinToString(", ")
    }

    private fun anuncioDeSacador(nome: String): String = when (idioma) {
        Textos.PT -> "saque de $nome"
        else -> "$nome to serve"
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
