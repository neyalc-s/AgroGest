/* Executado por test_login_browser.py, com dados temporários. */
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
let count = 0;
const keepAlive = setInterval(() => {}, 1000);
function check(name, condition) { assert.ok(condition, name); count++; console.log('OK:', name); }

(async function () {
  const options = { headless: true, args: ['--no-sandbox'] };
  if (process.env.AGROGEST_BROWSER_EXECUTABLE) options.executablePath = process.env.AGROGEST_BROWSER_EXECUTABLE;
  const browser = await playwright.chromium.launch(options);
  const errors = [];
  const emails = ['admin@agro.mz', 'ana@teste.mz', 'joao@teste.mz', 'amelia@teste.mz'];
  try {
    for (const width of [1440, 390]) {
      for (const email of emails) {
        const context = await browser.newContext({ viewport: { width, height: 980 }, reducedMotion: 'reduce' });
        const page = await context.newPage();
        page.on('pageerror', error => errors.push(error.message));
        let posts = 0;
        page.on('request', request => { if (request.method() === 'POST' && request.url() === base + '/login') posts++; });
        await page.goto(base + '/login');
        const account = page.locator('[data-demo-account]').filter({ has: page.locator('small', { hasText: email }) });
        check('Todas as contas visíveis a ' + width + ' px', await page.locator('[data-demo-account]').count() === 4);
        await account.click();
        check('Clique preenche email e senha a ' + width + ' px: ' + email,
          await page.locator('input[name="email"]').inputValue() === email &&
          await page.locator('input[name="senha"]').inputValue() === await account.getAttribute('data-demo-senha'));
        check('Clique aguarda o botão Entrar: ' + email, page.url().endsWith('/login') && posts === 0);
        check('Uma só conta fica seleccionada', await page.locator('[data-demo-account][aria-pressed="true"]').count() === 1);
        check('Página cabe na largura do ecrã', await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth));
        if (email === 'admin@agro.mz' && process.env.AGROGEST_VISUAL_DIR) {
          fs.mkdirSync(process.env.AGROGEST_VISUAL_DIR, { recursive: true });
          await page.screenshot({ path: path.join(process.env.AGROGEST_VISUAL_DIR, 'login-' + width + '.png'), fullPage: true });
        }
        await page.getByRole('button', { name: 'Entrar', exact: false }).click();
        await page.waitForURL(base + (email === 'admin@agro.mz' ? '/admin' : '/catalogo'));
        check('Botão Entrar abre o perfil correcto: ' + email, posts === 1);
        await context.close();
      }
    }
    const context = await browser.newContext({ viewport: { width: 390, height: 980 }, reducedMotion: 'reduce' });
    const page = await context.newPage();
    page.on('pageerror', error => errors.push(error.message));
    await page.goto(base + '/registo');
    await page.locator('input[name="nome"]').fill('Cliente do navegador');
    await page.locator('input[name="email"]').fill('navegador@teste.mz');
    await page.locator('input[name="telefone"]').fill('841234567');
    await page.locator('input[name="endereco"]').fill('Nampula');
    await page.locator('input[name="senha"]').fill('senha-navegador');
    await page.locator('input[name="confirmacao"]').fill('senha-navegador');
    await page.getByRole('button', { name: 'Criar a minha conta' }).click();
    await page.waitForURL(base + '/catalogo');
    await page.getByRole('button', { name: 'Terminar sessão' }).click();
    await page.waitForURL(base + '/login');
    const fresh = page.locator('[data-demo-account]').filter({ has: page.locator('small', { hasText: 'navegador@teste.mz' }) });
    check('Conta recém-criada aparece automaticamente', await fresh.count() === 1 && await page.locator('[data-demo-account]').count() === 5);
    await fresh.click();
    check('Conta nova também preenche os dois campos', await page.locator('input[name="email"]').inputValue() === 'navegador@teste.mz' && (await page.locator('input[name="senha"]').inputValue()).startsWith('demo-'));
    await page.getByRole('button', { name: 'Entrar', exact: false }).click();
    await page.waitForURL(base + '/catalogo');
    check('Conta nova entra depois do botão', page.url().endsWith('/catalogo'));
    await context.close();
    check('Sem erros JavaScript', errors.length === 0);
    console.log(JSON.stringify({ verificacoes: count, resultado: 'PASSOU' }));
  } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; }).finally(() => clearInterval(keepAlive));
