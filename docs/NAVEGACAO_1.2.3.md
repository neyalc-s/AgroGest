# Navegação e ícones — versão 1.2.3

## Problema corrigido

Cada clique no menu lateral abria um documento completo. Os ícones usavam referências a um ficheiro SVG separado, cuja apresentação precisava de ser refeita a cada página. Na versão 1.2.2, três cliques foram reproduzidos no navegador: três novos documentos, três pedidos ao SVG e substituição do menu inteiro.

## Comportamento actual

- O menu lateral, os seus ícones e o cabeçalho permanecem no mesmo documento ao escolher outra secção.
- O Servlet e a JSP continuam a gerar o conteúdo no servidor. O JavaScript recebe esse HTML e actualiza o conteúdo principal, o título, a secção activa e o contador do carrinho.
- Cada ícone inclui os próprios caminhos SVG no HTML. Não precisa de obter um ficheiro de ícones para aparecer, mesmo numa abertura directa ou no login.
- Voltar e Avançar recuperam a página pelo servidor. Não é guardada uma cópia de dados antigos no JavaScript.
- Um novo clique cancela o pedido anterior, evitando que uma resposta atrasada substitua o último destino escolhido.
- No telemóvel, o menu fecha e o foco passa para o conteúdo. A gestão do teclado e a preferência de movimento reduzido mantêm-se.
- Formulários, compras, confirmações, impressão e logout continuam a usar os controlos e pedidos normais.
- Links modificados, por exemplo Ctrl+clique, mantêm o comportamento normal do browser. Sem JavaScript, os links continuam a abrir as páginas completas.
- Uma falha do pedido parcial, um erro do servidor, uma sessão terminada ou uma mudança de conta/versão encaminham para a navegação normal.

O JavaScript usa as APIs do browser, sem bibliotecas ou dependências novas. As validações de sessão, perfil, CSRF, stock, preços e persistência continuam no servidor.

## Testar no navegador

Os testes opcionais requerem Node.js, Playwright e um browser Chromium instalado para o Playwright. Execute a partir da pasta `AgroGest`, com a porta 8081 livre:

```bash
python3 tests/test_navegacao_browser.py
python3 tests/test_login_browser.py
```

As suites iniciam uma aplicação com dados temporários e encerram-na no fim. Se usar um Chromium noutro caminho, defina `AGROGEST_BROWSER_EXECUTABLE`. Estas dependências servem apenas para os testes; não são necessárias para executar a aplicação.

O novo teste verifica a identidade dos elementos do menu e dos ícones, pedidos de rede, todas as secções administrativas, histórico, cliques rápidos, formulários recebidos pela navegação parcial, carrinho, compra, factura, falha de rede, fim de sessão e menu móvel com e sem movimento reduzido. Os resultados executados estão em `VALIDACAO.md`.

## Instalar a actualização

Pare normalmente a versão anterior, conserve uma cópia da pasta `dados` e extraia o ZIP completo. Copie os seus dados para a nova pasta e inicie pelos scripts habituais. Não use duas instâncias sobre a mesma pasta de dados. Se mantiver uma aba antiga aberta, recarregue-a depois de iniciar a nova versão.

O WAR incluído já contém a correcção. O selector temporário de utilizadores continua ligado pelos scripts de arranque e aguarda o botão **Entrar**, como na versão 1.2.2.
