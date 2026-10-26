package com.kriptobr.placar.tablet

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Manual dentro do aplicativo. Segue o idioma da interface, entao trocar
 * a tela para portugues troca o manual junto.
 */
@Composable
fun TelaManual(
    idiomaUi: String,
    onTrocarIdioma: () -> Unit,
    onFechar: () -> Unit
) {
    val secoes = Manual.secoes(idiomaUi)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF7000000))
    ) {
        Column(modifier = Modifier.padding(horizontal = 34.dp, vertical = 24.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(
                    Textos.get("manual", idiomaUi),
                    color = Color.White,
                    fontSize = 24.sp,
                    letterSpacing = 1.sp
                )
                Row {
                    Botao(idiomaUi.uppercase(), VERDE) { onTrocarIdioma() }
                    Botao(Textos.get("voltar", idiomaUi), CINZA_TEXTO) { onFechar() }
                }
            }

            Box(modifier = Modifier.height(10.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(secoes) { secao ->
                    Text(
                        text = secao.titulo,
                        color = VERDE,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
                    )
                    secao.itens.forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(".", color = VERDE, fontSize = 15.sp)
                            Box(modifier = Modifier.width(12.dp))
                            Text(
                                text = item,
                                color = Color(0xFFCFCEC6),
                                fontSize = 15.sp,
                                lineHeight = 21.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
