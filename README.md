# AgroGest - Sistema de Gestão Agrícola

**Versão 1.2.3.** O menu lateral e os seus ícones permanecem no ecrã ao mudar de secção. Os ícones são desenhados directamente no HTML, sem um pedido externo. O login temporário por clique e as correcções de gravação e confirmação de compra continuam incluídos. Consulte `docs/NAVEGACAO_1.2.3.md`, `docs/LOGIN_TEMPORARIO.md` e `docs/CORRECOES_1.2.1.md`.

Se já usou a versão anterior, pare-a normalmente, faça uma cópia de segurança e copie a pasta `dados` para a nova pasta extraída antes de iniciar. A versão revista lê o estado gravado pela versão 1.0. Não execute as duas versões simultaneamente sobre os mesmos dados.

Trabalho prático de Sistemas Distribuídos, Engenharia Informática. Aplicação Java 17 com Jakarta Servlets 6.0, JSP, JSTL e Apache Tomcat 10.1.61. Dados em memória e serialização Java para ficheiro, conforme o enunciado. Não utiliza base de dados nem Spring.

## 1. Começar em poucos passos

1. Extraia **todo o ZIP** para uma pasta onde tenha permissão de escrita. Não execute de dentro do ZIP.
2. Instale Java 17 ou superior. Confirme no terminal: `java -version`. Para estudar e recompilar, instale o **JDK 17**.
3. **Linux:** abra um terminal dentro da pasta `AgroGest` e execute:

   ```bash
   bash scripts/iniciar.sh
   ```

   **Windows:** abra `scripts/iniciar.bat`. Se o Java não for encontrado, configure `JAVA_HOME` para a pasta do JDK e reabra o terminal.

4. Aguarde a mensagem `Server startup` e abra **http://localhost:8081/agrogest/**.
5. Mantenha o terminal do servidor aberto enquanto utiliza a aplicação.

O Tomcat e o WAR estão incluídos. **Não precisa de Maven nem de Internet para executar a versão pronta.** O Java é instalado separadamente. As ilustrações de demonstração estão incluídas e funcionam offline. URLs de fotografias externas que adicionar precisam de acesso à Internet.

### Parar e conservar os dados

- Linux: execute `bash scripts/parar.sh` noutro terminal, ou use Ctrl+C no terminal do servidor.
- Windows: abra `scripts/parar.bat` e aguarde o encerramento no terminal principal.
- Confirme nos logs a mensagem **“estado gravado ao encerrar”**. Evite fechar à força o processo Java.
- Em **Guardar dados → Gravar agora**, pode guardar sem parar o servidor.

## 2. Contas de demonstração

| Perfil | Email | Palavra-passe |
| --- | --- | --- |
| Administrador | admin@agro.mz | admin234 |
| Cliente | joao@teste.mz | cliente123 |
| Cliente | ana@teste.mz | cliente123 |
| Cliente sem compras iniciais | amelia@teste.mz | cliente123 |

Também pode criar uma conta em **Criar conta de cliente**. O administrador é criado automaticamente no arranque. O registo público nunca atribui o perfil de administrador.

No primeiro arranque sem ficheiro guardado, são criados dados **fictícios de demonstração**: 3 fornecedores, 4 produtos, 3 clientes, 1 campanha, 1 cultivo, 1 colheita e 3 compras. As compras iniciais descontam stock através do mesmo serviço usado nas compras normais. As datas de validade são calculadas a partir do primeiro arranque. O sistema usa o fuso de Moçambique nas vendas.

### Selector temporário de utilizadores

Ao iniciar com `scripts/iniciar.sh` ou `scripts/iniciar.bat`, a página de login mostra todas as contas existentes. Clique numa conta: os dois campos são preenchidos, mas só entra depois de carregar em **Entrar**. O selector acompanha automaticamente os novos registos.

O preenchimento usa `admin234` para administradores e `cliente123` para clientes. A palavra-passe original e o seu hash mantêm-se, e o login manual continua a funcionar. Contas inactivas aparecem desactivadas.

Para desligar a opção em Linux: `AGROGEST_DEMO_LOGIN=false bash scripts/iniciar.sh`. No Windows, defina `AGROGEST_DEMO_LOGIN=false` antes de executar `scripts/iniciar.bat`. Num Tomcat externo, o selector fica desligado por omissão; pode activá-lo com `AGROGEST_DEMO_LOGIN=true` ou `-Dagrogest.demoLogin=true`.

## 3. O que o programa faz

- Administrador: gerir fornecedores e produtos, editar preços e stock, repor stock, criar/fechar/reabrir/remover campanhas, registar/remover cultivos, registar colheitas e calcular o valor produzido.
- Listar produtos, fornecedores, clientes, stock em armazém e compras, com ordenação pelos campos aplicáveis.
- Relatórios de clientes por nome, volume de compras, número de compras e clientes sem compras.
- Cliente: registar-se, entrar, filtrar e pesquisar o catálogo, gerir o carrinho em sessão, confirmar compras, consultar histórico e imprimir facturas.
- Venda indivisível: ou todos os produtos são vendidos, ou não há desconto de stock nem factura.
- Factura com número `FT-ANO-N`, dados do cliente, itens, preços, subtotal, IVA de 16% e total.
- Gravação manual e no encerramento, recuperação no arranque, backup e substituição atómica do ficheiro.

O IVA de 16% foi aplicado **por exigência do enunciado**. Este projecto académico não certifica facturação fiscal nem processa pagamentos bancários.

## 4. Ficheiros a ler

| Ficheiro | Para quê |
| --- | --- |
| `docs/Manual_Utilizacao.pdf` | Passos das operações de administrador e cliente |
| [Manual_Utilizador.pdf](Manual_Utilizador.pdf) | Manual do utilizador |
| `docs/Guia_Teoria_Defesa.md` | Teoria e preparação da defesa num formato pesquisável e editável |
| `docs/APRESENTACAO_E_DEMONSTRACAO.md` | Texto para explicar o trabalho e sequência da demonstração |
| `docs/REQUISITOS.md` | Correspondência entre cada requisito e a implementação |
| `docs/INTERFACE.md` | Alterações visuais, acessibilidade e explicação do CSS/JavaScript |
| `docs/NAVEGACAO_1.2.3.md` | Correcção do recarregamento dos ícones e funcionamento do menu |
| `docs/VALIDACAO.md` | Testes executados e limites da verificação |
| `docs/DECISOES.md` | Pontos não detalhados no enunciado e decisões adoptadas |
| `dist/agrogest.war` | Aplicação pronta para publicar no Tomcat |

## 5. Organização do código e MVC

| Pasta/pacote | Responsabilidade | Exemplo |
| --- | --- | --- |
| `model` | POJOs Serializable com dados, construtores e getters/setters | `Produto`, `Venda` |
| `repo` | Mapas em memória e contadores AtomicInteger | `DataStore`, `Estado` |
| `service` | Regras de negócio e sincronização | `VendaService`, `GestaoService` |
| `servlet` | Receber GET/POST, chamar serviços, preparar a vista | `CompraServlet`, `AdminServlet` |
| `filter` | Sessão, perfil e token dos formulários | `AuthFilter` |
| `util` | Validação, hashes e formatação | `Validacao`, `Senhas` |
| `listener` | Carregar no arranque e gravar no encerramento | `AplicacaoListener` |
| `WEB-INF/views` | Páginas JSP com JSTL e Expression Language | `catalogo.jsp`, `factura.jsp` |
| `WEB-INF/tags` | Elementos reutilizados das páginas | `layout.tag`, `csrf.tag` |
| `assets` | CSS próprio, JavaScript pequeno e SVGs locais | `style.css` |

O browser envia um pedido HTTP ao Tomcat. O filtro verifica a sessão e o perfil. O Servlet chama o serviço, que usa os dados em memória. O Servlet escolhe uma JSP; o Tomcat gera HTML e envia-o ao browser. As JSP ficam dentro de `WEB-INF`, para serem abertas através dos controladores.

## 6. Recompilar depois de alterar o código

Requisitos adicionais: JDK 17 e Maven 3.9 ou superior no PATH. A primeira compilação precisa de Internet para descarregar dependências.

```bash
java -version
javac -version
mvn -version
mvn clean verify
```

O novo WAR fica em `target/agrogest.war`. Para actualizar a versão usada pelos scripts:

```bash
bash scripts/compilar.sh
```

No Windows, execute `scripts/compilar.bat`. Pare o servidor antes de substituir a aplicação, compile e volte a executar `iniciar`. O script copia `dist/agrogest.war` para o Tomcat.

Os testes de regras de negócio fazem parte de `mvn verify`. Para o teste HTTP completo em Linux, com a porta 8081 livre:

```bash
python3 tests/test_http.py
python3 tests/test_revisao.py
python3 tests/test_correcoes.py
python3 tests/test_login_demo.py
```

As suites iniciam o Tomcat com uma pasta temporária, usam contas e dados de teste e encerram-no no fim. Usam apenas a biblioteca padrão do Python. Não é necessário Python para utilizar o programa.

O segundo comando acrescenta casos de formulários antigos, compras simultâneas reais, bloqueio de uma segunda instância e recuperação após uma falha abrupta simulada. Requer também as portas 8082 e 8106 livres. Execute os testes com as instâncias de utilização normal paradas. O terceiro comando verifica mudanças de preço e carrinho depois do resumo, tokens antigos, reenvios, nova confirmação e conservação das facturas após reiniciar.

## 7. Publicar o WAR num Tomcat já instalado

1. Instale/configure um **Apache Tomcat 10.1** com Java 17.
2. Pare o Tomcat de forma normal.
3. Copie `dist/agrogest.war` para `TOMCAT/webapps/agrogest.war`.
4. Defina uma pasta de dados com permissão de escrita, opcionalmente com a variável `AGROGEST_DATA_DIR`.
5. Inicie o Tomcat. A pasta `agrogest` será criada pelo deploy do WAR.
6. Abra `http://localhost:PORTA/agrogest/`. A porta é a do seu Tomcat, normalmente 8080. O Tomcat incluído neste ZIP usa 8081.

Os imports são `jakarta.servlet`, compatíveis com Tomcat 10.1. Um Tomcat 9 usa a API antiga `javax.servlet` e não é o servidor indicado para este WAR.

## 8. Portas e acesso pela rede local

No Tomcat incluído, edite `runtime/tomcat/conf/server.xml` com o servidor parado.

- `Connector port="8081"`: porta HTTP. Se estiver ocupada, altere-a, por exemplo, para `8082`, e use a nova porta no browser.
- `Server port="8105"`: porta local de paragem. Altere-a se já existir outro Tomcat a usá-la.
- `address="127.0.0.1"` no Connector: acesso apenas pelo computador onde corre o programa.
- Para uma demonstração com outro computador/telemóvel na mesma rede, pode trocar **apenas o endereço do Connector** por `0.0.0.0`, autorizar a porta na firewall e abrir `http://IP_DO_SERVIDOR:8081/agrogest/` no outro dispositivo. Mantenha a porta de paragem limitada a `127.0.0.1`.

## 9. Operar com systemd em Linux

O serviço de exemplo está em `deploy/agrogest.service`. Estes passos são para uma máquina Linux com systemd, como Ubuntu. Ajuste o caminho de `JAVA_HOME` se o JDK estiver noutro local.

```bash
sudo useradd --system --home /opt/agrogest --shell /usr/sbin/nologin agrogest
sudo mkdir -p /opt/agrogest
sudo cp -a . /opt/agrogest/
sudo mkdir -p /opt/agrogest/runtime/tomcat/webapps
sudo cp dist/agrogest.war /opt/agrogest/runtime/tomcat/webapps/agrogest.war
sudo chmod +x /opt/agrogest/runtime/tomcat/bin/*.sh
sudo chown -R agrogest:agrogest /opt/agrogest
sudo cp deploy/agrogest.service /etc/systemd/system/agrogest.service
sudo systemctl daemon-reload
sudo systemctl enable --now agrogest
sudo systemctl status agrogest
```

Execute os comandos a partir da pasta extraída do projecto. Se a conta `agrogest` já existir, não a recrie. Os comandos com `sudo` são executados por si na máquina de instalação. Os scripts de arranque normal não precisam de privilégios de administrador.

```bash
sudo systemctl stop agrogest
sudo systemctl start agrogest
sudo systemctl restart agrogest
sudo journalctl -u agrogest -n 80 --no-pager
```

O systemd cria `/var/lib/agrogest` com `StateDirectory`. O Tomcat escreve os seus logs em `runtime/tomcat/logs`. Não inicie simultaneamente o serviço e `scripts/iniciar.sh`, pois ambos usam as mesmas portas.

## 10. Onde ficam os dados e como restaurar

- Arranque pelos scripts: `dados/agrogest.ser`.
- Cópia de segurança da gravação anterior: `dados/agrogest.ser.bak`.
- Tomcat externo sem configuração: `CATALINA_BASE/data/agrogest/`.
- A propriedade Java `-Dagrogest.dataDir=CAMINHO` tem prioridade sobre a variável `AGROGEST_DATA_DIR`.
- `agrogest.lock` permite bloquear a pasta enquanto a aplicação está activa. A existência do ficheiro vazio é normal; o bloqueio é libertado quando o processo termina. Não é necessário apagá-lo para reiniciar.
- A gravação cria um temporário na mesma pasta e usa `ATOMIC_MOVE`. Se o sistema de ficheiros não suportar a operação, apresenta erro e preserva a versão anterior. Antes de substituir o principal ou o backup, relê o temporário com a mesma política do arranque. O limite suportado é 64 MiB por ficheiro, 2 000 000 de referências, arrays até 1 000 000 de elementos e profundidade 40. Um estado acima desses limites é recusado sem substituir os ficheiros anteriores.
- Se o principal estiver danificado, tenta carregar o backup. Se os dois falharem, não inventa dados novos: o arranque falha e conserva os ficheiros para recuperação.
- Para fazer cópia do seu trabalho, use **Gravar agora**, pare normalmente e copie toda a pasta de dados.
- Para repor os dados de demonstração: **pare primeiro o servidor**, faça uma cópia da pasta de dados, remova `agrogest.ser` e `agrogest.ser.bak` dessa pasta e volte a iniciar.

O botão **Eliminar ficheiros** elimina o estado em disco e o backup, mas conserva a memória. O encerramento normal volta a gravar a memória. Isso permite demonstrar a eliminação manual sem destruir o trabalho em curso.

## 11. Problemas comuns

| Sintoma | O que verificar |
| --- | --- |
| `java: command not found` | Instalar Java e abrir um novo terminal |
| `UnsupportedClassVersionError` | Está a executar com Java anterior a 17 |
| `Address already in use` | Outra aplicação usa 8081 ou 8105; parar a outra ou alterar a porta |
| Página não abre imediatamente | Aguardar o arranque; o primeiro pedido compila a JSP |
| HTTP 403 em `/admin/*` | Entrar com administrador; clientes não podem abrir essas rotas |
| HTTP 403 após enviar formulário antigo | Reabrir a página; o token muda depois do login |
| HTTP 404 em Tomcat externo | Confirmar o nome `agrogest.war`, o deploy e o contexto `/agrogest/` |
| Maven não descarrega bibliotecas | Verificar a Internet/proxy; para executar, usar o WAR incluído |
| Alterações desapareceram depois de terminar à força | Só o último estado gravado é recuperado; guardar antes de parar |
| Erro ao gravar | Verificar a pasta de dados, permissões e suporte à substituição atómica |
| Produto alterado desde a abertura do formulário | Reabrir Editar e conferir os dados actuais; uma compra, reposição ou outra edição pode ter ocorrido |
| Pasta de dados já em uso | Parar a outra instância; não apagar o ficheiro de bloqueio enquanto o servidor está activo |

## 12. Referências técnicas

- Enunciado fornecido: **AgroGest Trabalho prático 2026(1).pdf**, 7 páginas.
- Tomcat 10.1 e APIs: https://tomcat.apache.org/migration-10.1.html
- Deploy de WAR: https://tomcat.apache.org/tomcat-10.1-doc/deployer-howto.html
- Maven WAR: https://maven.apache.org/plugins/maven-war-plugin/usage.html
- Jakarta Servlet 6.0: https://jakarta.ee/specifications/servlet/6.0/
- Serialização Java 17: https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/index.html
- API `Files.move`: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html

As licenças do Tomcat acompanham a distribuição em `runtime/tomcat/LICENSE` e `NOTICE`. Os nomes, contactos e compras iniciais são fictícios para demonstração.

## Pastas do projecto

| Local | Conteúdo |
| --- | --- |
| `src/` | Código Java, páginas JSP, estilos e recursos da aplicação |
| `scripts/` | Iniciar, parar e compilar, em Linux (`.sh`) e Windows (`.bat`) |
| `dist/` | WAR pronto a executar, versão e ficheiro de verificação SHA-256 |
| `runtime/` | Tomcat incluído para execução local |
| `docs/` | Manuais e documentação técnica |
| `tests/` | Testes HTTP e de navegador |
| `deploy/` | Configuração de execução como serviço |
| `dados/` | Estado local guardado, excluído do Git |
| `target/` | Ficheiros gerados pelo Maven, excluídos do Git |

A raiz mantém o `README.md`, o `pom.xml` e a configuração do Git. Para compilar e actualizar o WAR local, execute `bash scripts/compilar.sh` ou abra `scripts/compilar.bat`.

A partir da raiz, confirme a integridade dos ficheiros distribuídos com:

```bash
sha256sum -c dist/SHA256SUMS.txt
```
