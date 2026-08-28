package com.kriptobr.placar.tablet

/**
 * Idioma da interface e idioma da voz sao independentes.
 * E comum querer a tela em um e a chamada em outro, porque quem
 * precisa entender a contagem e quem esta jogando com voce.
 */
object Textos {

    const val EN = "en"
    const val PT = "pt"

    private val mapa = mapOf(
        EN to mapOf(
            "esquerda" to "LEFT",
            "direita" to "RIGHT",
            "sacador" to "SERVER",
            "saque_direita" to "SERVE RIGHT",
            "saque_esquerda" to "SERVE LEFT",
            "venceu" to "WINNER",
            "fim_game" to "GAME OVER",
            "ponto_de_jogo" to "GAME POINT",
            "desfazer" to "undo",
            "travado" to "locked, double tap to unlock",
            "destravado" to "unlocked",
            "aguardando" to "waiting for watch",
            "conectado" to "watch connected",
            "menu" to "Settings",
            "novo_game" to "New game",
            "inverter" to "Swap sides",
            "idioma_voz" to "Voice language",
            "idioma_tela" to "Screen language",
            "repetir" to "Repeat score",
            "fechar" to "Close",
            "quem_saca" to "Who serves first?",
            "confirmar_novo" to "Start a new game? The current score will be lost.",
            "sim" to "Yes",
            "nao" to "No",
            "voz_faltando" to "Voice not installed for this language. Install it in the Android settings, under Text to speech."
        ),
        PT to mapOf(
            "esquerda" to "ESQUERDA",
            "direita" to "DIREITA",
            "sacador" to "SACADOR",
            "saque_direita" to "SAQUE PELA DIREITA",
            "saque_esquerda" to "SAQUE PELA ESQUERDA",
            "venceu" to "VENCEU",
            "fim_game" to "FIM DE GAME",
            "ponto_de_jogo" to "PONTO DE JOGO",
            "desfazer" to "desfazer",
            "travado" to "travado, toque duas vezes para destravar",
            "destravado" to "destravado",
            "aguardando" to "aguardando o relogio",
            "conectado" to "relogio conectado",
            "menu" to "Ajustes",
            "novo_game" to "Novo game",
            "inverter" to "Inverter lados",
            "idioma_voz" to "Idioma da voz",
            "idioma_tela" to "Idioma da tela",
            "repetir" to "Repetir o placar",
            "fechar" to "Fechar",
            "quem_saca" to "Quem saca primeiro?",
            "confirmar_novo" to "Comecar um novo game? O placar atual sera perdido.",
            "sim" to "Sim",
            "nao" to "Nao",
            "voz_faltando" to "Voz nao instalada para este idioma. Instale nas configuracoes do Android, em Conversao de texto em fala."
        )
    )

    fun get(chave: String, idioma: String): String =
        mapa[idioma]?.get(chave) ?: mapa[EN]?.get(chave) ?: chave
}
