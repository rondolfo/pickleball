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
import com.kriptobr.placar.core.EstadoJogo
import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.Regras

/**
 * Tela unica de inicio de jogo: quem joga de cada lado e quem saca primeiro.
 * Substitui o encadeamento de dialogos, que dava mais toques para o mesmo.
 */
@Composable
fun TelaNovoJogo(
    jogadores: List<Jogador>,
    duplaEsquerda: Dupla,
    duplaDireita: Dupla,
    idiomaUi: String,
    versaoFotos: Int,
    onMudar: (Lado, Dupla) -> Unit,
    onComecar: (Lado) -> Unit,
    onFechar: () -> Unit
) {
    var escolhendo by remember { mutableStateOf<Pair<Lado, Int>?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF7000000))
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Cabecalho(Textos.get("iniciar_jogo", idiomaUi), idiomaUi, onFechar)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ColunaLado(
                    Textos.get("esquerda", idiomaUi), duplaEsquerda, jogadores,
                    idiomaUi, versaoFotos,
                    onTrocar = { onMudar(Lado.ESQUERDA, duplaEsquerda.copy(a = duplaEsquerda.b, b = duplaEsquerda.a)) }
                ) { posicao -> escolhendo = Lado.ESQUERDA to posicao }
                ColunaLado(
                    Textos.get("direita", idiomaUi), duplaDireita, jogadores,
                    idiomaUi, versaoFotos,
                    onTrocar = { onMudar(Lado.DIREITA, duplaDireita.copy(a = duplaDireita.b, b = duplaDireita.a)) }
                ) { posicao -> escolhendo = Lado.DIREITA to posicao }
            }

            Box(modifier = Modifier.height(26.dp))

            Text(
                text = Textos.get("quem_saca", idiomaUi),
                color = Color.White,
                fontSize = 19.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = Textos.get("saca_quem_direita", idiomaUi),
                color = CINZA_TEXTO,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Botao(Textos.get("esquerda", idiomaUi), VERDE) { onComecar(Lado.ESQUERDA) }
                Box(modifier = Modifier.width(40.dp))
                Botao(Textos.get("direita", idiomaUi), VERDE) { onComecar(Lado.DIREITA) }
            }
        }
    }

    escolhendo?.let { (lado, posicao) ->
        // Quem ja esta escalado some da lista, senao a mesma pessoa acaba
        // nos dois lados da rede ou duas vezes na mesma dupla.
        val ocupados = (duplaEsquerda.ids + duplaDireita.ids).toSet()
        val noSlot = if (lado == Lado.ESQUERDA) {
            if (posicao == 0) duplaEsquerda.a else duplaEsquerda.b
        } else {
            if (posicao == 0) duplaDireita.a else duplaDireita.b
        }

        SeletorDeJogador(
            jogadores = jogadores.filter { it.id == noSlot || !ocupados.contains(it.id) },
            titulo = Textos.get("escolher_jogador", idiomaUi),
            idiomaUi = idiomaUi,
            versaoFotos = versaoFotos,
            permitirLimpar = true,
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

/**
 * A vaga de cima e quem comeca na quadra da direita. O aplicativo descobre
 * quem saca a partir disso, entao a tela diz com todas as letras, e o botao
 * de trocar conserta sem precisar escolher os dois de novo.
 */
@Composable
private fun ColunaLado(
    titulo: String,
    dupla: Dupla,
    jogadores: List<Jogador>,
    idiomaUi: String,
    versaoFotos: Int,
    onTrocar: () -> Unit,
    onSlot: (Int) -> Unit
) {
    val porId = jogadores.associateBy { it.id }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, color = VERDE, fontSize = 17.sp, letterSpacing = 2.sp)
        Box(modifier = Modifier.height(12.dp))
        Text(
            Textos.get("comeca_direita", idiomaUi),
            color = CINZA_TEXTO,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.width(320.dp).padding(start = 4.dp, bottom = 4.dp)
        )
        SlotDeJogador(porId[dupla.a], idiomaUi, versaoFotos) { onSlot(0) }
        Box(modifier = Modifier.height(10.dp))
        Text(
            Textos.get("comeca_esquerda", idiomaUi),
            color = CINZA_TEXTO,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.width(320.dp).padding(start = 4.dp, bottom = 4.dp)
        )
        SlotDeJogador(porId[dupla.b], idiomaUi, versaoFotos) { onSlot(1) }
        if (dupla.a != null && dupla.b != null) {
            Box(modifier = Modifier.height(6.dp))
            Botao(Textos.get("trocar_posicao", idiomaUi), CINZA_TEXTO) { onTrocar() }
        }
    }
}

@Composable
fun SlotDeJogador(
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
fun SeletorDeJogador(
    jogadores: List<Jogador>,
    titulo: String,
    idiomaUi: String,
    versaoFotos: Int,
    permitirLimpar: Boolean,
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
            Text(titulo, color = CINZA_TEXTO, fontSize = 15.sp, letterSpacing = 2.sp)
            Box(modifier = Modifier.height(10.dp))

            LazyColumn(modifier = Modifier.height(300.dp)) {
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
                if (permitirLimpar) {
                    Botao(Textos.get("limpar", idiomaUi), AMBAR) { onEscolher(null) }
                } else {
                    Box(modifier = Modifier.width(1.dp))
                }
                Botao(Textos.get("fechar", idiomaUi), CINZA_TEXTO) { onFechar() }
            }
        }
    }
}

/**
 * Substituicao no meio do jogo. O placar nao muda: quem entra assume o
 * lado, e a troca fica registrada com horario para as estatisticas.
 */
@Composable
fun TelaSubstituicao(
    jogadores: List<Jogador>,
    duplaEsquerda: Dupla,
    duplaDireita: Dupla,
    idiomaUi: String,
    versaoFotos: Int,
    onSubstituir: (Lado, Int, Jogador) -> Unit,
    onFechar: () -> Unit
) {
    var saindo by remember { mutableStateOf<Triple<Lado, Int, Jogador>?>(null) }
    val porId = jogadores.associateBy { it.id }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF7000000))
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Cabecalho(Textos.get("quem_sai", idiomaUi), idiomaUi, onFechar)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Textos.get("esquerda", idiomaUi), color = VERDE, fontSize = 16.sp)
                    Box(modifier = Modifier.height(12.dp))
                    listOf(0 to duplaEsquerda.a, 1 to duplaEsquerda.b).forEach { (pos, id) ->
                        val jogador = porId[id]
                        SlotDeJogador(jogador, idiomaUi, versaoFotos) {
                            if (jogador != null) saindo = Triple(Lado.ESQUERDA, pos, jogador)
                        }
                        Box(modifier = Modifier.height(10.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Textos.get("direita", idiomaUi), color = VERDE, fontSize = 16.sp)
                    Box(modifier = Modifier.height(12.dp))
                    listOf(0 to duplaDireita.a, 1 to duplaDireita.b).forEach { (pos, id) ->
                        val jogador = porId[id]
                        SlotDeJogador(jogador, idiomaUi, versaoFotos) {
                            if (jogador != null) saindo = Triple(Lado.DIREITA, pos, jogador)
                        }
                        Box(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    saindo?.let { (lado, posicao, quemSai) ->
        val emQuadra = duplaEsquerda.ids + duplaDireita.ids
        SeletorDeJogador(
            jogadores = jogadores.filter { !emQuadra.contains(it.id) },
            titulo = Textos.get("quem_entra", idiomaUi) + "   (" + quemSai.curto + ")",
            idiomaUi = idiomaUi,
            versaoFotos = versaoFotos,
            permitirLimpar = false,
            onEscolher = { entrando ->
                if (entrando != null) onSubstituir(lado, posicao, entrando)
                saindo = null
            },
            onFechar = { saindo = null }
        )
    }
}

/**
 * Correcao manual do placar, para quando alguem percebe o erro tarde.
 * O placar corrigido vira o novo ponto de partida do log.
 */
@Composable
fun TelaCorrecao(
    estadoAtual: EstadoJogo,
    duplaEsquerda: Dupla,
    duplaDireita: Dupla,
    jogadores: List<Jogador>,
    idiomaUi: String,
    onAplicar: (EstadoJogo) -> Unit,
    onFechar: () -> Unit
) {
    var esquerda by remember { mutableStateOf(estadoAtual.pontosEsquerda) }
    var direita by remember { mutableStateOf(estadoAtual.pontosDireita) }
    var sacando by remember { mutableStateOf(estadoAtual.sacando) }
    var sacador by remember { mutableStateOf(estadoAtual.sacador) }
    // Quem esta com a bola e a unica coisa que o placar nao diz sozinho.
    // Ao trocar a dupla que saca, o palpite inicial e quem esta na direita.
    var indice by remember { mutableStateOf(estadoAtual.indiceSacador) }
    val porId = jogadores.associateBy { it.id }
    val duplaQueSaca = if (sacando == Lado.ESQUERDA) duplaEsquerda else duplaDireita
    fun nomeDaVaga(i: Int): String {
        val id = if (i == 0) duplaQueSaca.a else duplaQueSaca.b
        return porId[id]?.curto ?: Textos.get(if (i == 0) "vaga_a" else "vaga_b", idiomaUi)
    }
    fun mudarDupla(nova: Lado) {
        if (nova == sacando) return
        sacando = nova
        indice = Regras.naDireitaPeloPlacar(if (nova == Lado.ESQUERDA) esquerda else direita)
    }

    Painel {
        Text(
            Textos.get("corrigir", idiomaUi),
            color = CINZA_TEXTO,
            fontSize = 16.sp,
            letterSpacing = 2.sp
        )
        Box(modifier = Modifier.height(16.dp))

        Contador(Textos.get("pontos_esq", idiomaUi), esquerda) { esquerda = it }
        Contador(Textos.get("pontos_dir", idiomaUi), direita) { direita = it }

        Box(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                Textos.get("sacando_agora", idiomaUi),
                color = CINZA_TEXTO,
                fontSize = 15.sp
            )
            Botao(
                Textos.get("esquerda", idiomaUi),
                if (sacando == Lado.ESQUERDA) VERDE else CINZA_TEXTO
            ) { mudarDupla(Lado.ESQUERDA) }
            Botao(
                Textos.get("direita", idiomaUi),
                if (sacando == Lado.DIREITA) VERDE else CINZA_TEXTO
            ) { mudarDupla(Lado.DIREITA) }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                Textos.get("com_a_bola", idiomaUi),
                color = CINZA_TEXTO,
                fontSize = 15.sp
            )
            Botao(nomeDaVaga(0), if (indice == 0) VERDE else CINZA_TEXTO) { indice = 0 }
            Botao(nomeDaVaga(1), if (indice == 1) VERDE else CINZA_TEXTO) { indice = 1 }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                Textos.get("numero_sacador", idiomaUi),
                color = CINZA_TEXTO,
                fontSize = 15.sp
            )
            Botao("1", if (sacador == 1) VERDE else CINZA_TEXTO) { sacador = 1 }
            Botao("2", if (sacador == 2) VERDE else CINZA_TEXTO) { sacador = 2 }
        }

        Box(modifier = Modifier.height(12.dp))
        Text(
            Textos.get("aviso_correcao", idiomaUi),
            color = AMBAR,
            fontSize = 13.sp,
            modifier = Modifier.width(430.dp)
        )

        Box(modifier = Modifier.height(14.dp))
        Row {
            Botao(Textos.get("cancelar", idiomaUi), CINZA_TEXTO) { onFechar() }
            Botao(Textos.get("aplicar", idiomaUi), VERDE) {
                onAplicar(Regras.estadoCorrigido(esquerda, direita, sacando, sacador, indice))
            }
        }
    }
}

@Composable
private fun Contador(rotulo: String, valor: Int, onMudar: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .width(430.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rotulo, color = CINZA_TEXTO, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Botao("-", AMBAR) { if (valor > 0) onMudar(valor - 1) }
            Text(
                valor.toString(),
                color = Color.White,
                fontSize = 26.sp,
                modifier = Modifier.width(50.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Botao("+", VERDE) { onMudar(valor + 1) }
        }
    }
}
