# AgroGest - Sistema de Gestão Agrícola

O **AgroGest** é uma aplicação web criada para apoiar um agricultor da província de Nampula na organização da sua actividade. Permite acompanhar a produção agrícola, gerir produtos e stock, registar vendas e emitir facturas. O sistema pode ser utilizado pelo administrador e pelos clientes, através de um navegador.

## Informações do trabalho

- **Instituição:** ISCTEM
- **Curso:** Engenharia Informática
- **Disciplina:** Sistemas Distribuídos
- **Docente:** Eng. Damião Salatiel

### Estudantes

| Nome | Código |
| --- | --- |
| Neyal Santilal | 20240629 |
| Usseid Adil | 20240394 |
| Zahra Muhammad Shoib | 20240418 |

## O que permite fazer

### Administrador

- Registar fornecedores e gerir os produtos, preços e quantidades disponíveis.
- Organizar campanhas agrícolas e registar os respectivos cultivos e colheitas.
- Consultar clientes, compras, facturas e produtos em armazém.
- Acompanhar relatórios de vendas e dos clientes que mais compram ou compram com maior frequência.
- Guardar os dados do sistema.

### Cliente

- Criar uma conta e consultar o catálogo de produtos.
- Adicionar produtos ao carrinho, alterar quantidades e confirmar compras.
- Consultar o histórico de compras e visualizar ou imprimir as suas facturas.

O sistema verifica se existe stock suficiente antes de concluir uma compra. As facturas apresentam os produtos, as quantidades, o subtotal, o IVA de 16% e o total.

## Como instalar e executar

### No computador

1. Obtenha o projecto no [repositório GitHub](https://github.com/neyalc-s/AgroGest) ou extraia o ZIP completo para uma pasta.
2. Instale **Java 17 ou superior**. Pode confirmar a instalação com `java -version` no terminal.
3. Abra a pasta do projecto e inicie a aplicação:

   **Linux:** abra o terminal nessa pasta e execute:

   ```bash
   bash iniciar.sh
   ```

   **Windows:** abra o ficheiro `iniciar.bat`.

4. Aguarde o arranque e abra [http://localhost:8081/agrogest/](http://localhost:8081/agrogest/) no navegador.
5. Mantenha o terminal do servidor aberto enquanto utiliza o sistema.

O pacote pronto inclui o Tomcat e a aplicação compilada. Para o executar, basta ter o Java instalado.

Para parar no Linux, use `bash parar.sh` noutro terminal ou pressione **Ctrl+C** no terminal do servidor. No Windows, abra `parar.bat`. O encerramento normal permite guardar os dados.

### Num servidor Tomcat — publicação da aplicação

1. Prepare um servidor com **Java 17 ou superior** e **Apache Tomcat 10.1**.
2. Com o Tomcat parado, copie `dist/agrogest.war` para a pasta `webapps` do servidor.
3. Inicie o Tomcat e aguarde a publicação da aplicação.
4. Abra o endereço do servidor seguido de `/agrogest/`. Numa instalação local com a porta habitual do Tomcat, o endereço é [http://localhost:8080/agrogest/](http://localhost:8080/agrogest/).

Se tiver alterado o código, instale também o JDK e o Maven e execute, na pasta do projecto:

```bash
mvn clean verify
```

Este comando compila o projecto e executa os testes. Nesse caso, publique o novo ficheiro `target/agrogest.war`.

## Contas de teste

| Perfil | Email | Palavra-passe |
| --- | --- | --- |
| Administrador | admin@agro.mz | admin234 |
| Cliente | joao@teste.mz | cliente123 |
| Cliente | ana@teste.mz | cliente123 |
| Cliente | amelia@teste.mz | cliente123 |

As contas e os registos iniciais são fictícios e servem para demonstrar o funcionamento do sistema. Também é possível criar uma conta de cliente na página de registo.

Quando o selector de demonstração estiver activo na página de entrada, clique numa conta para preencher o email e a palavra-passe (`admin234` para administradores ou `cliente123` para clientes) e depois carregue em **Entrar**.

## Como o sistema está organizado

O AgroGest foi desenvolvido em **Java**, com **Servlets e páginas JSP**, e é executado no **Apache Tomcat**. O **Maven** organiza as dependências e prepara o ficheiro WAR usado na publicação.

O código segue a arquitectura **MVC**, que separa as responsabilidades em três partes:

| Parte | Responsabilidade |
| --- | --- |
| Modelo | Representa os dados, como produtos, clientes, campanhas e vendas. |
| Controlo | Recebe as acções dos utilizadores através dos Servlets e chama os serviços necessários. |
| Vista | Apresenta as páginas e os resultados ao utilizador através das JSP. |

Os serviços aplicam as regras de funcionamento, como verificar o stock e calcular os valores das compras. Os dados são mantidos em listas e mapas na memória, sem utilização de uma base de dados.

## Como são guardados os dados

Na execução local, o sistema guarda o estado em ficheiro ao encerrar normalmente e recupera-o no próximo arranque. O administrador também pode usar **Guardar dados → Gravar agora** durante a utilização.

**Na demonstração alojada no Render Free, os dados são temporários:** um reinício ou uma nova publicação pode repor as contas, os produtos e o stock iniciais. As alterações realizadas nessa demonstração podem perder-se.

## Manual de utilização

O [manual de utilização em PDF](docs/Manual_Utilizacao.pdf) explica, passo a passo, as principais operações do administrador e do cliente. A pasta `docs` contém também a restante documentação do trabalho.
