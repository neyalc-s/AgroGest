# AgroGest · Botanical Luxe

O redesign mantém Java 17, Servlets, JSP/JSTL, rotas e serviços existentes. Nenhum ficheiro Java, contrato de formulário, cálculo financeiro, permissão ou formato de persistência foi alterado.

## Sistema visual

`src/main/webapp/assets/style.css` centraliza as cores, espaçamentos, tipografia, bordas e raios.

| Token | Valor | Utilização |
| --- | --- | --- |
| `--forest` | `#183D32` | Navegação, marca, títulos e acções principais |
| `--sage` | `#527A55` | Ícones e barras do gráfico |
| `--ivory` | `#F6F1E7` | Fundo principal |
| `--soft-sage` | `#DCE6D5` | Destaques, estados positivos e resumo financeiro |
| `--gold` | `#D8A15D` | Marca, selecção e destaque do gráfico |
| `--terracotta` | `#C66A45` | Ícones de apoio |
| `--charcoal` | `#29342D` | Texto principal |
| `--botanical-grey` | `#788078` | Separadores e pormenores secundários |
| `--white` | `#FFFFFF` | Tabelas, formulários e cartões |

O texto secundário usa `--muted: #5B655B` e os pequenos títulos verdes usam `--text-sage: #466849`, variantes mais escuras para preservar contraste nas superfícies claras. Erros e acções destrutivas usam `--danger: #A33235`.

A escala `--space-1` a `--space-7` contém 4, 8, 12, 16, 24, 32 e 48 px. Os raios principais são 14 e 8 px. A tipografia Space Grotesk está alojada localmente, com licença OFL em `assets/fonts/LICENSE.txt` e alternativas de sistema. A tipografia de ecrã tem texto base de 17 px, campos de 18 px e textos auxiliares de pelo menos 14 px (em unidades rem), com títulos maiores e espaçamento adaptado. O login usa uma escala mais compacta nos botões de demonstração e nas instruções auxiliares para caber no ecrã sem deslocamento. Georgia é usada apenas nos destaques editoriais. Não existem dependências externas de fontes, ícones ou gráficos.

## Componentes e páginas

- `layout.tag`: navegação administrativa e de cliente, cabeçalho, mensagens e rodapé partilhados. Clientes e armazém passam a ter acesso directo, conservando as listagens existentes.
- `auth.tag`: registo com apresentação editorial e formulário adaptável. O login usa `tipo="login"`, fundo agrícola contínuo e uma ilha marfim vertical, arredondada, centrada na metade direita, com largura de até 540 px. A imagem de fundo é rodada 180 graus, com o círculo dourado na zona inferior esquerda e afastado do texto. As contas predefinidas usam botões compactos em duas colunas; a página e o painel cabem sem deslocamento nas dimensões verificadas, incluindo mensagens de erro.
- `marca.tag`: símbolo de crescimento/campos e assinatura AgroGest, também usados na factura; favicon correspondente.
- CSS partilhado para títulos, painéis, formulários expansíveis, campos, botões, badges, tabelas, filtros, abas, produtos, carrinho, checkout, relatórios, persistência e erros.
- Painel com métricas existentes, compras recentes, campanhas, atalhos e barras proporcionais aos valores reais das últimas cinco compras, no máximo. O gráfico não apresenta uma tendência temporal nem um relatório mensal.
- Estados de stock baseados apenas na quantidade existente e na validade. Não foi criado um limite de stock baixo.
- Navegação móvel conserva foco, Escape e isolamento do conteúdo. Uma alternativa sem JavaScript apresenta os links directamente.
- Tabelas mantêm todas as colunas com deslocamento horizontal independente e indicação acessível. Facturas conservam o formato A4 e ocultam a navegação ao imprimir.

## Execução e verificação

`dist/agrogest.war` foi actualizado para que `bash scripts/iniciar.sh` apresente o redesign pelo fluxo habitual. Os testes usam pastas temporárias; não utilizam os dados locais do utilizador.

Foi corrigida uma falha anterior ao redesign: faltava `src/test/resources/estado-v1.ser`, exigido pelo teste de compatibilidade. Foi restaurada a cópia legada existente, com SHA-256 `38fa63c5fa715413d8dd47c6ca56958aa109b61d870f4e98b7929ca052230f2b`, e adicionada a excepção no `.gitignore`.

Verificações realizadas:

- `mvn -B clean verify`: 41 testes Java aprovados.
- `python3 tests/test_http.py`: 128 verificações, incluindo produtos/stock, campanhas, cultivos, colheitas, fornecedores, relatórios, compras, facturas, permissões e persistência.
- `python3 tests/test_login_demo.py`: 24 verificações.
- `python3 tests/test_login_browser.py`: 79 verificações no Chrome, incluindo ausência de scroll na página e no painel em nove dimensões de 320×640 a 1920×1080, preenchimento por clique, erros de autenticação e registo.
- `python3 tests/test_navegacao_browser.py`: 96 verificações, incluindo submissões após navegação parcial, carrinho, compra, histórico, foco móvel e movimento reduzido.
- `python3 tests/test_botanical_browser.py`: 460 verificações a 1440, 1024, 768, 390 e 320 px, incluindo formulários abertos, registo, tabelas, estados vazios, permissões, impressão, estados reais de stock/validade e navegação móvel sem JavaScript.

Os testes de navegador precisam de Node.js, Playwright e Chromium. Aceitam `CODEX_PRIMARY_RUNTIME_NODE_MODULES`, `AGROGEST_BROWSER_EXECUTABLE` e, para capturas opcionais, `AGROGEST_VISUAL_DIR`.

A revisão visual foi efectuada no Chrome em desktop e telemóvel, incluindo uma factura exportada em A4. Outros motores de navegador não foram verificados nesta sessão. O sistema continua a registar compras sem processar pagamentos bancários, conforme o comportamento existente.
