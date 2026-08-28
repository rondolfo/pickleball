package com.kriptobr.placar.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kriptobr.placar.core.EstadoJogo
import com.kriptobr.placar.core.Lado

val VERDE = Color(0xFF97C459)
val CINZA_BORDA = Color(0xFF444441)
val CINZA_TEXTO = Color(0xFF888880)
val AMBAR = Color(0xFFEF9F27)
val FUNDO_MENU = Color(0xFF1C1C1A)

@Composable
fun TelaPlacar(
    estado: EstadoJogo,
    nomeEsquerda: String,
    nomeDireita: String,
    idiomaUi: String,
    idiomaVoz: String,
    conectado: Boolean,
    endereco: String,
    travado: Boolean,
    onPonto: (Lado) -> Unit,
    onDesfazer: () -> Unit,
    onDestravar: () -> Unit,
    onAbrirMenu: () -> Unit,
    onTrocarIdiomaVoz: () -> Unit,
    onRepetir: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LadoDoPlacar(Lado.ESQUERDA, nomeEsquerda, estado, idiomaUi, travado,
                Modifier.weight(1f), onPonto = { onPonto(Lado.ESQUERDA) }, onDestravar = onDestravar)
            LadoDoPlacar(Lado.DIREITA, nomeDireita, estado, idiomaUi, travado,
                Modifier.weight(1f), onPonto = { onPonto(Lado.DIREITA) }, onDestravar = onDestravar)
        }

        Rodape(
            estado = estado,
            idiomaUi = idiomaUi,
            idiomaVoz = idiomaVoz,
            conectado = conectado,
            endereco = endereco,
            travado = travado,
            onDesfazer = onDesfazer,
            onAbrirMenu = onAbrirMenu,
            onTrocarIdiomaVoz = onTrocarIdiomaVoz,
            onRepetir = onRepetir
        )
    }
}

@Composable
private fun LadoDoPlacar(
    lado: Lado,
    rotulo: String,
    estado: EstadoJogo,
    idiomaUi: String,
    travado: Boolean,
    modifier: Modifier,
    onPonto: () -> Unit,
    onDestravar: () -> Unit
) {
    val sacando = estado.sacando == lado && !estado.encerrado
    val venceu = estado.vencedor == lado
    val corBorda = if (venceu || sacando) VERDE else CINZA_BORDA

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .padding(10.dp)
            .border(3.dp, corBorda, RoundedCornerShape(14.dp))
            .pointerInput(travado) {
                detectTapGestures(
                    onDoubleTap = { if (travado) onDestravar() },
                    onTap = { if (!travado) onPonto() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val tamanhoNumero = (maxHeight.value * 0.58f).sp
        val tamanhoRotulo = (maxHeight.value * 0.065f).coerceAtLeast(12f).sp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = rotulo,
                color = if (sacando) VERDE else CINZA_TEXTO,
                fontSize = tamanhoRotulo,
                letterSpacing = 2.sp
            )

            Text(
                text = estado.pontosDe(lado).toString(),
                color = if (sacando || venceu) Color.White else Color(0xFFB4B2A9),
                fontSize = tamanhoNumero,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Text(
                text = when {
                    venceu -> Textos.get("venceu", idiomaUi)
                    sacando -> rotuloSaque(estado, idiomaUi)
                    else -> ""
                },
                color = if (sacando || venceu) VERDE else Color.Transparent,
                fontSize = tamanhoRotulo,
                letterSpacing = 2.sp
            )
        }
    }
}

private fun rotuloSaque(estado: EstadoJogo, idiomaUi: String): String {
    val sacador = "${Textos.get("sacador", idiomaUi)} ${estado.sacador}"
    val chave = if (estado.ladoDoSaque == "direita") "saque_direita" else "saque_esquerda"
    return "$sacador  .  ${Textos.get(chave, idiomaUi)}"
}

@Composable
private fun Rodape(
    estado: EstadoJogo,
    idiomaUi: String,
    idiomaVoz: String,
    conectado: Boolean,
    endereco: String,
    travado: Boolean,
    onDesfazer: () -> Unit,
    onAbrirMenu: () -> Unit,
    onTrocarIdiomaVoz: () -> Unit,
    onRepetir: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .background(if (conectado) VERDE else AMBAR, CircleShape)
            )
            Text(
                text = if (conectado) {
                    "  " + Textos.get("conectado", idiomaUi)
                } else {
                    "  " + Textos.get("aguardando", idiomaUi) + "  .  " + endereco
                },
                color = CINZA_TEXTO,
                fontSize = 14.sp
            )
            if (travado) {
                Text(
                    text = "   .   " + Textos.get("travado", idiomaUi),
                    color = Color(0xFF5F5E5A),
                    fontSize = 14.sp
                )
            }
        }

        Text(
            text = when {
                estado.encerrado -> Textos.get("fim_game", idiomaUi)
                estado.pontoDeJogo -> estado.chamada.replace("-", " . ") +
                    "   " + Textos.get("ponto_de_jogo", idiomaUi)
                else -> estado.chamada.replace("-", " . ")
            },
            color = if (estado.pontoDeJogo || estado.encerrado) VERDE else CINZA_TEXTO,
            fontSize = 19.sp,
            letterSpacing = 3.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .clickable { onRepetir() }
                .padding(6.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = idiomaVoz.uppercase(),
                color = VERDE,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onTrocarIdiomaVoz() }
                    .padding(8.dp)
            )
            Box(modifier = Modifier.width(14.dp))
            Text(
                text = Textos.get("desfazer", idiomaUi),
                color = CINZA_TEXTO,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { onDesfazer() }
                    .padding(8.dp)
            )
            Box(modifier = Modifier.width(14.dp))
            Text(
                text = "\u2699",
                color = CINZA_TEXTO,
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable { onAbrirMenu() }
                    .padding(8.dp)
            )
        }
    }
}
