package com.kriptobr.placar.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kriptobr.placar.core.EstadoJogo
import com.kriptobr.placar.core.Lado

/**
 * Nomes nas quatro quadras de servico, ja resolvidos pela posicao atual.
 */
data class PosicoesQuadra(
    val esqNaDireita: String,
    val esqNaEsquerda: String,
    val dirNaDireita: String,
    val dirNaEsquerda: String
)

/**
 * Desenho da quadra vista de cima, com a rede no meio.
 *
 * Cada dupla encara a rede em sentido contrario, entao a quadra da direita
 * de cada uma fica em pontas opostas do desenho. E por isso que sacador e
 * recebedor aparecem na diagonal, exatamente como acontece em jogo.
 *
 * Verde cheio: quem saca. Contorno verde: quem recebe.
 */
@Composable
fun QuadraDoSaque(
    estado: EstadoJogo,
    posicoes: PosicoesQuadra,
    altura: Dp,
    modifier: Modifier = Modifier
) {
    // qual quadrante esta sacando
    val sacandoNaDireitaDaPropriaDupla =
        estado.indiceSacador == estado.naDireitaDe(estado.sacando)

    val quadranteSacador = when {
        estado.sacando == Lado.ESQUERDA && sacandoNaDireitaDaPropriaDupla -> "esqDir"
        estado.sacando == Lado.ESQUERDA -> "esqEsq"
        sacandoNaDireitaDaPropriaDupla -> "dirDir"
        else -> "dirEsq"
    }

    // o recebedor fica na diagonal do sacador
    val quadranteRecebedor = when (quadranteSacador) {
        "esqDir" -> "dirDir"
        "esqEsq" -> "dirEsq"
        "dirDir" -> "esqDir"
        else -> "esqEsq"
    }

    Row(
        modifier = modifier
            .fillMaxHeight()
            .border(2.dp, CINZA_BORDA, RoundedCornerShape(6.dp))
            .padding(3.dp)
    ) {
        // dupla da esquerda: encara a rede olhando para a direita do desenho,
        // entao a quadra direita dela fica embaixo
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Quadrante(
                posicoes.esqNaEsquerda, "esqEsq" == quadranteSacador,
                "esqEsq" == quadranteRecebedor, altura, Modifier.weight(1f)
            )
            Quadrante(
                posicoes.esqNaDireita, "esqDir" == quadranteSacador,
                "esqDir" == quadranteRecebedor, altura, Modifier.weight(1f)
            )
        }

        // a rede
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(CINZA_TEXTO)
        )

        // dupla da direita: encara a rede no sentido oposto,
        // entao a quadra direita dela fica em cima
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Quadrante(
                posicoes.dirNaDireita, "dirDir" == quadranteSacador,
                "dirDir" == quadranteRecebedor, altura, Modifier.weight(1f)
            )
            Quadrante(
                posicoes.dirNaEsquerda, "dirEsq" == quadranteSacador,
                "dirEsq" == quadranteRecebedor, altura, Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Quadrante(
    nome: String,
    saca: Boolean,
    recebe: Boolean,
    altura: Dp,
    modifier: Modifier
) {
    val fundo = if (saca) VERDE else Color.Transparent
    val contorno = when {
        saca -> VERDE
        recebe -> VERDE.copy(alpha = 0.55f)
        else -> CINZA_BORDA
    }
    val corTexto = when {
        saca -> Color(0xFF14140F)
        recebe -> Color.White
        else -> CINZA_TEXTO
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp)
            .background(fundo, RoundedCornerShape(4.dp))
            .border(if (saca || recebe) 2.dp else 1.dp, contorno, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nome,
            color = corTexto,
            fontSize = (altura.value * 0.16f).coerceIn(11f, 26f).sp,
            fontWeight = if (saca) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxSize().padding(top = (altura.value * 0.05f).dp)
        )
    }
}
