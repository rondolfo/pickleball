package com.kriptobr.placar.watch

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.kriptobr.placar.core.Lado

private val VERDE = Color(0xFF97C459)
private val AMBAR = Color(0xFFEF9F27)
private val CINZA = Color(0xFF888880)
private val FUNDO_LADO = Color(0xFF1C1C1A)

class MainActivity : ComponentActivity() {

    private var conectado by mutableStateOf(false)
    private var placar by mutableStateOf("0 . 0 . 2")
    private var procura by mutableStateOf("")
    private var naFila by mutableStateOf(0)
    private var cliente: ClienteTablet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        cliente = ClienteTablet(
            contexto = this,
            aoReceberEstado = { estado ->
                val texto = estado.optString("chamada", "").replace("-", " . ")
                runOnUiThread { if (texto.isNotEmpty()) placar = texto }
            },
            aoMudarConexao = { ligado -> runOnUiThread { conectado = ligado } },
            aoConfirmarPonto = {
                runOnUiThread { naFila = cliente?.pontosNaFila() ?: 0 }
                vibrarConfirmacao()
            },
            aoMudarProcura = { texto -> runOnUiThread { procura = texto } }
        ).also { it.iniciar() }

        setContent {
            TelaRelogio(
                placar = placar,
                conectado = conectado,
                procura = procura,
                naFila = naFila,
                onPonto = { lado ->
                    vibrar(40)
                    cliente?.enviarRally(lado)
                    naFila = cliente?.pontosNaFila() ?: 0
                },
                onDesfazer = {
                    vibrar(140)
                    cliente?.enviarDesfazer()
                }
            )
        }
    }

    override fun onDestroy() {
        cliente?.parar()
        cliente = null
        super.onDestroy()
    }

    /** Toque registrado no relogio. */
    private fun vibrar(duracao: Long) {
        runCatching {
            vibrador().vibrate(
                VibrationEffect.createOneShot(duracao, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }

    /**
     * Toque diferente para quando o tablet confirma que o ponto entrou.
     * Com o tablet longe, esta e a unica forma de ter certeza sem olhar.
     */
    private fun vibrarConfirmacao() {
        runCatching {
            val padrao = longArrayOf(0, 30, 70, 30)
            vibrador().vibrate(VibrationEffect.createWaveform(padrao, -1))
        }
    }

    private fun vibrador(): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
}

@Composable
private fun TelaRelogio(
    placar: String,
    conectado: Boolean,
    procura: String,
    naFila: Int,
    onPonto: (Lado) -> Unit,
    onDesfazer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (conectado) VERDE else AMBAR, CircleShape)
            )
            Text(
                text = "  " + if (conectado) placar else (procura.ifEmpty { "..." }),
                color = CINZA,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace
            )
            if (naFila > 0) {
                Text(text = "  +$naFila", color = AMBAR, fontSize = 13.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            BotaoLado("LEFT", Modifier.weight(1f)) { onPonto(Lado.ESQUERDA) }
            Spacer(modifier = Modifier.width(4.dp))
            BotaoLado("RIGHT", Modifier.weight(1f)) { onPonto(Lado.DIREITA) }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable { onDesfazer() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "undo", color = CINZA, fontSize = 14.sp)
        }
    }
}

@Composable
private fun BotaoLado(rotulo: String, modifier: Modifier, onClique: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(3.dp)
            .background(FUNDO_LADO)
            .clickable { onClique() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rotulo,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
