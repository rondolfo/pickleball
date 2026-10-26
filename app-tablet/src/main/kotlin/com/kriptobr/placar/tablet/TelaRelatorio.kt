package com.kriptobr.placar.tablet

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File

/**
 * Relatorio da temporada aberto dentro do aplicativo.
 *
 * Usa WebView em vez de mandar o arquivo para o navegador porque o Android
 * trata mal um endereco de conteudo com HTML, e muitos navegadores recusam.
 * Aqui abre sempre, e o botao de compartilhar entrega o arquivo para quem
 * quiser levar para o grupo.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TelaRelatorio(
    arquivo: File?,
    idiomaUi: String,
    onCompartilhar: () -> Unit,
    onFechar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Textos.get("relatorio", idiomaUi),
                    color = Color.White,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )
                Row {
                    Botao(Textos.get("compartilhar", idiomaUi), VERDE) { onCompartilhar() }
                    Botao(Textos.get("voltar", idiomaUi), CINZA_TEXTO) { onFechar() }
                }
            }

            if (arquivo == null) {
                Text(
                    text = Textos.get("sem_dados", idiomaUi),
                    color = CINZA_TEXTO,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    // A carga fica no factory, que roda uma vez. Em update ela
                    // recarregava a cada recomposicao e a pagina piscava,
                    // perdendo a rolagem de quem estava lendo.
                    factory = { contexto ->
                        WebView(contexto).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.allowFileAccess = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            // O arquivo tem sempre o mesmo nome. Sem isso, o
                            // relatorio novo apareceria com os numeros velhos.
                            settings.cacheMode = WebSettings.LOAD_NO_CACHE
                            setBackgroundColor(android.graphics.Color.BLACK)
                            loadUrl("file://" + arquivo.absolutePath)
                        }
                    }
                )
            }
        }
    }
}
