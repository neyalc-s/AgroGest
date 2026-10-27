'use strict';

// Confirmações e impressão continuam a usar os controlos normais do browser.
document.addEventListener('submit', function (event) {
  const message = event.target.dataset.confirm;
  if (message && !window.confirm(message)) event.preventDefault();
  if (!event.defaultPrevented) cancelarNavegacao();
});
document.addEventListener('click', function (event) {
  if (event.target.closest('[data-print]')) window.print();
  if (event.target.closest('.menu-toggle')) mudarMenu(!document.body.classList.contains('nav-open'));
  if (event.target.closest('.sidebar-close,.nav-overlay')) mudarMenu(false);
});
document.addEventListener('error', function (event) {
  if (event.target.matches('img.product-photo') && !event.target.dataset.fallback) {
    event.target.dataset.fallback = 'true';
    event.target.src = document.body.dataset.fallback || document.querySelector('link[rel="icon"]').href.replace('favicon.svg', 'img/semente.svg');
  }
}, true);

const menu = document.querySelector('.sidebar');
const botaoMenu = document.querySelector('.menu-toggle');
const conteudo = document.querySelector('.workspace');
const estrutura = document.querySelector('.shell');
const estadoNavegacao = document.querySelector('#estado-navegacao');
const ecraMovel = window.matchMedia('(max-width:960px)');
let pedidoNavegacao = null;

// No telemóvel, o foco fica no menu até este ser fechado.
function mudarMenu(abrir) {
  if (!menu || !botaoMenu) return;
  abrir = abrir && ecraMovel.matches;
  const estavaAberto = document.body.classList.contains('nav-open');
  document.body.classList.toggle('nav-open', abrir);
  botaoMenu.setAttribute('aria-expanded', String(abrir));
  menu.inert = ecraMovel.matches && !abrir;
  conteudo.inert = abrir;
  if (abrir) {
    menu.setAttribute('role', 'dialog');
    menu.setAttribute('aria-modal', 'true');
  } else {
    menu.removeAttribute('role');
    menu.removeAttribute('aria-modal');
  }
  if (abrir) menu.querySelector('.sidebar-close').focus();
  else if (estavaAberto && ecraMovel.matches) botaoMenu.focus();
}
document.addEventListener('keydown', function (event) {
  if (event.key === 'Escape') mudarMenu(false);
  if (event.key !== 'Tab' || !document.body.classList.contains('nav-open')) return;
  const elementos = menu.querySelectorAll('a[href], button:not([disabled])');
  const primeiro = elementos[0];
  const ultimo = elementos[elementos.length - 1];
  if (event.shiftKey && document.activeElement === primeiro) {
    event.preventDefault();
    ultimo.focus();
  } else if (!event.shiftKey && document.activeElement === ultimo) {
    event.preventDefault();
    primeiro.focus();
  }
});
ecraMovel.addEventListener('change', () => mudarMenu(false));
mudarMenu(false);

// Um único observador é reutilizado quando muda o conteúdo da página.
function actualizarTabela(tabela) {
  const larga = tabela.scrollWidth > tabela.clientWidth + 1;
  tabela.classList.toggle('is-scrollable', larga);
  if (larga) {
    tabela.setAttribute('tabindex', '0');
    tabela.setAttribute('role', 'region');
    tabela.setAttribute('aria-label', 'Tabela com deslocamento horizontal');
  } else {
    tabela.removeAttribute('tabindex');
    tabela.removeAttribute('role');
    tabela.removeAttribute('aria-label');
  }
}
const observadorTabelas = window.ResizeObserver ? new ResizeObserver(function (entradas) {
  entradas.forEach(entrada => actualizarTabela(entrada.target));
}) : null;
if (!observadorTabelas) window.addEventListener('resize', function () {
  document.querySelectorAll('.table-wrap').forEach(actualizarTabela);
});

function iniciarConteudo() {
  document.querySelectorAll('.sidebar nav > a, .tabs > a').forEach(function (link) {
    if (link.classList.contains('active')) link.setAttribute('aria-current', 'page');
    else link.removeAttribute('aria-current');
  });
  if (observadorTabelas) observadorTabelas.disconnect();
  document.querySelectorAll('.table-wrap').forEach(function (tabela) {
    const nota = document.createElement('p');
    nota.className = 'table-hint';
    nota.textContent = 'Desloque para o lado para ver todas as colunas.';
    tabela.after(nota);
    actualizarTabela(tabela);
    if (observadorTabelas) observadorTabelas.observe(tabela);
  });
}
iniciarConteudo();

function cancelarNavegacao() {
  if (pedidoNavegacao) pedidoNavegacao.abort();
  pedidoNavegacao = null;
  document.querySelector('#conteudo').removeAttribute('aria-busy');
  if (estadoNavegacao) estadoNavegacao.textContent = '';
}

// O servidor continua a produzir a JSP. Só o conteúdo muda; menu e ícones ficam.
async function navegar(endereco, historico, deslocamento) {
  cancelarNavegacao();
  const pedido = new AbortController();
  pedidoNavegacao = pedido;
  const actual = document.querySelector('#conteudo');
  actual.setAttribute('aria-busy', 'true');
  estadoNavegacao.textContent = 'A carregar a página.';
  mudarMenu(false);
  try {
    const resposta = await fetch(endereco, {
      credentials: 'same-origin', cache: 'no-store', signal: pedido.signal
    });
    const html = await resposta.text();
    if (pedido.signal.aborted) return;
    const pagina = new DOMParser().parseFromString(html, 'text/html');
    const novaEstrutura = pagina.querySelector('.shell');
    const novoConteudo = pagina.querySelector('#conteudo');
    const novoTitulo = pagina.querySelector('.breadcrumb strong');
    // Erros, fim de sessão ou outra conta seguem a navegação normal do browser.
    if (!resposta.ok || !novaEstrutura || !novoConteudo || !novoTitulo ||
        novaEstrutura.dataset.utilizador !== estrutura.dataset.utilizador ||
        novaEstrutura.dataset.versao !== estrutura.dataset.versao) {
      window.location.assign(resposta.url || endereco);
      return;
    }
    const posicaoActual = window.scrollY;
    actual.replaceWith(document.importNode(novoConteudo, true));
    document.title = pagina.title;
    document.querySelector('.breadcrumb strong').textContent = novoTitulo.textContent;
    const novosLinks = Array.from(pagina.querySelectorAll('.sidebar nav > a'));
    menu.querySelectorAll('nav > a').forEach(function (link) {
      const novo = novosLinks.find(item => item.getAttribute('href') === link.getAttribute('href'));
      link.classList.toggle('active', !!novo && novo.classList.contains('active'));
      const contador = link.querySelector('.nav-count');
      if (contador && novo) contador.textContent = novo.querySelector('.nav-count').textContent;
    });
    const csrf = pagina.querySelector('.topbar input[name="csrf"]');
    if (csrf) document.querySelector('.topbar input[name="csrf"]').value = csrf.value;
    if (historico) {
      history.replaceState({ agrogest: true, deslocamento: posicaoActual }, '', window.location.href);
      history.pushState({ agrogest: true, deslocamento: 0 }, '', resposta.url);
    } else {
      history.replaceState(history.state, '', resposta.url);
    }
    iniciarConteudo();
    document.querySelector('#conteudo').focus({ preventScroll: true });
    window.scrollTo(0, deslocamento || 0);
    estadoNavegacao.textContent = novoTitulo.textContent + ' carregado.';
  } catch (error) {
    if (!pedido.signal.aborted) window.location.assign(endereco);
  } finally {
    if (pedidoNavegacao === pedido) {
      pedidoNavegacao = null;
      document.querySelector('#conteudo').removeAttribute('aria-busy');
    }
  }
}

if (window.fetch && window.AbortController && window.history.pushState) {
  history.replaceState({ agrogest: true, deslocamento: window.scrollY }, '', window.location.href);
  document.addEventListener('click', function (event) {
    const link = event.target.closest('.sidebar nav > a, .sidebar > a.brand');
    if (!link || event.defaultPrevented || event.button !== 0 || event.ctrlKey ||
        event.metaKey || event.shiftKey || event.altKey || link.hasAttribute('download') ||
        (link.target && link.target !== '_self') || link.origin !== window.location.origin) return;
    event.preventDefault();
    if (link.href === window.location.href) {
      cancelarNavegacao();
      mudarMenu(false);
      document.querySelector('#conteudo').focus({ preventScroll: true });
      return;
    }
    navegar(link.href, true, 0);
  });
  window.addEventListener('popstate', function (event) {
    if (event.state && event.state.agrogest) navegar(window.location.href, false, event.state.deslocamento);
    else window.location.reload();
  });
  // Um clique normal no conteúdo deixa o browser abrir a página completa.
  document.addEventListener('click', function (event) {
    const link = event.target.closest('a[href]');
    if (link && !event.defaultPrevented && event.button === 0 && !event.ctrlKey &&
        !event.metaKey && !event.shiftKey && !event.altKey && !link.hash &&
        !link.hasAttribute('download') && (!link.target || link.target === '_self')) cancelarNavegacao();
  });
}
