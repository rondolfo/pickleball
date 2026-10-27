package com.kriptobr.placar.tablet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kriptobr.placar.core.EstadoJogo
import com.kriptobr.placar.core.Lado
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

val VERDE = Color(0xFF97C459)
val CINZA_BORDA = Color(0xFF444441)
val CINZA_TEXTO = Color(0xFF888880)
val AMBAR = Color(0xFFEF9F27)
val FUNDO_MENU = Color(0xFF1C1C1A)
private val NUMERO_APAGADO = Color(0xFFB4B2A9)
private val BOLA = Color(0xFFE4EF4C)
private val FURO = Color(0xFFB9C234)

/**
 * Roboto Condensed, que vem em todo Android. Digito condensado e pesado e
 * mais alto no mesmo espaco, e altura e o que conta para ler de longe.
 */
private val CONDENSADA_NEGRITO = FontFamily(
    androidx.compose.ui.text.font.Typeface(
        android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
    )
)
private val CONDENSADA = FontFamily(
    androidx.compose.ui.text.font.Typeface(
        android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
    )
)

/*
 * Toda a geometria fica numa grade de 1280 por 800, a mesma do prototipo
 * Placar na Quadra, e e escalada para o tamanho real da tela. Assim o que
 * foi testado no navegador e o que aparece aqui.
 */
private const val GRADE_L = 1280f
private const val GRADE_A = 800f
private const val AREA_DE_JOGO = 750f
private const val PAINEL_Y = 8f
private const val PAINEL_L = 624f
private const val PAINEL_A = 738f
private const val LINHA_CIMA = 205f
private const val LINHA_BAIXO = 535f
private const val NUMERO_L = 330f
private const val NUMERO_MAX = 372f
private const val TAM_SACADOR = 220f
private const val TAM_RECEBEDOR = 168f
private const val TAM_OUTRO = 124f
private const val TAM_BOLA = 64f

private fun painelX(lado: Lado) = if (lado == Lado.ESQUERDA) 8f else 648f
private fun colunaX(lado: Lado) = if (lado == Lado.ESQUERDA) 146f else 1134f
private fun numeroX(lado: Lado) = if (lado == Lado.ESQUERDA) 452f else 828f

/**
 * Uma das quatro quadras de saque.
 * indice e o jogador da dupla: 0 e a vaga que comecou na direita, 1 a outra.
 */
private data class Vaga(val lado: Lado, val emCima: Boolean, val indice: Int) {
    val x: Float get() = colunaX(lado)
    val y: Float get() = if (emCima) LINHA_CIMA else LINHA_BAIXO
}

private data class Quadra(val vagas: List<Vaga>, val sacador: Vaga?, val recebedor: Vaga?)

/**
 * Cada dupla encara a rede em sentido contrario, entao a quadra da direita
 * de cada uma fica em pontas opostas: embaixo para a dupla da esquerda, em
 * cima para a da direita. E isso que poe sacador e recebedor na diagonal.
 */
private fun montarQuadra(e: EstadoJogo): Quadra {
    fun vagasDe(lado: Lado): List<Vaga> {
        val naDireita = e.naDireitaDe(lado)
        return if (lado == Lado.ESQUERDA) {
            listOf(Vaga(lado, true, 1 - naDireita), Vaga(lado, false, naDireita))
        } else {
            listOf(Vaga(lado, true, naDireita), Vaga(lado, false, 1 - naDireita))
        }
    }
    val todas = vagasDe(Lado.ESQUERDA) + vagasDe(Lado.DIREITA)
    if (e.encerrado) return Quadra(todas, null, null)

    val sacador = todas.first { it.lado == e.sacando && it.indice == e.indiceSacador }
    val sacaDaDireita = e.indiceSacador == e.naDireitaDe(e.sacando)
    val outro = e.sacando.oposto()
    val indiceRecebedor = if (sacaDaDireita) e.naDireitaDe(outro) else 1 - e.naDireitaDe(outro)
    val recebedor = todas.first { it.lado == outro && it.indice == indiceRecebedor }
    return Quadra(todas, sacador, recebedor)
}

/** Onde a bola descansa: no alto do circulo do sacador, do lado da rede. */
private fun pontoDaBola(sacador: Vaga): Offset {
    val r = TAM_SACADOR / 2f
    val x = if (sacador.lado == Lado.ESQUERDA) sacador.x + r * 0.72f else sacador.x - r * 0.72f
    return Offset(x, sacador.y - r * 0.78f)
}

/** Curva do saque: sobe e desce, como a bola de verdade. */
private fun naCurva(a: Offset, b: Offset, t: Float): Offset {
    val c = Offset((a.x + b.x) / 2f, min(a.y, b.y) - 150f)
    val u = 1f - t
    return Offset(
        u * u * a.x + 2f * u * t * c.x + t * t * b.x,
        u * u * a.y + 2f * u * t * c.y + t * t * b.y
    )
}

// Fases da animacao, em milissegundos: a bola aparece com o sacador,
// voa ate quem recebe e volta a ficar com o sacador.
private const val MS_APARECE = 420f
private const val MS_VOO = 1000f
private const val MS_VOLTA = 360f
private const val MS_TOTAL = MS_APARECE + MS_VOO + MS_VOLTA

@Composable
fun TelaPlacar(
    estado: EstadoJogo,
    duplaEsquerda: Dupla,
    duplaDireita: Dupla,
    jogadores: List<Jogador>,
    versaoFotos: Int,
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
    onRepetir: () -> Unit,
    onTrocarParceiros: (Lado) -> Unit
) {
    val porId = jogadores.associateBy { it.id }
    fun jogadorDa(v: Vaga): Jogador? {
        val dupla = if (v.lado == Lado.ESQUERDA) duplaEsquerda else duplaDireita
        return porId[if (v.indice == 0) dupla.a else dupla.b]
    }

    val quadra = montarQuadra(estado)
    val acento = if (estado.pontoDeJogo) AMBAR else VERDE

    fun diametro(v: Vaga): Float = when {
        v == quadra.sacador -> TAM_SACADOR
        v == quadra.recebedor -> TAM_RECEBEDOR
        estado.encerrado && estado.vencedor == v.lado -> TAM_RECEBEDOR
        else -> TAM_OUTRO
    }

    // Os gestos leem sempre o valor mais novo, sem reiniciar a cada ponto.
    val travadoAgora by rememberUpdatedState(travado)
    val quadraAgora by rememberUpdatedState(quadra)
    val aoPonto by rememberUpdatedState(onPonto)
    val aoDestravar by rememberUpdatedState(onDestravar)
    val aoTrocar by rememberUpdatedState(onTrocarParceiros)
    val diametroAgora by rememberUpdatedState(::diametro)

    // A bola recomeca a animacao sempre que o saque muda de situacao.
    val progresso = remember { Animatable(MS_TOTAL) }
    LaunchedEffect(estado, duplaEsquerda, duplaDireita) {
        if (estado.encerrado) return@LaunchedEffect
        progresso.snapTo(0f)
        progresso.animateTo(MS_TOTAL, tween(durationMillis = MS_TOTAL.toInt(), easing = LinearEasing))
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val u = min(maxWidth.value / GRADE_L, maxHeight.value / GRADE_A)
        val densidade = LocalDensity.current
        fun d(v: Float): Dp = (v * u).dp
        // Tamanho travado na grade: o ajuste de fonte do sistema nao desmonta o placar.
        fun t(v: Float): TextUnit = with(densidade) { (v * u).dp.toSp() }

        val medidor = rememberTextMeasurer()
        fun tamanhoDoNumero(texto: String): Float {
            val limite = with(densidade) { d(NUMERO_L - 10f).toPx() }
            var tamanho = NUMERO_MAX
            while (tamanho > 200f) {
                val largura = medidor.measure(
                    texto,
                    TextStyle(fontFamily = CONDENSADA_NEGRITO, fontSize = t(tamanho), letterSpacing = t(-6f))
                ).size.width
                if (largura <= limite) break
                tamanho -= 8f
            }
            return tamanho
        }

        Box(
            modifier = Modifier
                .size(d(GRADE_L), d(GRADE_A))
                .pointerInput(Unit) {
                    fun naGrade(p: Offset) = Offset(p.x / size.width * GRADE_L, p.y / size.height * GRADE_A)
                    detectTapGestures(
                        onDoubleTap = { if (travadoAgora) aoDestravar() },
                        onTap = { p ->
                            val g = naGrade(p)
                            if (!travadoAgora && g.y < AREA_DE_JOGO) {
                                aoPonto(if (g.x < GRADE_L / 2f) Lado.ESQUERDA else Lado.DIREITA)
                            }
                        },
                        // Toque longo na foto troca os parceiros daquela dupla:
                        // e o conserto para quando alguem diz "nao sou eu que saco".
                        onLongPress = { p ->
                            val g = naGrade(p)
                            quadraAgora.vagas.firstOrNull { v ->
                                hypot(g.x - v.x, g.y - v.y) <= diametroAgora(v) / 2f + 24f
                            }?.let { aoTrocar(it.lado) }
                        }
                    )
                }
        ) {
            // ---------- paineis das duplas ----------
            for (lado in listOf(Lado.ESQUERDA, Lado.DIREITA)) {
                val sacando = estado.sacando == lado && !estado.encerrado
                val venceu = estado.vencedor == lado
                val destacado = sacando || venceu
                val cor = if (sacando) acento else VERDE
                Box(
                    modifier = Modifier
                        .offset(d(painelX(lado)), d(PAINEL_Y))
                        .size(d(PAINEL_L), d(PAINEL_A))
                        .background(
                            if (destacado) cor.copy(alpha = 0.11f) else Color.White.copy(alpha = 0.045f),
                            RoundedCornerShape(d(16f))
                        )
                        .border(
                            if (destacado) d(6f) else d(2f),
                            if (destacado) cor else CINZA_BORDA,
                            RoundedCornerShape(d(16f))
                        )
                )
            }

            // ---------- linha do saque, atras de numeros e fotos ----------
            val sacador = quadra.sacador
            val recebedor = quadra.recebedor
            if (sacador != null && recebedor != null) {
                val inicio = pontoDaBola(sacador)
                val fim = Offset(recebedor.x, recebedor.y)
                Canvas(modifier = Modifier.size(d(GRADE_L), d(GRADE_A))) {
                    val k = size.width / GRADE_L
                    // pontos espacados pelo comprimento, como uma linha pontilhada
                    val amostras = (0..200).map { naCurva(inicio, fim, it / 200f) }
                    var acumulado = 0f
                    var proximo = 0f
                    for (i in 1 until amostras.size) {
                        val a = amostras[i - 1]
                        val b = amostras[i]
                        acumulado += hypot(b.x - a.x, b.y - a.y)
                        if (acumulado >= proximo) {
                            drawCircle(acento.copy(alpha = 0.38f), 6f * k, Offset(b.x * k, b.y * k))
                            proximo += 32f
                        }
                    }
                }
            }

            // ---------- numeros ----------
            for (lado in listOf(Lado.ESQUERDA, Lado.DIREITA)) {
                val destacado = (estado.sacando == lado && !estado.encerrado) || estado.vencedor == lado
                val texto = estado.pontosDe(lado).toString()
                val tamanho = remember(texto, u) { tamanhoDoNumero(texto) }
                Box(
                    modifier = Modifier
                        .offset(d(numeroX(lado) - NUMERO_L / 2f), d(150f))
                        .size(d(NUMERO_L), d(440f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = texto,
                        color = if (destacado) Color.White else NUMERO_APAGADO,
                        fontSize = t(tamanho),
                        fontFamily = CONDENSADA_NEGRITO,
                        letterSpacing = t(-6f),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.wrapContentSize(unbounded = true)
                    )
                }

                val rotulo = when {
                    estado.vencedor == lado -> "GAME"
                    estado.sacando == lado && estado.pontoDeJogo -> "GAME POINT"
                    else -> null
                }
                if (rotulo != null) {
                    Text(
                        text = rotulo,
                        color = if (estado.vencedor == lado) VERDE else AMBAR,
                        fontSize = t(50f),
                        fontFamily = CONDENSADA_NEGRITO,
                        letterSpacing = t(5f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .offset(d(painelX(lado) + 20f), d(30f))
                            .width(d(584f))
                    )
                }
            }

            // ---------- jogadores nas quadras ----------
            for (vaga in quadra.vagas) {
                val jogador = jogadorDa(vaga)
                val ehSacador = vaga == quadra.sacador
                val ehRecebedor = vaga == quadra.recebedor
                val venceu = estado.encerrado && estado.vencedor == vaga.lado
                val dm = diametro(vaga)
                // anel por fora e um respiro preto de 4 por dentro, para o anel
                // aparecer sobre qualquer foto ou cor de circulo
                val anel = when {
                    ehSacador -> 12f
                    ehRecebedor || venceu -> 9f
                    else -> 0f
                }
                val corAnel = when {
                    ehSacador -> acento
                    ehRecebedor -> Color.White
                    else -> VERDE
                }
                val externo = dm + 2f * anel

                Box(
                    modifier = Modifier
                        .offset(d(vaga.x - externo / 2f), d(vaga.y - externo / 2f))
                        .size(d(externo))
                        .alpha(if (ehSacador || ehRecebedor || venceu) 1f else 0.42f)
                        .drawBehind {
                            if (ehSacador) {
                                drawCircle(
                                    Brush.radialGradient(
                                        listOf(acento.copy(alpha = 0.45f), Color.Transparent),
                                        center = center,
                                        radius = size.minDimension * 0.78f
                                    ),
                                    radius = size.minDimension * 0.78f
                                )
                            }
                            if (anel > 0f) {
                                drawCircle(corAnel)
                                drawCircle(Color.Black, radius = size.minDimension / 2f * (dm / 2f + 4f) / (externo / 2f))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (jogador != null) {
                        Avatar(jogador, d(dm), versaoFotos)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(d(dm))
                                .background(Color(0xFF2A2A27), CircleShape)
                        )
                    }
                }

                if (ehSacador) {
                    val bx = if (vaga.lado == Lado.ESQUERDA) vaga.x + dm / 2f - 54f else vaga.x - dm / 2f - 38f
                    Box(
                        modifier = Modifier
                            .offset(d(bx), d(vaga.y + dm / 2f - 82f))
                            .size(d(92f))
                            .background(Color.Black, CircleShape)
                            .border(d(6f), acento, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = estado.sacador.toString(),
                            color = acento,
                            fontSize = t(66f),
                            fontFamily = CONDENSADA_NEGRITO,
                            maxLines = 1,
                            modifier = Modifier.wrapContentSize(unbounded = true)
                        )
                    }
                }

                if (jogador != null) {
                    val tamanhoNome = when {
                        ehSacador -> 46f
                        ehRecebedor || venceu -> 32f
                        else -> 24f
                    }
                    Text(
                        text = jogador.curto.uppercase(),
                        color = when {
                            ehSacador -> acento
                            ehRecebedor || venceu -> Color.White
                            else -> CINZA_TEXTO
                        },
                        fontSize = t(tamanhoNome),
                        fontFamily = if (ehSacador || ehRecebedor || venceu) CONDENSADA_NEGRITO else CONDENSADA,
                        letterSpacing = t(2f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .offset(d(vaga.x - 150f), d(vaga.y + dm / 2f + if (ehSacador) 24f else 16f))
                            .width(d(300f))
                    )
                }
            }

            // ---------- bola ----------
            if (sacador != null && recebedor != null) {
                val repouso = pontoDaBola(sacador)
                val destino = Offset(recebedor.x, recebedor.y)
                Canvas(modifier = Modifier.size(d(GRADE_L), d(GRADE_A))) {
                    val k = size.width / GRADE_L
                    val ms = progresso.value
                    when {
                        ms < MS_APARECE -> {
                            val s = 0.2f + 0.8f * (ms / 260f).coerceAtMost(1f)
                            desenharBola(repouso, s, k)
                        }
                        ms < MS_APARECE + MS_VOO -> {
                            val f = (ms - MS_APARECE) / MS_VOO
                            val tt = if (f < 0.5f) 2f * f * f else 1f - (-2f * f + 2f).let { it * it } / 2f
                            // rastro: a bola deixa pontos que vao sumindo
                            for (passo in 1..6) {
                                val tr = tt - passo * 0.06f
                                if (tr <= 0f) break
                                val p = naCurva(repouso, destino, tr)
                                drawCircle(
                                    BOLA.copy(alpha = 0.5f * (1f - passo / 7f)),
                                    14f * k,
                                    Offset(p.x * k, p.y * k)
                                )
                            }
                            desenharBola(naCurva(repouso, destino, tt), 1f + 0.75f * sin(PI.toFloat() * tt), k)
                        }
                        ms < MS_TOTAL -> {
                            val f = (ms - MS_APARECE - MS_VOO) / MS_VOLTA
                            val s = if (f < 0.6f) f / 0.6f * 1.18f else 1.18f - (f - 0.6f) / 0.4f * 0.18f
                            desenharBola(repouso, s, k)
                        }
                        else -> desenharBola(repouso, 1f, k)
                    }
                }
            }

            // ---------- rodape, para quem esta na mesa ----------
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
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
    }
}

/** Bola de pickleball, com os furos, no ponto dado da grade. */
private fun DrawScope.desenharBola(centro: Offset, escala: Float, k: Float) {
    val r = TAM_BOLA / 2f * escala * k
    val c = Offset(centro.x * k, centro.y * k)
    drawCircle(Color.Black.copy(alpha = 0.35f), r, c + Offset(0f, 6f * k))
    drawCircle(BOLA, r, c)
    val f = r / 28f
    val furos = listOf(
        0f to -18f, 0f to 18f, -18f to 0f, 18f to 0f, 0f to 0f,
        -12f to -12f, 12f to -12f, -12f to 12f, 12f to 12f
    )
    for ((fx, fy) in furos) drawCircle(FURO, 3.4f * f, c + Offset(fx * f, fy * f))
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
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (conectado) VERDE else AMBAR, CircleShape)
            )
            Text(
                text = if (conectado) {
                    "  " + Textos.get("conectado", idiomaUi)
                } else {
                    "  " + Textos.get("aguardando", idiomaUi) + "  .  " + endereco
                },
                color = CINZA_TEXTO,
                fontSize = 13.sp
            )
            if (travado) {
                Text(
                    text = "   .   " + Textos.get("travado", idiomaUi),
                    color = Color(0xFF5F5E5A),
                    fontSize = 13.sp
                )
            }
        }

        Text(
            text = if (estado.pontoDeJogo) {
                estado.chamada.replace("-", " . ") + "   " +
                    Textos.get("ponto_de_jogo", idiomaUi)
            } else {
                estado.chamada.replace("-", " . ")
            },
            color = if (estado.pontoDeJogo) VERDE else CINZA_TEXTO,
            fontSize = 18.sp,
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
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onTrocarIdiomaVoz() }
                    .padding(8.dp)
            )
            Box(modifier = Modifier.width(10.dp))
            Text(
                text = Textos.get("desfazer", idiomaUi),
                color = CINZA_TEXTO,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable { onDesfazer() }
                    .padding(8.dp)
            )
            Box(modifier = Modifier.width(10.dp))
            Text(
                text = "\u2699",
                color = CINZA_TEXTO,
                fontSize = 19.sp,
                modifier = Modifier
                    .clickable { onAbrirMenu() }
                    .padding(8.dp)
            )
        }
    }
}
