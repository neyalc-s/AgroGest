package mz.agrogest;

import mz.agrogest.model.*;
import mz.agrogest.repo.*;
import mz.agrogest.service.*;
import mz.agrogest.util.RegraNegocio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DemonstracaoTest {
    DataStore store;
    ContaService contas;

    @BeforeEach void preparar() {
        store = new DataStore();
        DadosExemplo.criar(store);
        contas = new ContaService(store);
    }

    @Test void todasAsContasRecebemAcessoSemAlterarAsSenhasOriginais() {
        List<AcessoDemonstracao> acessos = contas.acessosDemonstracao();
        assertEquals(4, acessos.size());
        assertEquals(Perfil.ADMIN, acessos.get(0).getPerfil());
        for (AcessoDemonstracao a : acessos) {
            String hash = contas.procurar(a.getUtilizadorId()).getSenhaHash();
            assertEquals(a.getUtilizadorId(), contas.autenticarDemonstracao(a, a.getEmail(), a.getCredencial()).getId());
            assertEquals(hash, contas.procurar(a.getUtilizadorId()).getSenhaHash());
            String original = a.getPerfil() == Perfil.ADMIN ? "admin234" : "cliente123";
            assertEquals(a.getUtilizadorId(), contas.autenticar(a.getEmail(), original).getId());
        }
    }

    @Test void novaContaTambemApareceComAcessoTemporario() {
        Utilizador novo = contas.registar("Cliente novo", "novo@teste.mz", "segredo-novo", "84", "Nampula");
        AcessoDemonstracao a = contas.acessosDemonstracao().stream().filter(x -> x.getUtilizadorId() == novo.getId()).findFirst().orElseThrow();
        assertEquals(novo.getId(), contas.autenticarDemonstracao(a, a.getEmail(), a.getCredencial()).getId());
        assertEquals(novo.getId(), contas.autenticar(novo.getEmail(), "segredo-novo").getId());
        assertNotEquals("segredo-novo", a.getCredencial());
    }

    @Test void credencialErradaOutroEmailOuContaInactivaSaoRecusados() {
        AcessoDemonstracao a = contas.acessosDemonstracao().get(0);
        assertThrows(RegraNegocio.class, () -> contas.autenticarDemonstracao(a, a.getEmail(), "errada"));
        assertThrows(RegraNegocio.class, () -> contas.autenticarDemonstracao(a, "outro@teste.mz", a.getCredencial()));
        synchronized (store) { store.estado().utilizadores.get(a.getUtilizadorId()).setAtivo(false); }
        assertThrows(RegraNegocio.class, () -> contas.autenticarDemonstracao(a, a.getEmail(), a.getCredencial()));
    }
}
