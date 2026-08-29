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
    onFechar: () -> Unit
) {
    val porId = jogadores.associateBy { it.id }
    val resumos = jogadores
        .map { EstatisticasJogador.calcular(partidas, it.id) }
        .filter { it.partidas > 0 }
        .sortedWith(compareByDescending<ResumoJogador> { it.aproveitamento }.thenByDescending { it.partidas })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF7000000))
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Cabecalho(Textos.get("estatisticas", idiomaUi), idiomaUi, onFechar)

            if (resumos.isEmpty()) {
                Text(
                    Textos.get("sem_dados", idiomaUi),
                    color = CINZA_TEXTO,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(resumos, key = { it.id }) { resumo ->
                    val jogador = porId[resumo.id] ?: return@items
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            Text(
                                "${resumo.vitorias} ${Textos.get("vitorias", idiomaUi)}  .  " +
                                    "${resumo.derrotas} ${Textos.get("derrotas", idiomaUi)}",
                                color = CINZA_TEXTO,
                                fontSize = 14.sp
                            )
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
            Botao(Textos.get("exportar", idiomaUi), VERDE) { onExportar() }
        }
    }
}
