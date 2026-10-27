package mz.agrogest.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados simples do domínio. As regras estão nos serviços. */
public class Colheita implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int cultivoId;
    private String cultura;
    private LocalDate data;
    private BigDecimal quantidade;
    private BigDecimal precoKg;
    private BigDecimal total;

    public Colheita() {}

    public Colheita(int id, int cultivoId, String cultura, LocalDate data, BigDecimal quantidade, BigDecimal precoKg, BigDecimal total) {
        this.id = id;
        this.cultivoId = cultivoId;
        this.cultura = cultura;
        this.data = data;
        this.quantidade = quantidade;
        this.precoKg = precoKg;
        this.total = total;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCultivoId() { return cultivoId; }
    public void setCultivoId(int cultivoId) { this.cultivoId = cultivoId; }
    public String getCultura() { return cultura; }
    public void setCultura(String cultura) { this.cultura = cultura; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public BigDecimal getPrecoKg() { return precoKg; }
    public void setPrecoKg(BigDecimal precoKg) { this.precoKg = precoKg; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
