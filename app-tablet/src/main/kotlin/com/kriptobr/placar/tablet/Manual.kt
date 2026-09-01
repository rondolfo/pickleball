package com.kriptobr.placar.tablet

/**
 * Manual dentro do aplicativo, nos dois idiomas.
 *
 * Fica aqui e nao em Textos porque e texto corrido, nao rotulo de tela.
 * Acompanha o idioma da interface.
 */
data class SecaoManual(val titulo: String, val itens: List<String>)

object Manual {

    fun secoes(idioma: String): List<SecaoManual> =
        if (idioma == Textos.PT) portugues() else ingles()

    private fun ingles(): List<SecaoManual> = listOf(
        SecaoManual(
            "In short",
            listOf(
                "The tablet shows the score and calls it out loud. The watch is the remote control.",
                "Tap the side that won the rally. Never think about the rules: the app handles serve changes, second server and side outs.",
                "Everything lives on this tablet. No account, no cloud, nothing published anywhere."
            )
        ),
        SecaoManual(
            "Starting a game",
            listOf(
                "Tap the gear at the bottom right, then New game.",
                "Pick the four players and choose which side serves first. You can also play with no names at all.",
                "Games go to 11, win by 2, traditional doubles scoring: only the serving team scores."
            )
        ),
        SecaoManual(
            "Scoring a point",
            listOf(
                "From the watch: tap the team that won the rally. The watch buzzes once on the tap and twice when the tablet confirms.",
                "From the tablet: double tap to unlock, then tap either half. The screen relocks by itself after a few seconds.",
                "From a Bluetooth clicker, once mapped in Test mode.",
                "Undo is on the watch, on the tablet footer, and on the clicker. It rewinds the whole state, not just the number."
            )
        ),
        SecaoManual(
            "Reading the screen",
            listOf(
                "The two large numbers are the score. The green frame marks who is serving.",
                "The band underneath shows who serves, from which side, and the court diagram with all four positions.",
                "In the diagram, solid green is the server and the green outline is the receiver. They are always diagonal.",
                "The small three numbers at the bottom are the official call: server score, receiver score, server number."
            )
        ),
        SecaoManual(
            "Voice",
            listOf(
                "The tablet calls the score after every point, with a short delay so an undo never speaks a wrong score.",
                "Tap EN or PT in the footer to switch the voice language instantly.",
                "The name of the new server is announced whenever the serve changes hands.",
                "Tap the small call at the bottom to repeat the last announcement.",
                "If a language is missing, install it in Android settings under Text to speech."
            )
        ),
        SecaoManual(
            "Players",
            listOf(
                "Gear, then Players. Full name is the only required field.",
                "Short name is what shows on the big screen during a game.",
                "Email is what puts the person on the summary sent after playing.",
                "Photo is optional, from the camera or the gallery. Without one you get a colored circle with initials."
            )
        ),
        SecaoManual(
            "Rotation",
            listOf(
                "Gear, then Rotation. Mark who is here today.",
                "The suggestion favours whoever has waited longest, avoids repeating partners and avoids repeating opponents.",
                "Shuffle varies the pairing but still respects who is waiting.",
                "Use this to start the next game with the suggested four."
            )
        ),
        SecaoManual(
            "During a game",
            listOf(
                "Substitute a player: gear, then Substitute. The score does not change and both players count as having played.",
                "Swap sides: gear, then Swap sides, or long press the score line on the watch. Use it whenever you change ends.",
                "Fix the score: gear, then Fix the score. Undo will not go back past a manual correction."
            )
        ),
        SecaoManual(
            "After playing",
            listOf(
                "Every finished game is saved by itself. A game abandoned halfway is kept as unfinished.",
                "Gear, then Leaderboard, for standings by player, today or all time.",
                "Email today's summary sends one message at the end of the night with every result and the standings.",
                "Export to CSV opens the whole history in a spreadsheet."
            )
        ),
        SecaoManual(
            "Backup",
            listOf(
                "Gear, then Backup. One file with players, photos, matches and rotation memory.",
                "Save it somewhere safe once you have entered everyone.",
                "Updating the app never erases data. Uninstalling does."
            )
        ),
        SecaoManual(
            "When something goes wrong",
            listOf(
                "Status dot on the watch: blue is Bluetooth, green is Wi-Fi, amber is no connection.",
                "An amber counter on the watch means points are waiting to be sent. They go through as soon as the link is back.",
                "If the connection never comes up, open Status on the tablet and check what is missing.",
                "Test mode lets you check voice, clicker keys and round trip time before leaving home. Nothing there is saved."
            )
        )
    )

    private fun portugues(): List<SecaoManual> = listOf(
        SecaoManual(
            "Em resumo",
            listOf(
                "O tablet mostra o placar e fala em voz alta. O relogio e o controle remoto.",
                "Toque na dupla que ganhou o rally. Nunca pense na regra: o aplicativo cuida de troca de sacador, segundo sacador e troca de saque.",
                "Tudo fica neste tablet. Sem conta, sem nuvem, sem nada publicado."
            )
        ),
        SecaoManual(
            "Comecar um jogo",
            listOf(
                "Toque na engrenagem no canto inferior direito e escolha Novo game.",
                "Escolha os quatro jogadores e quem saca primeiro. Tambem da para jogar sem nome nenhum.",
                "Game ate 11, com vantagem de 2, no formato tradicional: so pontua quem saca."
            )
        ),
        SecaoManual(
            "Marcar ponto",
            listOf(
                "Pelo relogio: toque na dupla que ganhou o rally. Ele vibra uma vez no toque e duas quando o tablet confirma.",
                "Pelo tablet: toque duas vezes para destravar e depois toque na metade da tela. Ele trava sozinho depois de alguns segundos.",
                "Pelo controle Bluetooth, depois de mapear no modo de teste.",
                "O desfazer esta no relogio, no rodape do tablet e no controle. Ele reverte o estado inteiro, nao apenas o numero."
            )
        ),
        SecaoManual(
            "Ler a tela",
            listOf(
                "Os dois numeros grandes sao o placar. A moldura verde marca quem esta sacando.",
                "A faixa de baixo mostra quem saca, de que lado, e o desenho da quadra com as quatro posicoes.",
                "No desenho, verde cheio e quem saca e contorno verde e quem recebe. Eles sempre ficam na diagonal.",
                "Os tres numeros pequenos embaixo sao a chamada oficial: pontos de quem saca, pontos de quem recebe, numero do sacador."
            )
        ),
        SecaoManual(
            "Voz",
            listOf(
                "O tablet fala o placar depois de cada ponto, com um atraso curto para que um desfazer nunca chegue a falar placar errado.",
                "Toque em EN ou PT no rodape para trocar o idioma da voz na hora.",
                "O nome de quem assume o saque e anunciado sempre que o saque muda de dupla.",
                "Toque na chamada pequena do rodape para repetir o ultimo anuncio.",
                "Se faltar algum idioma, instale nas configuracoes do Android, em conversao de texto em fala."
            )
        ),
        SecaoManual(
            "Jogadores",
            listOf(
                "Engrenagem e depois Jogadores. Nome completo e o unico campo obrigatorio.",
                "Nome curto e o que aparece na tela grande durante o jogo.",
                "E-mail e o que coloca a pessoa no resumo enviado depois de jogar.",
                "Foto e opcional, pela camera ou pela galeria. Sem foto aparece um circulo colorido com as iniciais."
            )
        ),
        SecaoManual(
            "Rodizio",
            listOf(
                "Engrenagem e depois Rodizio. Marque quem esta presente hoje.",
                "A sugestao prioriza quem esperou mais, evita repetir parceiro e evita repetir adversario.",
                "Sortear varia a montagem mas continua respeitando quem esta esperando.",
                "Use para comecar o proximo jogo ja com os quatro sugeridos."
            )
        ),
        SecaoManual(
            "Durante o jogo",
            listOf(
                "Substituir jogador: engrenagem e depois Substituir. O placar nao muda e os dois contam como tendo jogado.",
                "Inverter lados: engrenagem e depois Inverter, ou toque longo na linha do placar no relogio. Use sempre que trocarem de ponta.",
                "Corrigir o placar: engrenagem e depois Corrigir. O desfazer nao volta alem de uma correcao manual."
            )
        ),
        SecaoManual(
            "Depois de jogar",
            listOf(
                "Toda partida encerrada e salva sozinha. Partida abandonada no meio fica marcada como incompleta.",
                "Engrenagem e depois Ranking, para a classificacao por jogador, de hoje ou de sempre.",
                "Enviar o resumo de hoje manda uma mensagem so no fim da noite, com todos os resultados e o ranking.",
                "Exportar em CSV abre o historico inteiro numa planilha."
            )
        ),
        SecaoManual(
            "Backup",
            listOf(
                "Engrenagem e depois Backup. Um arquivo unico com jogadores, fotos, partidas e a memoria do rodizio.",
                "Guarde num lugar seguro depois de cadastrar todo mundo.",
                "Atualizar o aplicativo nunca apaga os dados. Desinstalar apaga."
            )
        ),
        SecaoManual(
            "Quando algo da errado",
            listOf(
                "Ponto de status no relogio: azul e Bluetooth, verde e Wi-Fi, ambar e sem conexao.",
                "Um contador ambar no relogio significa pontos esperando envio. Eles entram assim que a conexao voltar.",
                "Se a conexao nunca subir, abra Status no tablet e veja o que esta faltando.",
                "O modo de teste permite conferir voz, teclas do controle e tempo de resposta antes de sair de casa. Nada ali e gravado."
            )
        )
    )
}
