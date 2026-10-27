# Validação da versão 1.2.3 — 27/10/2026

- `mvn clean verify`: **41 testes Java, 0 falhas e 0 erros; BUILD SUCCESS**.
- Suites HTTP: **201 verificações passaram** (128 + 32 + 22 + 19).
- Navegação no Chromium: **96 verificações passaram**, com cliques reais nas dez secções administrativas, perfis de administrador e cliente, desktop e telemóvel.
- Dez cliques no menu: **zero carregamentos completos de documento, zero pedidos ao ficheiro de ícones**. A identidade dos elementos do menu, cabeçalho e ícones foi conservada.
- Voltar/Avançar, resposta atrasada após cliques rápidos, Ctrl+clique, criação/remoção de fornecedor e gravação manual passaram.
- Carrinho, contador, resumo com token, compra, factura, histórico, logout, fim de sessão e recurso à navegação normal após falha de rede passaram.
- Menu móvel em 390 px com e sem movimento reduzido: foco, fecho, reabertura, Escape e ausência de transbordo horizontal passaram. Links normais também verificados sem JavaScript.
- Login por clique: **52 verificações de navegador passaram**, incluindo todas as contas e um novo registo. Nenhum erro JavaScript ou HTTP 500 observado no teste de navegação.

O WAR foi recompilado com as fontes actualizadas. A verificação foi feita em Linux e Chromium; não foram executados testes em Firefox, Windows nem aparelhos móveis físicos nesta versão. O manual em PDF continua a descrever as mesmas operações; a mudança da navegação está documentada em `NAVEGACAO_1.2.3.md` e `INTERFACE.md`.

---

# Validação da versão 1.2.2 — 27/10/2026

- `mvn clean verify`: **41 testes Java, 0 falhas e 0 erros**.
- Suites HTTP: **201 verificações passaram** (128 + 32 + 22 + 19).
- Navegador Chromium: **52 verificações passaram**, com cliques reais, desktop e telemóvel; páginas inspeccionadas visualmente.
- O clique preenche os dois campos e aguarda o botão Entrar, incluindo numa conta recém-criada.
- O modo pode ser desligado e as senhas originais continuam válidas.

Detalhes em `LOGIN_TEMPORARIO.md`. Abaixo mantém-se o histórico das revisões anteriores.

---

# Validação da versão 1.2.1 — 27/10/2026

- `mvn clean verify`: **38 testes Java, 0 falhas e 0 erros; BUILD SUCCESS**.
- `python3 tests/test_http.py`: **128 verificações passaram**.
- `python3 tests/test_revisao.py`: **32 verificações passaram**.
- `python3 tests/test_correcoes.py`: **22 verificações passaram**.
- Os testes Java incluem 15 000 vendas com recuperação e continuação dos IDs, e a recusa segura de 75 000 vendas acima do limite de leitura.
- Foi carregado no Tomcat um estado com 15 000 vendas escrito pelo WAR anterior: manteve o stock de 35 000, emitiu a factura 15 001, ficou com stock 34 999 e conservou essa factura após gravar e reiniciar.

As verificações desta versão e as instruções de actualização estão em `CORRECOES_1.2.1.md`. As notas seguintes registam a revisão anterior; os testes visuais, Windows e systemd dessa revisão não foram repetidos nesta correcção.

---

# Validação da versão revista 1.2.0

Verificações de 22/10/2026. Ambiente: Linux, OpenJDK 17.0.20, Maven 3.9.9, Apache Tomcat 10.1.61 e Chromium 141. Os dados e as imagens de inspecção são temporários e não substituem o estado inicial do ZIP.

## Testes Java

`mvn clean verify`: **30 testes, 0 falhas, 0 erros**.

São 26 testes de regras de negócio/persistência e 4 de exclusão da pasta de dados. Além dos casos da primeira versão, verificam:

- Formulário aberto antes de uma compra não repõe o stock vendido.
- Duas edições da mesma versão não se sobrepõem.
- Reposição de stock invalida uma edição anterior.
- Consultas devolvem cópias, sem expor os produtos internos para alterações externas.
- Um ficheiro serializado pelo WAR 1.0 é carregado e editado pela versão 1.1.
- Edição sem versão é recusada.
- Arredondamento de linhas mantém a soma exacta na factura.
- Compra e reposição simultâneas conservam a quantidade esperada.
- Falha ao substituir o backup conserva o ficheiro principal e elimina temporários.
- A mesma pasta é recusada a uma segunda instância, incluindo uma segunda JVM.
- Fechar um bloqueio já fechado não interfere com a instância seguinte.

Mantêm-se os testes de IVA, desconto de stock, falha no segundo item, produto removido, disputa da última unidade, quantidades inválidas, facturas alheias, email único, hash de senhas, datas, dependências de campanhas/cultivos, validade, relatórios, ordenação portuguesa, serialização, contadores e recuperação de ficheiros danificados.

O recurso `src/test/resources/estado-v1.ser` contém apenas dados fictícios produzidos pelo WAR anterior. Serve para o teste de compatibilidade e não acompanha a aplicação dentro do WAR.

## HTTP no Tomcat real

| Teste | Resultado |
| --- | --- |
| `python3 tests/test_http.py` | 128 verificações passaram |
| `python3 tests/test_revisao.py` | 32 verificações passaram |
| Total | 160 verificações HTTP/integradas passaram |

A primeira suite cobre autenticação, auto-registo, catálogo, carrinho, CRUD, campanhas, colheitas, relatórios, facturas, sessões, CSRF, texto escapado, gravação, eliminação e reinício. As ordenações têm verificações de acesso para os vários campos e sentidos.

A segunda acrescenta verificação dos valores e ordem apresentados, edições antigas e operações concorrentes através de pedidos HTTP reais. Testa dois clientes diferentes a disputar a última unidade e dois envios simultâneos do mesmo carrinho. Confirma também que preços, total e ID de cliente enviados adicionalmente pelo browser não substituem os dados calculados no servidor.

Foi iniciado um segundo Tomcat, noutras portas, com a mesma pasta de dados: a segunda aplicação foi recusada, o log explicou o bloqueio, o ficheiro guardado permaneceu igual e a primeira instância continuou funcional.

Foi simulado um ficheiro principal danificado e uma terminação abrupta sem execução do listener. No reinício, o sistema recuperou o backup anterior, registou o aviso no log e conseguiu voltar a gravar um estado válido.

## Interface e impressão

Revisão visual em Chromium, com navegação e interacções reais: **116 verificações passaram**. O catálogo foi verificado nas larguras 360, 390, 620, 768, 960, 961, 1280, 1366 e 1440 px. Foram também percorridas 13 páginas/estados administrativos em 1440 e 390 px.

Foram verificados:

- Tamanho calculado das letras: texto base e campos de 16 px, nomes de produtos de 22 px e preços de 28 px.
- Filtros, limpeza da pesquisa, estado vazio e contador de resultados, incluindo o singular “1 produto”.
- Adição ao carrinho, alteração de quantidade, confirmação de compra, emissão de factura e histórico pelo navegador.
- Ausência de transbordo horizontal da página, incluindo tabelas, formulários e uma factura com nome de produto longo.
- Deslocamento de tabelas pelo teclado e apresentação da nota quando existem mais colunas.
- Menu aberto/fechado pelo botão, por Escape e por toque fora; foco no botão de fechar; circulação com Tab e Shift+Tab dentro do menu; devolução do foco ao fechar.
- Conteúdo de fundo indisponível enquanto o menu está aberto, menu móvel fechado sem foco e recuperação da navegação desktop ao mudar a largura.
- Efeito dos cartões ao passar o ponteiro, abertura/recolha do formulário e identificação da página actual na navegação.
- Preferência de movimento reduzido: animações de entrada, transições do menu e deslocação no hover desactivadas, mantendo o menu funcional.
- Ausência de erros JavaScript, ficheiros visuais em falta e respostas HTTP 500.

A factura de um produto com 117 caracteres no nome foi impressa em A4 e novamente inspeccionada. As cinco colunas e os valores ficaram presentes numa página, dentro das margens. A largura mínima usada para leitura no ecrã é retirada na impressão, juntamente com as animações e as notas de deslocamento.

As imagens serviram para inspecção interna. Não são necessárias para executar o projecto e não fazem parte da entrega. A validação móvel usa tamanhos de viewport no Chromium, sem testes em aparelhos físicos.

## Documentação e pacote

- Manual de utilização: 8 páginas A4, incluindo a actualização a partir da versão anterior.
- Guia de teoria: 13 páginas A4, incluindo controlo optimista de versão, cópias para consulta e bloqueio da pasta.
- Renderização e revisão visual de todas as páginas, com verificação de limites do texto.
- README, decisões, matriz de requisitos e roteiro da defesa actualizados.
- WAR pronto, fontes completas, testes e recurso de compatibilidade incluídos.

## ZIP extraído e arranque

O ZIP 1.2.0 foi extraído para uma pasta com espaços. As fontes extraídas compilaram com `mvn clean verify` e os mesmos 30 testes passaram, incluindo o recurso serializado da versão anterior. O WAR pronto do ZIP passou as 128 verificações HTTP, incluindo arranque, gravação, encerramento e reinício. Foram conferidos os hashes e a correspondência dos ficheiros CSS, JavaScript, tags e páginas alteradas entre as fontes e o WAR.

Na revisão funcional 1.1 foram ainda verificados a paragem pelo script, o arranque só com Java no PATH, JAVA_HOME com JRE_HOME incompatível e o arranque apenas com JRE_HOME, sem o comando java no PATH. Os scripts de arranque e paragem são os mesmos nesta revisão visual.

## Limites

A validação foi executada em Linux. Os scripts Windows e a unit systemd foram revistos e documentados, mas não executados num Windows nem num sistema com systemd activo.

Os testes não constituem uma prova de ausência de todos os erros. A compra protege as validações previstas em memória numa instância. Uma falha abrupta pode perder alterações posteriores à última gravação. O bloqueio de pasta impede duas instâncias de escreverem sobre o mesmo estado, sem implementar replicação ou partilha de stock entre servidores.
