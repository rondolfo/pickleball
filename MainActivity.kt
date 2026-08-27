package com.kriptobr.placar.tablet

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kriptobr.placar.core.Evento
import com.kriptobr.placar.core.EstadoJogo
import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.Origem
import com.kriptobr.placar.core.Regras
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.UUID

private val VERDE = Color(0xFF97C459)
private val CINZA_BORDA = Color(0xFF444441)
private val CINZA_TEXTO = Color(0xFF888880)
private val AMBAR = Color(0xFFEF9F27)

class MainActivity : ComponentActivity() {

    private val eventos: SnapshotStateList<Evento> = mutableStateListOf()
    private val idsVistos = mutableSetOf<String>()

    private var clientes by mutableStateOf(0)
    private var enderecoLocal by mutableStateOf("")

    private var servidor: ServidorPlacar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        enderecoLocal = ipLocal()

        servidor = ServidorPlacar(
            contexto = this,
            aoReceberRally = { id, vencedor -> registrarRally(vencedor, Origem.RELOGIO, id) },
            aoReceberDesfazer = { runOnUiThread { desfazer() } },
            estadoJson = { estadoJson() },
            aoMudarClientes = { total -> runOnUiThread { clientes = total } }
        ).also { it.iniciar() }

        setContent {
            val estado = Regras.derivar(eventos)
            TelaPlacar(
                estado = estado,
                clientes = clientes,
                endereco = enderecoLocal,
                onPonto = { lado -> registrarRally(lado, Origem.TOQUE) },
                onDesfazer = { desfazer() }
            )
        }
    }

    override fun onDestroy() {
        servidor?.parar()
        servidor = null
        super.onDestroy()
    }

    private fun registrarRally(
        vencedor: Lado,
        origem: Origem,
        id: String = UUID.randomUUID().toString()
    ) {
        runOnUiThread {
            if (!idsVistos.add(id)) return@runOnUiThread

            val agora = System.currentTimeMillis()
            val ultimo = eventos.lastOrNull()
            if (ultimo != null && agora - ultimo.ts < 1000L) return@runOnUiThread

            eventos.add(Evento(id = id, vencedor = vencedor, origem = origem, ts = agora))
            servidor?.transmitir(estadoJson())
        }
    }

    private fun desfazer() {
        if (eventos.isEmpty()) return
        val removido = eventos.removeAt(eventos.lastIndex)
        idsVistos.remove(removido.id)
        servidor?.transmitir(estadoJson())
    }

    private fun estadoJson(): String {
        val estado = Regras.derivar(eventos)
        return JSONObject().apply {
            put("tipo", "ESTADO")
            put("esq", estado.pontosEsquerda)
            put("dir", estado.pontosDireita)
            put("sacando", estado.sacando.name)
            put("sacador", estado.sacador)
            put("chamada", estado.chamada)
            put("encerrado", estado.encerrado)
            put("pontoDeJogo", estado.pontoDeJogo)
        }.toString()
    }

    private fun ipLocal(): String = runCatching {
        NetworkInterface.getNetworkInterfaces()
            .toList()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
            ?.hostAddress
            ?: "sem rede"
    }.getOrDefault("sem rede")
}

@Composable
private fun TelaPlacar(
    estado: EstadoJogo,
    clientes: Int,
    endereco: String,
    onPonto: (Lado) -> Unit,
    onDesfazer: () -> Unit
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
            LadoDoPlacar(Lado.ESQUERDA, estado, Modifier.weight(1f)) { onPonto(Lado.ESQUERDA) }
            LadoDoPlacar(Lado.DIREITA, estado, Modifier.weight(1f)) { onPonto(Lado.DIREITA) }
        }

        RodapePlacar(estado, clientes, endereco, onDesfazer)
    }
}

@Composable
private fun LadoDoPlacar(
    lado: Lado,
    estado: EstadoJogo,
    modifier: Modifier,
    onPonto: () -> Unit
) {
    val sacando = estado.sacando == lado && !estado.encerrado
    val venceu = estado.vencedor == lado

    val corBorda = when {
        venceu -> VERDE
        sacando -> VERDE
        else -> CINZA_BORDA
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .padding(10.dp)
            .border(3.dp, corBorda, RoundedCornerShape(14.dp))
            .clickable { onPonto() },
        contentAlignment = Alignment.Center
    ) {
        val tamanhoNumero = (maxHeight.value * 0.58f).sp
        val tamanhoRotulo = (maxHeight.value * 0.07f).coerceAtLeast(12f).sp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (lado == Lado.ESQUERDA) "ESQUERDA" else "DIREITA",
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
                    venceu -> "VENCEU"
                    sacando -> "SACADOR ${estado.sacador} . SAQUE PELA ${estado.ladoDoSaque.uppercase()}"
                    else -> ""
                },
                color = if (sacando || venceu) VERDE else Color.Transparent,
                fontSize = tamanhoRotulo,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun RodapePlacar(
    estado: EstadoJogo,
    clientes: Int,
    endereco: String,
    onDesfazer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (clientes > 0) VERDE else AMBAR, CircleShape)
            )
            Text(
                text = if (clientes > 0) "  relogio conectado" else "  aguardando relogio . $endereco",
                color = CINZA_TEXTO,
                fontSize = 15.sp
            )
        }

        Text(
            text = if (estado.encerrado) "FIM DE GAME" else estado.chamada.replace("-", " . "),
            color = if (estado.pontoDeJogo) VERDE else CINZA_TEXTO,
            fontSize = 20.sp,
            letterSpacing = 4.sp,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = "desfazer",
            color = CINZA_TEXTO,
            fontSize = 15.sp,
            modifier = Modifier
                .clickable { onDesfazer() }
                .padding(8.dp)
        )
    }
}
