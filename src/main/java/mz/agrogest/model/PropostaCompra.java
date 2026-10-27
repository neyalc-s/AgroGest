package mz.agrogest.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Os valores exactos apresentados numa página de confirmação. */
public final class PropostaCompra implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String token;
    private final int clienteId;
    private final List<ItemVenda> itens;
    private final BigDecimal subtotal;
    private final BigDecimal iva;

    public PropostaCompra(String token, int clienteId, List<ItemVenda> itens, BigDecimal subtotal, BigDecimal iva) {
        this.token = token;
        this.clienteId = clienteId;
        this.itens = copiar(itens);
        this.subtotal = subtotal;
        this.iva = iva;
    }

    private static List<ItemVenda> copiar(List<ItemVenda> origem) {
        List<ItemVenda> copia = new ArrayList<>();
        for (ItemVenda i : origem) {
            copia.add(new ItemVenda(i.getProdutoId(), i.getNome(), i.getUnidade(), i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal()));
        }
        return copia;
    }

    public String getToken() { return token; }
    public int getClienteId() { return clienteId; }
    public List<ItemVenda> getItens() { return copiar(itens); }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getIva() { return iva; }
    public BigDecimal getTotal() { return subtotal.add(iva); }
}
