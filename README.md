# Placar Pickleball

Sistema de placar para pickleball em duplas tradicional, composto por dois aplicativos Android que conversam pela rede local.

Etapas 1, 2 e 4 entregues.

## O que funciona

Regras
- Duplas tradicional, inicio em 0-0-2, troca de sacador, troca de saque
- Vitoria por 11 com vantagem de 2
- Lado do saque derivado da pontuacao
- Destaque de ponto de jogo

Tablet
- Placar em numeros grandes, com moldura indicando quem saca
- Nome das duplas na tela quando os jogadores estao definidos
- Voz em ingles e portugues, com troca em um toque
- Idioma da tela separado do idioma da voz
- Novo game com confirmacao e escolha de quem saca primeiro
- Inverter lados, que espelha tambem os nomes das duplas
- Travamento contra toque acidental
- Repetir a ultima chamada tocando no placar do rodape

Jogadores
- Cadastro com nome, nome curto, e-mail, sexo e foto
- Foto opcional, escolhida da galeria sem exigir permissao
- Quem nao tem foto aparece com circulo colorido e iniciais
- Sexo e apenas informativo por enquanto

Partidas
- Toda partida encerrada e salva automaticamente
- Partida abandonada ao comecar outro game e salva como incompleta
- Recuperacao apos reinicio: o placar volta exatamente onde parou
- Historico com data, duplas e placar, e detalhe com estatisticas

Estatisticas
- Total de rallies e rallies ganhos por dupla
- Rallies ganhos recebendo, que e o que realmente diferencia no formato tradicional
- Turnos de saque encerrados sem pontuar
- Maior sequencia de pontos consecutivos
- Viradas no placar

E-mail
- Resumo pronto com placar, duracao e estatisticas
- Abre o aplicativo de e-mail preenchido, com os participantes em copia
- Nada e enviado sozinho: voce confere e toca em enviar
- Sem senha, sem servidor e sem autenticacao para manter

Relogio
- Dois botoes grandes e desfazer
- Vibracao no toque e vibracao dupla quando o tablet confirma
- Fila de pontos, descartados apenas apos confirmacao
- Contador de pontos aguardando envio

Conexao
- Tres caminhos em cascata: endereco salvo, anuncio na rede e varredura da faixa
- Reconexao automatica e reenvio seguro

## Onde ficam as coisas

Tudo fica atras da engrenagem no canto inferior direito do tablet: novo game, inverter lados, definir as duplas, jogadores, historico e idiomas.

O aplicativo continua abrindo pronto para jogar. Cadastro nunca e obrigatorio.

## Armazenamento

Arquivos JSON dentro do proprio aplicativo, sem banco de dados com processamento de anotacoes. Nesta escala a diferenca de desempenho e nula e o build fica mais simples. As fotos ficam reduzidas a 256 pixels.

Nao existe conta, login, nuvem nem sincronizacao. Os dados sao do grupo e ficam no tablet.

## O que ainda nao existe

Controle Bluetooth, tela de status, modo de teste, rodizio e estatisticas acumuladas por jogador.

## Estrutura

```
core/         regras, protocolo e estatisticas em Kotlin puro
app-tablet/   servidor, placar, voz, cadastro, historico e e-mail
app-watch/    controle remoto no Galaxy Watch Ultra
```

## Como gerar e instalar

Envie para a branch main e o fluxo build publica os artefatos apk-tablet e apk-relogio na aba Actions.

O do tablet e um toque no arquivo. O do relogio:

```
adb connect ENDERECO_DO_RELOGIO
adb install -r app-watch-debug.apk
```

## Limitacoes conhecidas

- A tela do relogio esta apenas em ingles
- Nao ha estatistica acumulada por jogador, apenas por partida
- O e-mail sai em texto simples
