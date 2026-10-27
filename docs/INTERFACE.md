# Interface revista - AgroGest 1.2.3

## Correcção do menu lateral

O menu e o cabeçalho permanecem no mesmo documento quando se escolhe outra secção no menu lateral. O JavaScript pede a página ao Servlet, recebe o HTML gerado pela JSP e substitui apenas o conteúdo principal. O título, a secção seleccionada, o contador do carrinho e o histórico são actualizados. Os formulários continuam a ser enviados normalmente ao servidor.

Os ícones são SVG com os seus desenhos dentro do HTML, gerados por `icone.tag`. Deixaram de depender do carregamento separado de `icones.svg`, inclusive no login e nas páginas abertas directamente. Os mesmos elementos do menu são conservados entre cliques, evitando que os ícones desapareçam e reapareçam.

Detalhes, testes e comandos em `NAVEGACAO_1.2.3.md`. Mantêm-se as características visuais descritas abaixo.

Esta revisão aumenta a legibilidade e melhora a apresentação das páginas do cliente e do administrador. A aplicação usa CSS próprio, JavaScript simples e as mesmas páginas JSP do projecto.

## O que mudou

- Texto base de 16 px, campos de formulário de 16 px e títulos principais de 32 a 40 px. Nos produtos, os nomes usam 22 px e os preços 28 px.
- Botões principais com pelo menos 48 px de altura e campos com mais espaço interior. Os botões compactos das tabelas usam 42 px.
- Nova composição do destaque agrícola, filtros num painel próprio e contador de resultados do catálogo.
- Cartões com margens mais generosas, cores consistentes, sombras discretas e acções alinhadas.
- Entrada e registo com formulários maiores e marca visível no telemóvel.
- Menu recolhível até 960 px, incluindo tablets. Abre com transição, fecha pelo botão, por Escape ou por toque fora e devolve o foco ao botão de abertura.
- O teclado permanece no menu enquanto este está aberto. O fundo e o menu fechado não recebem foco.
- Tabelas com texto de 15 px e deslocamento horizontal dentro do painel. Uma nota indica quando existem colunas fora da área visível; é possível focar a tabela e usar as setas do teclado.
- Identificação da página actual na navegação e indicadores de foco visíveis.
- Endereços versionados dos ficheiros CSS e JavaScript para carregar os novos estilos após actualizar a aplicação.

## Animações e acessibilidade

A entrada dos blocos demora 0,45 segundos, com pequenos intervalos entre cartões. Os botões reagem em 0,2 segundos e o menu em 0,28 segundos. Os cartões e as fotografias têm um movimento discreto ao passar o ponteiro. A abertura dos formulários usa uma entrada breve do conteúdo.

Não há animações contínuas nem atrasos artificiais nas operações. Se o sistema operativo ou o browser tiver a preferência de movimento reduzido activa, a regra CSS `prefers-reduced-motion: reduce` desliga as animações, as transições e os deslocamentos ao passar o ponteiro. Os controlos continuam a funcionar.

A impressão tem regras próprias: retira a navegação e as animações, conserva as cinco colunas da factura e ajusta a tabela à folha A4.

## Onde estudar o código

| Ficheiro | O que observar |
| --- | --- |
| `src/main/webapp/assets/style.css` | Variáveis de cor, tamanhos, disposição, estados de foco, animações, regras para telemóvel e impressão |
| `src/main/webapp/assets/app.js` | Menu, navegação parcial, histórico, gestão de foco, confirmações e tabelas largas |
| `src/main/webapp/WEB-INF/tags/icone.tag` | Desenhos SVG presentes no HTML, sem pedido separado |
| `src/main/webapp/WEB-INF/tags/layout.tag` | Estrutura comum do menu, cabeçalho e conteúdo |
| `src/main/webapp/WEB-INF/tags/auth.tag` | Estrutura da entrada e do registo |
| `src/main/webapp/WEB-INF/views/catalogo.jsp` | Filtros, contador e cartões com dados enviados pelo Servlet |

O CSS define a apresentação. As media queries adaptam a composição à largura do ecrã e à preferência de movimento. As transições suavizam uma mudança de estado, como passar o ponteiro num botão. Os keyframes definem os estados inicial e final de uma animação, como a entrada de um painel.

O JavaScript acrescenta interacções no browser. O atributo `inert` impede o foco e a interacção numa área temporariamente indisponível. `aria-expanded` comunica se o menu está aberto e `aria-current` identifica a página actual. O `ResizeObserver` volta a verificar se uma tabela precisa de deslocamento quando a sua largura muda.

As contas de demonstração, as operações e os comandos de execução encontram-se no README. Os testes realizados nesta revisão estão em `VALIDACAO.md`.
