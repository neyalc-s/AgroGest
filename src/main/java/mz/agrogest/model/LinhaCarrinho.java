package mz.agrogest.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** Dados simples do domínio. As regras estão nos serviços. */
public class LinhaCarrinho implements Serializable {
    private static final long serialVersionUID = 1L;
    private int produtoId;
    private String nome;
    private String unidade;
    private BigDecimal quantidade;
    private BigDecimal preco;
    private BigDecimal stock;
    private BigDecimal subtotal;

    public LinhaCarrinho() {}

    public LinhaCarrinho(int produtoId, String nome, String unidade, BigDecimal quantidade, BigDecimal preco, BigDecimal stock, BigDecimal subtotal) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.unidade = unidade;
        this.quantidade = quantidade;
        this.preco = preco;
        this.stock = stock;
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
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public BigDecimal getStock() { return stock; }
    public void setStock(BigDecimal stock) { this.stock = stock; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
