# Decisões sobre pontos não detalhados no enunciado

O enunciado foi analisado nas 7 páginas, incluindo a arquitectura (página 6) e o modelo de domínio (página 7). As escolhas abaixo completam os pontos em aberto e não são apresentadas como exigências adicionais do docente.

| Ponto | Decisão implementada | Razão |
| --- | --- | --- |
| Número `FT--` e exemplo `FT-2026-1` | `FT-ANO-ID`, com contador de vendas que continua entre anos | Segue o exemplo e mantém os números únicos após reiniciar |
| “Clientes que compraram, por volume” | Soma dos totais das facturas, incluindo IVA, em MT, por ordem decrescente | A expressão não define volume físico; o relatório identifica a unidade e a interpretação |
| Quantidades e unidades | Produtos e carrinho aceitam até 3 casas decimais; preços têm 2 | Permite tanto kg fraccionários como quantidades inteiras, sem usar `double` no dinheiro |
| Valores na factura | Preço unitário sem IVA; arredondar cada linha a 2 casas; somar linhas; IVA = subtotal × 0,16 arredondado | Mantém os valores impressos coerentes com a soma das linhas |
| Taxa de IVA | 16%, valor fixo do exercício | Não se infere a taxa legal em vigor |
| Validade | Opcional; vazio significa “Sem validade”; produtos expirados não podem ser comprados | Ferramentas e maquinaria podem não ter validade; a validação evita vender produtos expirados |
| Datas e campanha fechada | Plantio dentro da campanha; colheita entre plantio e fim; campanha fechada bloqueia novos registos | Dá significado operacional ao fecho e permite reabrir |
| Colheitas e stock comercial | São módulos separados; registar colheita não cria nem repõe um produto | O enunciado não define uma ligação automática nem a conversão de unidades |
| Fornecedor com produtos | Remoção bloqueada | Evita um produto com fornecedor inexistente |
| Tipo de fornecedor de ferramentas | Usam-se apenas os 3 tipos pedidos; um fornecedor de MAQUINARIA pode fornecer FERRAMENTA | O enunciado inclui ferramentas no catálogo, mas não define um quarto tipo de fornecedor |
| Remover produto já vendido | Permitido; itens e dados da venda guardam uma cópia histórica | As facturas antigas não podem depender de produtos ainda existentes |
| Actualizar produto e repor | Editar define o stock total; repor soma uma quantidade positiva | Oferece as duas interpretações de actualização/reposição |
| Eliminar persistência | POST protegido remove `.ser` e `.bak`; conserva a memória | O requisito não pede apagar todas as entidades; o encerramento volta a gravar |
| Gravação automática | No encerramento normal do contexto Tomcat; manual a qualquer momento | Cumpre o ciclo de vida pedido; não promete recuperar alterações nunca gravadas após corte de energia |
| Ficheiro principal danificado | Recupera backup anterior válido; se não houver nenhum válido, falha sem substituir os ficheiros | Evita apagar silenciosamente dados do utilizador |
| Escopo da sincronização | Um único monitor `synchronized(store)` em todos os serviços | Um lock diferente em cada serviço não impediria uma venda de concorrer com a edição do stock |
| Formulário de edição antigo | Conferir a versão do produto antes de aceitar alterações | Um formulário aberto antes de uma compra não pode repor o stock que foi vendido |
| Objectos entregues à página | Cópias dos produtos obtidas dentro do monitor | Evita leitura de campos de momentos diferentes e mutações por fora dos serviços |
| Duas instâncias na mesma pasta | Exclusão por FileLock durante o ciclo de vida da aplicação | A substituição atómica do ficheiro não resolve duas memórias independentes a gravar o mesmo destino |
| Compatibilidade com versão 1.0 | Conserva serialVersionUID e aceita o novo campo de versão inicialmente a zero | Permite carregar e editar os produtos já guardados |
| IDs | `AtomicInteger` por tipo de entidade, começando em 1, sem reutilizar IDs removidos | Compatível com as referências do modelo e com reinícios |
| Confirmação de compra | Guarda uma proposta com token próprio, itens, quantidades e preços apresentados; exige correspondência no POST | Uma alteração durante a leitura do resumo não pode gerar uma factura com valores diferentes |
| Propostas em vários separadores | Abrir uma nova página de confirmação substitui a proposta anterior da sessão; o formulário anterior pede nova confirmação | Mantém uma única proposta activa por sessão e impede confirmar a proposta de outro separador |
| Limites de persistência | 64 MiB, 2 000 000 de referências, arrays até 1 000 000 e profundidade 40; validar o temporário com o leitor antes de publicar | Não apresentar uma gravação como concluída quando o arranque a rejeitaria |
| Sessão | Guarda ID do utilizador, carrinho, token CSRF e proposta de compra; expira após 30 minutos | Sessão e persistência do domínio são mecanismos diferentes |
| Contas e dados iniciais | Administrador fixo do enunciado e dados fictícios para facilitar demonstração | Os exemplos são fluxos ilustrativos, não uma obrigação de ter exactamente 5 clientes ou 4 vendas |
| Interface e fotos | CSS próprio e ilustrações SVG locais; aceita URLs HTTP/HTTPS | Funciona offline com os exemplos, sem acrescentar bibliotecas de interface |
| Pagamento | Confirmar regista a venda e emite a factura; não integra bancos/M-Pesa | O enunciado não pede um gateway de pagamento |
| Serviço systemd | Ficheiro e instruções incluídos; activação feita na máquina de destino | Esta execução não dispõe de systemd como gestor do sistema |

Não foram adicionados base de dados, ORM, Spring ou serviços externos. O sistema trabalha com uma instância do Tomcat. A sincronização em Java não coordena várias instâncias em computadores diferentes.
