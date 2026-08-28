# Placar Pickleball

Sistema de placar para pickleball em duplas tradicional, composto por dois aplicativos Android que conversam pela rede local.

Etapas 1 e 2 entregues. A versao ja pode ir para a quadra.

## O que funciona

Regras
- Duplas tradicional, inicio em 0-0-2, troca de sacador, troca de saque
- Vitoria por 11 com vantagem de 2
- Lado do saque derivado da pontuacao, par pela direita e impar pela esquerda
- Destaque de ponto de jogo

Tablet
- Placar em numeros grandes, com moldura indicando quem saca
- Numero do sacador e lado do saque
- Chamada 4-2-1 no rodape
- Voz em ingles e portugues, com troca em um toque
- Idioma da tela separado do idioma da voz
- Novo game, com confirmacao e escolha de quem saca primeiro
- Inverter lados
- Travamento contra toque acidental, com toque duplo destravando por 8 segundos
- Repetir a ultima chamada tocando no placar do rodape

Relogio
- Dois botoes grandes e desfazer
- Vibracao no toque e vibracao diferente quando o tablet confirma
- Fila de pontos, que so sao descartados apos confirmacao
- Indicador de quantos pontos aguardam envio

Conexao
- Tres caminhos em cascata: endereco salvo da ultima vez, anuncio na rede local e varredura da faixa de enderecos
- Reconexao automatica e silenciosa
- Reenvio seguro, porque cada evento tem identificador e o tablet ignora repetidos

## O que ainda nao existe

Controle Bluetooth, cadastro de jogadores, relatorio de partidas, tela de status, modo de teste e rodizio.

## Voz

Os termos ficam em ingles nos dois idiomas, porque e o vocabulario que os jogadores usam. Apenas os numeros mudam de idioma.

Em portugues, os termos sao escritos foneticamente no arquivo Voz.kt, para o motor de voz nao pronunciar letra por letra. Se algo soar estranho em quadra, e so ajustar aquelas linhas.

A voz em ingles ja vem instalada na maioria dos tablets. A voz em portugues normalmente precisa ser baixada nas configuracoes do Android, em Conversao de texto em fala. O aplicativo avisa no menu quando a voz do idioma escolhido nao esta disponivel.

## Como usar na quadra

1. Abra o aplicativo no tablet e deixe na mesa
2. Abra o aplicativo no relogio e espere o ponto ficar verde
3. Toque no lado que ganhou o rally
4. Para comecar outro game, use a engrenagem no canto inferior direito do tablet

A tela do tablet nasce travada. Para marcar ponto pela tela, toque duas vezes para destravar. Ela volta a travar sozinha depois de alguns segundos sem uso.

## Estrutura

```
core/         regras do jogo em Kotlin puro, usadas pelos dois aplicativos
app-tablet/   servidor, placar visual e voz
app-watch/    controle remoto no Galaxy Watch Ultra
```

## Como gerar os APKs

Envie os arquivos para a branch main. O fluxo build compila sozinho e publica os artefatos apk-tablet e apk-relogio na aba Actions.

## Como instalar

O do tablet e um toque no arquivo. O do relogio usa adb. O passo a passo esta em INSTALACAO.md. Como o pareamento ja foi feito uma vez, agora basta:

```
adb connect ENDERECO_DO_RELOGIO
adb install -r app-watch-debug.apk
```

## Limitacoes conhecidas

- O placar nao e gravado, entao reiniciar o aplicativo zera o jogo
- Nao ha cadastro de jogadores nem relatorio
- A tela do relogio esta apenas em ingles
