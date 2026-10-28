# AgroGest 1.2.1 — correcções de 27/10/2026

Esta versão corrige os dois problemas prioritários identificados na revisão: recuperação de estados maiores e confirmação de uma compra com condições diferentes das apresentadas.

## 1. Gravação e recuperação dos dados

O leitor anterior recusava estados com mais de 200 000 referências, mas a gravação podia produzir esses ficheiros sem apresentar erro. O próximo arranque podia recuperar um backup antigo ou falhar se não existisse backup válido.

A versão 1.2.1 usa uma política de leitura definida: até 64 MiB por ficheiro, 2 000 000 de referências, arrays até 1 000 000 de elementos e profundidade máxima 40. As restrições de tipos continuam activas.

Depois de escrever e sincronizar o temporário, a gravação relê-o com o mesmo leitor usado no arranque. Só depois dessa validação pode actualizar o backup e substituir o principal. Se a leitura recusar o temporário, a operação apresenta erro, elimina o temporário e conserva o principal e o backup anteriores.

Os nomes dos ficheiros e os `serialVersionUID` das entidades existentes foram conservados. Continua a ser possível usar a pasta de dados da versão anterior. Um estado que ultrapasse os limites suportados é recusado de forma explícita; a aplicação não promete armazenamento ilimitado.

**Código:** `service/PersistenciaService.java`.

## 2. Confirmação associada ao resumo apresentado

Ao abrir **Finalizar compra**, o servidor prepara uma proposta com os itens, quantidades, preços, nomes e unidades apresentados. A página recebe um identificador dessa proposta, separado do token CSRF. Os valores apresentados são obtidos da mesma proposta.

Ao carregar em **Confirmar compra**, o servidor verifica o identificador e confere as condições da proposta com o carrinho e os produtos actuais. Esta verificação ocorre dentro do mesmo monitor usado para validar o stock e registar a venda.

Se o preço, os produtos ou as quantidades tiverem mudado, nenhuma venda é registada e nenhum stock é descontado. O cliente regressa ao carrinho, abre o resumo actualizado e confirma de novo. Uma reposição de stock que conserve as condições comerciais não invalida a proposta; o stock disponível continua a ser conferido no momento da compra.

Depois de uma compra concluída, a proposta é retirada da sessão e o carrinho fica vazio. O reenvio do formulário antigo não pode comprar um carrinho repovoado depois dessa compra. Login e registo também removem propostas da sessão anterior.

Existe uma proposta activa por sessão: abrir uma nova página de confirmação substitui a anterior. Um formulário de outro separador que ficou antigo pede nova confirmação.

**Código:** `model/PropostaCompra.java`, `service/VendaService.java`, `servlet/CompraServlet.java`, `servlet/AuthServlet.java` e `WEB-INF/views/compra.jsp`.

## 3. Testes executados

Ambiente: Linux, Java 17.0.20, Maven 3.9.9 e Tomcat 10.1 incluído no pacote.

| Comando | Resultado |
| --- | --- |
| `mvn clean verify` | 38 testes Java; 0 falhas e 0 erros; WAR gerado |
| `python3 tests/test_http.py` | 128 verificações passaram |
| `python3 tests/test_revisao.py` | 32 verificações passaram |
| `python3 tests/test_correcoes.py` | 22 verificações passaram |

São **38 testes Java e 182 verificações HTTP/integradas**. As suites HTTP foram executadas com o WAR recompilado desta versão. Também foi carregado no Tomcat um estado com 15 000 vendas escrito pelo WAR anterior: o stock de 35 000 foi mantido, a compra seguinte recebeu o ID 15 001 e a nova factura continuou acessível depois de gravar e reiniciar.

Os oito novos testes Java verificam:

- Primeira gravação e recuperação de um estado com 15 000 vendas, sem depender de um backup pequeno.
- Continuação da numeração, gravação seguinte e recuperação de backup com esse volume de dados.
- Recusa de um estado com 75 000 vendas que excede o limite de referências, sem alterar principal ou backup nem deixar temporários.
- Mudança de preço e de quantidade depois de abrir o resumo.
- Protecção da proposta contra alterações nos objectos entregues à vista.
- Proposta ausente ou pertencente a outro cliente.
- Reposição de stock sem mudança de condições.
- Falta de stock depois de preparar o resumo.

A nova suite HTTP verifica ainda a ausência ou falsificação do identificador, a separação entre CSRF e confirmação, os formulários antigos, o reenvio após repovoar o carrinho, a factura com o novo total e a conservação dessa factura depois de reiniciar.

Os dois testes HTTP anteriores foram adaptados para abrir a página de confirmação e enviar o identificador do formulário, como faz o navegador. Mantêm as verificações de concorrência, permissões, falhas parciais, gravação e recuperação.

Os testes de interface visual, os scripts Windows e a instalação do serviço systemd não foram reexecutados nesta correcção. O manual PDF continua a explicar as mesmas operações e botões; este ficheiro documenta as novas verificações e mensagens.

## 4. Actualizar conservando os dados

1. Na versão anterior, use **Guardar dados → Gravar agora**, se essa gravação ainda estiver dentro dos limites suportados por essa versão.
2. Pare o servidor normalmente e faça uma cópia de segurança de toda a pasta `dados`.
3. Extraia todo o novo ZIP para outra pasta.
4. Copie a pasta `dados` anterior para dentro da nova pasta `AgroGest`.
5. Execute `bash scripts/iniciar.sh` em Linux ou `scripts/iniciar.bat` em Windows.
6. Confira os produtos, o stock e as facturas antes de continuar o trabalho.

Se usar um caminho personalizado por `AGROGEST_DATA_DIR`, mantenha esse caminho. Execute uma única instância por pasta de dados. Os testes usam dados temporários e não acompanham a entrega como dados de utilização.

Para executar sem conservar dados anteriores, basta extrair o ZIP, ter Java 17 ou superior e iniciar normalmente. Endereço: `http://localhost:8081/agrogest/`.
