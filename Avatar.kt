package com.kriptobr.placar.tablet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Foto quando existe, circulo com iniciais quando nao existe.
 * Ninguem precisa cadastrar foto para o aplicativo funcionar.
 */
@Composable
fun Avatar(jogador: Jogador, tamanho: Dp = 48.dp, versao: Int = 0) {
    val contexto = LocalContext.current
    val bitmap = remember(jogador.id, jogador.temFoto, versao) {
        if (jogador.temFoto) Foto.carregar(Repositorio(contexto).arquivoFoto(jogador.id)) else null
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = jogador.nome,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(tamanho)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(tamanho)
                .background(Color(Foto.corDe(jogador.id)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = jogador.iniciais,
                color = Color(0xFF14140F),
                fontSize = (tamanho.value * 0.36f).sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
