# AgroGest
## Manual de utilização

Sistema de Gestão Agrícola - Sistemas Distribuídos

Este manual explica as operações principais com os nomes dos menus e botões que aparecem no programa. Os dados de demonstração são fictícios. Leia a secção do administrador para gerir a actividade agrícola e a secção do cliente para efectuar compras.

### Antes de começar

1. Extraia todo o ZIP.
2. Confirme que tem Java 17 ou superior: `java -version`.
3. Em Linux, abra um terminal na pasta AgroGest e execute `bash scripts/iniciar.sh`. Em Windows, abra `scripts/iniciar.bat`.
4. Aguarde o arranque e abra http://localhost:8081/agrogest/.
5. Mantenha o terminal do servidor aberto.

### Contas para experimentar

| Perfil | Email | Palavra-passe |
| --- | --- | --- |
| Administrador | admin@agro.mz | admin234 |
| Cliente | joao@teste.mz | cliente123 |
| Outro cliente | ana@teste.mz | cliente123 |
| Cliente inicialmente sem compras | amelia@teste.mz | cliente123 |

### Navegação

O menu fica à esquerda. Em ecrãs pequenos, o botão com linhas no topo abre o menu. Feche-o pelo botão ×, tocando fora ou com a tecla Escape. O botão de saída, junto ao nome do utilizador, termina a sessão. As mensagens verdes confirmam operações. As mensagens em vermelho explicam dados inválidos ou regras que impediram a operação.

O administrador começa em **Visão geral**, onde vê os totais, as compras recentes e as campanhas. O cliente começa no **Catálogo**.

<!-- PAGEBREAK -->

## 1. Administrador: fornecedores e produtos

### Criar e remover fornecedor

1. Entre com a conta de administrador e abra **Fornecedores**.
2. Expanda **Adicionar fornecedor**.
3. Preencha nome, tipo, contacto e província. Os tipos disponíveis são SEMENTES, MAQUINARIA e FERTILIZANTES.
4. Carregue em **Criar fornecedor**. O novo fornecedor aparece na tabela.
5. Para remover, use **Remover** na linha e confirme. Se tiver produtos associados, a remoção é bloqueada: remova esses produtos primeiro.

### Criar produto

1. Abra **Produtos** e expanda **Adicionar produto**.
2. Preencha nome, categoria, fornecedor, preço sem IVA, stock, unidade e descrição.
3. Se necessário, indique validade e URL da foto. Pode deixar a foto vazia para usar a ilustração da categoria. Validade vazia significa “Sem validade”.
4. Carregue em **Guardar produto**.
5. Use **Ver catálogo** para conferir a apresentação ao cliente.

Exemplo: Semente Feijão nhemba; categoria SEMENTE; preço 320,00 MT; stock 400 kg; fornecedor de sementes; descrição da semente; validade escolhida no calendário. No campo numérico, use o separador decimal aceite pelo navegador, como `320.00`.

### Editar e repor stock

- **Editar:** abre o formulário com os dados actuais. Altere os campos e use **Guardar produto**. O campo stock define a quantidade total que deve ficar disponível.
- Se o produto mudar enquanto estiver a editar, o sistema recusa o formulário antigo. Reabra **Editar** e confira os dados; o stock de uma compra entretanto realizada fica conservado.
- **Repor:** escreva uma quantidade positiva na linha do produto e carregue em **Repor**. Essa quantidade é somada ao stock. Exemplo: 20 kg existentes + reposição de 5 kg = 25 kg.
- **Remover:** retira o produto do catálogo. As facturas já emitidas mantêm o nome e preço originais. Um cliente que ainda tenha o produto no carrinho terá de o remover antes de comprar.

Preços aceitam até 2 casas decimais. Quantidades aceitam até 3. Não são aceites preços ou reposições negativos. Um produto fora de validade não pode ser comprado.

### Ordenar as tabelas

Escolha o campo em **Ordenar por**, indique **Crescente** ou **Decrescente** e carregue em **Ordenar**. Em Produtos, “data” ordena pela validade. Em Fornecedores, pode ordenar por nome, província ou tipo.

<!-- PAGEBREAK -->

## 2. Administrador: campanhas, cultivos e colheitas

### Campanhas agrícolas

1. Abra **Campanhas** e expanda **Nova campanha**.
2. Indique o nome e as datas de início e fim.
3. Use **Criar campanha**. A data final deve ser igual ou posterior à inicial.
4. Na tabela, **Fechar** impede novos cultivos e novas colheitas. **Reabrir** permite continuar os registos.
5. **Remover** só funciona se a campanha não tiver cultivos.

Exemplo: “Campanha 2026/2027”, de 01/10/2026 a 30/06/2027.

### Cultivos

1. Abra **Cultivos** e expanda **Registar cultivo**.
2. Seleccione uma campanha aberta.
3. Preencha cultura, variedade, área em hectares e data de plantio.
4. Use **Registar cultivo**. O plantio deve estar dentro do período da campanha.
5. Pode usar **Remover** apenas quando o cultivo ainda não tem colheitas.

Exemplo: cultura “Feijão nhemba”, variedade “Macia”, 8 hectares, plantio em 15/11/2026, associado à campanha anterior.

### Colheitas

1. Abra **Colheitas** e expanda **Registar colheita**.
2. Seleccione o cultivo.
3. Indique data, quantidade em kg e preço por kg.
4. Use **Registar colheita**. A campanha deve estar aberta e a data deve ficar entre o plantio e o fim da campanha.
5. Consulte o total calculado na tabela.

Exemplo: em 15/03/2027, colheita de 3200 kg a 22,00 MT/kg.

**Valor total = 3200 × 22 = 70 400,00 MT.**

Este é o valor da produção registada. Não é automaticamente uma venda, não emite factura e não repõe o catálogo comercial. Para alterar o stock de um produto, use **Produtos → Repor**.

<!-- PAGEBREAK -->

## 3. Administrador: compras, listagens e relatórios

### Consultar compras detalhadas

1. Abra **Compras**.
2. Ordene por data, valor ou nome do cliente, conforme precisar.
3. Use **Ver factura** na compra pretendida.
4. Confira nome, email, endereço e telefone do cliente, data, produtos, quantidades, unidades, preços, subtotais, IVA e total.
5. Use **Imprimir factura** para abrir a impressão do navegador. Também pode escolher “Guardar como PDF”, se o navegador oferecer essa opção.

O administrador pode consultar todas as facturas. Cada cliente só pode consultar as suas.

### Listagens

Abra **Listagens** e escolha:

- **Produtos:** todo o catálogo.
- **Fornecedores:** parceiros e contactos.
- **Clientes:** nome, email, telefone, endereço e data de registo.
- **Armazém:** apenas produtos com stock superior a zero.
- **Compras detalhadas:** acesso às facturas de todas as vendas.

Cada tabela tem ordenação pelos campos que fazem sentido para os seus dados. Um cliente, por exemplo, não tem “preço” ou “stock”; a sua tabela ordena por nome, email ou data de registo.

### Relatórios

Abra **Relatórios** e escolha um separador:

| Separador | Informação e ordem |
| --- | --- |
| Todos por nome | Todos os clientes, por nome crescente |
| Quem mais comprou | Clientes com compras, por total gasto em MT, decrescente |
| Mais assíduos | Clientes com compras, por número de compras, decrescente |
| Sem compras | Clientes que nunca compraram, por nome |

O volume soma o total das facturas com IVA. Em caso de empate no volume ou na frequência, o nome determina a ordem.

Os dados iniciais incluem clientes com diferentes números de compras e uma cliente sem compras, para permitir experimentar os quatro relatórios.

<!-- PAGEBREAK -->

## 4. Cliente: registo, catálogo e carrinho

### Criar a sua conta

1. Na página inicial, use **Criar conta de cliente**.
2. Preencha nome, email, telefone, endereço e palavra-passe.
3. Repita a palavra-passe e use **Criar a minha conta**.
4. Se o email já estiver registado, o sistema informa. A diferença entre maiúsculas e minúsculas não cria outro email.
5. Após o registo, entra automaticamente no catálogo.

Para entrar novamente, use o email e a palavra-passe no formulário **Entrar na sua conta**.

### Encontrar produtos

1. Abra **Catálogo**.
2. Consulte nome, descrição, imagem, fornecedor, validade, preço e stock.
3. Pode pesquisar pelo nome, escolher uma categoria e ordenar por nome, preço crescente ou maior stock.
4. Use **Filtrar** para aplicar ou **Limpar** para ver tudo.

Os preços do catálogo não incluem IVA. O resumo da compra acrescenta os 16% previstos no exercício.

### Gerir o carrinho

1. No produto escolhido, indique a quantidade e use **Adicionar**.
2. Abra **Meu carrinho** no menu ou o botão **Carrinho** no catálogo.
3. Para alterar uma quantidade, escreva o novo valor e use **Actualizar** na mesma linha.
4. Use **Remover** para retirar um item.
5. Use **Esvaziar carrinho** e confirme para retirar todos os itens.
6. Consulte subtotal, IVA e total no resumo.

O carrinho continua disponível enquanto a sessão estiver activa, mesmo que visite outra página. Terminar a sessão ou deixá-la expirar perde o carrinho dessa sessão. O carrinho não reserva stock: outro cliente pode comprar antes da sua confirmação.

<!-- PAGEBREAK -->

## 5. Cliente: comprar, consultar e imprimir

### Confirmar uma compra

1. No carrinho, use **Finalizar compra**.
2. Confira o seu nome, endereço, telefone, email e produtos.
3. Confira os valores. O sistema verifica novamente o stock e os preços actuais ao confirmar.
4. Use **Confirmar compra** uma vez.
5. Se todos os produtos existirem e tiverem stock, a compra é registada, o stock de todos é descontado e o carrinho fica vazio.
6. A factura abre automaticamente com número sequencial, por exemplo `FT-2026-4`.

Se um produto não existir ou faltar stock, a compra inteira é recusada. Nenhum dos outros produtos é descontado. Leia a mensagem, ajuste o carrinho e tente novamente.

### Perceber os valores

Exemplo com um item de preço unitário de 100,00 MT e quantidade de 2:

- Subtotal da linha: 2 × 100,00 = 200,00 MT.
- Subtotal da compra: soma dos subtotais das linhas = 200,00 MT.
- IVA: 200,00 × 0,16 = 32,00 MT.
- Total: 200,00 + 32,00 = 232,00 MT.

Os valores monetários são arredondados a 2 casas decimais. A confirmação regista a compra para o exercício; não faz cobrança por cartão, banco ou M-Pesa.

### Consultar compras anteriores

1. Abra **Minhas compras**.
2. A lista apresenta as compras mais recentes primeiro.
3. Use **Ver factura** na compra que pretende consultar.
4. Use **Imprimir factura**.
5. Na janela do navegador, seleccione impressora ou “Guardar como PDF”.

As facturas conservam os nomes, contactos, preços e quantidades do momento da compra. A remoção posterior de um produto não apaga a sua factura.

Tentar abrir a factura de outro cliente devolve HTTP 403. Tentar abrir a área de administrador com conta de cliente também devolve HTTP 403.

<!-- PAGEBREAK -->

## 6. Guardar, parar e recuperar o trabalho

### Gravar sem encerrar

1. Entre como administrador.
2. Abra **Guardar dados**.
3. Use **Gravar agora**.
4. Aguarde a mensagem de sucesso. O indicador passa a mostrar que existe um ficheiro guardado.

O programa guarda `agrogest.ser` e conserva a versão anterior em `agrogest.ser.bak`. A gravação usa um temporário para evitar substituir um ficheiro válido por um ficheiro escrito apenas em parte.

### Encerrar normalmente

1. Guarde manualmente se quiser assegurar o último ponto antes de parar.
2. Linux: use `bash scripts/parar.sh` noutro terminal ou Ctrl+C no terminal principal.
3. Windows: abra `scripts/parar.bat`.
4. Aguarde a mensagem de estado gravado.
5. Volte a executar `iniciar` quando precisar. Os dados guardados são recuperados automaticamente.

No arranque normal pelos scripts, a pasta dos dados chama-se `dados`, dentro de AgroGest. Não apague esta pasta se quiser conservar as suas alterações. Para mudar a localização ou usar um Tomcat externo, consulte README.md.

### Eliminar os ficheiros guardados

Em **Guardar dados → Eliminar ficheiros**, confirme a operação. Isto elimina o ficheiro principal e o backup. Os dados que o programa já tem em memória continuam a existir e serão gravados de novo no encerramento normal.

Para voltar aos dados iniciais, pare primeiro o servidor, faça uma cópia da pasta de dados, retire os dois ficheiros dessa pasta e volte a iniciar.

### Quando algo não corre bem

- Porta ocupada: consulte a secção de portas no README.
- Formulário expirado/403: reabra a página e entre novamente, se necessário.
- Erro de stock: altere as quantidades do carrinho.
- Erro ao gravar: verifique a permissão de escrita na pasta de dados e os logs em `runtime/tomcat/logs`.
- Corte de energia ou processo terminado à força: o próximo arranque recupera o último estado efectivamente gravado, que pode não incluir as alterações mais recentes.
- Principal danificado: o sistema tenta o backup. Se nenhum funcionar, preserva os ficheiros e não inicia com dados novos por cima deles.

<!-- PAGEBREAK -->

## 7. Actualizar para a versão revista

### Conservar os dados da versão anterior

1. Na versão anterior, use **Guardar dados → Gravar agora**.
2. Pare o servidor normalmente e aguarde a confirmação de gravação.
3. Faça uma cópia de segurança da pasta `dados`.
4. Extraia o novo ZIP para outra pasta.
5. Copie a pasta `dados` da versão anterior para dentro da nova pasta AgroGest.
6. Execute o novo `scripts/iniciar.sh` ou `scripts/iniciar.bat`.
7. Entre e confira as suas compras e produtos. Não precisa de voltar aos dados de demonstração.

### Formulário de produto desactualizado

Uma compra pode acontecer entre abrir **Editar** e carregar em **Guardar produto**. Também pode haver uma reposição ou uma edição noutro separador. Nesses casos, o programa avisa que o produto mudou e conserva os dados actuais. Reabra a edição, confirme o stock e volte a fazer a alteração pretendida.

### Evitar duas instâncias sobre os mesmos dados

A versão revista bloqueia a pasta de dados enquanto a utiliza. Se outra instância tentar usar a mesma pasta, a aplicação dessa segunda instância não inicia e o log explica o motivo. A primeira continua a funcionar.

O ficheiro `agrogest.lock` pode permanecer na pasta depois de parar. O que determina o bloqueio é o processo activo, não a presença do ficheiro. Não precisa de apagar esse ficheiro para reiniciar.

### Facturas com nomes longos

A tabela passa a distribuir o nome do produto por várias linhas, conservando as colunas de quantidade, unidade, preço e subtotal. Confira a pré-visualização antes de imprimir uma factura com muitos itens.
