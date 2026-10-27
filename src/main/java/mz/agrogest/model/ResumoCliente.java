package mz.agrogest.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** Dados simples do domínio. As regras estão nos serviços. */
public class ResumoCliente implements Serializable {
    private static final long serialVersionUID = 1L;
    private int clienteId;
    private String nome;
    private String email;
    private int compras;
    private BigDecimal volume;

    public ResumoCliente() {}

    public ResumoCliente(int clienteId, String nome, String email, int compras, BigDecimal volume) {
        this.clienteId = clienteId;
        this.nome = nome;
        this.email = email;
        this.compras = compras;
        this.volume = volume;
    }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getCompras() { return compras; }
    public void setCompras(int compras) { this.compras = compras; }
    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }
}
