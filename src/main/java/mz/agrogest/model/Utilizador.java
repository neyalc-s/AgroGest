package mz.agrogest.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Dados simples do domínio. As regras estão nos serviços. */
public class Utilizador implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nome;
    private String email;
    private String senhaHash;
    private Perfil perfil;
    private String telefone;
    private String endereco;
    private boolean ativo;
    private LocalDateTime criadoEm;

    public Utilizador() {}

    public Utilizador(int id, String nome, String email, String senhaHash, Perfil perfil, String telefone, String endereco, boolean ativo, LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.telefone = telefone;
        this.endereco = endereco;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
