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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kriptobr.placar.core.Lado

@Composable
fun MenuAjustes(
    idiomaUi: String,
    idiomaVoz: String,
    vozIndisponivel: Boolean,
    onNovoGame: () -> Unit,
    onInverter: () -> Unit,
    onTrocarIdiomaVoz: () -> Unit,
    onTrocarIdiomaTela: () -> Unit,
    onRepetir: () -> Unit,
    onFechar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
            .clickable { onFechar() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(FUNDO_MENU, RoundedCornerShape(16.dp))
                .border(2.dp, CINZA_BORDA, RoundedCornerShape(16.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Textos.get("menu", idiomaUi),
                color = CINZA_TEXTO,
                fontSize = 15.sp,
                letterSpacing = 2.sp
            )

            Box(modifier = Modifier.padding(6.dp))

            ItemMenu(Textos.get("novo_game", idiomaUi), onNovoGame)
            ItemMenu(Textos.get("inverter", idiomaUi), onInverter)
            ItemMenu(
                Textos.get("idioma_voz", idiomaUi) + "   " + idiomaVoz.uppercase(),
                onTrocarIdiomaVoz
            )
            ItemMenu(
                Textos.get("idioma_tela", idiomaUi) + "   " + idiomaUi.uppercase(),
                onTrocarIdiomaTela
            )
            ItemMenu(Textos.get("repetir", idiomaUi), onRepetir)

            if (vozIndisponivel) {
                Box(modifier = Modifier.padding(6.dp))
                Text(
                    text = Textos.get("voz_faltando", idiomaUi),
                    color = AMBAR,
                    fontSize = 13.sp,
                    modifier = Modifier.width(360.dp)
                )
            }

            Box(modifier = Modifier.padding(6.dp))
            ItemMenu(Textos.get("fechar", idiomaUi), onFechar, cor = CINZA_TEXTO)
        }
    }
}

@Composable
fun DialogoConfirmar(
    mensagem: String,
    textoSim: String,
    textoNao: String,
    onSim: () -> Unit,
    onNao: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(FUNDO_MENU, RoundedCornerShape(16.dp))
                .border(2.dp, CINZA_BORDA, RoundedCornerShape(16.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = mensagem, color = Color.White, fontSize = 19.sp)
            Box(modifier = Modifier.padding(10.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                ItemMenu(textoNao, onNao, cor = CINZA_TEXTO)
                Box(modifier = Modifier.width(28.dp))
                ItemMenu(textoSim, onSim, cor = VERDE)
            }
        }
    }
}

@Composable
fun DialogoPrimeiroSaque(
    idiomaUi: String,
    onEscolher: (Lado) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(FUNDO_MENU, RoundedCornerShape(16.dp))
                .border(2.dp, CINZA_BORDA, RoundedCornerShape(16.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Textos.get("quem_saca", idiomaUi),
                color = Color.White,
                fontSize = 20.sp
            )
            Box(modifier = Modifier.padding(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ItemMenu(Textos.get("esquerda", idiomaUi), { onEscolher(Lado.ESQUERDA) }, cor = VERDE)
                Box(modifier = Modifier.width(28.dp))
                ItemMenu(Textos.get("direita", idiomaUi), { onEscolher(Lado.DIREITA) }, cor = VERDE)
            }
        }
    }
}

@Composable
private fun ItemMenu(texto: String, onClique: () -> Unit, cor: Color = Color.White) {
    Text(
        text = texto,
        color = cor,
        fontSize = 20.sp,
        modifier = Modifier
            .clickable { onClique() }
            .padding(horizontal = 22.dp, vertical = 13.dp)
    )
}
