package com.kriptobr.placar.tablet

import android.content.Context
import android.util.Log
import com.kriptobr.placar.core.EstadoJogo
import com.kriptobr.placar.core.Evento
import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.MemoriaRodizio
import com.kriptobr.placar.core.Origem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * Persistencia em arquivos JSON dentro do proprio aplicativo.
 *
 * Escolha deliberada: um banco com processamento de anotacoes traria
 * dependencia de build sem beneficio real nesta escala.
 */
class Repositorio(private val contexto: Context) {

    companion object {
        private const val TAG = "Repositorio"
    }

    private val base: File get() = contexto.filesDir
    private val arquivoJogadores: File get() = File(base, "jogadores.json")
    private val pastaPartidas: File get() = File(base, "partidas").also { it.mkdirs() }
    private val arquivoAtual: File get() = File(base, "atual.json")
    private val arquivoRodizio: File get() = File(base, "rodizio.json")
    val pastaExport: File get() = File(base, "export").also { it.mkdirs() }
    val pastaFotos: File get() = File(base, "fotos").also { it.mkdirs() }

    fun arquivoFoto(idJogador: String): File = File(pastaFotos, "$idJogador.jpg")

    // ---------- gravacao segura ----------

    /**
     * Grava num arquivo temporario e so depois troca pelo definitivo.
     *
     * Antes cada arquivo era reescrito no lugar. Se o tablet desligasse no
     * meio de uma gravacao da lista de jogadores, o arquivo ficava pela
     * metade, a leitura seguinte devolvia uma lista vazia, e a proxima
     * gravacao salvava essa lista vazia por cima: o cadastro inteiro sumia.
     * A troca por nome e atomica no Android: ou fica o arquivo velho inteiro,
     * ou o novo inteiro, nunca um pedaco.
     *
     * Para os cadastros, a versao anterior tambem fica guardada em .bak.
     */
    private fun gravarSeguro(destino: File, conteudo: String, manterCopia: Boolean) {
        val pasta = destino.parentFile ?: base
        val temporario = File(pasta, destino.name + ".tmp")
        temporario.outputStream().use { saida ->
            saida.write(conteudo.toByteArray(Charsets.UTF_8))
            saida.fd.sync()
        }
        if (manterCopia && destino.exists()) {
            runCatching { destino.copyTo(copiaDe(destino), overwrite = true) }
        }
        if (!temporario.renameTo(destino)) {
            destino.delete()
            if (!temporario.renameTo(destino)) {
                throw IOException("nao foi possivel substituir ${destino.name}")
            }
        }
    }

    private fun copiaDe(arquivo: File): File = File(arquivo.parentFile ?: base, arquivo.name + ".bak")

    /**
     * Le o arquivo principal e, se ele estiver estragado, a copia anterior.
     *
     * Um arquivo que nao abre e guardado com outro nome antes de qualquer
     * coisa, para a proxima gravacao nao apagar o que ainda daria para
     * recuperar a mao.
     */
    private fun <T> lerComReserva(arquivo: File, ler: (String) -> T): T? {
        for (candidato in listOf(arquivo, copiaDe(arquivo))) {
            if (!candidato.exists()) continue
            val resultado = runCatching { ler(candidato.readText()) }
            if (resultado.isSuccess) return resultado.getOrNull()
            Log.w(TAG, "arquivo ilegivel: ${candidato.name}: ${resultado.exceptionOrNull()?.message}")
            runCatching {
                candidato.copyTo(
                    File(candidato.parentFile ?: base, "${candidato.name}.ilegivel-${System.currentTimeMillis()}"),
                    overwrite = false
                )
            }
        }
        return null
    }

    // ---------- jogadores ----------

    fun carregarJogadores(): List<Jogador> = lerComReserva(arquivoJogadores) { texto ->
        val lista = JSONArray(texto)
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
    } ?: emptyList()

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
            gravarSeguro(arquivoJogadores, array.toString(), manterCopia = true)
        }.onFailure { Log.e(TAG, "falha ao salvar jogadores: ${it.message}") }
    }

    // ---------- partidas ----------

    fun salvarPartida(partida: Partida) {
        runCatching {
            gravarSeguro(
                File(pastaPartidas, "${partida.inicio}-${partida.id}.json"),
                paraJson(partida).toString(),
                manterCopia = false
            )
        }.onFailure { Log.e(TAG, "falha ao salvar partida: ${it.message}") }
    }

    fun listarPartidas(limite: Int = 300): List<Partida> = runCatching {
        pastaPartidas.listFiles()
            ?.sortedByDescending { it.name }
            ?.take(limite)
            ?.mapNotNull { arquivo ->
                runCatching { deJson(JSONObject(arquivo.readText())) }.getOrNull()
            }
            ?: emptyList()
    }.getOrDefault(emptyList())

    fun salvarAtual(partida: Partida) {
        runCatching { gravarSeguro(arquivoAtual, paraJson(partida).toString(), manterCopia = false) }
    }

    fun carregarAtual(): Partida? = runCatching {
        if (!arquivoAtual.exists()) null
        else deJson(JSONObject(arquivoAtual.readText()))
    }.getOrNull()

    fun limparAtual() {
        runCatching { if (arquivoAtual.exists()) arquivoAtual.delete() }
    }

    // ---------- rodizio ----------

    fun carregarRodizio(): Pair<MemoriaRodizio, Set<String>> = lerComReserva(arquivoRodizio) { texto ->
        val objeto = JSONObject(texto)

        val ultima = mutableMapOf<String, Int>()
        objeto.optJSONObject("ultima")?.let { mapa ->
            mapa.keys().forEach { chave -> ultima[chave] = mapa.getInt(chave) }
        }
        val jogos = mutableMapOf<String, Int>()
        objeto.optJSONObject("jogos")?.let { mapa ->
            mapa.keys().forEach { chave -> jogos[chave] = mapa.getInt(chave) }
        }
        val parceiro = mutableMapOf<String, String>()
        objeto.optJSONObject("parceiro")?.let { mapa ->
            mapa.keys().forEach { chave -> parceiro[chave] = mapa.getString(chave) }
        }
        val adversarios = mutableMapOf<String, Set<String>>()
        objeto.optJSONObject("adversarios")?.let { mapa ->
            mapa.keys().forEach { chave ->
                val lista = mapa.getJSONArray(chave)
                adversarios[chave] = (0 until lista.length()).map { lista.getString(it) }.toSet()
            }
        }
        val presentes = objeto.optJSONArray("presentes")?.let { lista ->
            (0 until lista.length()).map { lista.getString(it) }.toSet()
        } ?: emptySet()

        MemoriaRodizio(
            rodada = objeto.optInt("rodada", 0),
            ultimaRodadaPorJogador = ultima,
            jogosPorJogador = jogos,
            ultimoParceiro = parceiro,
            ultimosAdversarios = adversarios
        ) to presentes
    } ?: (MemoriaRodizio() to emptySet())

    fun salvarRodizio(memoria: MemoriaRodizio, presentes: Set<String>) {
        runCatching {
            val objeto = JSONObject().apply {
                put("rodada", memoria.rodada)
                put("ultima", JSONObject(memoria.ultimaRodadaPorJogador as Map<*, *>))
                put("jogos", JSONObject(memoria.jogosPorJogador as Map<*, *>))
                put("parceiro", JSONObject(memoria.ultimoParceiro as Map<*, *>))
                put("adversarios", JSONObject().also { mapa ->
                    memoria.ultimosAdversarios.forEach { (chave, valores) ->
                        mapa.put(chave, JSONArray(valores.toList()))
                    }
                })
                put("presentes", JSONArray(presentes.toList()))
            }
            gravarSeguro(arquivoRodizio, objeto.toString(), manterCopia = true)
        }.onFailure { Log.e(TAG, "falha ao salvar rodizio: ${it.message}") }
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

        partida.base?.let { estado ->
            put("base", JSONObject().apply {
                put("esq", estado.pontosEsquerda)
                put("dir", estado.pontosDireita)
                put("sacando", estado.sacando.name)
                put("sacador", estado.sacador)
            })
        }

        put("escalacoes", JSONArray().also { array ->
            partida.escalacoes.forEach { escalacao ->
                array.put(JSONObject().apply {
                    put("ts", escalacao.ts)
                    put("esqA", escalacao.esquerda.a ?: "")
                    put("esqB", escalacao.esquerda.b ?: "")
                    put("dirA", escalacao.direita.a ?: "")
                    put("dirB", escalacao.direita.b ?: "")
                })
            }
        })

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
        val escalacoes = objeto.optJSONArray("escalacoes") ?: JSONArray()

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
            escalacoes = (0 until escalacoes.length()).map { indice ->
                val e = escalacoes.getJSONObject(indice)
                Escalacao(
                    ts = e.optLong("ts"),
                    esquerda = Dupla(
                        e.optString("esqA").ifBlank { null },
                        e.optString("esqB").ifBlank { null }
                    ),
                    direita = Dupla(
                        e.optString("dirA").ifBlank { null },
                        e.optString("dirB").ifBlank { null }
                    )
                )
            },
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
            base = objeto.optJSONObject("base")?.let { b ->
                EstadoJogo(
                    pontosEsquerda = b.optInt("esq"),
                    pontosDireita = b.optInt("dir"),
                    sacando = Lado.valueOf(b.optString("sacando", "ESQUERDA")),
                    sacador = b.optInt("sacador", 2)
                )
            },
            completa = objeto.optBoolean("completa", false)
        )
    }
}
