package mz.agrogest.model;

import java.io.Serializable;

/** Dados do selector temporário; não contém o hash nem a senha original. */
public final class AcessoDemonstracao implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int utilizadorId;
    private final String nome;
    private final String email;
    private final Perfil perfil;
    private final boolean ativo;
    private final String credencial;

    public AcessoDemonstracao(int utilizadorId, String nome, String email, Perfil perfil, boolean ativo, String credencial) {
        this.utilizadorId = utilizadorId;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.ativo = ativo;
        this.credencial = credencial;
    }

    public int getUtilizadorId() { return utilizadorId; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Perfil getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
    public String getCredencial() { return credencial; }
}
