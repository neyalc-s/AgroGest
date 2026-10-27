package mz.agrogest.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** Dados simples do domínio. As regras estão nos serviços. */
public class ItemVenda implements Serializable {
    private static final long serialVersionUID = 1L;
    private int produtoId;
    private String nome;
    private String unidade;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;

    public ItemVenda() {}

    public ItemVenda(int produtoId, String nome, String unidade, BigDecimal quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.unidade = unidade;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
    }

    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
