/* Executado por test_navegacao_browser.py, sempre com dados temporários. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
let playwright;
try { playwright = require('playwright'); }
catch (error) {
  if (!process.env.CODEX_PRIMARY_RUNTIME_NODE_MODULES) throw error;
  playwright = require(path.join(process.env.CODEX_PRIMARY_RUNTIME_NODE_MODULES, 'playwright'));
}
const base = 'http://127.0.0.1:8081/agrogest';
const keepAlive = setInterval(() => {}, 1000);
let count = 0;
function check(name, condition) { assert.ok(condition, name); count++; console.log('OK:', name); }

async function login(page, email) {
  await page.goto(base + '/login');
  await page.locator('input[name="email"]').fill(email);
  await page.locator('input[name="senha"]').fill(email === 'admin@agro.mz' ? 'admin234' : 'cliente123');
  await page.getByRole('button', { name: 'Entrar', exact: false }).click();
  await page.waitForURL(base + (email === 'admin@agro.mz' ? '/admin' : '/catalogo'));
}
async function lembrar(page) {
  await page.evaluate(() => {
    window.elementosOriginais = {
      documento: document.documentElement, menu: document.querySelector('.sidebar'),
      cabecalho: document.querySelector('.topbar'),
      icones: Array.from(document.querySelectorAll('.sidebar .icon, .topbar .icon'))
    };
  });
}
async function preservado(page) {
  return page.evaluate(() => {
    const p = window.elementosOriginais;
    return !!p && p.documento === document.documentElement && p.menu === document.querySelector('.sidebar') &&
      p.cabecalho === document.querySelector('.topbar') &&
      p.icones.every((icone, i) => icone === document.querySelectorAll('.sidebar .icon, .topbar .icon')[i]);
  });
}
async function ir(page, route, mobile = false) {
  if (mobile) await page.locator('.menu-toggle').click();
  await page.locator('.sidebar nav > a[href="/agrogest' + route + '"]').click();
  await page.waitForURL(base + route);
  await page.waitForFunction(() => !document.querySelector('#conteudo').hasAttribute('aria-busy'));
}

(async function () {
  const options = { headless: true, args: ['--no-sandbox'] };
  if (process.env.AGROGEST_BROWSER_EXECUTABLE) options.executablePath = process.env.AGROGEST_BROWSER_EXECUTABLE;
  const browser = await playwright.chromium.launch(options);
  const errors = [];
  const context = await browser.newContext({ viewport: { width: 1440, height: 980 } });
  const page = await context.newPage();
  page.on('pageerror', e => errors.push(e.message));
  page.on('response', r => { if (r.status() >= 500) errors.push('HTTP ' + r.status() + ' ' + r.url()); });
  let documents = 0, sprites = 0, fetches = 0;
  page.on('request', r => {
    if (r.resourceType() === 'document') documents++;
    if (r.url().includes('/icones.svg')) sprites++;
    if (r.resourceType() === 'fetch') fetches++;
  });
  try {
    await login(page, 'admin@agro.mz');
    await lembrar(page);
    const initialDocuments = documents;
    const routes = ['/admin/fornecedores', '/admin/produtos', '/admin/campanhas', '/admin/cultivos',
      '/admin/colheitas', '/admin/compras', '/admin/listagens', '/admin/relatorios', '/admin/persistencia', '/admin'];
    for (const route of routes) {
      await ir(page, route);
      check('Menu, cabeçalho e ícones conservados: ' + route, await preservado(page));
      check('Título e indicação de página actual: ' + route,
        (await page.title()).startsWith(await page.locator('.breadcrumb strong').textContent()) &&
        await page.locator('.sidebar nav > a.active[aria-current="page"]').count() === 1 &&
        await page.locator('.sidebar nav > a.active').getAttribute('href') === '/agrogest' + route);
      check('Tabelas inicializadas uma vez: ' + route,
        await page.locator('.table-wrap').count() === await page.locator('.table-hint').count());
    }
    check('Nenhum carregamento completo nos dez cliques', documents === initialDocuments);
    check('Nenhum pedido externo dos ícones', sprites === 0);
    check('Os SVG contêm o desenho imediatamente', await page.locator('.icon use').count() === 0 &&
      await page.locator('.sidebar .icon path, .sidebar .icon rect, .sidebar .icon circle').count() > 10);
    const beforeSame = fetches;
    await ir(page, '/admin');
    check('Clique na página actual não pede nem recria a página', fetches === beforeSame && await preservado(page));

    await ir(page, '/admin/produtos');
    await ir(page, '/admin/campanhas');
    await page.goBack();
    await page.waitForFunction(() => document.querySelector('.breadcrumb strong').textContent === 'Produtos' &&
      !document.querySelector('#conteudo').hasAttribute('aria-busy'));
    check('Voltar recupera produtos e mantém o menu', page.url() === base + '/admin/produtos' && await preservado(page));
    await page.goForward();
    await page.waitForFunction(() => document.querySelector('.breadcrumb strong').textContent === 'Campanhas' &&
      !document.querySelector('#conteudo').hasAttribute('aria-busy'));
    check('Avançar recupera campanhas e mantém o menu', page.url() === base + '/admin/campanhas' && await preservado(page));

    // A resposta do primeiro clique é atrasada até o segundo já estar concluído.
    let release, received, finished;
    const gate = new Promise(resolve => { release = resolve; });
    const intercepted = new Promise(resolve => { received = resolve; });
    const ended = new Promise(resolve => { finished = resolve; });
    await page.route('**/agrogest/admin/produtos', async route => {
      const response = await route.fetch();
      received();
      await gate;
      try { await route.fulfill({ response }); } catch (error) { /* Pedido cancelado pelo segundo clique. */ }
      finished();
    });
    await page.locator('.sidebar nav > a[href$="/admin/produtos"]').click();
    await intercepted;
    await ir(page, '/admin/fornecedores');
    release();
    await ended;
    check('Cliques rápidos conservam o último destino', page.url() === base + '/admin/fornecedores' &&
      await page.locator('.breadcrumb strong').textContent() === 'Fornecedores' && await preservado(page));
    await page.unroute('**/agrogest/admin/produtos');

    const beforeModified = fetches;
    await page.locator('.sidebar nav > a[href$="/admin/produtos"]').evaluate(link => {
      const event = new MouseEvent('click', { bubbles: true, cancelable: true, ctrlKey: true });
      link.dispatchEvent(event);
      window.cliqueModificadoInterceptado = event.defaultPrevented;
    });
    check('Ctrl+clique fica a cargo do browser', fetches === beforeModified &&
      !await page.evaluate(() => window.cliqueModificadoInterceptado));

    // Um formulário que chegou por navegação parcial conserva CSRF e submissão.
    await page.locator('details > summary').click();
    const form = page.locator('form').filter({ has: page.locator('input[name="acao"][value="criar"]') });
    await form.locator('input[name="nome"]').fill('Fornecedor Navegador 123');
    await form.locator('input[name="contacto"]').fill('841112233');
    await form.locator('input[name="provincia"]').fill('Nampula');
    await form.getByRole('button', { name: 'Criar fornecedor' }).click();
    await page.waitForFunction(() => document.querySelector('tbody').textContent.includes('Fornecedor Navegador 123'));
    check('Criar fornecedor funciona após navegação parcial', await page.locator('tbody').textContent().then(t => t.includes('Fornecedor Navegador 123')));
    await ir(page, '/admin/produtos');
    await ir(page, '/admin/fornecedores');
    const row = page.locator('tr').filter({ hasText: 'Fornecedor Navegador 123' });
    page.once('dialog', dialog => dialog.dismiss());
    await row.getByRole('button', { name: 'Remover' }).click();
    check('Confirmação cancelada mantém fornecedor', await row.count() === 1);
    page.once('dialog', dialog => dialog.accept());
    await row.getByRole('button', { name: 'Remover' }).click();
    await page.waitForFunction(() => !document.querySelector('tbody').textContent.includes('Fornecedor Navegador 123'));
    check('Confirmação aceite remove fornecedor', await row.count() === 0);
    await ir(page, '/admin/persistencia');
    await page.getByRole('button', { name: 'Gravar agora' }).click();
    await page.waitForSelector('.alert.success');
    check('Gravação manual funciona após troca de página', await page.getByText('Ficheiro gravado', { exact: true }).count() === 1);

    if (process.env.AGROGEST_VISUAL_DIR) {
      fs.mkdirSync(process.env.AGROGEST_VISUAL_DIR, { recursive: true });
      await page.screenshot({ path: path.join(process.env.AGROGEST_VISUAL_DIR, 'navegacao-desktop.png'), fullPage: true, animations: 'disabled' });
    }
    await page.getByRole('button', { name: 'Terminar sessão' }).click();
    await page.waitForURL(base + '/login');
    check('Logout conserva funcionamento normal', await page.locator('[data-demo-account]').count() === 4);

    // Cliente: contador, formulários, confirmação, factura e histórico reais.
    await login(page, 'amelia@teste.mz');
    await lembrar(page);
    await ir(page, '/carrinho');
    await ir(page, '/catalogo');
    const product = page.locator('.product-card').filter({ hasText: 'Sementes de milho' });
    await product.locator('input[name="quantidade"]').fill('1');
    await product.getByRole('button', { name: 'Adicionar' }).click();
    await page.waitForSelector('.alert.success');
    await lembrar(page);
    await ir(page, '/carrinho');
    check('Carrinho e contador actualizados', (await page.locator('.nav-count').textContent()).trim() === '1' &&
      (await page.locator('#conteudo').textContent()).includes('Sementes de milho') && await preservado(page));
    await page.getByRole('link', { name: 'Finalizar compra' }).click();
    await page.waitForURL(base + '/compra');
    check('Resumo mantém token da confirmação', (await page.locator('input[name="confirmacaoCompra"]').inputValue()).length > 20);
    await page.getByRole('button', { name: 'Confirmar compra' }).click();
    await page.waitForURL('**/factura?id=*');
    check('Compra emite factura com o item e IVA', (await page.locator('.invoice').textContent()).includes('Sementes de milho') &&
      (await page.locator('.invoice').textContent()).includes('IVA (16%)'));
    await lembrar(page);
    await ir(page, '/minhas-compras');
    check('Histórico mostra a compra com navegação parcial', await page.locator('tbody a[href*="/factura?id="]').count() === 1 && await preservado(page));
    await ir(page, '/carrinho');
    check('Carrinho vazio depois da compra', (await page.locator('#conteudo').textContent()).includes('Ainda há espaço') &&
      (await page.locator('.nav-count').textContent()).trim() === '0');

    // Falha de rede apenas no fetch: o link abre por navegação normal.
    await page.route('**/agrogest/catalogo', route => route.request().resourceType() === 'fetch' ? route.abort() : route.continue());
    const beforeFallback = documents;
    await ir(page, '/catalogo');
    check('Falha de fetch tem navegação normal de recurso', documents === beforeFallback + 1 &&
      await page.locator('.product-card').count() === 4);
    await page.unroute('**/agrogest/catalogo');
    await lembrar(page);
    await context.clearCookies();
    await page.locator('.sidebar nav > a[href$="/carrinho"]').click();
    await page.waitForURL(base + '/login');
    check('Fim de sessão abre login sem manter conteúdo antigo', await page.locator('.sidebar').count() === 0);
    await context.close();

    for (const reducedMotion of ['no-preference', 'reduce']) {
      for (const email of ['admin@agro.mz', 'joao@teste.mz']) {
        const mobile = await browser.newContext({ viewport: { width: 390, height: 980 }, reducedMotion });
        const p = await mobile.newPage();
        p.on('pageerror', e => errors.push(e.message));
        await login(p, email);
        await lembrar(p);
        const mobileRoutes = email === 'admin@agro.mz' ? ['/admin/produtos', '/admin/relatorios', '/admin/compras'] : ['/carrinho', '/minhas-compras', '/catalogo'];
        for (const route of mobileRoutes) {
          await ir(p, route, true);
          check('Menu e ícones preservados em 390px: ' + email + ' / ' + reducedMotion + ' / ' + route, await preservado(p));
          check('Menu fecha e o conteúdo recebe foco', await p.evaluate(() =>
            !document.body.classList.contains('nav-open') && document.querySelector('.sidebar').inert &&
            !document.querySelector('.workspace').inert && document.activeElement.id === 'conteudo'));
          check('Página sem transbordo horizontal', await p.evaluate(() => document.documentElement.scrollWidth <= innerWidth));
        }
        await p.locator('.menu-toggle').click();
        check('Menu reabre depois da navegação', await p.evaluate(() => document.querySelector('.workspace').inert &&
          document.activeElement.classList.contains('sidebar-close')));
        await p.keyboard.press('Escape');
        check('Escape fecha e devolve foco', await p.evaluate(() => !document.body.classList.contains('nav-open') &&
          document.activeElement.classList.contains('menu-toggle')));
        if (process.env.AGROGEST_VISUAL_DIR && email === 'admin@agro.mz' && reducedMotion === 'no-preference') {
          await p.screenshot({ path: path.join(process.env.AGROGEST_VISUAL_DIR, 'navegacao-mobile.png'), fullPage: true, animations: 'disabled' });
        }
        await mobile.close();
      }
    }
    const noJs = await browser.newContext({ javaScriptEnabled: false, reducedMotion: 'reduce' });
    const n = await noJs.newPage();
    await login(n, 'admin@agro.mz');
    await n.locator('.sidebar nav > a[href$="/admin/produtos"]').click();
    await n.waitForURL(base + '/admin/produtos');
    check('Links continuam funcionais sem JavaScript', (await n.locator('h1').textContent()).includes('Produtos') &&
      await n.locator('.icon path, .icon rect').count() > 10);
    await noJs.close();
    check('Sem erros JavaScript nem HTTP 500', errors.length === 0);
    console.log(JSON.stringify({ verificacoes: count, resultado: 'PASSOU' }));
  } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; }).finally(() => clearInterval(keepAlive));
