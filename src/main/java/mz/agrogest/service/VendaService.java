package mz.agrogest.service;

import mz.agrogest.model.*;
import mz.agrogest.repo.*;
import mz.agrogest.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class VendaService {
    public static final BigDecimal TAXA_IVA = new BigDecimal("0.16");
    private final DataStore store;
    public VendaService(DataStore store) { this.store = store; }

    /** Prepara o mesmo resumo que será conferido ao confirmar a compra. */
    public PropostaCompra prepararCompra(int clienteId, Map<Integer, BigDecimal> carrinho) {
        synchronized (store) {
            Estado e = store.estado();
            validarCliente(e, clienteId);
            List<ItemVenda> itens = validarItens(e, carrinho);
            BigDecimal subtotal = subtotal(itens);
            return new PropostaCompra(UUID.randomUUID().toString(), clienteId, itens, subtotal, Validacao.dinheiro(subtotal.multiply(TAXA_IVA)));
        }
    }

    /** Entrada usada pelo Servlet: exige as condições que o cliente viu. */
    public Venda confirmarCompra(int clienteId, Map<Integer, BigDecimal> carrinho, PropostaCompra proposta) {
        if (proposta == null) throw new RegraNegocio("Reabra a confirmação e confira o resumo antes de comprar.");
        return efectuarCompra(clienteId, carrinho, proposta);
    }

    /** Entrada interna para os dados de demonstração e testes de regras. */
    public Venda comprar(int clienteId, Map<Integer, BigDecimal> carrinho) {
        return efectuarCompra(clienteId, carrinho, null);
    }

    /** RN01/RN02: validar TUDO antes de descontar o PRIMEIRO produto. */
    private Venda efectuarCompra(int clienteId, Map<Integer, BigDecimal> carrinho, PropostaCompra proposta) {
        synchronized (store) {
            Estado e = store.estado();
            Utilizador cliente = validarCliente(e, clienteId);
            List<ItemVenda> itens = validarItens(e, carrinho);
            if (proposta != null) conferirProposta(clienteId, itens, proposta);
            BigDecimal subtotal = subtotal(itens);
            BigDecimal iva = Validacao.dinheiro(subtotal.multiply(TAXA_IVA));
            LocalDateTime agora = LocalDateTime.now(ZoneId.of("Africa/Maputo")); int id = e.vendaId.incrementAndGet();
            Venda venda = new Venda(id, "FT-" + agora.getYear() + "-" + id, cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.getTelefone(), cliente.getEndereco(), agora, itens, subtotal, iva, subtotal.add(iva));
            // Nenhuma validação susceptível de falhar fica depois deste ponto.
            for (ItemVenda item : itens) {
                Produto p = e.produtos.get(item.getProdutoId()); p.setQuantidade(p.getQuantidade().subtract(item.getQuantidade()));
                p.setVersao(p.getVersao() + 1);
            }
            e.vendas.put(venda.getId(), venda); return venda;
        }
    }

    private Utilizador validarCliente(Estado e, int clienteId) {
        Utilizador cliente = e.utilizadores.get(clienteId);
        if (cliente == null || cliente.getPerfil() != Perfil.CLIENTE || !cliente.isAtivo()) throw new RegraNegocio("Só os clientes podem efectuar compras.");
        return cliente;
    }

    private List<ItemVenda> validarItens(Estado e, Map<Integer, BigDecimal> carrinho) {
        if (carrinho == null || carrinho.isEmpty()) throw new RegraNegocio("O carrinho está vazio.");
        // O Servlet mantém o monitor da sessão; os serviços mantêm o do store.
        Map<Integer, BigDecimal> pedido = new LinkedHashMap<>(carrinho);
        List<ItemVenda> itens = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> linha : pedido.entrySet()) {
            Produto p = e.produtos.get(linha.getKey());
            if (p == null) throw new RegraNegocio("Um produto foi removido do catálogo. Remova-o do carrinho.");
            BigDecimal q = Validacao.numero(linha.getValue() == null ? null : linha.getValue().toPlainString(), "quantidade", 3, false);
            if (p.isExpirado()) throw new RegraNegocio(p.getNome() + ": validade expirada.");
            if (p.getQuantidade().compareTo(q) < 0) throw new RegraNegocio(p.getNome() + ": stock insuficiente. Disponível: " + p.getQuantidade().stripTrailingZeros().toPlainString() + " " + p.getUnidade() + ".");
            BigDecimal valor = Validacao.dinheiro(p.getPreco().multiply(q));
            itens.add(new ItemVenda(p.getId(), p.getNome(), p.getUnidade(), q, p.getPreco(), valor));
        }
        return itens;
    }

    private BigDecimal subtotal(List<ItemVenda> itens) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda i : itens) total = total.add(i.getSubtotal());
        return Validacao.dinheiro(total);
    }

    private void conferirProposta(int clienteId, List<ItemVenda> actuais, PropostaCompra proposta) {
        Map<Integer, ItemVenda> apresentados = new HashMap<>();
        for (ItemVenda i : proposta.getItens()) apresentados.put(i.getProdutoId(), i);
        boolean igual = proposta.getClienteId() == clienteId && apresentados.size() == actuais.size();
        for (ItemVenda actual : actuais) {
            ItemVenda anterior = apresentados.get(actual.getProdutoId());
            if (anterior == null || anterior.getQuantidade().compareTo(actual.getQuantidade()) != 0
                    || anterior.getPrecoUnitario().compareTo(actual.getPrecoUnitario()) != 0
                    || !Objects.equals(anterior.getNome(), actual.getNome())
                    || !Objects.equals(anterior.getUnidade(), actual.getUnidade())) igual = false;
        }
        if (!igual) throw new RegraNegocio("O carrinho ou os produtos foram alterados. Confira o resumo actualizado e confirme novamente. Nenhuma compra foi registada.");
    }
    public Venda factura(int id, Utilizador utilizador) {
        synchronized (store) {
            Venda v = store.estado().vendas.get(id);
            if (v == null) return null;
            if (utilizador.getPerfil() != Perfil.ADMIN && v.getClienteId() != utilizador.getId()) throw new SecurityException("Esta factura pertence a outro cliente.");
            return v;
        }
    }
    public List<Venda> historico(int clienteId) {
        synchronized (store) {
            List<Venda> r = new ArrayList<>();
            for (Venda v : store.estado().vendas.values()) if (v.getClienteId() == clienteId) r.add(v);
            r.sort(Comparator.comparing(Venda::getData).reversed()); return r;
        }
    }
    public List<LinhaCarrinho> linhas(Map<Integer, BigDecimal> carrinho) {
        synchronized (store) {
            List<LinhaCarrinho> r = new ArrayList<>();
            for (Map.Entry<Integer, BigDecimal> l : carrinho.entrySet()) {
                Produto p = store.estado().produtos.get(l.getKey());
                r.add(new LinhaCarrinho(l.getKey(), p == null ? "Produto removido" : p.getNome(), p == null ? "" : p.getUnidade(), l.getValue(), p == null ? BigDecimal.ZERO : p.getPreco(), p == null ? BigDecimal.ZERO : p.getQuantidade(), p == null ? BigDecimal.ZERO : Validacao.dinheiro(p.getPreco().multiply(l.getValue()))));
            }
            return r;
        }
    }
}
