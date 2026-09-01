// Cache para funcionar sem internet depois da primeira abertura.
// A estrategia e rede primeiro: online voce sempre pega a versao nova,
// offline cai para o que ja esta guardado.
const VERSAO = "placar-v2";
const ARQUIVOS = [
  "./", "./index.html", "./estilo.css", "./regras.js", "./app.js",
  "./manifest.webmanifest", "./icon-192.png", "./icon-512.png"
];

self.addEventListener("install", (ev) => {
  ev.waitUntil(caches.open(VERSAO).then((c) => c.addAll(ARQUIVOS)).then(() => self.skipWaiting()));
});

self.addEventListener("activate", (ev) => {
  ev.waitUntil(
    caches.keys()
      .then((nomes) => Promise.all(nomes.filter((n) => n !== VERSAO).map((n) => caches.delete(n))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (ev) => {
  if (ev.request.method !== "GET") return;
  ev.respondWith(
    fetch(ev.request)
      .then((resposta) => {
        const copia = resposta.clone();
        caches.open(VERSAO).then((c) => c.put(ev.request, copia)).catch(() => {});
        return resposta;
      })
      .catch(() => caches.match(ev.request).then((r) => r || caches.match("./index.html")))
  );
});
