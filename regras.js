// Regras do pickleball em duplas tradicional.
// Porte fiel do modulo core do aplicativo Android: mesma logica, mesmos nomes.

const ESQ = "ESQUERDA";
const DIR = "DIREITA";
const oposto = (l) => (l === ESQ ? DIR : ESQ);

const PONTOS_PARA_VENCER = 11;
const VANTAGEM_MINIMA = 2;

function estadoInicial(primeiroSaque = ESQ) {
  return {
    pontosEsquerda: 0,
    pontosDireita: 0,
    sacando: primeiroSaque,
    sacador: 2,
    encerrado: false,
    vencedor: null,
    naDireitaEsquerda: 0,
    naDireitaDireita: 0,
    indiceSacador: 0,
  };
}

const pontosDe = (e, lado) => (lado === ESQ ? e.pontosEsquerda : e.pontosDireita);
const naDireitaDe = (e, lado) => (lado === ESQ ? e.naDireitaEsquerda : e.naDireitaDireita);
const pontosSacador = (e) => pontosDe(e, e.sacando);
const pontosRecebedor = (e) => pontosDe(e, oposto(e.sacando));

// Direita quando quem saca esta na quadra da direita.
const ladoDoSaque = (e) =>
  e.indiceSacador === naDireitaDe(e, e.sacando) ? "direita" : "esquerda";

const chamada = (e) => `${pontosSacador(e)}-${pontosRecebedor(e)}-${e.sacador}`;

const pontoDeJogo = (e) =>
  !e.encerrado &&
  pontosSacador(e) >= PONTOS_PARA_VENCER - 1 &&
  pontosSacador(e) - pontosRecebedor(e) >= VANTAGEM_MINIMA - 1;

// Ponto para quem saca: os parceiros trocam de lado e o mesmo jogador continua.
function pontuar(e) {
  const novo = { ...e };
  if (e.sacando === ESQ) {
    novo.pontosEsquerda += 1;
    novo.naDireitaEsquerda = 1 - e.naDireitaEsquerda;
  } else {
    novo.pontosDireita += 1;
    novo.naDireitaDireita = 1 - e.naDireitaDireita;
  }
  const meus = pontosDe(novo, e.sacando);
  const deles = pontosDe(novo, oposto(e.sacando));
  if (meus >= PONTOS_PARA_VENCER && meus - deles >= VANTAGEM_MINIMA) {
    novo.encerrado = true;
    novo.vencedor = e.sacando;
  }
  return novo;
}

// Sacador 1 perde: a bola vai para o parceiro, ninguem troca de lado.
// Sacador 2 perde: troca de saque, e saca quem estiver na quadra da direita.
function perderSaque(e) {
  if (e.sacador === 1) {
    return { ...e, sacador: 2, indiceSacador: 1 - e.indiceSacador };
  }
  const entrando = oposto(e.sacando);
  return {
    ...e,
    sacando: entrando,
    sacador: 1,
    indiceSacador: naDireitaDe(e, entrando),
  };
}

function aplicar(estado, evento) {
  if (estado.encerrado) return estado;
  return evento.vencedor === estado.sacando ? pontuar(estado) : perderSaque(estado);
}

// O estado nunca e editado: e sempre recalculado a partir do log.
function derivarDe(eventos, base) {
  return eventos.reduce((acc, ev) => aplicar(acc, ev), base);
}

function derivar(eventos, primeiroSaque = ESQ) {
  return derivarDe(eventos, estadoInicial(primeiroSaque));
}

// Estatisticas da partida, derivadas do mesmo log.
function analisar(eventos, base) {
  let estado = base;
  let rEsq = 0, rDir = 0, recEsq = 0, recDir = 0;
  let secEsq = 0, secDir = 0, seqEsq = 0, seqDir = 0;
  let maiorEsq = 0, maiorDir = 0, viradas = 0;
  let noTurno = 0, liderAnterior = null;

  for (const evento of eventos) {
    const sacandoAntes = estado.sacando;
    if (evento.vencedor === ESQ) rEsq++; else rDir++;
    if (evento.vencedor !== sacandoAntes) {
      if (evento.vencedor === ESQ) recEsq++; else recDir++;
    }

    const depois = aplicar(estado, evento);
    const pontuou = pontosDe(depois, sacandoAntes) > pontosDe(estado, sacandoAntes);

    if (pontuou) {
      noTurno++;
      if (sacandoAntes === ESQ) {
        seqEsq++; seqDir = 0;
        if (seqEsq > maiorEsq) maiorEsq = seqEsq;
      } else {
        seqDir++; seqEsq = 0;
        if (seqDir > maiorDir) maiorDir = seqDir;
      }
    }

    if (depois.sacando !== sacandoAntes) {
      if (noTurno === 0) {
        if (sacandoAntes === ESQ) secEsq++; else secDir++;
      }
      noTurno = 0;
    }

    let lider = null;
    if (depois.pontosEsquerda > depois.pontosDireita) lider = ESQ;
    else if (depois.pontosDireita > depois.pontosEsquerda) lider = DIR;
    if (lider && liderAnterior && lider !== liderAnterior) viradas++;
    if (lider) liderAnterior = lider;

    estado = depois;
  }

  return {
    pontosEsquerda: estado.pontosEsquerda,
    pontosDireita: estado.pontosDireita,
    ralliesEsquerda: rEsq,
    ralliesDireita: rDir,
    ralliesRecebendoEsquerda: recEsq,
    ralliesRecebendoDireita: recDir,
    turnosSecosEsquerda: secEsq,
    turnosSecosDireita: secDir,
    maiorSequenciaEsquerda: maiorEsq,
    maiorSequenciaDireita: maiorDir,
    viradas,
    totalRallies: eventos.length,
  };
}
