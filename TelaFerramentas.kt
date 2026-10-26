package com.kriptobr.placar.tablet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tela de status: uma olhada e voce sabe se esta tudo pronto antes de
 * sair de casa. Verde e ambar, sem rolagem.
 */
@Composable
fun TelaStatus(
    idiomaUi: String,
    relogioConectado: Boolean,
    segundosDesdeContato: Long?,
    ultimaTecla: String,
    vozEnOk: Boolean,
    vozPtOk: Boolean,
    saidaAudio: String,
    ultimaPartida: String,
    endereco: String,
    onFechar: () -> Unit
) {
    Painel {
        Text(
            Textos.get("status", idiomaUi),
            color = CINZA_TEXTO,
            fontSize = 16.sp,
            letterSpacing = 2.sp
        )
        Box(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.width(520.dp)) {
            LinhaInfo(
                Textos.get("conectado", idiomaUi),
                if (relogioConectado) "OK" else Textos.get("aguardando", idiomaUi),
                if (relogioConectado) VERDE else AMBAR
            )
            LinhaInfo(
                Textos.get("ultimo_contato", idiomaUi),
                segundosDesdeContato?.let { "$it s" } ?: Textos.get("nunca", idiomaUi),
                if (segundosDesdeContato != null) VERDE else AMBAR
            )
            LinhaInfo(
                Textos.get("ultima_tecla", idiomaUi),
                ultimaTecla.ifBlank { Textos.get("nunca", idiomaUi) },
                if (ultimaTecla.isNotBlank()) VERDE else AMBAR
            )
            LinhaInfo(
                "EN",
                if (vozEnOk) Textos.get("disponivel", idiomaUi)
                else Textos.get("indisponivel", idiomaUi),
                if (vozEnOk) VERDE else AMBAR
            )
            LinhaInfo(
                "PT",
                if (vozPtOk) Textos.get("disponivel", idiomaUi)
                else Textos.get("indisponivel", idiomaUi),
                if (vozPtOk) VERDE else AMBAR
            )
            LinhaInfo(Textos.get("saida_audio", idiomaUi), saidaAudio)
            LinhaInfo(
                Textos.get("ultima_partida", idiomaUi),
                ultimaPartida.ifBlank { Textos.get("nunca", idiomaUi) }
            )
            LinhaInfo(Textos.get("endereco", idiomaUi), endereco)
        }

        Box(modifier = Modifier.height(16.dp))
        Botao(Textos.get("fechar", idiomaUi), CINZA_TEXTO) { onFechar() }
    }
}

/**
 * Modo de teste. Fica fora do fluxo de jogo e nada aqui entra no historico,
 * senao voce acaba com partidas fantasma no relatorio.
 */
@Composable
fun TelaTeste(
    idiomaUi: String,
    ultimaTecla: String,
    codigoUltimaTecla: Int?,
    tempoIdaEVolta: Long?,
    onFalarEn: () -> Unit,
    onFalarPt: () -> Unit,
    onMapear: (String) -> Unit,
    onMedir: () -> Unit,
    onDerrubar: () -> Unit,
    onFechar: () -> Unit
) {
    Painel {
        Text(
            Textos.get("teste", idiomaUi),
            color = CINZA_TEXTO,
            fontSize = 16.sp,
            letterSpacing = 2.sp
        )
        Text(Textos.get("aviso_teste", idiomaUi), color = AMBAR, fontSize = 13.sp)
        Box(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.width(520.dp)) {
            Botao(Textos.get("falar_en", idiomaUi), Color.White) { onFalarEn() }
            Botao(Textos.get("falar_pt", idiomaUi), Color.White) { onFalarPt() }

            Box(modifier = Modifier.height(10.dp))
            LinhaInfo(
                Textos.get("capturar_tecla", idiomaUi),
                ultimaTecla.ifBlank { "..." },
                if (ultimaTecla.isNotBlank()) VERDE else CINZA_TEXTO
            )

            if (codigoUltimaTecla != null) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        Textos.get("mapear_para", idiomaUi),
                        color = CINZA_TEXTO,
                        fontSize = 14.sp
                    )
                    Botao(Textos.get("esquerda", idiomaUi), VERDE) {
                        onMapear(ControleBluetooth.ACAO_ESQUERDA)
                    }
                    Botao(Textos.get("direita", idiomaUi), VERDE) {
                        onMapear(ControleBluetooth.ACAO_DIREITA)
                    }
                    Botao(Textos.get("desfazer", idiomaUi), AMBAR) {
                        onMapear(ControleBluetooth.ACAO_DESFAZER)
                    }
                }
            }

            Box(modifier = Modifier.height(10.dp))
            LinhaInfo(
                Textos.get("medir_tempo", idiomaUi),
                tempoIdaEVolta?.let { "$it ms" } ?: "...",
                if (tempoIdaEVolta != null) VERDE else CINZA_TEXTO
            )

            Row(horizontalArrangement = Arrangement.Start) {
                Botao(Textos.get("medir_tempo", idiomaUi), Color.White) { onMedir() }
                Box(modifier = Modifier.width(10.dp))
                Botao(Textos.get("derrubar", idiomaUi), AMBAR) { onDerrubar() }
            }
        }

        Box(modifier = Modifier.height(14.dp))
        Botao(Textos.get("fechar", idiomaUi), CINZA_TEXTO) { onFechar() }
    }
}

/**
 * Backup e restauracao. O arquivo unico guarda jogadores, fotos, partidas
 * e a memoria do rodizio.
 */
@Composable
fun TelaBackup(
    idiomaUi: String,
    aviso: String,
    onExportar: () -> Unit,
    onImportar: () -> Unit,
    onFechar: () -> Unit
) {
    Painel {
        Text(
            Textos.get("backup", idiomaUi),
            color = CINZA_TEXTO,
            fontSize = 16.sp,
            letterSpacing = 2.sp
        )
        Box(modifier = Modifier.height(12.dp))

        Text(
            Textos.get("aviso_backup", idiomaUi),
            color = CINZA_TEXTO,
            fontSize = 14.sp,
            modifier = Modifier.width(480.dp)
        )

        Box(modifier = Modifier.height(18.dp))

        Botao(Textos.get("exportar_backup", idiomaUi), VERDE) { onExportar() }
        Botao(Textos.get("importar_backup", idiomaUi), Color.White) { onImportar() }

        if (aviso.isNotBlank()) {
            Box(modifier = Modifier.height(10.dp))
            Text(aviso, color = AMBAR, fontSize = 14.sp)
        }

        Box(modifier = Modifier.height(14.dp))
        Botao(Textos.get("fechar", idiomaUi), CINZA_TEXTO) { onFechar() }
    }
}
