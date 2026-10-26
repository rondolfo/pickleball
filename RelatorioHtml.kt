package com.kriptobr.placar.tablet

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Grava e compartilha o relatorio da temporada.
 * O conteudo em si e montado por RelatorioConteudo, que nao depende do Android.
 */
object RelatorioHtml {

    fun gerar(
        contexto: Context,
        partidas: List<Partida>,
        jogadores: List<Jogador>,
        idioma: String
    ): File {
        val arquivo = File(Repositorio(contexto).pastaExport, "pickleball-report.html")
        arquivo.writeText(RelatorioConteudo.montar(partidas, jogadores, idioma))
        return arquivo
    }

    fun compartilhar(contexto: Context, arquivo: File): Boolean = runCatching {
        val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.arquivos", arquivo)
        val intencao = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        contexto.startActivity(Intent.createChooser(intencao, "Report"))
        true
    }.getOrDefault(false)
}
