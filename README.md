# Placar Pickleball

Sistema de placar para pickleball em duplas tradicional, composto por dois aplicativos Android que conversam pela rede local.

Etapa 0 do plano de entrega. O objetivo desta etapa e provar que a cadeia inteira funciona: compilar no GitHub, instalar nos dois aparelhos, o relogio achar o tablet sozinho e o placar responder.

## O que ja funciona nesta etapa

- Regras completas de duplas tradicional, incluindo inicio em 0-0-2, troca de sacador, troca de saque e vitoria por 11 com vantagem de 2
- Tablet exibindo os dois placares em numeros grandes, com moldura indicando quem saca, numero do sacador, lado do saque e a chamada 4-2-1
- Comando pelo relogio, com vibracao imediata
- Comando por toque nas metades da tela do tablet
- Desfazer nos dois aparelhos
- Descoberta automatica do tablet pelo relogio, sem digitar endereco
- Reconexao automatica e silenciosa
- Protecao contra toque duplicado e contra evento reenviado

## O que ainda nao existe

Voz, controle Bluetooth, cadastro de jogadores, relatorio, tela de status, modo de teste e travamento da tela do tablet. Tudo isso entra nas etapas seguintes.

## Estrutura

```
core/         regras do jogo em Kotlin puro, usadas pelos dois aplicativos
app-tablet/   servidor, placar visual e banco de dados no futuro
app-watch/    controle remoto no Galaxy Watch Ultra
```

O modulo `core` nao depende de Android. Tablet e relogio usam exatamente o mesmo codigo de regras, entao os dois nunca discordam sobre o placar.

## Arquitetura

O tablet e a unica fonte de verdade. Qualquer entrada, venha do relogio, do toque na tela ou futuramente do controle Bluetooth, produz o mesmo evento:

```
Evento(id, vencedor, origem, ts)
```

O estado nunca e editado. Ele e sempre recalculado a partir da lista de eventos. Desfazer e apenas remover o ultimo evento e recalcular. Essa escolha e o que torna o desfazer confiavel mesmo quando o ultimo ponto causou troca de saque.

## Como gerar os APKs

1. Crie um repositorio no GitHub e envie estes arquivos
2. Abra a aba Actions e rode o fluxo `build`, ou apenas envie um commit para a branch `main`
3. Ao terminar, baixe os artefatos `apk-tablet` e `apk-relogio`

Nao e necessario Android Studio nem Gradle instalado na sua maquina. Os APKs saem assinados em modo debug, o que dispensa criar chave de assinatura.

## Como instalar

O do tablet e um toque no arquivo. O do relogio exige adb, uma unica vez. O passo a passo esta em INSTALACAO.md.

## Limitacoes conhecidas desta etapa

- Se a conexao cair exatamente durante o envio, aquele ponto pode se perder. A fila persistente entra na etapa 1
- A tela do tablet nao tem travamento contra toque acidental
- O primeiro saque e sempre da esquerda, sem opcao de escolher
- O placar nao e gravado, entao reiniciar o aplicativo zera o jogo
