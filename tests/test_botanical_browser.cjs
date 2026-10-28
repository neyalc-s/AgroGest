/* Run through test_botanical_browser.py, never against the user's saved data. */
const assert = require('node:assert/strict');
const path = require('node:path');
const fs = require('node:fs');
let playwright;
try { playwright = require('playwright'); }
catch (error) {
  if (!process.env.CODEX_PRIMARY_RUNTIME_NODE_MODULES) throw error;
  playwright = require(path.join(process.env.CODEX_PRIMARY_RUNTIME_NODE_MODULES, 'playwright'));
}
const base = 'http://127.0.0.1:8081/agrogest';
const output = process.env.AGROGEST_VISUAL_DIR;
const errors = [];
let checks = 0;
function check(label, value) { assert.ok(value, label); checks++; console.log('OK:', label); }
async function screenshot(page, name) {
  if (!output) return;
  fs.mkdirSync(output, { recursive: true });
  await page.screenshot({ path: path.join(output, name + '.png'), fullPage: true, animations: 'disabled' });
}
async function visit(page, route, width) {
  const response = await page.goto(base + route);
  check('Route renders: ' + route + ' at ' + width, response.status() === 200);
  await page.evaluate(() => document.fonts.ready);
  check('No page overflow: ' + route + ' at ' + width,
    await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth));
  check('Visible heading: ' + route, await page.locator('main h1').isVisible());
  check('No missing icons: ' + route, await page.locator('svg.icon').evaluateAll(icons =>
    icons.every(icon => icon.querySelector('path,rect,circle'))));
}
(async () => {
  const browser = await playwright.chromium.launch({
    headless: true, executablePath: process.env.AGROGEST_BROWSER_EXECUTABLE, args: ['--no-sandbox']
  });
  try {
    for (const width of [1440, 1024, 768, 390, 320]) {
      const context = await browser.newContext({ viewport: { width, height: 1000 }, reducedMotion: 'reduce' });
      const page = await context.newPage();
      page.on('pageerror', e => errors.push(e.message));
      page.on('response', r => { if (r.status() >= 500) errors.push(r.status() + ' ' + r.url()); });
      await visit(page, '/registo', width);
      if ([1440, 390].includes(width)) await screenshot(page, 'registo-' + width);
      await visit(page, '/login', width);
      if ([1440, 390].includes(width)) await screenshot(page, 'login-' + width);
      await page.locator('[data-demo-email="admin@agro.mz"]').click();
      await page.getByRole('button', { name: 'Entrar', exact: false }).click();
      await page.waitForURL(base + '/admin');
      for (const route of ['/admin', '/admin/produtos', '/admin/produtos?editar=1', '/admin/fornecedores',
        '/admin/campanhas', '/admin/cultivos', '/admin/colheitas', '/admin/clientes', '/admin/armazem',
        '/admin/compras', '/admin/listagens', '/admin/relatorios', '/admin/persistencia', '/factura?id=1']) {
        await visit(page, route, width);
        if (route === '/admin') {
          check('Chart values match actual recent purchases',
            await page.locator('.chart-label strong').allTextContents().then(async values => {
              const totals = await page.locator('tbody .num strong').allTextContents();
              return JSON.stringify(values) === JSON.stringify(totals);
            }));
        }
        const details = page.locator('details.panel');
        if (await details.count()) {
          await details.evaluateAll(items => items.forEach(item => { item.open = true; }));
          check('Expanded forms fit viewport: ' + route + ' at ' + width,
            await page.locator('details input:not([type="hidden"]), details select, details textarea')
              .evaluateAll(items => items.every(item => {
                const r = item.getBoundingClientRect();
                return r.left >= 0 && r.right <= innerWidth + 1;
              })));
        }
        if ([1440, 390].includes(width) && ['/admin', '/admin/produtos?editar=1', '/admin/campanhas', '/admin/relatorios', '/factura?id=1'].includes(route)) {
          await screenshot(page, route.replace(/\W+/g, '-') + '-' + width);
        }
        if (route === '/factura?id=1' && width === 1440 && output) {
          await page.emulateMedia({ media: 'print' });
          check('Print hides application navigation', !await page.locator('.sidebar').isVisible());
          await page.pdf({ path: path.join(output, 'factura.pdf'), format: 'A4' });
          await page.emulateMedia({ media: 'screen' });
        }
      }
      await page.getByRole('button', { name: 'Terminar sessão' }).click();
      await page.waitForURL(base + '/login');
      await page.locator('[data-demo-email="amelia@teste.mz"]').click();
      await page.getByRole('button', { name: 'Entrar', exact: false }).click();
      await page.waitForURL(base + '/catalogo');
      await visit(page, '/catalogo', width);
      if ([1440, 390].includes(width)) await screenshot(page, 'catalogo-' + width);
      await page.locator('input[name="q"]').fill('naoexiste-botanical');
      await page.getByRole('button', { name: 'Filtrar', exact: true }).click();
      check('Real catalogue empty state', await page.getByText('Nenhum produto encontrado.', { exact: true }).isVisible());
      await page.goto(base + '/catalogo');
      const product = page.locator('.product-card').filter({ hasText: 'Sementes de milho' });
      await product.getByRole('button', { name: 'Adicionar', exact: true }).click();
      await page.waitForSelector('.alert.success');
      await visit(page, '/carrinho', width);
      if ([1440, 390].includes(width)) await screenshot(page, 'carrinho-' + width);
      await visit(page, '/compra', width);
      if ([1440, 390].includes(width)) await screenshot(page, 'compra-' + width);
      await visit(page, '/minhas-compras', width);
      const denied = await page.goto(base + '/admin');
      check('Customer permissions retained', denied.status() === 403);
      check('Error page fits viewport', await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth));
      await context.close();
    }
    // Actual inventory edits exercise readable critical states and unavailable shopping actions.
    const states = await browser.newContext({ viewport: { width: 390, height: 1000 }, reducedMotion: 'reduce' });
    const statePage = await states.newPage();
    statePage.on('pageerror', e => errors.push(e.message));
    await statePage.goto(base + '/login');
    await statePage.locator('[data-demo-email="admin@agro.mz"]').click();
    await statePage.getByRole('button', { name: 'Entrar', exact: false }).click();
    await statePage.waitForURL(base + '/admin');
    for (const [id, field, value] of [[1, 'validade', '2000-01-01'], [2, 'quantidade', '0']]) {
      await statePage.goto(base + '/admin/produtos?editar=' + id);
      await statePage.locator('details.panel input[name="' + field + '"]').fill(value);
      await statePage.getByRole('button', { name: 'Guardar produto', exact: true }).click();
      await statePage.waitForSelector('.alert.success');
      check('Inventory edit submits successfully: ' + field, await statePage.locator('.alert.success').isVisible());
    }
    check('Expired inventory has a textual status', await statePage.locator('tr').filter({ hasText: 'Sementes de milho' }).getByText(/Expirada/).isVisible());
    check('Zero-stock inventory has a textual status', await statePage.locator('tr').filter({ hasText: 'Fertilizante NPK' }).getByText('Sem stock', { exact: true }).isVisible());
    await statePage.getByRole('button', { name: 'Terminar sessão' }).click();
    await statePage.waitForURL(base + '/login');
    await statePage.locator('[data-demo-email="amelia@teste.mz"]').click();
    await statePage.getByRole('button', { name: 'Entrar', exact: false }).click();
    await statePage.waitForURL(base + '/catalogo');
    for (const [name, status] of [['Sementes de milho', 'Validade expirada'], ['Fertilizante NPK', 'Sem stock']]) {
      const card = statePage.locator('.product-card').filter({ hasText: name });
      check('Unavailable product communicates reason and disables purchase: ' + name,
        await card.getByText(status, { exact: true }).isVisible() && await card.getByRole('button', { name: 'Adicionar', exact: true }).isDisabled());
    }
    await states.close();
    const noJs = await browser.newContext({ javaScriptEnabled: false, viewport: { width: 390, height: 900 } });
    const fallback = await noJs.newPage();
    await fallback.goto(base + '/login');
    await fallback.locator('input[name="email"]').fill('admin@agro.mz');
    await fallback.locator('input[name="senha"]').fill('admin234');
    await fallback.getByRole('button', { name: 'Entrar', exact: false }).click();
    await fallback.waitForURL(base + '/admin');
    check('Mobile navigation works without JavaScript', await fallback.locator('.sidebar nav > a[href$="/admin/produtos"]').isVisible());
    await fallback.locator('.sidebar nav > a[href$="/admin/produtos"]').click();
    await fallback.waitForURL(base + '/admin/produtos');
    check('No-JavaScript mobile page fits', await fallback.evaluate(() => document.documentElement.scrollWidth <= innerWidth));
    await noJs.close();
    check('No JavaScript or server errors', errors.length === 0);
    console.log(JSON.stringify({ verificacoes: checks, resultado: 'PASSOU' }));
  } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
