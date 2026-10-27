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

## Bluetooth na versao 0.8

O relogio passou a falar com o tablet por Bluetooth de baixa energia, sem depender de rede sem fio. O tablet anuncia um servico e o relogio conecta direto.

O Wi-Fi continua ativo como reserva. Se o tablet nao suportar modo periferico, ou se o Bluetooth falhar, o Wi-Fi assume sozinho e nada se perde. O ponto de status no relogio indica qual caminho esta em uso: azul e Bluetooth, verde e Wi-Fi, ambar e sem conexao.

## Botoes do relogio

Os botoes passaram a mostrar o nome da dupla, com faixa colorida, em vez de esquerda e direita. Quando o grupo troca de ponta, os lados deixam de significar o mesmo para quem olha, e o erro aparece. Nome nao tem esse problema.

Toque longo na linha do placar inverte os lados no tablet inteiro, para quando voces trocam de ponta de verdade.

## Ranking e resumo da noite

A tela de estatisticas virou ranking, com filtro entre hoje e sempre, posicao, vitorias, derrotas, aproveitamento e saldo de pontos.

O e-mail agora pode sair uma vez so no fim da noite, com todos os resultados do dia e o ranking, em vez de um e-mail por partida. O envio por partida continua disponivel.

## Sobre a chamada de troca de saque

"Side out" e o termo oficial e significa que o saque mudou de dupla, nao que a bola saiu. Como isso confunde na quadra, o padrao passou a ser anunciar apenas quem assume o saque, que e inequivoco. Quem preferir o termo oficial pode ligar no menu.

## Versao web, na pasta docs

Aplicativo completo rodando no navegador, para quem nao tem Android. Funciona em iPad, iPhone, notebook e no proprio tablet.

E independente do aplicativo Android: mesmas regras, dados proprios, guardados no navegador. Nao ha sincronizacao entre os dois.

### O que tem

Placar com numeros grandes, faixa de saque com o desenho da quadra, voz em ingles e portugues, cadastro de jogadores com foto pela camera, rodizio, substituicao, correcao manual, historico, ranking, resumo por e-mail, exportacao em CSV, backup em arquivo e manual embutido.

### O que nao tem

O controle pelo Galaxy Watch. Navegador nao pode funcionar como periferico Bluetooth, e no iOS nem existe Web Bluetooth. Nesta versao o comando e o toque na tela ou um controle Bluetooth pareado ao aparelho, que o sistema enxerga como teclado.

### Como publicar

1. Envie a pasta docs junto com o resto do projeto
2. No GitHub, va em Settings, Pages
3. Em Source escolha Deploy from a branch
4. Selecione a branch main e a pasta /docs, e salve

O endereco fica em https://SEU_USUARIO.github.io/pickleball/

### Como usar num iPad

Abra o endereco no Safari, toque no botao de compartilhar e escolha Adicionar a Tela de Inicio. Isso e importante por dois motivos: ele passa a abrir em tela cheia, sem barra do navegador, e o Safari deixa de apagar os dados por inatividade, o que acontece com sites comuns depois de algumas semanas.

Depois de aberto uma vez, funciona sem internet.

### Cuidados

- A voz no iPhone e no iPad so comeca depois do primeiro toque na tela, por exigencia do sistema
- Use o backup com frequencia. Os dados vivem no navegador daquele aparelho
- O backup da versao web e um arquivo JSON, e nao e o mesmo formato do backup do aplicativo Android

## Relatorio da temporada, na versao 1.0

Engrenagem, Relatorio da temporada. O aplicativo monta um relatorio visual com todas as partidas salvas e abre na propria tela. O botao compartilhar entrega o arquivo HTML para WhatsApp, e-mail ou nuvem, e ele abre em qualquer navegador sem depender do aplicativo.

O relatorio sai em ingles ou portugues, acompanhando o idioma da tela. Tem classificacao, saldo de pontos, confrontos diretos, duplas, partidas por noite, tabela completa e destaques. Os graficos sao SVG estatico, entao funcionam offline e no e-mail.

### Quem entra na classificacao

Um jogador precisa de 15 partidas para ser classificado. Abaixo disso aparece como visitante, com um ponto no lugar da posicao e o nome esmaecido.

Sem esse corte, quem apareceu numa unica noite e ganhou tres jogos ficava na frente de quem joga toda semana, e a classificacao deixava de significar o que o grupo espera dela. No filtro de hoje o minimo cai para 2, porque ali o recorte e a propria noite.

### Partida valida

O relatorio e o ranking consideram apenas partidas que descrevem um jogo real: dois contra dois, ninguem repetido nos dois lados, e vencedor com 11 ou mais e dois de vantagem.

Isso substituiu a marcacao `completa`, que nao servia como criterio. Ao comecar um game novo logo apos o anterior terminar, o aplicativo regravava o arquivo da partida encerrada por cima, com uma copia ainda sem fim nem duracao. Das 135 partidas do historico, apenas 18 continuavam marcadas como completas, e o ranking da tela estava contando so essas. As duas pontas foram corrigidas: a regravacao nao acontece mais, e a estatistica nao depende daquela marca.

### Escalacao invalida

A tela de montar duplas agora remove da lista quem ja esta em outro lugar da quadra. Era por ali que entravam as partidas com a mesma pessoa nos dois lados.

## Acabamento visual da versao 1.0

- Botoes de toda a interface ganharam moldura arredondada e fundo fraco na propria cor. Antes eram texto puro, sem indicacao de onde terminava a area de toque
- O menu, que era uma coluna unica de dezessete itens, virou quatro colunas por assunto: Jogo, Pessoas, Dados e Ajustes. Idioma e side out mostram o valor atual a direita, em vez de esconde-lo dentro do nome
- No placar, o lado de quem saca ganhou preenchimento fraco alem da moldura. A espessura da borda sozinha nao se le a dez metros
- Ponto de jogo pinta esse lado de ambar em vez de verde. Como so a dupla que saca pode pontuar, o ponto de jogo e sempre dela. O rodape continua escrevendo ponto de jogo, entao a informacao nao depende da cor
- As linhas do ranking viraram cartoes, que juntam o nome da esquerda com os numeros da direita. Em tela larga eles flutuavam soltos nas duas pontas
- Cabecalho das telas com regra fina abaixo do titulo

## Placar na quadra, na versao 1.1

O tablet fica na mesa e o grupo esta na quadra, a ate 9 metros. Dessa distancia, so se le o que tem varios centimetros de altura: os numeros e as cores. O nome do sacador e o desenho da quadra da versao anterior tinham menos de 1 cm e viravam borrao. A versao 1.1 redesenha o placar em torno disso. O desenho foi testado antes num prototipo de navegador, o Placar na Quadra, com simulacao de distancia.

### O que aparece

- A faixa de baixo saiu. Cada metade da tela e o lado de uma dupla, e cada jogador aparece na quadra de saque em que esta
- Quem saca: foto grande, anel verde com brilho e o numero 1 ou 2 em cima da foto
- Quem recebe: anel branco, sempre na diagonal de quem saca
- A cada ponto, a bola voa de quem saca ate quem recebe e volta a ficar com quem saca. Uma linha pontilhada marca a diagonal
- Ponto de jogo pinta a dupla que saca de ambar, com GAME POINT escrito no alto
- No fim do game, os vencedores ficam com anel verde e GAME no alto
- Os numeros usam fonte condensada, mais alta no mesmo espaco. Com dois digitos, encolhem so o necessario para caber

Jogador sem foto continua aparecendo como circulo colorido com as iniciais. A paleta desses circulos deixou de usar o verde e o ambar, que sao as cores de destaque: um circulo verde sumia dentro do proprio anel verde. Um respiro preto separa o anel da foto, para o anel aparecer sobre qualquer imagem.

### Quem saca, e como consertar

O aplicativo sabe quem saca porque sabe quem comecou o jogo na quadra da direita. A tela de novo jogo agora diz isso com todas as letras: a vaga de cima e quem comeca na direita, e um botao troca os dois de lugar.

Se no meio do jogo a pessoa errada aparecer sacando, segure o dedo na foto dela por meio segundo. Os dois parceiros trocam de lugar, o placar fica igual, e a voz anuncia quem saca.

### Correcao do placar

A correcao guardava as posicoes dos jogadores de antes dela. Se a correcao mudava o placar de uma dupla ou a dupla que saca, essas posicoes deixavam de bater com o jogo, e o aplicativo passava a apontar o sacador errado e o lado errado do saque.

Agora as posicoes vem do proprio placar. Um parceiro so troca de lado quando a dupla dele pontua sacando, entao quem comecou na direita esta na direita sempre que o placar da dupla e par. O que o placar nao diz e quem esta com a bola, e a tela de correcao passou a perguntar isso.

Inverter lados depois de uma correcao tambem espelha as posicoes, e nao so o placar.

As duas coisas foram testadas em 3.000 jogos sorteados, no nucleo do Android e na versao web: a posicao bate com a paridade em todos os estados, corrigir em qualquer ponto reproduz o jogo exato, continuar da correcao da o mesmo resultado que continuar pelo historico, e inverter funciona nos dois casos.

### Voz

- Desfazer fala o placar que voltou a valer: "correction, 5, 4, 2". Se o ponto desfeito ainda nao tinha sido falado, nada e dito
- No fim do game: "Game. Mike and Sarah win, 11 to 7", ou "Vitoria de Mike e Sarah, 11 a 7" em portugues
- Correcao manual e troca de parceiros falam o placar e quem saca

### Outras mudancas

- Brilho no maximo enquanto o placar esta na frente. Ao sair do aplicativo, volta ao normal
- Gravacao segura: cada arquivo e gravado num temporario e so depois troca de nome. Antes, se o tablet desligasse no meio da gravacao da lista de jogadores, a lista podia ficar pela metade e a gravacao seguinte salvava uma lista vazia por cima. Os cadastros tambem guardam a versao anterior em .bak, usada se o principal nao abrir
- Acoes do GitHub em Node 24: checkout v5, setup-java v5, upload-artifact v6 e setup-gradle v5. O setup-gradle ficou na v5 de proposito, porque a v6 muda os termos de uso do cache da Gradle

### Versao web

Mesmo placar, mesma correcao, mesma troca de parceiros com o dedo segurado na foto e mesma voz. No iPad, que e mais quadrado que um tablet Android, o placar cresce na altura em vez de deixar faixas pretas. O cache do aplicativo web mudou de versao, entao quem ja tem a pagina instalada recebe a nova na proxima abertura com internet.

### O relogio

Nada mudou no relogio nesta versao. Nao precisa reinstalar.
