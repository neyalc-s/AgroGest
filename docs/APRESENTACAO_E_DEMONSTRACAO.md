# Roteiro de apresentação e demonstração

Este roteiro permite apresentar o projecto em cerca de 8 a 10 minutos. Os tempos são orientativos. Estuda primeiro o Guia_Teoria_Defesa e adapta a linguagem à tua forma de falar.

## 1. Problema e objectivo - cerca de 1 minuto

**Podes dizer:**

“O nosso trabalho é o AgroGest, um sistema de gestão agrícola para o contexto de um agricultor de Nampula. O objectivo é organizar fornecedores e produtos, acompanhar campanhas, cultivos e colheitas e permitir que clientes comprem produtos com emissão de factura. A aplicação tem dois perfis: administrador e cliente.”

“Seguimos a restrição do enunciado: os dados ficam em memória, sem base de dados. Para conservar o estado entre execuções, usamos serialização Java para ficheiro.”

**Mostrar:** página de login e contas de demonstração. Não é necessário ler todas as funcionalidades de uma vez.

## 2. Tecnologias e MVC - cerca de 1 minuto

**Podes dizer:**

“A aplicação usa Java 17, Jakarta Servlets, JSP com JSTL e Tomcat 10.1. O padrão MVC separa os dados e as regras de negócio, os controladores que recebem pedidos e as páginas que mostram os resultados.”

“Por exemplo, o CompraServlet recebe a confirmação, o VendaService valida o stock e cria a venda, e a factura.jsp apresenta a factura. O AuthFilter verifica a sessão e as permissões antes de executar as rotas protegidas.”

**Mostrar:** as pastas `model`, `repo`, `service`, `servlet`, `filter`, `util` e `listener` no editor. Localizar `VendaService.comprar`, sem ler o ficheiro todo.

## 3. Demonstração do administrador - cerca de 2 minutos

1. Entrar com `admin@agro.mz / admin234` e mostrar **Visão geral**.
2. Abrir **Produtos**, usar **Editar** num produto e explicar os dados que podem mudar. Evitar alterar preços se não for preciso.
3. Repor uma pequena quantidade e mostrar a diferença no stock.
4. Abrir **Campanhas**, **Cultivos** e **Colheitas**, mostrando a relação entre os registos.
5. Tentar remover a campanha de demonstração com cultivos. A operação deve ser bloqueada.
6. Mostrar a colheita de 3200 kg a 22 MT/kg e o total de 70 400 MT.

**Podes dizer:**

“Uma campanha pode ter vários cultivos, e cada cultivo pode ter várias colheitas. O sistema protege essas referências: não deixa remover uma campanha com cultivos nem um cultivo com colheitas. O total da colheita é a quantidade multiplicada pelo preço por kg.”

“O stock comercial é gerido nos produtos. O registo de colheita não altera automaticamente o catálogo, porque o enunciado não define essa ligação.”

## 4. Demonstração do cliente - cerca de 2 minutos

1. Sair e entrar com `joao@teste.mz / cliente123`. Também podes usar uma janela privada e criar uma conta nova para mostrar o auto-registo.
2. No **Catálogo**, filtrar uma categoria e limpar o filtro.
3. Adicionar 3 kg de sementes de milho e 1 saco de fertilizante, se houver stock.
4. Abrir **Meu carrinho**, alterar uma quantidade e carregar em **Actualizar**.
5. Usar **Finalizar compra**, conferir o resumo e **Confirmar compra**.
6. Mostrar a factura e o botão **Imprimir factura**. Fechar a janela de impressão sem precisar de imprimir em papel.
7. Abrir **Minhas compras** e mostrar a compra recente.

**Podes dizer:**

“O carrinho está na sessão do cliente. Não reserva stock. Na confirmação, o servidor verifica novamente todos os produtos e quantidades. A factura inclui subtotal, IVA de 16%, conforme o enunciado, e total. O preço e os dados do cliente ficam registados na venda, para a factura não mudar se o catálogo mudar depois.”

Se quiseres provar o controlo de acesso, escreve `/agrogest/admin/produtos` ainda com a conta de cliente: deve aparecer HTTP 403. Para mostrar facturas alheias, utiliza a conta Ana numa janela privada e tenta abrir o ID de uma factura do João.

## 5. Concorrência e atomicidade - cerca de 1 minuto

**Podes dizer:**

“O Tomcat pode executar vários pedidos ao mesmo tempo. Se dois clientes tentarem comprar a última unidade sem sincronização, ambos poderiam ler o mesmo stock. Usamos um único monitor com synchronized sobre o DataStore, partilhado pelos serviços.”

“A compra valida todos os itens antes de descontar o primeiro. Se faltar stock num deles, não há descontos nem factura. O AtomicInteger tem outra função: gerar os IDs sequenciais com segurança. Não substitui a sincronização da compra.”

“Também conferimos a versão do produto ao guardar uma edição. Se uma compra ou reposição aconteceu enquanto o formulário estava aberto, pedimos para reabrir os dados, em vez de aceitar um stock antigo.”

**Mostrar no código:** em `VendaService.comprar`, localizar `synchronized (store)`, o primeiro ciclo de validação e o ciclo final de desconto. Se houver tempo, mencionar o teste `doisClientesDisputamUltimaUnidadeSemStockNegativo`.

## 6. Persistência e encerramento - cerca de 1 a 2 minutos

1. Voltar à conta de administrador.
2. Mostrar **Relatórios**, alternando “Quem mais comprou”, “Mais assíduos” e “Sem compras”.
3. Abrir **Guardar dados** e usar **Gravar agora**.
4. Parar normalmente com o script fornecido.
5. Iniciar novamente, entrar e mostrar que a última compra continua no histórico.

**Podes dizer:**

“O listener carrega os dados no arranque e grava no encerramento normal. A gravação escreve primeiro um temporário e faz uma substituição atómica, mantendo o estado anterior como backup. Se o principal estiver danificado, tenta o backup. Se nenhum for válido, não substitui silenciosamente os dados.”

“Uma paragem forçada pode perder alterações posteriores à última gravação. Este projecto foi desenhado para uma instância do Tomcat. O synchronized não coordena servidores diferentes.”

## Antes da defesa

- Confirmar Java 17, arranque e acesso a `http://localhost:8081/agrogest/`.
- Fazer uma compra completa uma vez para conhecer os botões.
- Ter uma conta de administrador e uma de cliente em navegadores/janelas separadas, se for alternar perfis.
- Garantir que os produtos escolhidos ainda têm stock e validade.
- Deixar abertos no editor `VendaService`, `AuthFilter`, `PersistenciaService` e `AplicacaoListener`.
- Guardar o estado antes de começar. Levar o ZIP e o WAR como cópia.
- Se o docente pedir deploy externo, mostrar a cópia do WAR para `webapps`. Se pedir operação como serviço, usar os comandos systemd do README numa máquina onde esse serviço tenha sido instalado.

Não digas que o projecto tem base de dados, pagamento electrónico ou sincronização entre vários servidores. Essas funcionalidades não fazem parte desta versão.
