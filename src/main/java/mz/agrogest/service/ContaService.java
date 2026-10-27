package mz.agrogest.service;

import mz.agrogest.model.*;
import mz.agrogest.repo.DataStore;
import mz.agrogest.repo.Estado;
import mz.agrogest.util.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class ContaService {
    private final DataStore store;
    public ContaService(DataStore store) { this.store = store; }
    public Utilizador registar(String nome, String email, String senha, String telefone, String endereco) {
        nome = Validacao.texto(nome, "nome", 100);
        email = Validacao.texto(email, "email", 160).toLowerCase(Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new RegraNegocio("Email inválido.");
        telefone = Validacao.texto(telefone, "telefone", 30);
        endereco = Validacao.texto(endereco, "endereço", 240);
        String hash = Senhas.criarHash(senha);
        synchronized (store) {
            for (Utilizador u : store.estado().utilizadores.values()) {
                if (u.getEmail().equalsIgnoreCase(email)) throw new RegraNegocio("Este email já está registado.");
            }
            Estado e = store.estado();
            Utilizador u = new Utilizador(e.utilizadorId.incrementAndGet(), nome, email, hash, Perfil.CLIENTE, telefone, endereco, true, LocalDateTime.now(ZoneId.of("Africa/Maputo")));
            e.utilizadores.put(u.getId(), u);
            return u;
        }
    }
    public Utilizador autenticar(String email, String senha) {
        Utilizador encontrado = null;
        synchronized (store) {
            for (Utilizador u : store.estado().utilizadores.values()) {
                if (email != null && u.getEmail().equalsIgnoreCase(email.trim())) encontrado = u;
            }
        }
        if (encontrado == null || !encontrado.isAtivo() || !Senhas.verificar(senha, encontrado.getSenhaHash())) throw new RegraNegocio("Email ou palavra-passe incorrectos.");
        return encontrado;
    }
    public void garantirAdministrador() {
        synchronized (store) {
            for (Utilizador u : store.estado().utilizadores.values()) if (u.getPerfil() == Perfil.ADMIN) return;
            Estado e = store.estado();
            Utilizador admin = new Utilizador(e.utilizadorId.incrementAndGet(), "Administrador", "admin@agro.mz", Senhas.criarHash("admin234"), Perfil.ADMIN, "+258 84 000 0000", "Nampula, Moçambique", true, LocalDateTime.now(ZoneId.of("Africa/Maputo")));
            e.utilizadores.put(admin.getId(), admin);
        }
    }
    public Utilizador procurar(int id) { synchronized (store) { return store.estado().utilizadores.get(id); } }

    /** Selector de todas as contas, usado apenas pelo modo de demonstração. */
    public List<AcessoDemonstracao> acessosDemonstracao() {
        synchronized (store) {
            List<AcessoDemonstracao> acessos = new ArrayList<>();
            for (Utilizador u : store.estado().utilizadores.values()) {
                acessos.add(new AcessoDemonstracao(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.isAtivo(), "demo-" + UUID.randomUUID()));
            }
            acessos.sort(Comparator.comparing(AcessoDemonstracao::getPerfil)
                    .thenComparing(AcessoDemonstracao::getNome, Formatos.nomes()));
            return acessos;
        }
    }

    /** O controlador fornece o acesso emitido para a própria sessão do browser. */
    public Utilizador autenticarDemonstracao(AcessoDemonstracao acesso, String email, String credencial) {
        if (acesso == null || email == null || credencial == null
                || !acesso.getEmail().equalsIgnoreCase(email.trim())
                || !MessageDigest.isEqual(acesso.getCredencial().getBytes(StandardCharsets.UTF_8), credencial.getBytes(StandardCharsets.UTF_8))) {
            throw new RegraNegocio("Email ou palavra-passe incorrectos.");
        }
        synchronized (store) {
            Utilizador u = store.estado().utilizadores.get(acesso.getUtilizadorId());
            if (u == null || !u.isAtivo() || !u.getEmail().equalsIgnoreCase(email.trim())) {
                throw new RegraNegocio("Esta conta não está disponível.");
            }
            return u;
        }
    }
}
