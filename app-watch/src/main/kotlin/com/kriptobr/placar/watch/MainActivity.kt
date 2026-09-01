package com.kriptobr.placar.watch

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.kriptobr.placar.core.Lado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val VERDE = Color(0xFF97C459)
private val AZUL = Color(0xFF6BA6D6)
private val LARANJA = Color(0xFFEF9F27)
private val CINZA = Color(0xFF888880)

class MainActivity : ComponentActivity() {

    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var conectado by mutableStateOf(false)
    private var porBluetooth by mutableStateOf(false)
    private var idioma by mutableStateOf("en")
    private var placar by mutableStateOf("0 . 0 . 2")
    private var sacador by mutableStateOf("")
    private var ladoSaque by mutableStateOf("")
    private var nomeEsquerda by mutableStateOf("")
    private var nomeDireita by mutableStateOf("")
    private var naFila by mutableStateOf(0)

    private var wifi: ClienteTablet? = null
    private var ble: ClienteBle? = null

    private val pedidoBluetooth = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { iniciarBluetooth() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        wifi = ClienteTablet(
            contexto = this,
            aoReceberEstado = { estado -> aplicarEstado(estado, false) },
            aoMudarConexao = { ligado ->
                runOnUiThread { if (!porBluetooth) conectado = ligado }
            },
            aoConfirmarPonto = { confirmar() },
            aoMudarProcura = { }
        ).also { it.iniciar() }

        pedirPermissoesBluetooth()
        iniciarBluetooth()

        // reenvio periodico do que ainda nao foi confirmado por bluetooth
        escopo.launch {
            while (isActive) {
                delay(700)
                ble?.reenviarPendentes()
                naFila = (ble?.naFila ?: 0) + (wifi?.pontosNaFila() ?: 0)
            }
        }

        setContent {
            TelaRelogio(
                placar = placar,
                sacador = sacador,
                ladoSaque = ladoSaque,
                nomeEsquerda = nomeEsquerda,
                nomeDireita = nomeDireita,
                idioma = idioma,
                conectado = conectado,
                porBluetooth = porBluetooth,
                naFila = naFila,
                onPonto = { lado ->
                    vibrar(40)
                    enviarRally(lado)
                },
                onDesfazer = {
                    vibrar(140)
                    if (porBluetooth) ble?.enviarDesfazer() else wifi?.enviarDesfazer()
                },
                onInverter = {
                    vibrar(200)
                    wifi?.enviarInverter()
                    ble?.enviarInverter()
                }
            )
        }
    }

    override fun onDestroy() {
        escopo.cancel()
        ble?.parar()
        wifi?.parar()
        super.onDestroy()
    }

    private fun enviarRally(lado: Lado) {
        if (porBluetooth) ble?.enviarRally(lado) else wifi?.enviarRally(lado)
        naFila = (ble?.naFila ?: 0) + (wifi?.pontosNaFila() ?: 0)
    }

    private fun aplicarEstado(estado: org.json.JSONObject, viaBle: Boolean) {
        val texto = estado.optString("chamada", "").replace("-", " . ")
        val idiomaDoTablet = estado.optString("idioma", "en")
        val nome = estado.optString("sacadorNome", "")
        val lado = estado.optString("ladoSaque", "")
        val esq = estado.optString("nomeEsq", "")
        val dir = estado.optString("nomeDir", "")

        runOnUiThread {
            if (texto.isNotEmpty()) placar = texto
            if (idiomaDoTablet.isNotEmpty()) idioma = idiomaDoTablet
            sacador = nome
            ladoSaque = lado
            if (esq.isNotEmpty()) nomeEsquerda = esq
            if (dir.isNotEmpty()) nomeDireita = dir
            if (viaBle) conectado = true
        }
    }

    private fun confirmar() {
        runOnUiThread { naFila = (ble?.naFila ?: 0) + (wifi?.pontosNaFila() ?: 0) }
        vibrarConfirmacao()
    }

    // ---------- bluetooth ----------

    /**
     * Bluetooth e o caminho preferido: nao depende de rede nenhuma em quadra.
     * O Wi-Fi segue ativo como reserva, e assume sozinho se o BLE nao subir.
     */
    private fun iniciarBluetooth() {
        if (!temPermissoesBluetooth()) return
        if (ble != null) return

        val cliente = ClienteBle(
            contexto = this,
            aoReceberEstado = { estado -> aplicarEstado(estado, true) },
            aoMudarConexao = { ligado ->
                runOnUiThread {
                    porBluetooth = ligado
                    if (ligado) conectado = true
                }
            },
            aoConfirmarPonto = { confirmar() }
        )
        if (!cliente.suportado()) return
        ble = cliente
        cliente.iniciar()
    }

    private fun temPermissoesBluetooth(): Boolean {
        val necessarias = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return necessarias.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun pedirPermissoesBluetooth() {
        if (temPermissoesBluetooth()) return
        val pedir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        pedidoBluetooth.launch(pedir)
    }

    // ---------- vibracao ----------

    private fun vibrar(duracao: Long) {
        runCatching {
            vibrador().vibrate(
                VibrationEffect.createOneShot(duracao, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }

    private fun vibrarConfirmacao() {
        runCatching {
            vibrador().vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 70, 30), -1))
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
    sacador: String,
    ladoSaque: String,
    nomeEsquerda: String,
    nomeDireita: String,
    idioma: String,
    conectado: Boolean,
    porBluetooth: Boolean,
    naFila: Int,
    onPonto: (Lado) -> Unit,
    onDesfazer: () -> Unit,
    onInverter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // toque longo aqui inverte os lados, para quando o grupo troca de ponta
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(top = 6.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onInverter() })
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (!conectado) LARANJA else if (porBluetooth) AZUL else VERDE,
                        CircleShape
                    )
            )
            Text(
                text = "  $placar",
                color = CINZA,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
            if (naFila > 0) {
                Text(text = "  +$naFila", color = LARANJA, fontSize = 12.sp)
            }
        }

        if (sacador.isNotEmpty()) {
            Text(
                text = sacador.uppercase() + if (ladoSaque.isNotEmpty()) {
                    "  " + if (ladoSaque == "direita") {
                        if (idioma == "pt") "DIR" else "R"
                    } else {
                        if (idioma == "pt") "ESQ" else "L"
                    }
                } else "",
                color = VERDE,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            BotaoDupla(
                rotulo = nomeEsquerda.ifBlank { if (idioma == "pt") "ESQ" else "LEFT" },
                cor = AZUL,
                modifier = Modifier.weight(1f)
            ) { onPonto(Lado.ESQUERDA) }

            Spacer(modifier = Modifier.width(4.dp))

            BotaoDupla(
                rotulo = nomeDireita.ifBlank { if (idioma == "pt") "DIR" else "RIGHT" },
                cor = LARANJA,
                modifier = Modifier.weight(1f)
            ) { onPonto(Lado.DIREITA) }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clickable { onDesfazer() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (idioma == "pt") "desfazer" else "undo",
                color = CINZA,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Os botoes mostram o nome da dupla, nao o lado da quadra.
 *
 * Quando o grupo troca de ponta, esquerda e direita deixam de significar
 * o mesmo para quem esta olhando, e o erro aparece. Nome nao tem esse
 * problema. A cor reforca a leitura sem depender de ler o texto.
 */
@Composable
private fun BotaoDupla(
    rotulo: String,
    cor: Color,
    modifier: Modifier,
    onClique: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(3.dp)
            .background(cor.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
            .clickable { onClique() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(4.dp)
                    .background(cor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = rotulo,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
