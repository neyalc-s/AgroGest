package mz.agrogest.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados simples do domínio. As regras estão nos serviços. */
public class Produto implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nome;
    private CategoriaProduto categoria;
    private int fornecedorId;
    private String fornecedorNome;
    private BigDecimal preco;
    private BigDecimal quantidade;
    private String unidade;
    private String descricao;
    private String foto;
    private LocalDate validade;
    // Zero também é válido ao carregar ficheiros criados pela primeira versão.
    private int versao;

    public Produto() {}

    public Produto(int id, String nome, CategoriaProduto categoria, int fornecedorId, String fornecedorNome, BigDecimal preco, BigDecimal quantidade, String unidade, String descricao, String foto, LocalDate validade) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.fornecedorId = fornecedorId;
        this.fornecedorNome = fornecedorNome;
        this.preco = preco;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.descricao = descricao;
        this.foto = foto;
        this.validade = validade;
        this.versao = 1;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public CategoriaProduto getCategoria() { return categoria; }
    public void setCategoria(CategoriaProduto categoria) { this.categoria = categoria; }
    public int getFornecedorId() { return fornecedorId; }
    public void setFornecedorId(int fornecedorId) { this.fornecedorId = fornecedorId; }
    public String getFornecedorNome() { return fornecedorNome; }
    public void setFornecedorNome(String fornecedorNome) { this.fornecedorNome = fornecedorNome; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate validade) { this.validade = validade; }
    public int getVersao() { return versao; }
    public void setVersao(int versao) { this.versao = versao; }

    public boolean isExpirado() {
        return validade != null && validade.isBefore(LocalDate.now(java.time.ZoneId.of("Africa/Maputo")));
    }
}
