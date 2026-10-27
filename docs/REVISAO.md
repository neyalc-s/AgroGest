# Revisão profunda - AgroGest 1.1.0

Revisão de 22/10/2026, com nova comparação das 7 páginas do enunciado e dos fluxos da aplicação. A arquitectura continua a ser Java 17, Servlets, JSP/JSTL, mapas em memória e serialização, sem base de dados.

## Falhas reproduzidas e corrigidas

| Caso | Comportamento observado antes | Correcção |
| --- | --- | --- |
| Compra enquanto o administrador edita | Stock de 462; uma compra baixou para 461; enviar o formulário antigo fez voltar a 462 | A edição confere a versão do produto e recusa o formulário desactualizado |
| Duas edições ou reposição durante uma edição | Um formulário anterior podia substituir as alterações mais recentes | Edição, compra e reposição incrementam a versão, sempre no mesmo monitor |
| Factura com nome de produto longo, ainda dentro do limite permitido | Na impressão A4 desapareceram as colunas de unidade, preço unitário e subtotal | Tabela com larguras definidas e quebra de texto, conferida no PDF impresso |
| Menu no telemóvel | Abria, mas não tinha um botão próprio para fechar e os links fora do ecrã podiam receber foco | Botão ×, toque fora, Escape e bloqueio de foco no menu fechado |

## Outras melhorias

- As consultas de produtos devolvem cópias estáveis do stock, preço e versão, em vez dos objectos internos mutáveis do repositório.
- A pasta de dados fica bloqueada durante a utilização. Uma segunda aplicação não pode carregar e gravar o mesmo estado por cima da primeira.
- A recuperação pelo backup passa a deixar uma mensagem explícita no log.
- O arranque Linux selecciona de forma coerente o Java de JAVA_HOME ou JRE_HOME e apresenta uma mensagem quando o executável configurado é inválido.
- A versão 1.1 lê os ficheiros serializados pela versão 1.0. Há um ficheiro fictício de teste produzido pelo WAR anterior para verificar essa compatibilidade.
- Foram actualizados o manual, o guia de teoria, a matriz de requisitos, o roteiro da defesa e as instruções para conservar os dados ao actualizar.

## Testes da revisão

Os resultados completos estão em VALIDACAO.md. Incluem testes Java, pedidos HTTP reais no Tomcat, operações simultâneas de clientes diferentes, reenvio simultâneo do mesmo carrinho, bloqueio entre duas JVMs, duas aplicações Tomcat com a mesma pasta e recuperação de backup depois de uma terminação abrupta simulada.

Na interface, foram verificados o menu móvel, a largura das páginas, os formulários e a impressão de uma factura com 117 caracteres no nome do produto. As cinco colunas e os valores ficaram presentes na versão revista.

## Usar os dados da versão anterior

Pare normalmente a versão anterior e faça uma cópia de segurança da pasta `dados`. Extraia o novo ZIP para outra pasta e copie para lá os dados antes de iniciar. O manual tem os passos completos. Não é necessário apagar os dados nem voltar à demonstração inicial.

Os scripts Windows e a instalação systemd continuam documentados. Esta revisão foi executada em Linux, sem um Windows ou um gestor systemd activo para validar esses dois ambientes.
