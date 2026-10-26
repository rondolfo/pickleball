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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kriptobr.placar.core.Lado

@Composable
fun MenuAjustes(
    idiomaUi: String,
    idiomaVoz: String,
    vozIndisponivel: Boolean,
    dizerSideOut: Boolean,
    onAlternarSideOut: () -> Unit,
    onNovoGame: () -> Unit,
    onInverter: () -> Unit,
    onDuplas: () -> Unit,
    onSubstituir: () -> Unit,
    onRodizio: () -> Unit,
    onJogadores: () -> Unit,
    onHistorico: () -> Unit,
    onEstatisticas: () -> Unit,
    onRelatorio: () -> Unit,
    onCorrigir: () -> Unit,
    onStatus: () -> Unit,
    onTeste: () -> Unit,
    onBackup: () -> Unit,
    onManual: () -> Unit,
    onTrocarIdiomaVoz: () -> Unit,
    onTrocarIdiomaTela: () -> Unit,
    onRepetir: () -> Unit,
    onFechar: () -> Unit
) {
    // Dezessete itens numa coluna unica viravam uma lista alta demais e
    // desperdicavam a largura do tablet. Agrupados em colunas, a pessoa
    // encontra pelo assunto em vez de varrer a lista inteira.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
            .clickable { onFechar() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(FUNDO_MENU, RoundedCornerShape(18.dp))
                .border(1.dp, CINZA_BORDA, RoundedCornerShape(18.dp))
                .padding(horizontal = 30.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Textos.get("menu", idiomaUi),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = Textos.get("fechar", idiomaUi),
                    color = CINZA_TEXTO,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onFechar() }.padding(8.dp)
                )
            }

            Box(modifier = Modifier.padding(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(34.dp)) {

                GrupoMenu(Textos.get("g_jogo", idiomaUi)) {
                    ItemMenu(Textos.get("novo_game", idiomaUi), onNovoGame, cor = VERDE)
                    ItemMenu(Textos.get("montar", idiomaUi), onDuplas)
                    ItemMenu(Textos.get("substituir", idiomaUi), onSubstituir)
                    ItemMenu(Textos.get("inverter", idiomaUi), onInverter)
                    ItemMenu(Textos.get("corrigir", idiomaUi), onCorrigir)
                    ItemMenu(Textos.get("repetir", idiomaUi), onRepetir)
                }

                GrupoMenu(Textos.get("g_pessoas", idiomaUi)) {
                    ItemMenu(Textos.get("jogadores", idiomaUi), onJogadores)
                    ItemMenu(Textos.get("rodizio", idiomaUi), onRodizio)
                }

                GrupoMenu(Textos.get("g_dados", idiomaUi)) {
                    ItemMenu(Textos.get("relatorio", idiomaUi), onRelatorio, cor = VERDE)
                    ItemMenu(Textos.get("estatisticas", idiomaUi), onEstatisticas)
                    ItemMenu(Textos.get("historico", idiomaUi), onHistorico)
                    ItemMenu(Textos.get("backup", idiomaUi), onBackup)
                }

                GrupoMenu(Textos.get("g_ajustes", idiomaUi)) {
                    ItemMenu(
                        Textos.get("idioma_voz", idiomaUi),
                        onTrocarIdiomaVoz,
                        valor = idiomaVoz.uppercase()
                    )
                    ItemMenu(
                        Textos.get("idioma_tela", idiomaUi),
                        onTrocarIdiomaTela,
                        valor = idiomaUi.uppercase()
                    )
                    ItemMenu(
                        Textos.get("dizer_side_out", idiomaUi),
                        onAlternarSideOut,
                        valor = if (dizerSideOut) "ON" else "OFF"
                    )
                    ItemMenu(Textos.get("status", idiomaUi), onStatus)
                    ItemMenu(Textos.get("teste", idiomaUi), onTeste)
                    ItemMenu(Textos.get("manual", idiomaUi), onManual)
                }
            }

            if (vozIndisponivel) {
                Box(modifier = Modifier.padding(8.dp))
                Text(
                    text = Textos.get("voz_faltando", idiomaUi),
                    color = AMBAR,
                    fontSize = 13.sp,
                    modifier = Modifier.width(520.dp)
                )
            }
        }
    }
}

/** Uma coluna do menu, com o titulo do assunto acima dos itens. */
@Composable
private fun GrupoMenu(titulo: String, conteudo: @Composable () -> Unit) {
    Column(modifier = Modifier.width(210.dp)) {
        Text(
            text = titulo.uppercase(),
            color = CINZA_TEXTO,
            fontSize = 11.sp,
            letterSpacing = 1.6.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
        )
        conteudo()
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
                Botao(textoNao, CINZA_TEXTO) { onNao() }
                Box(modifier = Modifier.width(20.dp))
                Botao(textoSim, VERDE) { onSim() }
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
                Botao(Textos.get("esquerda", idiomaUi), VERDE) { onEscolher(Lado.ESQUERDA) }
                Box(modifier = Modifier.width(20.dp))
                Botao(Textos.get("direita", idiomaUi), VERDE) { onEscolher(Lado.DIREITA) }
            }
        }
    }
}

@Composable
private fun ItemMenu(
    texto: String,
    onClique: () -> Unit,
    cor: Color = Color.White,
    valor: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClique() }
            .padding(vertical = 11.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = texto, color = cor, fontSize = 17.sp)
        if (valor != null) {
            Text(text = valor, color = VERDE, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
