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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Botao de toda a interface.
 *
 * Era texto puro, sem moldura: nada indicava onde terminava a area de
 * toque, e num tablet em pe numa mesa a pessoa erra o alvo. A moldura usa
 * a propria cor do botao, entao verde, ambar e cinza seguem significando
 * confirmar, cuidado e sair, sem precisar de um segundo sistema de cores.
 */
@Composable
fun Botao(texto: String, cor: Color = Color.White, onClique: () -> Unit) {
    Text(
        text = texto,
        color = cor,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            // A margem de fora vem antes do fundo. Sem ela, dois botoes
            // vizinhos encostam um no outro e viram um bloco unico, o que
            // acontecia em quase toda tela depois que eles ganharam moldura.
            .padding(4.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClique() }
            .background(cor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .border(1.dp, cor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(horizontal = 22.dp, vertical = 13.dp)
    )
}

@Composable
fun Painel(conteudo: @Composable () -> Unit) {
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
                .padding(26.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            conteudo()
        }
    }
}

@Composable
fun LinhaInfo(rotulo: String, valor: String, corValor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(rotulo, color = CINZA_TEXTO, fontSize = 15.sp)
        Text(valor, color = corValor, fontSize = 15.sp, fontFamily = FontFamily.Monospace)
    }
}
