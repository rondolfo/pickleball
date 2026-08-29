# Placar Pickleball

Sistema de placar para pickleball em duplas tradicional, composto por dois aplicativos Android que conversam pela rede local.

Todas as etapas planejadas entregues, exceto o experimento da camera.

## Regras

- Duplas tradicional, inicio em 0-0-2, troca de sacador, troca de saque
- Vitoria por 11 com vantagem de 2
- Lado do saque derivado da pontuacao
- Destaque de ponto de jogo

## Tablet

- Placar em numeros grandes, com moldura indicando quem saca
- Nome das duplas na tela quando os jogadores estao definidos
- Voz em ingles e portugues, com troca em um toque
- Idioma da tela separado do idioma da voz
- Tela de inicio de jogo: quem joga de cada lado e quem saca primeiro, numa tela so
- Substituicao no meio do jogo, sem mexer no placar
- Correcao manual do placar
- Inverter lados, que espelha placar, saque e nomes
- Travamento contra toque acidental
- Repetir a ultima chamada tocando no placar do rodape

## Controles

Tres formas de marcar ponto, todas produzindo o mesmo evento:

- Relogio, com vibracao no toque e vibracao dupla quando o tablet confirma
- Toque nas metades da tela do tablet
- Controle Bluetooth de disparo, que se apresenta como teclado

O mapeamento do controle e configuravel. A captura de tecla do modo de teste mostra qual codigo chegou e permite atribuir a acao.

## Rodizio

Uma quadra, sem nivel. A sugestao respeita tres prioridades, nesta ordem:

1. quem esperou mais entra primeiro
2. evitar repetir parceiro
3. evitar repetir os mesmos adversarios

O desempate considera tambem o total de jogos. Sem isso os mesmos jogadores ganhavam a ordem fixa e nunca saiam de quadra. Simulado com 5, 6 e 7 presentes ao longo de 10 rodadas: distribuicao equilibrada e ninguem fora duas rodadas seguidas.

A opcao de sortear tambem respeita quem esta esperando, variando apenas a montagem.

## Jogadores e partidas

- Cadastro com nome, nome curto, e-mail, sexo e foto
- Foto opcional, escolhida da galeria sem exigir permissao
- Quem nao tem foto aparece com circulo colorido e iniciais
- Toda partida encerrada e salva; partida abandonada fica marcada como incompleta
- Recuperacao apos reinicio: o placar volta exatamente onde parou
- Historico com data, duplas e placar, e detalhe com estatisticas

## Estatisticas

Por partida: total de rallies, rallies ganhos, rallies ganhos recebendo, turnos de saque sem pontuar, maior sequencia e viradas.

Por jogador: partidas, vitorias, derrotas, aproveitamento, parceiro mais frequente e parceiro com melhores resultados. Quem entrou por substituicao conta como tendo jogado a partida, assim como quem saiu.

## Saidas

- Resumo por e-mail, abrindo o aplicativo de e-mail ja preenchido
- Exportacao em CSV, compartilhavel para planilha, nuvem ou e-mail
- Nada e enviado sozinho

## Ferramentas

Tela de status, com relogio, ultima tecla do controle, vozes instaladas, saida de audio, ultima partida e endereco do tablet.

Modo de teste, isolado do jogo e sem gravar nada no historico: falar placar nos dois idiomas, capturar e mapear tecla do controle, medir a ida e volta ate o relogio e derrubar a conexao para ver a reconexao.

## Arquitetura

Toda entrada vira o mesmo evento, com identificador proprio. O estado nunca e editado: e recalculado a partir do log. Desfazer e remover o ultimo evento e recalcular.

A correcao manual usa um estado base: em vez de inventar eventos falsos, o placar corrigido vira o novo ponto de partida. O custo e que o desfazer nao volta alem daquele ponto.

Substituicao nao toca no log de pontos. Ela grava uma escalacao com horario, ao lado dos eventos.

## Conexao

Tres caminhos em cascata: endereco salvo da ultima vez, anuncio na rede local e varredura da faixa de enderecos. Reconexao automatica. Pontos ficam numa fila ate o tablet confirmar, e o reenvio e seguro porque o tablet ignora identificadores repetidos.

## Armazenamento

Arquivos JSON dentro do proprio aplicativo. Sem conta, sem login, sem nuvem, sem sincronizacao. Fotos reduzidas a 256 pixels.

Backup e restauracao em Ajustes, Backup. Um arquivo unico com jogadores, fotos, partidas e a memoria do rodizio.

## Assinatura

Os APKs sao assinados com a chave fixa em `chave/placar.jks`, versionada junto com o projeto.

Sem isso, cada build do GitHub gera uma chave de debug nova, o Android recusa a atualizacao e a unica saida vira desinstalar, o que apaga tudo. Com a chave fixa, atualizar por cima preserva os dados.

A chave e autoassinada e serve apenas para uso proprio. Como esta versionada, quem tiver acesso ao repositorio consegue assinar um APK que o Android aceita como atualizacao deste aplicativo. Para uso pessoal isso e aceitavel, e deixar o repositorio privado remove ate esse risco.

## Estrutura

```
core/         regras, protocolo, estatisticas e rodizio em Kotlin puro
app-tablet/   servidor, placar, voz, cadastro, rodizio, historico e saidas
app-watch/    controle remoto no Galaxy Watch Ultra
```

## Como gerar e instalar

Envie para a branch main e o fluxo build publica apk-tablet e apk-relogio na aba Actions.

O do tablet e um toque no arquivo. O do relogio:

```
adb connect ENDERECO_DO_RELOGIO
adb install -r app-watch-debug.apk
```

## O que ficou de fora

- Camera em modo sombra, o experimento de deteccao por gesto
- Partida em melhor de tres
- Rodizio em mais de uma quadra

## Correcao de regra na versao 0.6

A versao anterior calculava o lado do saque pela regra simplificada, par saca pela direita. Essa regra so vale para o primeiro sacador.

Quando entra o segundo sacador, os parceiros nao trocam de lado, entao ele saca do lado onde ja esta, que pode contrariar a paridade. O nucleo agora rastreia a posicao dos jogadores em quadra, o que corrige o lado do saque e permite identificar qual pessoa esta sacando.

Regra de posicao: os parceiros trocam de lado sempre que a propria dupla marca ponto sacando. Ninguem troca de lado ao perder o rally.

## Desenho da quadra na versao 0.7

A faixa do saque passou a ter, ao lado do nome grande, um desenho da quadra vista de cima com os quatro jogadores nas posicoes atuais.

Verde cheio marca quem saca. Contorno verde marca quem recebe.

Detalhe geometrico: cada dupla encara a rede em sentido contrario, entao a quadra da direita de cada uma fica em pontas opostas do desenho. E isso que faz sacador e recebedor aparecerem na diagonal, como no jogo de verdade.

Se o desenho parecer invertido de onde voce fica, use inverter lados no menu, que espelha placar, nomes e posicoes juntos.
