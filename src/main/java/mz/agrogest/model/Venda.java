package mz.agrogest.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Dados simples do domínio. As regras estão nos serviços. */
public class Venda implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String numero;
    private int clienteId;
    private String clienteNome;
    private String clienteEmail;
    private String clienteTelefone;
    private String clienteEndereco;
    private LocalDateTime data;
    private List<ItemVenda> itens;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;

    public Venda() {}

    public Venda(int id, String numero, int clienteId, String clienteNome, String clienteEmail, String clienteTelefone, String clienteEndereco, LocalDateTime data, List<ItemVenda> itens, BigDecimal subtotal, BigDecimal iva, BigDecimal total) {
        this.id = id;
        this.numero = numero;
        this.clienteId = clienteId;
        this.clienteNome = clienteNome;
        this.clienteEmail = clienteEmail;
        this.clienteTelefone = clienteTelefone;
        this.clienteEndereco = clienteEndereco;
        this.data = data;
        this.itens = itens;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public String getClienteTelefone() { return clienteTelefone; }
    public void setClienteTelefone(String clienteTelefone) { this.clienteTelefone = clienteTelefone; }
    public String getClienteEndereco() { return clienteEndereco; }
    public void setClienteEndereco(String clienteEndereco) { this.clienteEndereco = clienteEndereco; }
    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
    public List<ItemVenda> getItens() { return itens; }
    public void setItens(List<ItemVenda> itens) { this.itens = itens; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getIva() { return iva; }
    public void setIva(BigDecimal iva) { this.iva = iva; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
