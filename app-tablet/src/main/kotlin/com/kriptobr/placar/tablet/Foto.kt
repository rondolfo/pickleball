package com.kriptobr.placar.tablet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * Foto do jogador.
 *
 * O seletor moderno de imagens do Android nao exige permissao nenhuma,
 * entao a foto sai barata. A imagem e reduzida antes de salvar para o
 * backup nao inchar.
 */
object Foto {

    private const val LADO_MAXIMO = 256

    fun salvar(contexto: Context, origem: Uri, destino: File): Boolean = runCatching {
        val entrada = contexto.contentResolver.openInputStream(origem) ?: return false
        val original = entrada.use { BitmapFactory.decodeStream(it) } ?: return false

        val maior = maxOf(original.width, original.height).coerceAtLeast(1)
        val escala = LADO_MAXIMO.toFloat() / maior
        val reduzida = if (escala < 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * escala).toInt().coerceAtLeast(1),
                (original.height * escala).toInt().coerceAtLeast(1),
                true
            )
        } else {
            original
        }

        destino.outputStream().use { saida ->
            reduzida.compress(Bitmap.CompressFormat.JPEG, 85, saida)
        }
        true
    }.getOrDefault(false)

    /**
     * Reduz e grava uma foto que ja esta em arquivo, como a que a camera
     * acabou de tirar em resolucao cheia.
     */
    fun reduzirNoLugar(arquivo: File): Boolean = runCatching {
        val original = BitmapFactory.decodeFile(arquivo.absolutePath) ?: return false
        val maior = maxOf(original.width, original.height).coerceAtLeast(1)
        val escala = LADO_MAXIMO.toFloat() / maior
        if (escala >= 1f) return true

        val reduzida = Bitmap.createScaledBitmap(
            original,
            (original.width * escala).toInt().coerceAtLeast(1),
            (original.height * escala).toInt().coerceAtLeast(1),
            true
        )
        arquivo.outputStream().use { saida ->
            reduzida.compress(Bitmap.CompressFormat.JPEG, 85, saida)
        }
        true
    }.getOrDefault(false)

    fun carregar(arquivo: File): Bitmap? = runCatching {
        if (!arquivo.exists()) null else BitmapFactory.decodeFile(arquivo.absolutePath)
    }.getOrNull()

    /** Cor estavel a partir do nome, para o circulo de iniciais. */
    fun corDe(texto: String): Long {
        // Sem o verde de quem saca, sem o ambar do ponto de jogo e sem verde
        // oliva: um circulo dessas cores sumia dentro do proprio anel de destaque.
        val paleta = listOf(
            0xFF6BA6D6, 0xFFD07A9E, 0xFF9B8ADB, 0xFF4FB3A5,
            0xFFD4694A, 0xFF7F8FA6, 0xFFA97C5B, 0xFFC8B68E
        )
        val indice = (texto.hashCode().toLong() and 0x7FFFFFFF) % paleta.size
        return paleta[indice.toInt()]
    }
}
