package com.kriptobr.placar.tablet

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kriptobr.placar.core.Lado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Resumo da partida enviado por e-mail.
 *
 * O aplicativo nao envia nada sozinho: ele monta a mensagem e abre o
 * aplicativo de e-mail ja preenchido. Voce confere e toca em enviar.
 * Sem senha, sem servidor, sem autenticacao para manter.
 */
object ResumoEmail {

    fun destinatarios(partida: Partida, jogadores: List<Jogador>): Array<String> {
        val porId = jogadores.associateBy { it.id }
        return (partida.duplaEsquerda.ids + partida.duplaDireita.ids)
            .mapNotNull { porId[it]?.email?.trim() }
            .filter { it.contains("@") }
            .distinct()
            .toTypedArray()
    }

    fun assunto(partida: Partida, jogadores: List<Jogador>, idioma: String): String {
        val data = formatoData(idioma).format(Date(partida.inicio))
        val esq = nomeDupla(partida.duplaEsquerda, jogadores, idioma)
        val dir = nomeDupla(partida.duplaDireita, jogadores, idioma)
        val cabecalho = if (idioma == Textos.PT) "Placar do jogo" else "Pickleball result"
        return "$cabecalho: $esq x $dir, $data"
    }

    fun corpo(partida: Partida, jogadores: List<Jogador>, idioma: String): String {
        val pt = idioma == Textos.PT
        val stats = partida.estatisticas()
        val esq = nomeDupla(partida.duplaEsquerda, jogadores, idioma)
        val dir = nomeDupla(partida.duplaDireita, jogadores, idioma)

        val vencedor = when {
            stats.pontosEsquerda > stats.pontosDireita -> esq
            stats.pontosDireita > stats.pontosEsquerda -> dir
            else -> null
        }

        val linhas = mutableListOf<String>()

        linhas.add(if (pt) "Resumo do jogo" else "Game summary")
        linhas.add("")
        linhas.add(formatoData(idioma).format(Date(partida.inicio)))
        partida.fim?.let { fim ->
            val minutos = ((fim - partida.inicio) / 60000L).coerceAtLeast(0)
            linhas.add(if (pt) "Duracao: $minutos minutos" else "Duration: $minutos minutes")
        }
        linhas.add("")
        linhas.add("$esq  ${stats.pontosEsquerda}")
        linhas.add("$dir  ${stats.pontosDireita}")

        if (vencedor != null && partida.completa) {
            linhas.add("")
            linhas.add(if (pt) "Vitoria: $vencedor" else "Winner: $vencedor")
        }

        linhas.add("")
        linhas.add(if (pt) "Numeros da partida" else "Match numbers")
        linhas.add(
            if (pt) "Total de rallies: ${stats.totalRallies}"
            else "Total rallies: ${stats.totalRallies}"
        )
        linhas.add(
            if (pt) "Rallies ganhos: $esq ${stats.ralliesEsquerda}, $dir ${stats.ralliesDireita}"
            else "Rallies won: $esq ${stats.ralliesEsquerda}, $dir ${stats.ralliesDireita}"
        )
        linhas.add(
            if (pt) "Rallies ganhos recebendo: $esq ${stats.ralliesRecebendoEsquerda}, $dir ${stats.ralliesRecebendoDireita}"
            else "Rallies won while receiving: $esq ${stats.ralliesRecebendoEsquerda}, $dir ${stats.ralliesRecebendoDireita}"
        )
        linhas.add(
            if (pt) "Turnos de saque sem pontuar: $esq ${stats.turnosSecosEsquerda}, $dir ${stats.turnosSecosDireita}"
            else "Service turns without scoring: $esq ${stats.turnosSecosEsquerda}, $dir ${stats.turnosSecosDireita}"
        )
        linhas.add(
            if (pt) "Maior sequencia de pontos: $esq ${stats.maiorSequenciaEsquerda}, $dir ${stats.maiorSequenciaDireita}"
            else "Longest point streak: $esq ${stats.maiorSequenciaEsquerda}, $dir ${stats.maiorSequenciaDireita}"
        )
        linhas.add(
            if (pt) "Viradas no placar: ${stats.viradas}"
            else "Lead changes: ${stats.viradas}"
        )

        linhas.add("")
        linhas.add(
            if (pt) "Enviado pelo Placar Pickleball"
            else "Sent from Pickleball Scoreboard"
        )

        return linhas.joinToString("\n")
    }

    /**
     * Abre o aplicativo de e-mail com tudo preenchido.
     * Retorna false quando nao ha aplicativo de e-mail instalado.
     */
    fun abrir(
        contexto: Context,
        partida: Partida,
        jogadores: List<Jogador>,
        idioma: String
    ): Boolean {
        val intencao = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, destinatarios(partida, jogadores))
            putExtra(Intent.EXTRA_SUBJECT, assunto(partida, jogadores, idioma))
            putExtra(Intent.EXTRA_TEXT, corpo(partida, jogadores, idioma))
        }
        return runCatching {
            contexto.startActivity(intencao)
            true
        }.getOrDefault(false)
    }

    fun nomeDupla(dupla: Dupla, jogadores: List<Jogador>, idioma: String): String {
        val porId = jogadores.associateBy { it.id }
        val nomes = dupla.ids.mapNotNull { porId[it]?.curto }
        return if (nomes.isEmpty()) {
            Textos.get(if (dupla.vazia) "sem_nome" else "sem_nome", idioma)
        } else {
            nomes.joinToString(" & ")
        }
    }

    fun nomeLado(
        lado: Lado,
        partida: Partida,
        jogadores: List<Jogador>,
        idioma: String
    ): String {
        val dupla = if (lado == Lado.ESQUERDA) partida.duplaEsquerda else partida.duplaDireita
        return if (dupla.vazia) {
            Textos.get(if (lado == Lado.ESQUERDA) "esquerda" else "direita", idioma)
        } else {
            nomeDupla(dupla, jogadores, idioma)
        }
    }

    private fun formatoData(idioma: String): SimpleDateFormat =
        if (idioma == Textos.PT) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        } else {
            SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
        }
}
