package com.kriptobr.placar.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kriptobr.placar.core.Analise
import com.kriptobr.placar.core.Lado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TelaResultado(
    partida: Partida,
    jogadores: List<Jogador>,
    idiomaUi: String,
    aviso: String,
    mostrarNovoGame: Boolean,
    onEmail: () -> Unit,
    onNovoGame: () -> Unit,
    onFechar: () -> Unit
) {
    val stats = Analise.calcular(partida.eventos, partida.primeiroSaque)
    val esq = ResumoEmail.nomeLado(Lado.ESQUERDA, partida, jogadores, idiomaUi)
    val dir = ResumoEmail.nomeLado(Lado.DIREITA, partida, jogadores, idiomaUi)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFA000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(FUNDO_MENU, RoundedCornerShape(16.dp))
                .border(2.dp, CINZA_BORDA, RoundedCornerShape(16.dp))
                .padding(30.dp)
                .width(640.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (partida.completa) {
                    Textos.get("fim_game", idiomaUi)
                } else {
                    Textos.get("resultado", idiomaUi)
                },
                color = VERDE,
                fontSize = 17.sp,
                letterSpacing = 3.sp
            )

            Box(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LadoResultado(esq, stats.pontosEsquerda, stats.pontosEsquerda > stats.pontosDireita)
                LadoResultado(dir, stats.pontosDireita, stats.pontosDireita > stats.pontosEsquerda)
            }

            Box(modifier = Modifier.height(18.dp))

            partida.fim?.let { fim ->
                val minutos = ((fim - partida.inicio) / 60000L).coerceAtLeast(0)
                Linha(
                    Textos.get("duracao", idiomaUi),
                    "$minutos ${Textos.get("minutos", idiomaUi)}"
                )
            }
            Linha(Textos.get("rallies", idiomaUi), stats.totalRallies.toString())
            Linha(
                Textos.get("rallies_ganhos", idiomaUi),
                "${stats.ralliesEsquerda}  x  ${stats.ralliesDireita}"
            )
            Linha(
                Textos.get("rallies_recebendo", idiomaUi),
                "${stats.ralliesRecebendoEsquerda}  x  ${stats.ralliesRecebendoDireita}"
            )
            Linha(
                Textos.get("turnos_secos", idiomaUi),
                "${stats.turnosSecosEsquerda}  x  ${stats.turnosSecosDireita}"
            )
            Linha(
                Textos.get("maior_sequencia", idiomaUi),
                "${stats.maiorSequenciaEsquerda}  x  ${stats.maiorSequenciaDireita}"
            )
            Linha(Textos.get("viradas", idiomaUi), stats.viradas.toString())

            if (aviso.isNotBlank()) {
                Box(modifier = Modifier.height(12.dp))
                Text(aviso, color = AMBAR, fontSize = 14.sp)
            }

            Box(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = Textos.get("fechar", idiomaUi),
                    color = CINZA_TEXTO,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onFechar() }.padding(12.dp)
                )
                Text(
                    text = Textos.get("enviar_email", idiomaUi),
                    color = VERDE,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onEmail() }.padding(12.dp)
                )
                if (mostrarNovoGame) {
                    Text(
                        text = Textos.get("novo_game", idiomaUi),
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.clickable { onNovoGame() }.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LadoResultado(nome: String, pontos: Int, venceu: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = nome,
            color = if (venceu) VERDE else CINZA_TEXTO,
            fontSize = 17.sp
        )
        Text(
            text = pontos.toString(),
            color = if (venceu) Color.White else Color(0xFFB4B2A9),
            fontSize = 72.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun Linha(rotulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(rotulo, color = CINZA_TEXTO, fontSize = 15.sp)
        Text(valor, color = Color.White, fontSize = 15.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun TelaHistorico(
    partidas: List<Partida>,
    jogadores: List<Jogador>,
    idiomaUi: String,
    onAbrir: (Partida) -> Unit,
    onFechar: () -> Unit
) {
    val formato = if (idiomaUi == Textos.PT) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    } else {
        SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF7000000))
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Cabecalho(Textos.get("historico", idiomaUi), idiomaUi, onFechar)

            if (partidas.isEmpty()) {
                Text(
                    text = Textos.get("sem_partidas", idiomaUi),
                    color = CINZA_TEXTO,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }

            LazyColumn {
                items(partidas, key = { it.id }) { partida ->
                    val stats = Analise.calcular(partida.eventos, partida.primeiroSaque)
                    val esq = ResumoEmail.nomeLado(Lado.ESQUERDA, partida, jogadores, idiomaUi)
                    val dir = ResumoEmail.nomeLado(Lado.DIREITA, partida, jogadores, idiomaUi)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAbrir(partida) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$esq  x  $dir", color = Color.White, fontSize = 17.sp)
                            Text(
                                text = formato.format(Date(partida.inicio)) +
                                    if (!partida.completa) {
                                        "   .   " + Textos.get("incompleta", idiomaUi)
                                    } else "",
                                color = CINZA_TEXTO,
                                fontSize = 13.sp
                            )
                        }
                        Box(modifier = Modifier.width(20.dp))
                        Text(
                            text = "${stats.pontosEsquerda} x ${stats.pontosDireita}",
                            color = VERDE,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
