# Correspondência com o enunciado

Referência: AgroGest Trabalho prático 2026(1).pdf, páginas 1 a 7. O objectivo desta tabela é permitir encontrar cada requisito no código e no programa.

| Enunciado | Implementação | Onde demonstrar/verificar |
| --- | --- | --- |
| Java, Servlets e Tomcat, sem BD | Java 17, Jakarta Servlet 6.0, WAR e Tomcat 10.1 incluído | pom.xml, dist/agrogest.war, scripts/iniciar.sh |
| MVC e POJOs Serializable | Pacotes por responsabilidade e JSP em WEB-INF | model, repo, service, servlet, WEB-INF/views |
| Admin fixo no arranque | garantirAdministrador, email e palavra-passe do exemplo | ContaService, AplicacaoListener |
| Extra temporário: selector de login | Todas as contas, preenchimento por clique e entrada manual; desactivável | AuthServlet, ContaService, login.jsp e login.js |
| Auto-registo de cliente | Nome, email único, hash da senha, telefone e endereço | AuthServlet, ContaService, /registo |
| Criar/listar/remover fornecedores | 3 tipos pedidos, contacto e província | GestaoService, /admin/fornecedores |
| Criar/listar/actualizar/remover produtos | Todos os campos pedidos, edição e reposição | GestaoService, /admin/produtos |
| Foto por URL e validade | URL HTTP/HTTPS ou ilustração local; validade opcional | Catálogo e formulário de produto |
| Criar/fechar/reabrir campanhas | Nome, início, fim e estado | /admin/campanhas |
| Não remover campanha com cultivos | Verificação dentro do monitor comum | GestaoService.removerCampanha |
| Cultivos por campanha | Cultura, variedade, hectares e plantio | /admin/cultivos |
| Não remover cultivo com colheitas | Verificação de dependências | GestaoService.removerCultivo |
| Colheitas por cultivo | Data, kg, preço/kg e cálculo do total | /admin/colheitas |
| Listagens | Produtos, fornecedores, clientes, armazém e compras | /admin/listagens |
| Ordenação das listagens | Campos relevantes: nome, preço, stock, data, valor, etc. | Selects de ordenação, AdminServlet |
| Compras detalhadas | Dados completos do cliente e itens da venda | /admin/compras e /factura?id=N |
| Clientes por nome crescente | Comparador de nome | /admin/relatorios?tipo=nome |
| Clientes que compraram por volume | Soma de total com IVA, descendente | /admin/relatorios?tipo=volume |
| Clientes mais assíduos | Número de compras, descendente | /admin/relatorios?tipo=assiduos |
| Extra: clientes sem compras | Clientes com zero vendas | /admin/relatorios?tipo=sem-compras |
| Catálogo completo e filtro categoria | Nome, descrição, foto, preço, validade, stock, fornecedor e categoria | /catalogo |
| Carrinho com quantidade | Adicionar, actualizar, remover e esvaziar | CarrinhoServlet, /carrinho/* |
| Carrinho persiste em sessão | Map na HttpSession | BaseServlet.carrinho |
| Confirmar os valores apresentados | Proposta em sessão, token próprio e conferência atómica de produtos, preços e quantidades | CompraServlet, VendaService.confirmarCompra, PropostaCompra |
| Avisar produto inexistente/sem stock | Mensagens de negócio; validação final na venda | VendaService.comprar |
| Factura sequencial FT, subtotal, IVA16%, total | Snapshot dos dados e cálculos BigDecimal | Venda, ItemVenda, VendaService |
| Imprimir factura | window.print e CSS @media print | factura.jsp, assets/style.css |
| Histórico de compras do cliente | Lista por ID do cliente, mais recente primeiro | /minhas-compras |
| RN a: não vender acima do stock | Compara quantidade com stock actual | VendaService, testes JUnit |
| RN b: desconto atómico de todos os itens | Validar tudo, depois descontar, num só lock | VendaService, testes de falha parcial e concorrência |
| RN c: cliente só vê próprias facturas | Verifica clienteId ou perfil ADMIN | FacturaServlet, VendaService.factura |
| RN d: /admin/* exclusivo, HTTP403 | Filtro sobre caminho descodificado | AuthFilter, testes de rotas normais e codificadas |
| RN e: sessão nas rotas indicadas | Rotas públicas limitadas a login/registo/assets | AuthFilter, testes HTTP |
| RN f: IDs AtomicInteger | Contador por tipo, incluído na serialização | Estado, testes de reinício |
| Escritas concorrentes protegidas | Mesmo synchronized(store) para serviços e persistência | GestaoService, ContaService, VendaService, PersistenciaService |
| Revisão: edição depois de compra/reposição | Versão do produto conferida dentro do monitor; recusa formulário desactualizado | GestaoService.guardarProduto, testes de revisão |
| Revisão: exclusão da pasta entre instâncias | FileLock adquirido antes de carregar e mantido até terminar | TravaDados, AplicacaoListener |
| Hash em vez de senha em texto puro | PBKDF2, salt e verificação de hash | Senhas, teste de bytes do .ser |
| Interface português, responsiva, CSS próprio | Layout comum, formulários, media queries, menu móvel | WEB-INF/tags/layout.tag, style.css |
| @WebServlet, @WebFilter, @WebListener | Configuração por anotações | servlet, filter, listener |
| JSP + JSTL + EL | Tags jakarta.tags.core e EL, sem lógica Java em scriptlets | WEB-INF/views e WEB-INF/tags |
| Persistência ao parar e carregar ao iniciar | Eventos do contexto | AplicacaoListener |
| ObjectOutputStream/ObjectInputStream | Serialização do Estado completo | PersistenciaService |
| Escrita atómica tolerante a falhas | Temporário na mesma pasta, sync, validação de leitura antes da publicação, ATOMIC_MOVE e backup | PersistenciaService, testes de corrupção |
| Endpoint de gravação manual | POST /admin/persistencia, acao=gravar | Menu Guardar dados |
| Endpoint de eliminação manual | POST /admin/persistencia, acao=eliminar | Menu Guardar dados |
| Configurar portas | 8081 HTTP e 8105 paragem, configuráveis | runtime/tomcat/conf/server.xml, README |
| Deploy WAR | WAR compilado e instruções Tomcat externo | dist/agrogest.war, README |
| Operação via systemd | Unit e comandos de instalação/operação | deploy/agrogest.service, README |
| Código completo em ZIP | Fontes, testes, configuração e recursos | ZIP entregue |
| README com instalação, contas e arquitectura | Instruções Linux/Windows, Maven, deploy e diagnóstico | README.md |
| Manual de utilização PDF | Passo a passo para os dois perfis | docs/Manual_Utilizacao.pdf |
| Apresentação e defesa com demonstração | Roteiro com fala, passos e ficheiros a explicar | docs/APRESENTACAO_E_DEMONSTRACAO.md |

A defesa ao vivo e a instalação do serviço systemd são actividades a executar na máquina de apresentação. O pacote inclui o material e a configuração necessários. As decisões em pontos ambíguos encontram-se em DECISOES.md.
