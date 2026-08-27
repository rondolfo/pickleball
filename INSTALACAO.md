# Instalacao

Este arquivo cobre os dois aparelhos. O tablet leva dois minutos. O relogio leva uns quinze na primeira vez e um comando nas vezes seguintes.

---

## Parte 1: tablet

1. Baixe o artefato `apk-tablet` do GitHub Actions e descompacte
2. Copie o arquivo APK para o tablet, por cabo, Google Drive ou e-mail
3. Toque no arquivo no tablet
4. O Android vai pedir permissao para instalar de fonte desconhecida. Autorize apenas dessa vez
5. Abra o aplicativo Placar Pickleball

A tela deve ficar preta com dois zeros grandes. No canto inferior esquerdo aparece um ponto ambar e o endereco de rede do tablet, indicando que ele esta esperando o relogio.

---

## Parte 2: computador

Instale o Platform Tools do Android, que contem o adb.

- Windows e Mac: baixe em `https://developer.android.com/tools/releases/platform-tools`
- Descompacte numa pasta de facil acesso, por exemplo a area de trabalho
- Abra o terminal ou o prompt de comando dentro dessa pasta

Para confirmar que funcionou, rode:

```
adb version
```

Se aparecer um numero de versao, esta pronto.

No Windows, use `adb` como no exemplo. No Mac e Linux, pode ser necessario escrever `./adb`.

---

## Parte 3: relogio

### 3.1 Ativar o modo desenvolvedor

1. No relogio, abra Configuracoes
2. Va em Sobre o relogio, depois Software
3. Toque sete vezes seguidas em Numero da versao
4. Vai aparecer um aviso de que o modo desenvolvedor foi ativado

### 3.2 Ligar a depuracao

1. Volte para Configuracoes
2. Entre em Opcoes do desenvolvedor
3. Ative Depuracao ADB
4. Ative Depurar via Wi-Fi

### 3.3 Colocar o relogio na mesma rede

1. Em Configuracoes, entre em Conexoes e depois Wi-Fi
2. Conecte na mesma rede do computador e do tablet

Importante: o Wear OS tende a desligar o Wi-Fi quando o relogio esta conectado ao celular por Bluetooth. Se o Wi-Fi nao ficar ativo, desligue o Bluetooth do relogio ou deixe o celular longe durante a instalacao.

### 3.4 Descobrir o endereco

Volte em Opcoes do desenvolvedor, Depurar via Wi-Fi. Vai aparecer algo como:

```
192.168.1.42:5555
```

Anote esse valor.

### 3.5 Conectar

No terminal do computador:

```
adb connect 192.168.1.42:5555
```

Troque pelo endereco que apareceu no seu relogio.

O relogio vai mostrar um pedido de autorizacao. Marque Sempre permitir deste computador e confirme.

Confira se conectou:

```
adb devices
```

O relogio deve aparecer na lista com a palavra `device` ao lado.

### 3.6 Instalar

```
adb install -r caminho/para/app-watch-debug.apk
```

Se preferir, arraste o arquivo APK para dentro do terminal depois de escrever `adb install -r ` e um espaco. O caminho e preenchido sozinho.

Ao terminar, vai aparecer `Success`.

### 3.7 Abrir

No relogio, procure o aplicativo chamado Placar na lista de aplicativos.

---

## Se der problema

### O relogio pede codigo de pareamento

Algumas versoes do Wear OS usam pareamento por codigo. Nesse caso, em Opcoes do desenvolvedor procure Parear novo dispositivo, que mostra um endereco diferente e um codigo de seis digitos. Rode:

```
adb pair 192.168.1.42:41234
```

Digite o codigo quando pedido. Depois faca o `adb connect` normalmente, usando o endereco da tela Depurar via Wi-Fi.

### adb devices mostra unauthorized

A autorizacao nao foi aceita no relogio. Rode `adb disconnect`, depois `adb connect` de novo e fique de olho na tela do relogio.

### adb connect nao responde

Confirme que relogio e computador estao na mesma rede. Redes de visitante costumam isolar aparelhos entre si e impedem tanto o adb quanto a comunicacao com o tablet.

### O aplicativo instala mas o ponto ambar nao fica verde

Relogio e tablet precisam estar na mesma rede. Confirme o Wi-Fi do relogio, lembrando do comportamento do Bluetooth descrito no item 3.3.

### INSTALL_FAILED_UPDATE_INCOMPATIBLE

Acontece ao instalar uma versao gerada com assinatura diferente. Desinstale antes:

```
adb uninstall com.kriptobr.placar.watch
```

---

## Nas proximas vezes

Depois da primeira configuracao, atualizar o aplicativo do relogio e apenas:

```
adb connect 192.168.1.42:5555
adb install -r app-watch-debug.apk
```

O endereco pode mudar quando o relogio reconecta na rede. Se falhar, confira o valor atual em Opcoes do desenvolvedor.
