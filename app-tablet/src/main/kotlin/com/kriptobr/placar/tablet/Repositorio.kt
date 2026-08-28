package com.kriptobr.placar.tablet

import android.content.Context
import android.util.Log
import com.kriptobr.placar.core.Evento
import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.Origem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persistencia em arquivos JSON dentro do proprio aplicativo.
 *
 * Escolha deliberada: um banco com processamento de anotacoes traria
 * dependencia de build sem beneficio real nesta escala. Alguns milhares
 * de partidas cabem tranquilamente aqui, e o backup vira copiar arquivos.
 */
class Repositorio(private val contexto: Context) {

    companion object {
        private const val TAG = "Repositorio"
    }

    private val base: File get() = contexto.filesDir
    private val arquivoJogadores: File get() = File(base, "jogadores.json")
    private val pastaPartidas: File get() = File(base, "partidas").also { it.mkdirs() }
    private val arquivoAtual: File get() = File(base, "atual.json")
    val pastaFotos: File get() = File(base, "fotos").also { it.mkdirs() }

    fun arquivoFoto(idJogador: String): File = File(pastaFotos, "$idJogador.jpg")

    // ---------- jogadores ----------

    fun carregarJogadores(): List<Jogador> = runCatching {
        if (!arquivoJogadores.exists()) return emptyList()
        val lista = JSONArray(arquivoJogadores.readText())
        (0 until lista.length()).map { indice ->
            val objeto = lista.getJSONObject(indice)
            Jogador(
                id = objeto.getString("id"),
                nome = objeto.optString("nome"),
                apelido = objeto.optString("apelido"),
                email = objeto.optString("email"),
                sexo = objeto.optString("sexo"),
                temFoto = objeto.optBoolean("temFoto", false)
            )
        }
    }.getOrElse {
        Log.w(TAG, "falha ao ler jogadores: ${it.message}")
        emptyList()
    }

    fun salvarJogadores(lista: List<Jogador>) {
        runCatching {
            val array = JSONArray()
            lista.forEach { jogador ->
                array.put(JSONObject().apply {
                    put("id", jogador.id)
                    put("nome", jogador.nome)
                    put("apelido", jogador.apelido)
                    put("email", jogador.email)
                    put("sexo", jogador.sexo)
                    put("temFoto", jogador.temFoto)
                })
            }
            arquivoJogadores.writeText(array.toString())
        }.onFailure { Log.e(TAG, "falha ao salvar jogadores: ${it.message}") }
    }

    // ---------- partidas ----------

    fun salvarPartida(partida: Partida) {
        runCatching {
            File(pastaPartidas, "${partida.inicio}-${partida.id}.json")
                .writeText(paraJson(partida).toString())
        }.onFailure { Log.e(TAG, "falha ao salvar partida: ${it.message}") }
    }

    fun listarPartidas(limite: Int = 200): List<Partida> = runCatching {
        pastaPartidas.listFiles()
            ?.sortedByDescending { it.name }
            ?.take(limite)
            ?.mapNotNull { arquivo ->
                runCatching { deJson(JSONObject(arquivo.readText())) }.getOrNull()
            }
            ?: emptyList()
    }.getOrDefault(emptyList())

    // ---------- partida em andamento ----------

    fun salvarAtual(partida: Partida) {
        runCatching { arquivoAtual.writeText(paraJson(partida).toString()) }
    }

    fun carregarAtual(): Partida? = runCatching {
        if (!arquivoAtual.exists()) null
        else deJson(JSONObject(arquivoAtual.readText()))
    }.getOrNull()

    fun limparAtual() {
        runCatching { if (arquivoAtual.exists()) arquivoAtual.delete() }
    }

    // ---------- conversao ----------

    private fun paraJson(partida: Partida): JSONObject = JSONObject().apply {
        put("id", partida.id)
        put("inicio", partida.inicio)
        partida.fim?.let { put("fim", it) }
        put("primeiroSaque", partida.primeiroSaque.name)
        put("esqA", partida.duplaEsquerda.a ?: "")
        put("esqB", partida.duplaEsquerda.b ?: "")
        put("dirA", partida.duplaDireita.a ?: "")
        put("dirB", partida.duplaDireita.b ?: "")
        put("completa", partida.completa)
        put("eventos", JSONArray().also { array ->
            partida.eventos.forEach { evento ->
                array.put(JSONObject().apply {
                    put("id", evento.id)
                    put("vencedor", evento.vencedor.name)
                    put("origem", evento.origem.name)
                    put("ts", evento.ts)
                })
            }
        })
    }

    private fun deJson(objeto: JSONObject): Partida {
        val eventos = objeto.optJSONArray("eventos") ?: JSONArray()
        return Partida(
            id = objeto.getString("id"),
            inicio = objeto.getLong("inicio"),
            fim = if (objeto.has("fim")) objeto.getLong("fim") else null,
            primeiroSaque = Lado.valueOf(objeto.optString("primeiroSaque", "ESQUERDA")),
            duplaEsquerda = Dupla(
                objeto.optString("esqA").ifBlank { null },
                objeto.optString("esqB").ifBlank { null }
            ),
            duplaDireita = Dupla(
                objeto.optString("dirA").ifBlank { null },
                objeto.optString("dirB").ifBlank { null }
            ),
            eventos = (0 until eventos.length()).map { indice ->
                val e = eventos.getJSONObject(indice)
                Evento(
                    id = e.getString("id"),
                    vencedor = Lado.valueOf(e.getString("vencedor")),
                    origem = runCatching { Origem.valueOf(e.optString("origem")) }
                        .getOrDefault(Origem.TOQUE),
                    ts = e.optLong("ts")
                )
            },
            completa = objeto.optBoolean("completa", false)
        )
    }
}
