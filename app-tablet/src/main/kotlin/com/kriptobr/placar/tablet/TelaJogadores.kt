package com.kriptobr.placar.tablet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

@Composable
fun TelaJogadores(
    jogadores: List<Jogador>,
    idiomaUi: String,
    versaoFotos: Int,
    onSalvar: (Jogador) -> Unit,
    onExcluir: (Jogador) -> Unit,
    onFechar: () -> Unit
) {
    var editando by remember { mutableStateOf<Jogador?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF7000000))
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Cabecalho(
                titulo = Textos.get("jogadores", idiomaUi),
                idiomaUi = idiomaUi,
                onFechar = onFechar
            )

            if (jogadores.isEmpty()) {
                Text(
                    text = Textos.get("sem_jogadores", idiomaUi),
                    color = CINZA_TEXTO,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(jogadores, key = { it.id }) { jogador ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editando = jogador }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(jogador, 46.dp, versaoFotos)
                        Box(modifier = Modifier.width(16.dp))
                        Column {
                            Text(jogador.nome, color = Color.White, fontSize = 18.sp)
                            if (jogador.email.isNotBlank()) {
                                Text(jogador.email, color = CINZA_TEXTO, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Text(
                text = "+  " + Textos.get("novo_jogador", idiomaUi),
                color = VERDE,
                fontSize = 19.sp,
                modifier = Modifier
                    .clickable {
                        editando = Jogador(
                            id = UUID.randomUUID().toString(),
                            nome = "",
                            apelido = ""
                        )
                    }
                    .padding(vertical = 12.dp)
            )
        }
    }

    editando?.let { alvo ->
        FormularioJogador(
            inicial = alvo,
            idiomaUi = idiomaUi,
            versaoFotos = versaoFotos,
            existente = jogadores.any { it.id == alvo.id },
            onSalvar = { atualizado -> onSalvar(atualizado); editando = null },
            onExcluir = { onExcluir(alvo); editando = null },
            onCancelar = { editando = null }
        )
    }
}

@Composable
private fun FormularioJogador(
    inicial: Jogador,
    idiomaUi: String,
    versaoFotos: Int,
    existente: Boolean,
    onSalvar: (Jogador) -> Unit,
    onExcluir: () -> Unit,
    onCancelar: () -> Unit
) {
    var nome by remember { mutableStateOf(inicial.nome) }
    var apelido by remember { mutableStateOf(inicial.apelido) }
    var email by remember { mutableStateOf(inicial.email) }
    var sexo by remember { mutableStateOf(inicial.sexo) }
    var temFoto by remember { mutableStateOf(inicial.temFoto) }
    var carimbo by remember { mutableStateOf(versaoFotos) }

    val contexto = LocalContext.current
    val seletor = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val destino = Repositorio(contexto).arquivoFoto(inicial.id)
            if (Foto.salvar(contexto, uri, destino)) {
                temFoto = true
                carimbo += 1
            }
        }
    }

    // o teclado do Android cobre o rodape do formulario em tablet deitado.
    // imePadding empurra o conteudo para cima e a rolagem garante o resto.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFA000000))
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(FUNDO_MENU, RoundedCornerShape(16.dp))
                .border(2.dp, CINZA_BORDA, RoundedCornerShape(16.dp))
                .padding(26.dp)
                .width(460.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(
                    inicial.copy(nome = nome.ifBlank { "?" }, temFoto = temFoto),
                    68.dp,
                    carimbo
                )
                Box(modifier = Modifier.width(18.dp))
                Text(
                    text = Textos.get("escolher_foto", idiomaUi),
                    color = VERDE,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable {
                            seletor.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                        .padding(8.dp)
                )
            }

            Box(modifier = Modifier.height(14.dp))

            Campo(nome, Textos.get("nome", idiomaUi)) { nome = it }
            Campo(apelido, Textos.get("apelido", idiomaUi)) { apelido = it }
            Campo(email, Textos.get("email", idiomaUi)) { email = it }

            Box(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OpcaoSexo(Textos.get("masculino", idiomaUi), sexo == "M") { sexo = "M" }
                OpcaoSexo(Textos.get("feminino", idiomaUi), sexo == "F") { sexo = "F" }
                OpcaoSexo(Textos.get("nao_informado", idiomaUi), sexo.isBlank()) { sexo = "" }
            }

            Box(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = Textos.get("cancelar", idiomaUi),
                    color = CINZA_TEXTO,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onCancelar() }.padding(12.dp)
                )
                if (existente) {
                    Text(
                        text = Textos.get("excluir", idiomaUi),
                        color = AMBAR,
                        fontSize = 18.sp,
                        modifier = Modifier.clickable { onExcluir() }.padding(12.dp)
                    )
                }
                Text(
                    text = Textos.get("salvar", idiomaUi),
                    color = VERDE,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clickable {
                            if (nome.isNotBlank()) {
                                onSalvar(
                                    inicial.copy(
                                        nome = nome.trim(),
                                        apelido = apelido.trim(),
                                        email = email.trim(),
                                        sexo = sexo,
                                        temFoto = temFoto
                                    )
                                )
                            }
                        }
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun Campo(valor: String, rotulo: String, onMudar: (String) -> Unit) {
    TextField(
        value = valor,
        onValueChange = onMudar,
        label = { Text(rotulo, color = CINZA_TEXTO) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0xFF2A2A26),
            unfocusedContainerColor = Color(0xFF2A2A26)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    )
}

@Composable
private fun OpcaoSexo(rotulo: String, ativo: Boolean, onClique: () -> Unit) {
    Text(
        text = rotulo,
        color = if (ativo) VERDE else CINZA_TEXTO,
        fontSize = 15.sp,
        modifier = Modifier.clickable { onClique() }.padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
fun Cabecalho(titulo: String, idiomaUi: String, onFechar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(titulo, color = Color.White, fontSize = 24.sp, letterSpacing = 1.sp)
        Text(
            text = Textos.get("voltar", idiomaUi),
            color = CINZA_TEXTO,
            fontSize = 17.sp,
            modifier = Modifier.clickable { onFechar() }.padding(10.dp)
        )
    }
}
