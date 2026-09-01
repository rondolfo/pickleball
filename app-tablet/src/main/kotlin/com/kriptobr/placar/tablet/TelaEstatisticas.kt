package com.kriptobr.placar.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaEstatisticas(
    jogadores: List<Jogador>,
    partidas: List<Partida>,
    idiomaUi: String,
    versaoFotos: Int,
    onExportar: () -> Unit,
    onEmailSessao: (List<Partida>) -> Unit,
    onFechar: () -> Unit
) {
    var soHoje by remember { mutableStateOf(true) }

    val porId = jogadores.associateBy { it.id }
    val doDia = EstatisticasJogador.doDia(partidas)
    val recorte = if (soHoje) doDia else partidas
    val resumos = EstatisticasJogador.ranking(recorte, jogadores)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF7000000))
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Cabecalho(Textos.get("ranking", idiomaUi), idiomaUi, onFechar)

            Row(modifier = Modifier.padding(bottom = 10.dp)) {
                Botao(
                    Textos.get("hoje", idiomaUi),
                    if (soHoje) VERDE else CINZA_TEXTO
                ) { soHoje = true }
                Botao(
                    Textos.get("sempre", idiomaUi),
                    if (!soHoje) VERDE else CINZA_TEXTO
                ) { soHoje = false }
                Text(
                    "${recorte.size} ${Textos.get("jogos", idiomaUi)}",
                    color = CINZA_TEXTO,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 14.dp, start = 10.dp)
                )
            }

            if (resumos.isEmpty()) {
                Text(
                    if (soHoje) Textos.get("sem_jogos_hoje", idiomaUi)
                    else Textos.get("sem_dados", idiomaUi),
                    color = CINZA_TEXTO,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(resumos, key = { it.id }) { resumo ->
                    val jogador = porId[resumo.id] ?: return@items
                    val posicao = resumos.indexOf(resumo) + 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$posicao",
                                color = if (posicao <= 3) VERDE else CINZA_TEXTO,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(38.dp)
                            )
                            Avatar(jogador, 44.dp, versaoFotos)
                            Box(modifier = Modifier.width(14.dp))
                            Column {
                                Text(jogador.nome, color = Color.White, fontSize = 17.sp)
                                val parceiro = resumo.parceiroMaisFrequente?.let { porId[it]?.curto }
                                if (parceiro != null) {
                                    Text(
                                        Textos.get("parceiro_frequente", idiomaUi) +
                                            ": $parceiro (${resumo.vezesComParceiro})",
                                        color = CINZA_TEXTO,
                                        fontSize = 12.sp
                                    )
                                }
                                val melhor = resumo.melhorParceiro?.let { porId[it]?.curto }
                                if (melhor != null && melhor != parceiro) {
                                    Text(
                                        Textos.get("melhor_parceiro", idiomaUi) +
                                            ": $melhor (${resumo.vitoriasComMelhorParceiro}/${resumo.jogosComMelhorParceiro})",
                                        color = CINZA_TEXTO,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${resumo.vitorias} ${Textos.get("vitorias", idiomaUi)}  .  " +
                                        "${resumo.derrotas} ${Textos.get("derrotas", idiomaUi)}",
                                    color = CINZA_TEXTO,
                                    fontSize = 14.sp
                                )
                                Text(
                                    Textos.get("saldo", idiomaUi) + "  " +
                                        (if (resumo.saldo >= 0) "+" else "") + resumo.saldo,
                                    color = if (resumo.saldo >= 0) VERDE else AMBAR,
                                    fontSize = 12.sp
                                )
                            }
                            Box(modifier = Modifier.width(18.dp))
                            Text(
                                "${resumo.aproveitamento}%",
                                color = VERDE,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.height(8.dp))
            Row {
                Botao(Textos.get("email_sessao", idiomaUi), VERDE) { onEmailSessao(doDia) }
                Botao(Textos.get("exportar", idiomaUi), Color.White) { onExportar() }
            }
        }
    }
}
