package com.kriptobr.placar.tablet

/**
 * Estilo e script do relatorio. Ficam separados para o gerador nao virar
 * um arquivo ilegivel, e para o visual acompanhar o tema do aparelho.
 *
 * As cores sao as mesmas do aplicativo e seguem uma paleta validada para
 * daltonismo e contraste nos dois temas.
 */
internal val ESTILO = """
:root{
  color-scheme:light;
  --plano:#f9f9f7; --sup:#fcfcfb;
  --ink:#0b0b0b; --ink2:#52514e; --mudo:#898781;
  --grade:#e1e0d9; --eixo:#c3c2b7; --anel:rgba(11,11,11,.10);
  --azul:#2a78d6; --vermelho:#e34948; --neutro:#f0efec; --bom:#006300;
}
@media (prefers-color-scheme:dark){ :root:where(:not([data-theme="light"])){
  color-scheme:dark;
  --plano:#0d0d0d; --sup:#1a1a19;
  --ink:#f0efec; --ink2:#c3c2b7; --mudo:#898781;
  --grade:#2c2c2a; --eixo:#383835; --anel:rgba(255,255,255,.10);
  --azul:#3987e5; --vermelho:#e66767; --neutro:#383835; --bom:#0ca30c;
}}
*{box-sizing:border-box}
body{margin:0;background:var(--plano);color:var(--ink);
  font-family:system-ui,-apple-system,"Segoe UI",sans-serif;line-height:1.5}
.wrap{max-width:1080px;margin:0 auto;padding:28px 20px 64px}
header{margin-bottom:26px}
h1{font-size:clamp(26px,4vw,38px);margin:0 0 6px;font-weight:600;letter-spacing:-.4px}
.sub{color:var(--ink2);font-size:15px;margin:0}
h2{font-size:19px;font-weight:600;margin:0 0 4px;letter-spacing:-.2px}
.leg{color:var(--ink2);font-size:14px;margin:0 0 16px}
section{background:var(--sup);border:1px solid var(--anel);border-radius:14px;
  padding:22px;margin-bottom:18px}
.kpis{display:grid;grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:12px;margin-bottom:18px}
.kpi{background:var(--sup);border:1px solid var(--anel);border-radius:14px;padding:16px 18px}
.kpi .v{font-size:30px;font-weight:600;letter-spacing:-1px;line-height:1.1}
.kpi .r{color:var(--ink2);font-size:13px;margin-top:2px}
.podio{display:grid;grid-template-columns:repeat(auto-fit,minmax(190px,1fr));gap:12px;margin-bottom:20px}
.p{border:1px solid var(--anel);border-radius:12px;padding:16px;background:var(--plano);position:relative}
.p.ouro{border-color:var(--azul);border-width:2px}
.p .pos{color:var(--mudo);font-size:12px;letter-spacing:1.5px;text-transform:uppercase}
.p .n{font-size:22px;font-weight:600;margin:3px 0 1px}
.p .d{color:var(--ink2);font-size:13px}
.p .pct{position:absolute;top:14px;right:16px;font-size:26px;font-weight:600;color:var(--azul)}
svg{display:block;width:100%;height:auto;overflow:visible}
.tick{fill:var(--mudo);font-size:12px}
.rot{fill:var(--ink);font-size:13px}
.val{fill:var(--ink);font-size:12.5px;font-variant-numeric:tabular-nums}
.gl{stroke:var(--grade);stroke-width:1}
.bl{stroke:var(--eixo);stroke-width:1}
table{width:100%;border-collapse:collapse;font-size:14px}
th{text-align:right;padding:9px 8px;color:var(--ink2);font-weight:600;font-size:12px;
  text-transform:uppercase;letter-spacing:.6px;border-bottom:1px solid var(--eixo);white-space:nowrap}
th:first-child,td:first-child{text-align:left}
td{padding:9px 8px;text-align:right;border-bottom:1px solid var(--grade);
  font-variant-numeric:tabular-nums}
tbody tr:last-child td{border-bottom:none}
.pos-n{color:var(--bom)} .neg-n{color:var(--vermelho)}
.grid-h{display:grid;gap:3px;overflow-x:auto}
.cel{border-radius:5px;padding:7px 4px;text-align:center;font-size:12.5px;
  font-variant-numeric:tabular-nums;border:1px solid var(--anel)}
.cel.vazio{background:transparent;border-color:transparent;color:var(--mudo)}
.cab{font-size:11.5px;color:var(--ink2);text-align:center;padding:4px 2px;
  white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.cabl{font-size:12.5px;color:var(--ink);text-align:left;padding:7px 6px;white-space:nowrap}
.marcas{display:flex;gap:16px;flex-wrap:wrap;font-size:13px;color:var(--ink2);margin-top:12px}
.marca{display:flex;align-items:center;gap:6px}
.pt{width:11px;height:11px;border-radius:3px;display:inline-block}
.rec{display:grid;grid-template-columns:repeat(auto-fit,minmax(230px,1fr));gap:14px}
.rec div{border-left:3px solid var(--azul);padding:2px 0 2px 12px}
.rec .t{color:var(--ink2);font-size:13px}
.rec .c{font-size:16px;font-weight:600;margin-top:2px}
.nota{border-left:3px solid var(--vermelho);padding-left:14px}
.nota p{margin:0 0 10px;font-size:14.5px;color:var(--ink2)}
.vazio{color:var(--ink2);font-size:16px;margin:0}
.tip{position:fixed;pointer-events:none;background:var(--ink);color:var(--sup);
  padding:7px 10px;border-radius:8px;font-size:12.5px;opacity:0;transition:opacity .12s;
  z-index:9;white-space:nowrap;font-variant-numeric:tabular-nums}
footer{color:var(--mudo);font-size:13px;text-align:center;margin-top:28px}
@media(max-width:640px){ section{padding:16px} .wrap{padding:18px 16px 48px} }
"""

internal val SCRIPT = """
(function(){
  var tip = document.getElementById("tip");
  if (!tip) return;
  function mostrar(ev, txt){
    tip.textContent = txt;
    tip.style.opacity = 1;
    var x = Math.min(ev.clientX + 14, window.innerWidth - tip.offsetWidth - 10);
    tip.style.left = x + "px";
    tip.style.top = (ev.clientY - 36) + "px";
  }
  var alvos = document.querySelectorAll("[data-t]");
  for (var i = 0; i < alvos.length; i++){
    (function(el){
      el.addEventListener("mousemove", function(e){ mostrar(e, el.getAttribute("data-t")); });
      el.addEventListener("mouseleave", function(){ tip.style.opacity = 0; });
    })(alvos[i]);
  }
})();
"""
