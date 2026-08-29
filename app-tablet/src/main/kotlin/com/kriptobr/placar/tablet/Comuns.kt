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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Botao(texto: String, cor: Color = Color.White, onClique: () -> Unit) {
    Text(
        text = texto,
        color = cor,
        fontSize = 18.sp,
        modifier = Modifier
            .clickable { onClique() }
            .padding(horizontal = 18.dp, vertical = 12.dp)
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
