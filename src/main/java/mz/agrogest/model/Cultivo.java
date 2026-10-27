package mz.agrogest.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados simples do domínio. As regras estão nos serviços. */
public class Cultivo implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int campanhaId;
    private String campanhaNome;
    private String cultura;
    private String variedade;
    private BigDecimal hectares;
    private LocalDate plantio;

    public Cultivo() {}

    public Cultivo(int id, int campanhaId, String campanhaNome, String cultura, String variedade, BigDecimal hectares, LocalDate plantio) {
        this.id = id;
        this.campanhaId = campanhaId;
        this.campanhaNome = campanhaNome;
        this.cultura = cultura;
        this.variedade = variedade;
        this.hectares = hectares;
        this.plantio = plantio;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCampanhaId() { return campanhaId; }
    public void setCampanhaId(int campanhaId) { this.campanhaId = campanhaId; }
    public String getCampanhaNome() { return campanhaNome; }
    public void setCampanhaNome(String campanhaNome) { this.campanhaNome = campanhaNome; }
    public String getCultura() { return cultura; }
    public void setCultura(String cultura) { this.cultura = cultura; }
    public String getVariedade() { return variedade; }
    public void setVariedade(String variedade) { this.variedade = variedade; }
    public BigDecimal getHectares() { return hectares; }
    public void setHectares(BigDecimal hectares) { this.hectares = hectares; }
    public LocalDate getPlantio() { return plantio; }
    public void setPlantio(LocalDate plantio) { this.plantio = plantio; }
}
