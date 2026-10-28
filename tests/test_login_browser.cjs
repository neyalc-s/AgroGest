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
    for (const [width, height] of [[1920,1080], [1440,900], [1366,768], [1280,720], [1024,768], [768,1024], [390,844], [375,667], [320,640]]) {
      const context = await browser.newContext({ viewport: { width, height }, reducedMotion: 'reduce' });
      const page = await context.newPage();
      page.on('pageerror', error => errors.push(error.message));
      await page.goto(base + '/login');
      await page.evaluate(() => document.fonts.ready);
      const geometry = () => page.evaluate(() => {
        const card = document.querySelector('.auth-card');
        const bg = document.querySelector('.auth-backdrop').getBoundingClientRect();
        const bounds = card.getBoundingClientRect();
        return {
          pageFits: document.documentElement.scrollHeight <= innerHeight && document.documentElement.scrollWidth <= innerWidth,
          cardFits: card.scrollHeight <= card.clientHeight + 1,
          backgroundCovers: bg.left <= 0 && bg.top <= 0 && bg.right >= innerWidth && bg.bottom >= innerHeight,
          island: Number.parseFloat(getComputedStyle(card).borderRadius) >= 20 && bounds.top >= 0 && bounds.bottom <= innerHeight,
          rightHalf: bounds.left >= innerWidth / 2,
          presetsFit: [...document.querySelectorAll('[data-demo-account]')].every(button => {
            const r = button.getBoundingClientRect();
            return r.top >= bounds.top && r.bottom <= bounds.bottom;
          })
        };
      });
      const normal = await geometry();
      if (!normal.cardFits || !normal.presetsFit) {
        console.log(await page.locator('.auth-card').evaluate(card => ({ height: card.clientHeight, content: card.scrollHeight, children: [...card.children].map(el => ({ tag: el.tagName, cls: el.className, height: el.getBoundingClientRect().height })) })));
        if (process.env.AGROGEST_VISUAL_DIR) await page.screenshot({ path: path.join(process.env.AGROGEST_VISUAL_DIR, 'login-island-overflow.png') });
      }
      check('Login sem deslocamento de página nem painel: ' + width + 'x' + height, normal.pageFits && normal.cardFits && normal.presetsFit);
      check('Imagem contínua e ilha arredondada: ' + width + 'x' + height, normal.backgroundCovers && normal.island && (width <= 960 || normal.rightHalf));
      if (process.env.AGROGEST_VISUAL_DIR) {
        fs.mkdirSync(process.env.AGROGEST_VISUAL_DIR, { recursive: true });
        await page.screenshot({ path: path.join(process.env.AGROGEST_VISUAL_DIR, 'login-island-' + width + 'x' + height + '.png'), fullPage: true });
      }
      await page.locator('input[name="email"]').fill('admin@agro.mz');
      await page.locator('input[name="senha"]').fill('incorrect-password');
      await page.getByRole('button', { name: 'Entrar', exact: false }).click();
      await page.waitForSelector('.alert.error');
      const error = await geometry();
      check('Erro de login continua visível sem deslocamento: ' + width + 'x' + height, error.pageFits && error.cardFits && error.presetsFit);
      await context.close();
    }
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
          await page.locator('input[name="senha"]').inputValue() === (email === 'admin@agro.mz' ? 'admin234' : 'cliente123'));
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
    check('Conta nova também preenche os dois campos', await page.locator('input[name="email"]').inputValue() === 'navegador@teste.mz' && await page.locator('input[name="senha"]').inputValue() === 'cliente123');
    await page.getByRole('button', { name: 'Entrar', exact: false }).click();
    await page.waitForURL(base + '/catalogo');
    check('Conta nova entra depois do botão', page.url().endsWith('/catalogo'));
    await context.close();
    check('Sem erros JavaScript', errors.length === 0);
    console.log(JSON.stringify({ verificacoes: count, resultado: 'PASSOU' }));
  } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; }).finally(() => clearInterval(keepAlive));
