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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kriptobr.placar.core.Lado

@Composable
fun TelaDuplas(
    jogadores: List<Jogador>,
    duplaEsquerda: Dupla,
    duplaDireita: Dupla,
    idiomaUi: String,
    versaoFotos: Int,
    onMudar: (Lado, Dupla) -> Unit,
    onFechar: () -> Unit
) {
    var escolhendo by remember { mutableStateOf<Pair<Lado, Int>?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF7000000))
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Cabecalho(Textos.get("montar", idiomaUi), idiomaUi, onFechar)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ColunaDupla(
                    titulo = Textos.get("esquerda", idiomaUi),
                    dupla = duplaEsquerda,
                    jogadores = jogadores,
                    idiomaUi = idiomaUi,
                    versaoFotos = versaoFotos,
                    onSlot = { posicao -> escolhendo = Lado.ESQUERDA to posicao }
                )
                ColunaDupla(
                    titulo = Textos.get("direita", idiomaUi),
                    dupla = duplaDireita,
                    jogadores = jogadores,
                    idiomaUi = idiomaUi,
                    versaoFotos = versaoFotos,
                    onSlot = { posicao -> escolhendo = Lado.DIREITA to posicao }
                )
            }
        }
    }

    escolhendo?.let { (lado, posicao) ->
        SeletorJogador(
            jogadores = jogadores,
            idiomaUi = idiomaUi,
            versaoFotos = versaoFotos,
            onEscolher = { jogador ->
                val atual = if (lado == Lado.ESQUERDA) duplaEsquerda else duplaDireita
                val nova = if (posicao == 0) atual.copy(a = jogador?.id) else atual.copy(b = jogador?.id)
                onMudar(lado, nova)
                escolhendo = null
            },
            onFechar = { escolhendo = null }
        )
    }
}

@Composable
private fun ColunaDupla(
    titulo: String,
    dupla: Dupla,
    jogadores: List<Jogador>,
    idiomaUi: String,
    versaoFotos: Int,
    onSlot: (Int) -> Unit
) {
    val porId = jogadores.associateBy { it.id }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, color = VERDE, fontSize = 17.sp, letterSpacing = 2.sp)
        Box(modifier = Modifier.height(14.dp))
        SlotJogador(porId[dupla.a], idiomaUi, versaoFotos) { onSlot(0) }
        Box(modifier = Modifier.height(10.dp))
        SlotJogador(porId[dupla.b], idiomaUi, versaoFotos) { onSlot(1) }
    }
}

@Composable
private fun SlotJogador(
    jogador: Jogador?,
    idiomaUi: String,
    versaoFotos: Int,
    onClique: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(320.dp)
            .border(2.dp, CINZA_BORDA, RoundedCornerShape(12.dp))
            .clickable { onClique() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (jogador != null) {
            Avatar(jogador, 44.dp, versaoFotos)
            Box(modifier = Modifier.width(14.dp))
            Text(jogador.nome, color = Color.White, fontSize = 18.sp)
        } else {
            Text(
                text = "+  " + Textos.get("escolher_jogador", idiomaUi),
                color = CINZA_TEXTO,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
private fun SeletorJogador(
    jogadores: List<Jogador>,
    idiomaUi: String,
    versaoFotos: Int,
    onEscolher: (Jogador?) -> Unit,
    onFechar: () -> Unit
) {
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
                .padding(22.dp)
                .width(440.dp)
        ) {
            Text(
                text = Textos.get("escolher_jogador", idiomaUi),
                color = CINZA_TEXTO,
                fontSize = 15.sp,
                letterSpacing = 2.sp
            )
            Box(modifier = Modifier.height(10.dp))

            LazyColumn(modifier = Modifier.height(320.dp)) {
                items(jogadores, key = { it.id }) { jogador ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEscolher(jogador) }
                            .padding(vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(jogador, 40.dp, versaoFotos)
                        Box(modifier = Modifier.width(14.dp))
                        Text(jogador.nome, color = Color.White, fontSize = 17.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Textos.get("limpar", idiomaUi),
                    color = AMBAR,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onEscolher(null) }.padding(10.dp)
                )
                Text(
                    text = Textos.get("fechar", idiomaUi),
                    color = CINZA_TEXTO,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onFechar() }.padding(10.dp)
                )
            }
        }
    }
}
