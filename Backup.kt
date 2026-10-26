package com.kriptobr.placar.tablet

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Backup completo em um unico arquivo.
 *
 * Chave de assinatura fixa resolve a atualizacao, mas nao resolve tablet
 * perdido, quebrado ou trocado. Isto resolve. Guarde o arquivo na nuvem
 * depois de cadastrar o grupo.
 */
object Backup {

    private val itens = listOf("jogadores.json", "rodizio.json", "atual.json")
    private val pastas = listOf("partidas", "fotos")

    fun exportar(contexto: Context): File {
        val repositorio = Repositorio(contexto)
        val carimbo = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        val destino = File(repositorio.pastaExport, "placar-backup-$carimbo.zip")

        ZipOutputStream(destino.outputStream().buffered()).use { zip ->
            itens.forEach { nome ->
                val arquivo = File(contexto.filesDir, nome)
                if (arquivo.exists()) gravar(zip, arquivo, nome)
            }
            pastas.forEach { pasta ->
                File(contexto.filesDir, pasta).listFiles()?.forEach { arquivo ->
                    if (arquivo.isFile) gravar(zip, arquivo, "$pasta/${arquivo.name}")
                }
            }
        }
        return destino
    }

    fun compartilhar(contexto: Context, arquivo: File): Boolean = runCatching {
        val uri = FileProvider.getUriForFile(
            contexto,
            "${contexto.packageName}.arquivos",
            arquivo
        )
        val intencao = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        contexto.startActivity(Intent.createChooser(intencao, "Backup"))
        true
    }.getOrDefault(false)

    /**
     * Restaura por cima do que existe. Nomes de entrada sao validados para
     * o arquivo nao conseguir escrever fora da pasta do aplicativo.
     */
    fun importar(contexto: Context, origem: Uri): Boolean = runCatching {
        val entrada = contexto.contentResolver.openInputStream(origem) ?: return false
        val raiz = contexto.filesDir
        File(raiz, "partidas").mkdirs()
        File(raiz, "fotos").mkdirs()

        var restaurados = 0
        ZipInputStream(entrada.buffered()).use { zip ->
            var item: ZipEntry? = zip.nextEntry
            while (item != null) {
                val nome = item.name
                if (!nome.contains("..") && !nome.startsWith("/")) {
                    val destino = File(raiz, nome)
                    if (destino.canonicalPath.startsWith(raiz.canonicalPath)) {
                        destino.parentFile?.mkdirs()
                        destino.outputStream().use { saida -> zip.copyTo(saida) }
                        restaurados++
                    }
                }
                zip.closeEntry()
                item = zip.nextEntry
            }
        }
        restaurados > 0
    }.getOrDefault(false)

    private fun gravar(zip: ZipOutputStream, arquivo: File, nome: String) {
        zip.putNextEntry(ZipEntry(nome))
        arquivo.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }
}
