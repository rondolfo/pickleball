package com.kriptobr.placar.tablet

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.kriptobr.placar.core.Analise
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exportacao das partidas em CSV.
 *
 * O arquivo e compartilhado por FileProvider, entao abre direto em
 * planilha, e-mail ou nuvem, sem precisar de permissao de armazenamento.
 */
object Exportacao {

    fun gerarCsv(contexto: Context, partidas: List<Partida>, jogadores: List<Jogador>): File {
        val porId = jogadores.associateBy { it.id }
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val linhas = StringBuilder()

        linhas.append(
            "data,duracao_min,esquerda,direita,pontos_esquerda,pontos_direita," +
                "completa,rallies,rallies_esq,rallies_dir,recebendo_esq,recebendo_dir," +
                "secos_esq,secos_dir,sequencia_esq,sequencia_dir,viradas\n"
        )

        partidas.sortedBy { it.inicio }.forEach { partida ->
            val stats = Analise.calcular(partida.eventos, partida.primeiroSaque)
            val minutos = partida.fim?.let { (it - partida.inicio) / 60000L } ?: 0L
            val esq = partida.duplaEsquerda.ids.mapNotNull { porId[it]?.curto }.joinToString(" & ")
            val dir = partida.duplaDireita.ids.mapNotNull { porId[it]?.curto }.joinToString(" & ")

            linhas.append(
                listOf(
                    formato.format(Date(partida.inicio)),
                    minutos.toString(),
                    escapar(esq),
                    escapar(dir),
                    stats.pontosEsquerda,
                    stats.pontosDireita,
                    if (partida.completa) "sim" else "nao",
                    stats.totalRallies,
                    stats.ralliesEsquerda,
                    stats.ralliesDireita,
                    stats.ralliesRecebendoEsquerda,
                    stats.ralliesRecebendoDireita,
                    stats.turnosSecosEsquerda,
                    stats.turnosSecosDireita,
                    stats.maiorSequenciaEsquerda,
                    stats.maiorSequenciaDireita,
                    stats.viradas
                ).joinToString(",")
            )
            linhas.append("\n")
        }

        val arquivo = File(Repositorio(contexto).pastaExport, "pickleball.csv")
        arquivo.writeText(linhas.toString())
        return arquivo
    }

    fun compartilhar(contexto: Context, arquivo: File): Boolean = runCatching {
        val uri = FileProvider.getUriForFile(
            contexto,
            "${contexto.packageName}.arquivos",
            arquivo
        )
        val intencao = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        contexto.startActivity(Intent.createChooser(intencao, "CSV"))
        true
    }.getOrDefault(false)
}

private fun escapar(texto: String): String =
    if (texto.contains(",") || texto.contains("\"")) {
        "\"" + texto.replace("\"", "\"\"") + "\""
    } else {
        texto
    }
