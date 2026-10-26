// Placar Pickleball, versao web. Independente do aplicativo Android:
// mesmas regras, dados proprios, tudo guardado no proprio navegador.

// ---------- textos ----------
const T = {
  en: {
    esquerda:"LEFT", direita:"RIGHT", sacador:"SERVER",
    saque_direita:"SERVE RIGHT", saque_esquerda:"SERVE LEFT",
    fim_game:"GAME OVER", ponto_de_jogo:"GAME POINT", venceu:"WINNER",
    desfazer:"undo", travado:"locked, tap to unlock", destravado:"unlocked, tap to lock",
    menu:"Settings", novo_game:"New game", inverter:"Swap sides",
    substituir:"Substitute a player", corrigir:"Fix the score", rodizio:"Rotation",
    jogadores:"Players", historico:"Match history", ranking:"Leaderboard",
    idioma_tela:"Screen language", side_out:'Say "side out"', backup:"Backup",
    manual:"How to use", fechar:"Close", salvar:"Save", excluir:"Delete",
    limpar:"Clear", cancelar:"Cancel", voltar:"Back",
    iniciar_jogo:"Start a game", quem_saca:"Who serves first?",
    escolher_jogador:"Choose a player", novo_jogador:"Add player",
    nome:"Full name", apelido:"Short name", email:"Email",
    tirar_foto:"Take a photo", escolher_foto:"Choose from gallery",
    quem_sai:"Who is leaving?", quem_entra:"Who is coming in?",
    sem_jogadores:"No players yet. Add the people you play with.",
    sem_partidas:"No matches saved yet.", sem_jogos_hoje:"No games played today.",
    sem_dados:"No finished matches yet.", incompleta:"unfinished",
    resultado:"Result", enviar_email:"Email the summary",
    email_sessao:"Email today's summary", exportar:"Export to CSV",
    sem_email:"No player has an email saved.",
    hoje:"Today", sempre:"All time", jogos:"games", vitorias:"W", derrotas:"L",
    saldo:"diff", duracao:"Duration", minutos:"min", rallies:"Total rallies",
    rallies_ganhos:"Rallies won", rallies_recebendo:"Rallies won while receiving",
    turnos_secos:"Service turns without scoring", maior_sequencia:"Longest point streak",
    viradas:"Lead changes", presentes:"Here today", sugestao:"Suggested next game",
    aceitar:"Use this", sortear:"Shuffle", esperando:"waiting", rodadas:"rounds",
    precisa_quatro:"Mark at least four people as here today.",
    confirmar_novo:"Start a new game? The current score will be lost.",
    aviso_correcao:"Fixing the score restarts the log from here, so undo will not go back past this point.",
    aviso_backup:"The backup holds players, matches and rotation. Save it somewhere safe after you enter everyone. Restoring replaces what is on this device.",
    backup_ok:"Done.", backup_erro:"That file could not be read.",
    pontos_esq:"Left points", pontos_dir:"Right points",
    sacando_agora:"Serving", numero_sacador:"Server number", aplicar:"Apply",
    voz:"Voice", velocidade:"Speed", testar:"Test", voz_padrao:"System default",
    sugerida:"suggested", sem_vozes:"No voice installed for this language on this device."
  },
  pt: {
    esquerda:"ESQUERDA", direita:"DIREITA", sacador:"SACADOR",
    saque_direita:"SAQUE PELA DIREITA", saque_esquerda:"SAQUE PELA ESQUERDA",
    fim_game:"FIM DE GAME", ponto_de_jogo:"PONTO DE JOGO", venceu:"VENCEU",
    desfazer:"desfazer", travado:"travado, toque para destravar",
    destravado:"destravado, toque para travar",
    menu:"Ajustes", novo_game:"Novo game", inverter:"Inverter lados",
    substituir:"Substituir jogador", corrigir:"Corrigir o placar", rodizio:"Rodizio",
    jogadores:"Jogadores", historico:"Historico de partidas", ranking:"Ranking",
    idioma_tela:"Idioma da tela", side_out:'Falar "side out"', backup:"Backup",
    manual:"Como usar", fechar:"Fechar", salvar:"Salvar", excluir:"Excluir",
    limpar:"Limpar", cancelar:"Cancelar", voltar:"Voltar",
    iniciar_jogo:"Iniciar um jogo", quem_saca:"Quem saca primeiro?",
    escolher_jogador:"Escolher jogador", novo_jogador:"Novo jogador",
    nome:"Nome completo", apelido:"Nome curto", email:"E-mail",
    tirar_foto:"Tirar foto", escolher_foto:"Escolher da galeria",
    quem_sai:"Quem esta saindo?", quem_entra:"Quem entra no lugar?",
    sem_jogadores:"Nenhum jogador cadastrado. Cadastre quem joga com voce.",
    sem_partidas:"Nenhuma partida salva ainda.", sem_jogos_hoje:"Nenhum jogo hoje ainda.",
    sem_dados:"Nenhuma partida encerrada ainda.", incompleta:"incompleta",
    resultado:"Resultado", enviar_email:"Enviar resumo por e-mail",
    email_sessao:"Enviar o resumo de hoje", exportar:"Exportar em CSV",
    sem_email:"Nenhum jogador tem e-mail cadastrado.",
    hoje:"Hoje", sempre:"Sempre", jogos:"jogos", vitorias:"v", derrotas:"d",
    saldo:"saldo", duracao:"Duracao", minutos:"min", rallies:"Total de rallies",
    rallies_ganhos:"Rallies ganhos", rallies_recebendo:"Rallies ganhos recebendo",
    turnos_secos:"Turnos de saque sem pontuar", maior_sequencia:"Maior sequencia de pontos",
    viradas:"Viradas no placar", presentes:"Presentes hoje", sugestao:"Proximo jogo sugerido",
    aceitar:"Usar esta", sortear:"Sortear", esperando:"esperando", rodadas:"rodadas",
    precisa_quatro:"Marque pelo menos quatro pessoas como presentes.",
    confirmar_novo:"Comecar um novo game? O placar atual sera perdido.",
    aviso_correcao:"Corrigir o placar reinicia o log a partir daqui, entao o desfazer nao volta alem deste ponto.",
    aviso_backup:"O backup guarda jogadores, partidas e rodizio. Guarde num lugar seguro depois de cadastrar todo mundo. Restaurar substitui o que esta neste aparelho.",
    backup_ok:"Pronto.", backup_erro:"Nao foi possivel ler esse arquivo.",
    pontos_esq:"Pontos da esquerda", pontos_dir:"Pontos da direita",
    sacando_agora:"Sacando", numero_sacador:"Numero do sacador", aplicar:"Aplicar",
    voz:"Voz", velocidade:"Velocidade", testar:"Testar", voz_padrao:"Padrao do sistema",
    sugerida:"sugerida", sem_vozes:"Nenhuma voz deste idioma instalada neste aparelho."
  }
};
const t = (k) => (T[P.idiomaUi] && T[P.idiomaUi][k]) || T.en[k] || k;

// ---------- armazenamento ----------
const CHAVES = { jogadores:"pb_jogadores", partidas:"pb_partidas",
                 atual:"pb_atual", rodizio:"pb_rodizio", prefs:"pb_prefs" };

const ler = (chave, padrao) => {
  try { const v = localStorage.getItem(chave); return v ? JSON.parse(v) : padrao; }
  catch (e) { return padrao; }
};
const gravar = (chave, valor) => {
  try { localStorage.setItem(chave, JSON.stringify(valor)); }
  catch (e) { console.warn("armazenamento cheio", e); }
};

let jogadores = ler(CHAVES.jogadores, []);
let partidas  = ler(CHAVES.partidas, []);
let rodizio   = ler(CHAVES.rodizio, { rodada:0, ultima:{}, jogos:{}, parceiro:{}, adversarios:{}, presentes:[] });
let P         = ler(CHAVES.prefs, { idiomaUi:"en", idiomaVoz:"en", sideOut:false, travado:false });
if (P.velocidade === undefined) P.velocidade = 1.15;
if (P.vozEn === undefined) P.vozEn = null;
if (P.vozPt === undefined) P.vozPt = null;

const partidaVazia = () => ({
  id: String(Date.now()) + Math.random().toString(16).slice(2),
  inicio: Date.now(), fim: null, primeiroSaque: ESQ,
  duplaEsq: [null, null], duplaDir: [null, null],
  escalacoes: [], eventos: [], base: null, completa: false
});
let atual = ler(CHAVES.atual, null) || partidaVazia();

const salvarAtual   = () => gravar(CHAVES.atual, atual);
const salvarTudo    = () => { gravar(CHAVES.jogadores, jogadores); gravar(CHAVES.partidas, partidas);
                              gravar(CHAVES.rodizio, rodizio); gravar(CHAVES.prefs, P); salvarAtual(); };

// ---------- estado do jogo ----------
const baseAtual = () => atual.base || estadoInicial(atual.primeiroSaque);
const estado    = () => derivarDe(atual.eventos, baseAtual());
const stats     = (p) => analisar(p.eventos, p.base || estadoInicial(p.primeiroSaque));

const acharJogador = (id) => jogadores.find((j) => j.id === id) || null;
const curto = (id) => {
  const j = acharJogador(id);
  if (!j) return null;
  return (j.apelido && j.apelido.trim()) || j.nome.trim().split(" ")[0];
};
const nomeDupla = (dupla) => {
  const nomes = dupla.map(curto).filter(Boolean);
  return nomes.length ? nomes.join(" & ") : null;
};
const nomeDoLado = (lado) => {
  const d = lado === ESQ ? atual.duplaEsq : atual.duplaDir;
  return nomeDupla(d) || t(lado === ESQ ? "esquerda" : "direita");
};
const nomeDoSacador = (e) => {
  const d = e.sacando === ESQ ? atual.duplaEsq : atual.duplaDir;
  return curto(d[e.indiceSacador]) || t(e.sacando === ESQ ? "esquerda" : "direita");
};

// ---------- voz ----------
let vozPendente = null, ultimaFala = "";
const vozLiberada = { ok:false };

function liberarVoz() {
  // o iOS so permite falar depois de um toque do usuario
  if (vozLiberada.ok || !window.speechSynthesis) return;
  try {
    const u = new SpeechSynthesisUtterance(" ");
    u.volume = 0; speechSynthesis.speak(u); vozLiberada.ok = true;
  } catch (e) {}
}

// A API do navegador nao informa o sexo da voz, entao a unica forma honesta
// e listar o que existe no aparelho e deixar pre-selecionada uma das
// conhecidas como femininas em cada idioma.
const VOZES_SUGERIDAS = {
  en: ["Samantha","Karen","Moira","Tessa","Victoria","Allison","Ava","Susan","Zoe",
       "Serena","Fiona","Google US English","Microsoft Aria","Microsoft Zira","Microsoft Jenny"],
  pt: ["Luciana","Joana","Fernanda","Google portugues do Brasil",
       "Google português do Brasil","Microsoft Maria","Microsoft Francisca"]
};

function vozesDoIdioma(idioma) {
  if (!window.speechSynthesis) return [];
  const prefixo = idioma === "pt" ? "pt" : "en";
  return speechSynthesis.getVoices().filter((v) => (v.lang || "").toLowerCase().startsWith(prefixo));
}

const ehSugerida = (nome, idioma) =>
  (VOZES_SUGERIDAS[idioma] || []).some((s) => (nome || "").toLowerCase().includes(s.toLowerCase()));

function vozEscolhida(idioma) {
  const lista = vozesDoIdioma(idioma);
  if (!lista.length) return null;
  const salva = idioma === "pt" ? P.vozPt : P.vozEn;
  if (salva) {
    const achada = lista.find((v) => v.name === salva);
    if (achada) return achada;
  }
  return lista.find((v) => ehSugerida(v.name, idioma)) || null;
}

function falarAgora(texto) {
  if (!window.speechSynthesis || !texto) return;
  try {
    speechSynthesis.cancel();
    const u = new SpeechSynthesisUtterance(texto);
    u.lang = P.idiomaVoz === "pt" ? "pt-BR" : "en-US";
    u.rate = P.velocidade || 1.15;
    const escolhida = vozEscolhida(P.idiomaVoz);
    if (escolhida) u.voice = escolhida;
    speechSynthesis.speak(u);
  } catch (e) {}
}

// no iOS a lista de vozes chega depois, entao vale redesenhar quando chegar
if (window.speechSynthesis) {
  speechSynthesis.onvoiceschanged = () => {
    if ($("telaVoz") && $("telaVoz").classList.contains("aberta")) desenharVozes();
  };
}

// Termos ficam em ingles nos dois idiomas, porque e o que os jogadores usam.
// Em portugues sao escritos foneticamente para o motor nao soletrar.
const termo = (k) => {
  if (P.idiomaVoz === "pt") {
    return { side_out:"sáid aut", game_point:"guêimi point", game:"guêimi" }[k] || k;
  }
  return { side_out:"side out", game_point:"game point", game:"game" }[k] || k;
};

function anunciar(anterior, novo) {
  const partes = [];
  if (novo.encerrado) {
    partes.push(termo("game"));
    partes.push(`${pontosSacador(novo)}, ${pontosRecebedor(novo)}, ${novo.sacador}`);
  } else {
    if (novo.sacando !== anterior.sacando && P.sideOut) partes.push(termo("side_out"));
    partes.push(`${pontosSacador(novo)}, ${pontosRecebedor(novo)}, ${novo.sacador}`);
    const trocou = novo.sacando !== anterior.sacando ||
                   novo.indiceSacador !== anterior.indiceSacador;
    if (trocou) {
      const nome = nomeDoSacador(novo);
      partes.push(P.idiomaVoz === "pt" ? `saque de ${nome}` : `${nome} to serve`);
    }
    if (pontoDeJogo(novo)) partes.push(termo("game_point"));
  }
  const texto = partes.join(", ");
  ultimaFala = texto;
  clearTimeout(vozPendente);
  // atraso curto: se o ponto for desfeito, o placar errado nunca e falado
  vozPendente = setTimeout(() => falarAgora(texto), 800);
}

// ---------- desenho da tela ----------
const $ = (id) => document.getElementById(id);

function desenhar() {
  const e = estado();

  $("nomeEsq").textContent = nomeDoLado(ESQ);
  $("nomeDir").textContent = nomeDoLado(DIR);
  $("ptsEsq").textContent = e.pontosEsquerda;
  $("ptsDir").textContent = e.pontosDireita;

  $("ladoEsq").className = "lado" + (e.sacando === ESQ && !e.encerrado ? " saca" : "") +
    (e.vencedor === ESQ ? " venceu" : "");
  $("ladoDir").className = "lado" + (e.sacando === DIR && !e.encerrado ? " saca" : "") +
    (e.vencedor === DIR ? " venceu" : "");

  if (e.encerrado) {
    $("sacadorNome").textContent = t("fim_game");
    $("sacadorDetalhe").textContent = "";
  } else {
    $("sacadorNome").textContent = nomeDoSacador(e).toUpperCase();
    $("sacadorDetalhe").textContent =
      t(ladoDoSaque(e) === "direita" ? "saque_direita" : "saque_esquerda") +
      "  .  " + t("sacador") + " " + e.sacador;
  }

  desenharQuadra(e);

  const chamadaTexto = `${pontosSacador(e)} . ${pontosRecebedor(e)} . ${e.sacador}`;
  $("chamada").textContent = pontoDeJogo(e)
    ? chamadaTexto + "   " + t("ponto_de_jogo") : chamadaTexto;
  $("chamada").className = "chamada" + (pontoDeJogo(e) ? " jogo" : "");

  $("btIdiomaVoz").textContent = P.idiomaVoz.toUpperCase();
  $("btDesfazer").textContent = t("desfazer");
  $("estadoTrava").textContent = P.travado ? t("travado") : t("destravado");
  $("pontinho").className = "pontinho" + (P.travado ? "" : " on");
  document.body.classList.toggle("travado", P.travado);
}

// Cada dupla encara a rede em sentido contrario, entao a quadra da direita
// de cada uma fica em pontas opostas. Por isso sacador e recebedor caem na diagonal.
function desenharQuadra(e) {
  const nomeEm = (dupla, indice, alt) => curto(dupla[indice]) || alt;
  const dEsq = e.naDireitaEsquerda, dDir = e.naDireitaDireita;

  const alvo = {
    qEsqDir: nomeEm(atual.duplaEsq, dEsq, "1"),
    qEsqEsq: nomeEm(atual.duplaEsq, 1 - dEsq, "2"),
    qDirDir: nomeEm(atual.duplaDir, dDir, "1"),
    qDirEsq: nomeEm(atual.duplaDir, 1 - dDir, "2")
  };

  const naDireitaPropria = e.indiceSacador === naDireitaDe(e, e.sacando);
  const quemSaca = e.sacando === ESQ
    ? (naDireitaPropria ? "qEsqDir" : "qEsqEsq")
    : (naDireitaPropria ? "qDirDir" : "qDirEsq");
  const diagonal = { qEsqDir:"qDirDir", qEsqEsq:"qDirEsq", qDirDir:"qEsqDir", qDirEsq:"qEsqEsq" };
  const quemRecebe = diagonal[quemSaca];

  for (const id of ["qEsqEsq","qEsqDir","qDirDir","qDirEsq"]) {
    const el = $(id);
    el.textContent = alvo[id];
    el.className = "quadrante" +
      (!e.encerrado && id === quemSaca ? " saca" : "") +
      (!e.encerrado && id === quemRecebe ? " recebe" : "");
  }
}

// ---------- acoes do jogo ----------
function marcarPonto(lado) {
  liberarVoz();
  const anterior = estado();
  if (anterior.encerrado) return;

  const agora = Date.now();
  const ultimo = atual.eventos[atual.eventos.length - 1];
  if (ultimo && agora - ultimo.ts < 1000) return; // toque acidental

  atual.eventos.push({ id: String(agora), vencedor: lado, ts: agora });
  const novo = estado();
  anunciar(anterior, novo);
  salvarAtual();
  desenhar();
  if (novo.encerrado) encerrarPartida();
}

function desfazer() {
  if (!atual.eventos.length) return;
  clearTimeout(vozPendente);
  atual.eventos.pop();
  atual.completa = false;
  salvarAtual();
  desenhar();
}

function encerrarPartida() {
  atual.fim = Date.now();
  atual.completa = true;
  partidas.push(JSON.parse(JSON.stringify(atual)));
  gravar(CHAVES.partidas, partidas);
  mostrarResultado(atual, true);
}

function novoGame(saqueInicial) {
  if (atual.eventos.length && !atual.completa) {
    partidas.push(JSON.parse(JSON.stringify(atual)));
    gravar(CHAVES.partidas, partidas);
  }
  clearTimeout(vozPendente);
  const duplaEsq = atual.duplaEsq.slice();
  const duplaDir = atual.duplaDir.slice();
  atual = partidaVazia();
  atual.primeiroSaque = saqueInicial;
  atual.duplaEsq = duplaEsq;
  atual.duplaDir = duplaDir;
  atual.escalacoes = [{ ts: Date.now(), esq: duplaEsq.slice(), dir: duplaDir.slice() }];
  salvarAtual();
  desenhar();
}

// Espelha o log inteiro: placar, saque e posicoes acompanham a troca de ponta.
function inverterLados() {
  atual.eventos = atual.eventos.map((ev) => ({ ...ev, vencedor: oposto(ev.vencedor) }));
  atual.primeiroSaque = oposto(atual.primeiroSaque);
  if (atual.base) {
    const b = atual.base;
    atual.base = { ...b,
      pontosEsquerda: b.pontosDireita, pontosDireita: b.pontosEsquerda,
      sacando: oposto(b.sacando),
      naDireitaEsquerda: b.naDireitaDireita, naDireitaDireita: b.naDireitaEsquerda };
  }
  const guarda = atual.duplaEsq; atual.duplaEsq = atual.duplaDir; atual.duplaDir = guarda;
  atual.escalacoes.push({ ts: Date.now(), esq: atual.duplaEsq.slice(), dir: atual.duplaDir.slice() });
  salvarAtual();
  desenhar();
}

// ---------- telas ----------
const abrir  = (id) => $(id).classList.add("aberta");
const fechar = (id) => $(id).classList.remove("aberta");
document.querySelectorAll("[data-fechar]").forEach((b) =>
  b.addEventListener("click", () => fechar(b.dataset.fechar)));

function avatarHtml(j, tamanho) {
  const cores = ["#97C459","#EF9F27","#6BA6D6","#D07A9E","#9B8ADB","#4FB3A5","#D4694A","#8FA33E"];
  const iniciais = j.nome.trim().split(" ").filter(Boolean).slice(0,2)
    .map((p) => p[0].toUpperCase()).join("") || "?";
  let soma = 0; for (const c of j.id) soma = (soma * 31 + c.charCodeAt(0)) % 9973;
  const estilo = j.foto
    ? `background-image:url(${j.foto})`
    : `background:${cores[soma % cores.length]}`;
  const dim = tamanho ? `width:${tamanho}px;height:${tamanho}px;font-size:${Math.round(tamanho*0.36)}px;` : "";
  return `<div class="av" style="${dim}${estilo}">${j.foto ? "" : iniciais}</div>`;
}

function traduzirTelas() {
  const mapa = {
    tMenu:"menu", tNovoJogo:"iniciar_jogo", tJogadores:"jogadores", tSeletor:"escolher_jogador",
    tSubstituir:"quem_sai", tCorrigir:"corrigir", tRodizio:"rodizio", tHistorico:"historico",
    tRanking:"ranking", tBackup:"backup", tManual:"manual", tResultado:"resultado",
    tQuemSaca:"quem_saca", tTirarFoto:"tirar_foto", tEscolherFoto:"escolher_foto",
    mNovoJogo:"novo_game", mInverter:"inverter", mSubstituir:"substituir", mCorrigir:"corrigir",
    mRodizio:"rodizio", mJogadores:"jogadores", mHistorico:"historico", mRanking:"ranking",
    mIdiomaTela:"idioma_tela", mBackup:"backup", mManual:"manual", mVoz:"voz",
    btNovoJogador:"novo_jogador", btSalvarJogador:"salvar", btExcluirJogador:"excluir",
    btLimparSlot:"limpar", btAplicarCorrecao:"aplicar", btHoje:"hoje", btSempre:"sempre",
    btEmailSessao:"email_sessao", btCsv:"exportar", btEmailPartida:"enviar_email",
    btNovoAposGame:"novo_game", tImportarBackup:"backup", tAvisoCorrecao:"aviso_correcao",
    tAvisoBackup:"aviso_backup"
  };
  for (const [id, chave] of Object.entries(mapa)) { const el = $(id); if (el) el.textContent = t(chave); }
  $("mSideOut").textContent = t("side_out") + "   " + (P.sideOut ? "ON" : "OFF");
  $("mIdiomaTela").textContent = t("idioma_tela") + "   " + P.idiomaUi.toUpperCase();
  $("comecarEsq").textContent = t("esquerda");
  $("comecarDir").textContent = t("direita");
  $("campoNome").placeholder = t("nome");
  $("campoApelido").placeholder = t("apelido");
  $("campoEmail").placeholder = t("email");
  document.querySelectorAll("[data-fechar]").forEach((b) => b.textContent = t("fechar"));
  document.documentElement.lang = P.idiomaUi;
}

// ---------- jogadores ----------
let editando = null, slotAlvo = null;

function listarJogadores() {
  const alvo = $("listaJogadores");
  if (!jogadores.length) { alvo.innerHTML = `<p class="vazio">${t("sem_jogadores")}</p>`; return; }
  alvo.innerHTML = jogadores.map((j) => `
    <button class="linha" data-jogador="${j.id}">
      ${avatarHtml(j)}
      <span><div>${j.nome}</div>${j.email ? `<div class="mini">${j.email}</div>` : ""}</span>
    </button>`).join("");
  alvo.querySelectorAll("[data-jogador]").forEach((b) =>
    b.addEventListener("click", () => abrirFormulario(acharJogador(b.dataset.jogador))));
}

function abrirFormulario(j) {
  editando = j ? { ...j } : { id: "j" + Date.now(), nome:"", apelido:"", email:"", foto:null };
  $("campoNome").value = editando.nome;
  $("campoApelido").value = editando.apelido;
  $("campoEmail").value = editando.email;
  $("avatarForm").outerHTML = avatarHtml(editando, 70).replace('class="av"', 'class="av" id="avatarForm"');
  $("btExcluirJogador").style.display = j ? "" : "none";
  abrir("telaFormulario");
}

function reduzirFoto(arquivo, aoTerminar) {
  const leitor = new FileReader();
  leitor.onload = () => {
    const img = new Image();
    img.onload = () => {
      const maior = Math.max(img.width, img.height) || 1;
      const escala = Math.min(1, 256 / maior);
      const c = document.createElement("canvas");
      c.width = Math.max(1, Math.round(img.width * escala));
      c.height = Math.max(1, Math.round(img.height * escala));
      c.getContext("2d").drawImage(img, 0, 0, c.width, c.height);
      aoTerminar(c.toDataURL("image/jpeg", 0.85));
    };
    img.src = leitor.result;
  };
  leitor.readAsDataURL(arquivo);
}

function ligarFoto(idInput) {
  $(idInput).addEventListener("change", (ev) => {
    const arquivo = ev.target.files && ev.target.files[0];
    if (!arquivo || !editando) return;
    reduzirFoto(arquivo, (dados) => {
      editando.foto = dados;
      $("avatarForm").outerHTML = avatarHtml(editando, 70).replace('class="av"', 'class="av" id="avatarForm"');
    });
    ev.target.value = "";
  });
}
ligarFoto("fotoCamera"); ligarFoto("fotoGaleria");

$("btSalvarJogador").addEventListener("click", () => {
  if (!editando || !$("campoNome").value.trim()) return;
  editando.nome = $("campoNome").value.trim();
  editando.apelido = $("campoApelido").value.trim();
  editando.email = $("campoEmail").value.trim();
  const i = jogadores.findIndex((j) => j.id === editando.id);
  if (i >= 0) jogadores[i] = editando; else jogadores.push(editando);
  jogadores.sort((a, b) => a.nome.toLowerCase().localeCompare(b.nome.toLowerCase()));
  gravar(CHAVES.jogadores, jogadores);
  fechar("telaFormulario"); listarJogadores(); desenhar();
});

$("btExcluirJogador").addEventListener("click", () => {
  if (!editando) return;
  jogadores = jogadores.filter((j) => j.id !== editando.id);
  rodizio.presentes = rodizio.presentes.filter((id) => id !== editando.id);
  [atual.duplaEsq, atual.duplaDir].forEach((d) => {
    for (let i = 0; i < 2; i++) if (d[i] === editando.id) d[i] = null;
  });
  gravar(CHAVES.jogadores, jogadores); gravar(CHAVES.rodizio, rodizio); salvarAtual();
  fechar("telaFormulario"); listarJogadores(); desenhar();
});

// ---------- selecao de jogadores ----------
function abrirSeletor(lado, posicao, excluir, aoEscolher) {
  slotAlvo = { lado, posicao, aoEscolher };
  const disponiveis = jogadores.filter((j) => !(excluir || []).includes(j.id));
  $("listaSeletor").innerHTML = disponiveis.length
    ? disponiveis.map((j) => `<button class="linha" data-sel="${j.id}">${avatarHtml(j)}<span>${j.nome}</span></button>`).join("")
    : `<p class="vazio">${t("sem_jogadores")}</p>`;
  $("listaSeletor").querySelectorAll("[data-sel]").forEach((b) =>
    b.addEventListener("click", () => { escolherSlot(acharJogador(b.dataset.sel)); }));
  abrir("telaSeletor");
}
function escolherSlot(j) {
  if (slotAlvo && slotAlvo.aoEscolher) slotAlvo.aoEscolher(j);
  fechar("telaSeletor");
}
$("btLimparSlot").addEventListener("click", () => escolherSlot(null));

function slotsHtml(idContainer, duplaEsq, duplaDir) {
  const bloco = (titulo, dupla, lado) => `
    <div style="flex:1;min-width:200px">
      <div style="color:var(--verde);letter-spacing:2px;font-size:14px;margin-bottom:8px">${titulo}</div>
      ${[0,1].map((i) => {
        const j = acharJogador(dupla[i]);
        return `<button class="linha" data-slot="${lado}:${i}" style="border:1px solid var(--borda);border-radius:12px;padding:12px;margin-bottom:8px">
          ${j ? avatarHtml(j) : ""}<span>${j ? j.nome : "+ " + t("escolher_jogador")}</span></button>`;
      }).join("")}
    </div>`;
  $(idContainer).innerHTML = `<div style="display:flex;gap:16px;flex-wrap:wrap">
    ${bloco(t("esquerda"), duplaEsq, "E")}${bloco(t("direita"), duplaDir, "D")}</div>`;
}

function abrirNovoJogo() {
  const render = () => {
    slotsHtml("slotsNovo", atual.duplaEsq, atual.duplaDir);
    $("slotsNovo").querySelectorAll("[data-slot]").forEach((b) => {
      b.addEventListener("click", () => {
        const [lado, i] = b.dataset.slot.split(":");
        const usados = atual.duplaEsq.concat(atual.duplaDir).filter(Boolean);
        abrirSeletor(lado, +i, usados, (j) => {
          const dupla = lado === "E" ? atual.duplaEsq : atual.duplaDir;
          dupla[+i] = j ? j.id : null;
          salvarAtual(); render(); desenhar();
        });
      });
    });
  };
  render();
  abrir("telaNovoJogo");
}

function abrirSubstituicao() {
  const render = () => {
    slotsHtml("slotsSubstituir", atual.duplaEsq, atual.duplaDir);
    $("slotsSubstituir").querySelectorAll("[data-slot]").forEach((b) => {
      b.addEventListener("click", () => {
        const [lado, i] = b.dataset.slot.split(":");
        const emQuadra = atual.duplaEsq.concat(atual.duplaDir).filter(Boolean);
        abrirSeletor(lado, +i, emQuadra, (j) => {
          if (!j) return;
          const dupla = lado === "E" ? atual.duplaEsq : atual.duplaDir;
          dupla[+i] = j.id; // o placar nao muda: so a escalacao
          atual.escalacoes.push({ ts: Date.now(),
            esq: atual.duplaEsq.slice(), dir: atual.duplaDir.slice() });
          salvarAtual(); render(); desenhar();
        });
      });
    });
  };
  render();
  abrir("telaSubstituir");
}

// ---------- correcao manual ----------
function abrirCorrecao() {
  const e = estado();
  let c = { esq: e.pontosEsquerda, dir: e.pontosDireita,
            sacando: e.sacando, sacador: e.sacador };
  const render = () => {
    $("controlesCorrecao").innerHTML = `
      <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 0">
        <span>${t("pontos_esq")}</span>
        <span><button class="bt alerta" data-c="esq-">-</button>
        <b style="padding:0 14px;font-size:22px">${c.esq}</b>
        <button class="bt ok" data-c="esq+">+</button></span></div>
      <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 0">
        <span>${t("pontos_dir")}</span>
        <span><button class="bt alerta" data-c="dir-">-</button>
        <b style="padding:0 14px;font-size:22px">${c.dir}</b>
        <button class="bt ok" data-c="dir+">+</button></span></div>
      <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 0">
        <span>${t("sacando_agora")}</span>
        <span><button class="bt ${c.sacando===ESQ?"ativo":""}" data-c="sacE">${t("esquerda")}</button>
        <button class="bt ${c.sacando===DIR?"ativo":""}" data-c="sacD">${t("direita")}</button></span></div>
      <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 0">
        <span>${t("numero_sacador")}</span>
        <span><button class="bt ${c.sacador===1?"ativo":""}" data-c="s1">1</button>
        <button class="bt ${c.sacador===2?"ativo":""}" data-c="s2">2</button></span></div>`;
    $("controlesCorrecao").querySelectorAll("[data-c]").forEach((b) =>
      b.addEventListener("click", () => {
        const a = b.dataset.c;
        if (a === "esq+") c.esq++; if (a === "esq-") c.esq = Math.max(0, c.esq - 1);
        if (a === "dir+") c.dir++; if (a === "dir-") c.dir = Math.max(0, c.dir - 1);
        if (a === "sacE") c.sacando = ESQ; if (a === "sacD") c.sacando = DIR;
        if (a === "s1") c.sacador = 1; if (a === "s2") c.sacador = 2;
        render();
      }));
  };
  render();
  $("btAplicarCorrecao").onclick = () => {
    const anterior = estado();
    atual.base = { ...anterior, pontosEsquerda: c.esq, pontosDireita: c.dir,
                   sacando: c.sacando, sacador: c.sacador,
                   encerrado: false, vencedor: null };
    atual.eventos = [];
    clearTimeout(vozPendente);
    salvarAtual(); fechar("telaCorrigir"); desenhar();
  };
  abrir("telaCorrigir");
}

// ---------- rodizio ----------
function sugerirRodizio(embaralhar) {
  const presentes = rodizio.presentes.filter((id) => acharJogador(id));
  if (presentes.length < 4) return null;
  // quem esperou mais entra primeiro; total de jogos desempata para
  // ninguem monopolizar a quadra
  const ordenados = presentes.slice().sort((a, b) => {
    const ua = rodizio.ultima[a] ?? -1, ub = rodizio.ultima[b] ?? -1;
    if (ua !== ub) return ua - ub;
    const ja = rodizio.jogos[a] || 0, jb = rodizio.jogos[b] || 0;
    if (ja !== jb) return ja - jb;
    return embaralhar ? Math.random() - 0.5 : a.localeCompare(b);
  });
  const q = ordenados.slice(0, 4);
  const montagens = [[[q[0],q[1]],[q[2],q[3]]], [[q[0],q[2]],[q[1],q[3]]], [[q[0],q[3]],[q[1],q[2]]]];
  const avaliadas = montagens.map(([esq, dir]) => {
    let custo = 0;
    if (rodizio.parceiro[esq[0]] === esq[1]) custo += 10;
    if (rodizio.parceiro[dir[0]] === dir[1]) custo += 10;
    esq.forEach((j) => { const ant = rodizio.adversarios[j] || [];
      custo += dir.filter((d) => ant.includes(d)).length; });
    return { esq, dir, custo };
  });
  const menor = Math.min(...avaliadas.map((a) => a.custo));
  const opcoes = avaliadas.filter((a) => a.custo === menor);
  return embaralhar ? opcoes[Math.floor(Math.random() * opcoes.length)] : opcoes[0];
}

function registrarRodizio(f) {
  rodizio.rodada += 1;
  f.esq.concat(f.dir).forEach((j) => {
    rodizio.ultima[j] = rodizio.rodada;
    rodizio.jogos[j] = (rodizio.jogos[j] || 0) + 1;
  });
  rodizio.parceiro[f.esq[0]] = f.esq[1]; rodizio.parceiro[f.esq[1]] = f.esq[0];
  rodizio.parceiro[f.dir[0]] = f.dir[1]; rodizio.parceiro[f.dir[1]] = f.dir[0];
  f.esq.forEach((j) => rodizio.adversarios[j] = f.dir.slice());
  f.dir.forEach((j) => rodizio.adversarios[j] = f.esq.slice());
  gravar(CHAVES.rodizio, rodizio);
}

function abrirRodizio() {
  const render = () => {
    $("presentes").innerHTML = `<div style="color:var(--verde);letter-spacing:2px;font-size:14px">${t("presentes")}</div>` +
      (jogadores.length ? jogadores.map((j) => {
        const p = rodizio.presentes.includes(j.id);
        const espera = Math.max(0, rodizio.rodada - (rodizio.ultima[j.id] ?? rodizio.rodada));
        return `<button class="linha" data-pres="${j.id}">
          <span style="width:22px;height:22px;flex:none;border-radius:4px;border:2px solid ${p?"var(--verde)":"var(--borda)"};background:${p?"var(--verde)":"transparent"}"></span>
          ${avatarHtml(j, 38)}
          <span><div style="color:${p?"#fff":"var(--cinza)"}">${j.nome}</div>
          ${p && espera > 0 ? `<div class="mini" style="color:var(--ambar)">${espera} ${t("rodadas")} ${t("esperando")}</div>` : ""}</span>
        </button>`;
      }).join("") : `<p class="vazio">${t("sem_jogadores")}</p>`);

    $("presentes").querySelectorAll("[data-pres]").forEach((b) =>
      b.addEventListener("click", () => {
        const id = b.dataset.pres;
        rodizio.presentes = rodizio.presentes.includes(id)
          ? rodizio.presentes.filter((x) => x !== id) : rodizio.presentes.concat([id]);
        gravar(CHAVES.rodizio, rodizio); render();
      }));

    const f = sugerirRodizio(false);
    $("sugestao").innerHTML = `<div style="color:var(--verde);letter-spacing:2px;font-size:14px;margin-bottom:8px">${t("sugestao")}</div>` +
      (f ? `<div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
              <div>${f.esq.map((id) => curto(id)).join(" & ")}</div>
              <div style="color:var(--cinza)">x</div>
              <div>${f.dir.map((id) => curto(id)).join(" & ")}</div>
            </div>
            <div class="acoes" style="justify-content:flex-start">
              <button class="bt" id="btSortear">${t("sortear")}</button>
              <button class="bt ok" id="btAceitar">${t("aceitar")}</button>
            </div>`
         : `<p class="vazio">${t("precisa_quatro")}</p>`);

    if (f) {
      $("btSortear").addEventListener("click", () => {
        const nova = sugerirRodizio(true);
        if (nova) { ultimaSugestao = nova; mostrarSugestao(nova); }
      });
      $("btAceitar").addEventListener("click", () => {
        const usar = ultimaSugestao || f;
        registrarRodizio(usar);
        atual.duplaEsq = usar.esq.slice(); atual.duplaDir = usar.dir.slice();
        salvarAtual(); ultimaSugestao = null;
        fechar("telaRodizio"); abrirNovoJogo();
      });
    }
  };
  let ultimaSugestao = null;
  const mostrarSugestao = (f) => {
    const alvo = $("sugestao").querySelector("div:nth-child(2)");
    if (alvo) alvo.innerHTML = `<div>${f.esq.map(curto).join(" & ")}</div>
      <div style="color:var(--cinza)">x</div><div>${f.dir.map(curto).join(" & ")}</div>`;
  };
  render();
  abrir("telaRodizio");
}

// ---------- historico, ranking e saidas ----------
const mesmoDia = (a, b) => {
  const x = new Date(a), y = new Date(b);
  return x.getFullYear() === y.getFullYear() && x.getMonth() === y.getMonth() &&
         x.getDate() === y.getDate();
};
const partidasDeHoje = () => partidas.filter((p) => mesmoDia(p.inicio, Date.now()));
const dataTexto = (ms) => new Date(ms).toLocaleString(P.idiomaUi === "pt" ? "pt-BR" : "en-US",
  { day:"2-digit", month:"2-digit", year:"numeric", hour:"2-digit", minute:"2-digit" });

const nomeDuplaDe = (p, lado) => {
  const d = lado === ESQ ? p.duplaEsq : p.duplaDir;
  return nomeDupla(d) || t(lado === ESQ ? "esquerda" : "direita");
};

function resumoJogador(lista, id) {
  let jogos = 0, vit = 0, feitos = 0, sofridos = 0;
  const conta = {}, ganhos = {};
  lista.filter((p) => p.completa).forEach((p) => {
    const todos = (p.escalacoes || []).flatMap((e) => e.esq.concat(e.dir))
      .concat(p.duplaEsq, p.duplaDir).filter(Boolean);
    if (!todos.includes(id)) return;
    const lado = p.duplaEsq.includes(id) ||
      (p.escalacoes || []).some((e) => e.esq.includes(id)) ? ESQ : DIR;
    jogos++;
    const s = stats(p);
    const venceuEsq = s.pontosEsquerda > s.pontosDireita;
    const venceu = (lado === ESQ && venceuEsq) || (lado === DIR && !venceuEsq);
    if (venceu) vit++;
    if (lado === ESQ) { feitos += s.pontosEsquerda; sofridos += s.pontosDireita; }
    else { feitos += s.pontosDireita; sofridos += s.pontosEsquerda; }
    const minha = lado === ESQ ? p.duplaEsq : p.duplaDir;
    minha.filter((x) => x && x !== id).forEach((parc) => {
      conta[parc] = (conta[parc] || 0) + 1;
      if (venceu) ganhos[parc] = (ganhos[parc] || 0) + 1;
    });
  });
  const maisFreq = Object.entries(conta).sort((a, b) => b[1] - a[1])[0];
  return { id, jogos, vitorias: vit, derrotas: jogos - vit,
           aproveitamento: jogos ? Math.round((vit * 100) / jogos) : 0,
           saldo: feitos - sofridos,
           parceiro: maisFreq ? maisFreq[0] : null, vezes: maisFreq ? maisFreq[1] : 0 };
}

const ranking = (lista) => jogadores.map((j) => resumoJogador(lista, j.id))
  .filter((r) => r.jogos > 0)
  .sort((a, b) => b.aproveitamento - a.aproveitamento || b.saldo - a.saldo || b.jogos - a.jogos);

let rankingHoje = true;
function desenharRanking() {
  const lista = rankingHoje ? partidasDeHoje() : partidas;
  const r = ranking(lista);
  $("btHoje").className = "bt" + (rankingHoje ? " ativo" : "");
  $("btSempre").className = "bt" + (rankingHoje ? "" : " ativo");
  $("listaRanking").innerHTML = r.length ? r.map((x, i) => {
    const j = acharJogador(x.id); if (!j) return "";
    const parc = x.parceiro ? curto(x.parceiro) : null;
    return `<div class="linha">
      <b style="width:30px;color:${i < 3 ? "var(--verde)" : "var(--cinza)"};font-family:monospace">${i+1}</b>
      ${avatarHtml(j)}
      <span style="flex:1"><div>${j.nome}</div>
      ${parc ? `<div class="mini">${parc} (${x.vezes})</div>` : ""}</span>
      <span style="text-align:right">
        <div class="mini">${x.vitorias}${t("vitorias")} . ${x.derrotas}${t("derrotas")}
        . ${t("saldo")} ${x.saldo >= 0 ? "+" : ""}${x.saldo}</div>
        <div style="color:var(--verde);font-size:20px;font-family:monospace">${x.aproveitamento}%</div>
      </span></div>`;
  }).join("") : `<p class="vazio">${rankingHoje ? t("sem_jogos_hoje") : t("sem_dados")}</p>`;
}

function desenharHistorico() {
  const ordenadas = partidas.slice().sort((a, b) => b.inicio - a.inicio);
  $("listaHistorico").innerHTML = ordenadas.length ? ordenadas.map((p) => {
    const s = stats(p);
    return `<button class="linha" data-partida="${p.id}">
      <span style="flex:1"><div>${nomeDuplaDe(p, ESQ)}  x  ${nomeDuplaDe(p, DIR)}</div>
      <div class="mini">${dataTexto(p.inicio)}${p.completa ? "" : "  .  " + t("incompleta")}</div></span>
      <b style="color:var(--verde);font-family:monospace;font-size:19px">${s.pontosEsquerda} x ${s.pontosDireita}</b>
    </button>`;
  }).join("") : `<p class="vazio">${t("sem_partidas")}</p>`;
  $("listaHistorico").querySelectorAll("[data-partida]").forEach((b) =>
    b.addEventListener("click", () => {
      const p = partidas.find((x) => x.id === b.dataset.partida);
      if (p) mostrarResultado(p, false);
    }));
}

let partidaExibida = null;
function mostrarResultado(p, recemEncerrada) {
  partidaExibida = p;
  const s = stats(p);
  const dur = p.fim ? Math.max(0, Math.round((p.fim - p.inicio) / 60000)) : null;
  const linha = (r, v) => `<div style="display:flex;justify-content:space-between;padding:4px 0">
    <span style="color:var(--cinza);font-size:14px">${r}</span>
    <span style="font-family:monospace;font-size:14px">${v}</span></div>`;
  $("tResultado").textContent = p.completa ? t("fim_game") : t("resultado");
  $("corpoResultado").innerHTML = `
    <div style="display:flex;justify-content:space-around;text-align:center;margin-bottom:14px">
      <div><div style="color:${s.pontosEsquerda>s.pontosDireita?"var(--verde)":"var(--cinza)"}">${nomeDuplaDe(p,ESQ)}</div>
        <div style="font-size:52px;font-family:monospace">${s.pontosEsquerda}</div></div>
      <div><div style="color:${s.pontosDireita>s.pontosEsquerda?"var(--verde)":"var(--cinza)"}">${nomeDuplaDe(p,DIR)}</div>
        <div style="font-size:52px;font-family:monospace">${s.pontosDireita}</div></div>
    </div>
    ${dur !== null ? linha(t("duracao"), dur + " " + t("minutos")) : ""}
    ${linha(t("rallies"), s.totalRallies)}
    ${linha(t("rallies_ganhos"), `${s.ralliesEsquerda}  x  ${s.ralliesDireita}`)}
    ${linha(t("rallies_recebendo"), `${s.ralliesRecebendoEsquerda}  x  ${s.ralliesRecebendoDireita}`)}
    ${linha(t("turnos_secos"), `${s.turnosSecosEsquerda}  x  ${s.turnosSecosDireita}`)}
    ${linha(t("maior_sequencia"), `${s.maiorSequenciaEsquerda}  x  ${s.maiorSequenciaDireita}`)}
    ${linha(t("viradas"), s.viradas)}`;
  $("btNovoAposGame").style.display = recemEncerrada ? "" : "none";
  abrir("telaResultado");
}

function emailsDe(lista) {
  const ids = new Set();
  lista.forEach((p) => p.duplaEsq.concat(p.duplaDir).filter(Boolean).forEach((i) => ids.add(i)));
  return [...ids].map((i) => (acharJogador(i) || {}).email).filter((e) => e && e.includes("@"));
}

function abrirEmail(assunto, corpo, destinos) {
  if (!destinos.length) { alert(t("sem_email")); return; }
  const url = `mailto:${destinos.join(",")}?subject=${encodeURIComponent(assunto)}&body=${encodeURIComponent(corpo)}`;
  window.location.href = url;
}

function corpoDaPartida(p) {
  const pt = P.idiomaUi === "pt", s = stats(p);
  const L = [];
  L.push(pt ? "Resumo do jogo" : "Game summary", "", dataTexto(p.inicio));
  if (p.fim) L.push((pt ? "Duracao: " : "Duration: ") + Math.round((p.fim - p.inicio)/60000) + " min");
  L.push("", `${nomeDuplaDe(p,ESQ)}  ${s.pontosEsquerda}`, `${nomeDuplaDe(p,DIR)}  ${s.pontosDireita}`, "");
  L.push(pt ? "Numeros da partida" : "Match numbers");
  L.push(`${t("rallies")}: ${s.totalRallies}`);
  L.push(`${t("rallies_ganhos")}: ${s.ralliesEsquerda} x ${s.ralliesDireita}`);
  L.push(`${t("rallies_recebendo")}: ${s.ralliesRecebendoEsquerda} x ${s.ralliesRecebendoDireita}`);
  L.push(`${t("turnos_secos")}: ${s.turnosSecosEsquerda} x ${s.turnosSecosDireita}`);
  L.push(`${t("maior_sequencia")}: ${s.maiorSequenciaEsquerda} x ${s.maiorSequenciaDireita}`);
  L.push(`${t("viradas")}: ${s.viradas}`);
  return L.join("\n");
}

function corpoDaSessao(lista) {
  const pt = P.idiomaUi === "pt", L = [];
  L.push(pt ? "Resumo da noite" : "Session summary", "", dataTexto(lista[0].inicio));
  L.push((pt ? "Partidas: " : "Games played: ") + lista.length, "");
  L.push(pt ? "Resultados" : "Results");
  lista.slice().sort((a,b) => a.inicio - b.inicio).forEach((p) => {
    const s = stats(p);
    L.push(`${nomeDuplaDe(p,ESQ)} ${s.pontosEsquerda}  x  ${s.pontosDireita} ${nomeDuplaDe(p,DIR)}`);
  });
  const r = ranking(lista);
  if (r.length) {
    L.push("", pt ? "Ranking do dia" : "Standings");
    r.forEach((x, i) => L.push(`${i+1}. ${curto(x.id)}  ${x.vitorias}${t("vitorias")} ${x.derrotas}${t("derrotas")}  ${x.aproveitamento}%  ${t("saldo")} ${x.saldo>=0?"+":""}${x.saldo}`));
  }
  return L.join("\n");
}

function baixar(nome, conteudo, tipo) {
  const blob = new Blob([conteudo], { type: tipo });
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = nome;
  document.body.appendChild(a); a.click();
  setTimeout(() => { URL.revokeObjectURL(a.href); a.remove(); }, 1000);
}

function exportarCsv() {
  const linhas = ["data,esquerda,direita,pontos_esq,pontos_dir,completa,rallies,recebendo_esq,recebendo_dir,secos_esq,secos_dir,seq_esq,seq_dir,viradas"];
  partidas.slice().sort((a,b) => a.inicio - b.inicio).forEach((p) => {
    const s = stats(p);
    const esc = (x) => (String(x).includes(",") ? `"${x}"` : x);
    linhas.push([new Date(p.inicio).toISOString(), esc(nomeDuplaDe(p,ESQ)), esc(nomeDuplaDe(p,DIR)),
      s.pontosEsquerda, s.pontosDireita, p.completa ? "sim" : "nao", s.totalRallies,
      s.ralliesRecebendoEsquerda, s.ralliesRecebendoDireita, s.turnosSecosEsquerda,
      s.turnosSecosDireita, s.maiorSequenciaEsquerda, s.maiorSequenciaDireita, s.viradas].join(","));
  });
  baixar("pickleball.csv", linhas.join("\n"), "text/csv");
}

// ---------- manual ----------
const MANUAL = {
  en: [
    ["In short", [
      "The screen shows the score and calls it out loud. Tap the side that won the rally.",
      "Never think about the rules: serve changes, second server and side outs are handled for you.",
      "Everything is stored on this device. No account, no cloud, no internet needed after the first load."]],
    ["Starting a game", [
      "Tap the gear at the bottom right, then New game.",
      "Pick the four players and choose which side serves first. You can also play with no names at all.",
      "Games go to 11, win by 2, traditional doubles: only the serving team scores."]],
    ["Scoring", [
      "Tap either half of the screen. Undo is in the footer.",
      "A Bluetooth clicker paired to this device also works: arrow or page keys score, Enter undoes.",
      "Tap the status text in the footer to lock the screen against accidental touches."]],
    ["Reading the screen", [
      "The band under the numbers shows who serves and the court with all four positions.",
      "Solid green is the server, green outline is the receiver. They are always diagonal.",
      "The three small numbers are the official call: server score, receiver score, server number."]],
    ["Voice", [
      "Tap EN or PT in the footer to switch the voice language.",
      "The new server's name is announced whenever the serve changes hands.",
      "Tap the small call at the bottom to repeat the last announcement.",
      "On iPhone and iPad the voice only starts after your first tap on the screen.",
      "Gear, then Voice, to pick which voice speaks and how fast. The list shows whatever voices this device has installed."]],
    ["Players and rotation", [
      "Gear, then Players. Only the full name is required. Photo and email are optional.",
      "Gear, then Rotation, to mark who is here and get the next pairing suggested.",
      "The suggestion favours whoever waited longest and avoids repeating partners."]],
    ["After playing", [
      "Every finished game is saved by itself.",
      "Leaderboard shows standings by player, today or all time.",
      "Email today's summary sends one message at the end with every result and the standings."]],
    ["Important", [
      "Add this page to your home screen. On iPhone and iPad, Safari may erase the data of sites not added to the home screen after a few weeks of no use.",
      "Use Backup regularly and keep the file somewhere safe.",
      "The Galaxy Watch remote only works with the Android version, not with this one."]]
  ],
  pt: [
    ["Em resumo", [
      "A tela mostra o placar e fala em voz alta. Toque na dupla que ganhou o rally.",
      "Nunca pense na regra: troca de sacador, segundo sacador e troca de saque sao automaticos.",
      "Tudo fica guardado neste aparelho. Sem conta, sem nuvem, sem internet depois da primeira abertura."]],
    ["Comecar um jogo", [
      "Toque na engrenagem no canto inferior direito e escolha Novo game.",
      "Escolha os quatro jogadores e quem saca primeiro. Tambem da para jogar sem nome nenhum.",
      "Game ate 11, com vantagem de 2, no formato tradicional: so pontua quem saca."]],
    ["Marcar ponto", [
      "Toque em qualquer metade da tela. O desfazer fica no rodape.",
      "Um controle Bluetooth pareado tambem funciona: setas ou teclas de pagina marcam ponto, Enter desfaz.",
      "Toque no texto de status no rodape para travar a tela contra toque acidental."]],
    ["Ler a tela", [
      "A faixa abaixo dos numeros mostra quem saca e a quadra com as quatro posicoes.",
      "Verde cheio e quem saca, contorno verde e quem recebe. Eles sempre ficam na diagonal.",
      "Os tres numeros pequenos sao a chamada oficial: pontos de quem saca, de quem recebe e o numero do sacador."]],
    ["Voz", [
      "Toque em EN ou PT no rodape para trocar o idioma da voz.",
      "O nome de quem assume o saque e anunciado sempre que o saque muda de dupla.",
      "Toque na chamada pequena do rodape para repetir o ultimo anuncio.",
      "No iPhone e no iPad a voz so comeca depois do seu primeiro toque na tela.",
      "Engrenagem e depois Voz, para escolher qual voz fala e a velocidade. A lista mostra as vozes instaladas neste aparelho."]],
    ["Jogadores e rodizio", [
      "Engrenagem e depois Jogadores. So o nome completo e obrigatorio. Foto e e-mail sao opcionais.",
      "Engrenagem e depois Rodizio, para marcar quem esta presente e receber a proxima formacao sugerida.",
      "A sugestao prioriza quem esperou mais e evita repetir parceiro."]],
    ["Depois de jogar", [
      "Toda partida encerrada e salva sozinha.",
      "O ranking mostra a classificacao por jogador, de hoje ou de sempre.",
      "Enviar o resumo de hoje manda uma mensagem so no fim, com todos os resultados e o ranking."]],
    ["Importante", [
      "Adicione esta pagina a tela de inicio. No iPhone e no iPad, o Safari pode apagar os dados de sites que nao estao na tela de inicio depois de algumas semanas sem uso.",
      "Use o backup com frequencia e guarde o arquivo num lugar seguro.",
      "O controle pelo Galaxy Watch funciona apenas na versao Android, nao nesta."]]
  ]
};

function desenharManual() {
  $("corpoManual").innerHTML = (MANUAL[P.idiomaUi] || MANUAL.en)
    .map(([titulo, itens]) => `<h3>${titulo}</h3><ul>${itens.map((i) => `<li>${i}</li>`).join("")}</ul>`)
    .join("");
}

// ---------- tela de voz ----------
let idiomaVozEditando = null;

function desenharVozes() {
  const idioma = idiomaVozEditando || P.idiomaVoz;
  $("abaEn").className = "bt" + (idioma === "en" ? " ativo" : "");
  $("abaPt").className = "bt" + (idioma === "pt" ? " ativo" : "");

  const lista = vozesDoIdioma(idioma);
  const salva = idioma === "pt" ? P.vozPt : P.vozEn;
  const atualEscolhida = vozEscolhida(idioma);

  if (!lista.length) {
    $("listaVozes").innerHTML = `<p class="vazio">${t("sem_vozes")}</p>`;
  } else {
    $("listaVozes").innerHTML = lista.map((v) => {
      const marcada = atualEscolhida && v.name === atualEscolhida.name;
      return `<button class="linha" data-voz="${v.name}">
        <span style="width:18px;height:18px;flex:none;border-radius:50%;
          border:2px solid ${marcada ? "var(--verde)" : "var(--borda)"};
          background:${marcada ? "var(--verde)" : "transparent"}"></span>
        <span style="flex:1"><div>${v.name}</div>
        <div class="mini">${v.lang}${ehSugerida(v.name, idioma) ? "  .  " + t("sugerida") : ""}</div></span>
      </button>`;
    }).join("");
    $("listaVozes").querySelectorAll("[data-voz]").forEach((b) =>
      b.addEventListener("click", () => {
        if (idioma === "pt") P.vozPt = b.dataset.voz; else P.vozEn = b.dataset.voz;
        gravar(CHAVES.prefs, P);
        desenharVozes();
        P.idiomaVoz = idioma; desenhar();
        falarAgora(idioma === "pt" ? "4, 2, 1, saque de Jeff" : "4, 2, 1, Jeff to serve");
      }));
  }

  $("tVoz").textContent = t("voz");
  $("tVelocidade").textContent = t("velocidade");
  $("btTestarVoz").textContent = t("testar");
  $("valorVelocidade").textContent = (P.velocidade || 1.15).toFixed(2);
  $("velocidade").value = P.velocidade || 1.15;
}

function abrirVoz() {
  idiomaVozEditando = P.idiomaVoz;
  liberarVoz();
  desenharVozes();
  abrir("telaVoz");
}

// ---------- backup ----------
function exportarBackup() {
  const pacote = { versao: 1, quando: Date.now(), jogadores, partidas, rodizio, prefs: P };
  const carimbo = new Date().toISOString().slice(0, 16).replace(/[:T]/g, "");
  baixar(`pickleball-backup-${carimbo}.json`, JSON.stringify(pacote), "application/json");
}

$("arquivoBackup").addEventListener("change", (ev) => {
  const arquivo = ev.target.files && ev.target.files[0];
  if (!arquivo) return;
  const leitor = new FileReader();
  leitor.onload = () => {
    try {
      const d = JSON.parse(leitor.result);
      if (!d || !Array.isArray(d.jogadores)) throw new Error("formato");
      jogadores = d.jogadores;
      partidas = Array.isArray(d.partidas) ? d.partidas : [];
      rodizio = d.rodizio || rodizio;
      if (d.prefs) P = { ...P, ...d.prefs };
      salvarTudo();
      $("avisoBackup").textContent = t("backup_ok");
      traduzirTelas(); listarJogadores(); desenhar();
    } catch (e) {
      $("avisoBackup").textContent = t("backup_erro");
    }
  };
  leitor.readAsText(arquivo);
  ev.target.value = "";
});

// ---------- eventos ----------
$("ladoEsq").addEventListener("click", () => { if (!P.travado) marcarPonto(ESQ); });
$("ladoDir").addEventListener("click", () => { if (!P.travado) marcarPonto(DIR); });
$("btDesfazer").addEventListener("click", desfazer);
$("chamada").addEventListener("click", () => { liberarVoz(); falarAgora(ultimaFala); });
$("estadoTrava").addEventListener("click", () => { P.travado = !P.travado; gravar(CHAVES.prefs, P); desenhar(); });
$("btIdiomaVoz").addEventListener("click", () => {
  P.idiomaVoz = P.idiomaVoz === "en" ? "pt" : "en"; gravar(CHAVES.prefs, P); desenhar();
});
$("btMenu").addEventListener("click", () => abrir("telaMenu"));

$("mNovoJogo").addEventListener("click", () => {
  if (atual.eventos.length && !confirm(t("confirmar_novo"))) return;
  fechar("telaMenu"); abrirNovoJogo();
});
$("mInverter").addEventListener("click", () => { inverterLados(); fechar("telaMenu"); });
$("mSubstituir").addEventListener("click", () => { fechar("telaMenu"); abrirSubstituicao(); });
$("mCorrigir").addEventListener("click", () => { fechar("telaMenu"); abrirCorrecao(); });
$("mRodizio").addEventListener("click", () => { fechar("telaMenu"); abrirRodizio(); });
$("mJogadores").addEventListener("click", () => { fechar("telaMenu"); listarJogadores(); abrir("telaJogadores"); });
$("mHistorico").addEventListener("click", () => { fechar("telaMenu"); desenharHistorico(); abrir("telaHistorico"); });
$("mRanking").addEventListener("click", () => { fechar("telaMenu"); desenharRanking(); abrir("telaRanking"); });
$("mVoz").addEventListener("click", () => { fechar("telaMenu"); abrirVoz(); });
$("abaEn").addEventListener("click", () => { idiomaVozEditando = "en"; desenharVozes(); });
$("abaPt").addEventListener("click", () => { idiomaVozEditando = "pt"; desenharVozes(); });
$("velocidade").addEventListener("input", (ev) => {
  P.velocidade = parseFloat(ev.target.value);
  $("valorVelocidade").textContent = P.velocidade.toFixed(2);
});
$("velocidade").addEventListener("change", () => { gravar(CHAVES.prefs, P); });
$("btTestarVoz").addEventListener("click", () => {
  const idioma = idiomaVozEditando || P.idiomaVoz;
  const guarda = P.idiomaVoz;
  P.idiomaVoz = idioma;
  falarAgora(idioma === "pt" ? "sáid aut, 4, 2, 1, saque de Jeff, guêimi point"
                             : "side out, 4, 2, 1, Jeff to serve, game point");
  P.idiomaVoz = guarda;
});

$("mBackup").addEventListener("click", () => { fechar("telaMenu"); $("avisoBackup").textContent = ""; abrir("telaBackup"); });
$("mManual").addEventListener("click", () => { fechar("telaMenu"); desenharManual(); abrir("telaManual"); });
$("mIdiomaTela").addEventListener("click", () => {
  P.idiomaUi = P.idiomaUi === "en" ? "pt" : "en";
  gravar(CHAVES.prefs, P); traduzirTelas(); desenhar();
});
$("mSideOut").addEventListener("click", () => {
  P.sideOut = !P.sideOut; gravar(CHAVES.prefs, P); traduzirTelas();
});

$("btNovoJogador").addEventListener("click", () => abrirFormulario(null));
$("comecarEsq").addEventListener("click", () => { novoGame(ESQ); fechar("telaNovoJogo"); });
$("comecarDir").addEventListener("click", () => { novoGame(DIR); fechar("telaNovoJogo"); });
$("btHoje").addEventListener("click", () => { rankingHoje = true; desenharRanking(); });
$("btSempre").addEventListener("click", () => { rankingHoje = false; desenharRanking(); });
$("btExportarBackup").addEventListener("click", exportarBackup);
$("btCsv").addEventListener("click", exportarCsv);
$("btEmailSessao").addEventListener("click", () => {
  const lista = partidasDeHoje();
  if (!lista.length) { alert(t("sem_jogos_hoje")); return; }
  const assunto = (P.idiomaUi === "pt" ? "Resumo da noite de pickleball, " : "Pickleball session summary, ")
    + dataTexto(lista[0].inicio);
  abrirEmail(assunto, corpoDaSessao(lista), emailsDe(lista));
});
$("btEmailPartida").addEventListener("click", () => {
  if (!partidaExibida) return;
  const assunto = (P.idiomaUi === "pt" ? "Placar do jogo: " : "Pickleball result: ")
    + nomeDuplaDe(partidaExibida, ESQ) + " x " + nomeDuplaDe(partidaExibida, DIR);
  abrirEmail(assunto, corpoDaPartida(partidaExibida), emailsDe([partidaExibida]));
});
$("btNovoAposGame").addEventListener("click", () => { fechar("telaResultado"); abrirNovoJogo(); });

// Controle Bluetooth: o aparelho o enxerga como teclado, entao basta ouvir teclas.
document.addEventListener("keydown", (ev) => {
  if (document.querySelector(".tela.aberta")) return;
  const k = ev.key;
  if (["ArrowLeft","PageUp","a","A"].includes(k)) { ev.preventDefault(); marcarPonto(ESQ); }
  else if (["ArrowRight","PageDown","d","D"].includes(k)) { ev.preventDefault(); marcarPonto(DIR); }
  else if (["Enter"," ","Backspace"].includes(k)) { ev.preventDefault(); desfazer(); }
});

// Mantem a tela acesa enquanto o jogo acontece.
let travaDeTela = null;
async function manterAcesa() {
  try { if ("wakeLock" in navigator) travaDeTela = await navigator.wakeLock.request("screen"); }
  catch (e) {}
}
document.addEventListener("visibilitychange", () => {
  if (document.visibilityState === "visible") manterAcesa();
});
document.body.addEventListener("click", () => { liberarVoz(); manterAcesa(); }, { once: true });

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => navigator.serviceWorker.register("sw.js").catch(() => {}));
}

traduzirTelas();
desenhar();
