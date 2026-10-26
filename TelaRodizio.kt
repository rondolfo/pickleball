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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kriptobr.placar.core.Formacao
import com.kriptobr.placar.core.MemoriaRodizio
import com.kriptobr.placar.core.Rodizio

/**
 * Rodizio de uma quadra so.
 *
 * A sugestao respeita quem esperou mais, evita repetir parceiro e evita
 * repetir os mesmos adversarios. Nada de nivel: a escolha foi ignorar,
 * entao o rodizio nunca julga a qualidade de ninguem.
 */
@Composable
fun TelaRodizio(
    jogadores: List<Jogador>,
    presentes: Set<String>,
    memoria: MemoriaRodizio,
    sugestao: Formacao?,
    idiomaUi: String,
    versaoFotos: Int,
    onAlternarPresenca: (String) -> Unit,
    onSortear: () -> Unit,
    onAceitar: (Formacao) -> Unit,
    onFechar: () -> Unit
) {
    val porId = jogadores.associateBy { it.id }
    val espera = Rodizio.esperaDe(presentes.toList(), memoria)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF7000000))
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Cabecalho(Textos.get("rodizio", idiomaUi), idiomaUi, onFechar)

            Row(modifier = Modifier.fillMaxWidth()) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        Textos.get("presentes", idiomaUi),
                        color = VERDE,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    )
                    Box(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.height(360.dp)) {
                        items(jogadores, key = { it.id }) { jogador ->
                            val presente = presentes.contains(jogador.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAlternarPresenca(jogador.id) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height(22.dp)
                                        .border(
                                            2.dp,
                                            if (presente) VERDE else CINZA_BORDA,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .background(
                                            if (presente) VERDE else Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                                Box(modifier = Modifier.width(12.dp))
                                Avatar(jogador, 38.dp, versaoFotos)
                                Box(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        jogador.nome,
                                        color = if (presente) Color.White else CINZA_TEXTO,
                                        fontSize = 16.sp
                                    )
                                    if (presente && (espera[jogador.id] ?: 0) > 0) {
                                        Text(
                                            "${espera[jogador.id]} ${Textos.get("rodadas", idiomaUi)} " +
                                                Textos.get("esperando", idiomaUi),
                                            color = AMBAR,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.width(30.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        Textos.get("sugestao", idiomaUi),
                        color = VERDE,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    )
                    Box(modifier = Modifier.height(14.dp))

                    if (sugestao == null) {
                        Text(
                            Textos.get("precisa_quatro", idiomaUi),
                            color = CINZA_TEXTO,
                            fontSize = 15.sp
                        )
                    } else {
                        LadoSugerido(
                            Textos.get("esquerda", idiomaUi),
                            sugestao.esquerda, porId, versaoFotos
                        )
                        Box(modifier = Modifier.height(12.dp))
                        Text(
                            "x",
                            color = CINZA_TEXTO,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Box(modifier = Modifier.height(12.dp))
                        LadoSugerido(
                            Textos.get("direita", idiomaUi),
                            sugestao.direita, porId, versaoFotos
                        )

                        Box(modifier = Modifier.height(22.dp))

                        Row(horizontalArrangement = Arrangement.Center) {
                            Botao(Textos.get("sortear", idiomaUi), CINZA_TEXTO) { onSortear() }
                            Botao(Textos.get("aceitar", idiomaUi), VERDE) { onAceitar(sugestao) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LadoSugerido(
    titulo: String,
    ids: List<String>,
    porId: Map<String, Jogador>,
    versaoFotos: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, CINZA_BORDA, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(titulo, color = CINZA_TEXTO, fontSize = 13.sp, letterSpacing = 2.sp)
        Box(modifier = Modifier.height(8.dp))
        ids.forEach { id ->
            val jogador = porId[id]
            if (jogador != null) {
                Row(
                    modifier = Modifier.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(jogador, 40.dp, versaoFotos)
                    Box(modifier = Modifier.width(12.dp))
                    Text(jogador.nome, color = Color.White, fontSize = 17.sp)
                }
            }
        }
    }
}
