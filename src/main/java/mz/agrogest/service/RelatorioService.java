package mz.agrogest.service;

import mz.agrogest.model.*;
import mz.agrogest.repo.DataStore;
import java.math.BigDecimal;
import java.util.*;

public class RelatorioService {
    private final DataStore store;
    public RelatorioService(DataStore store) { this.store = store; }
    public List<ResumoCliente> clientes(String tipo) {
        synchronized (store) {
            List<ResumoCliente> lista = new ArrayList<>();
            for (Utilizador u : store.estado().utilizadores.values()) {
                if (u.getPerfil() != Perfil.CLIENTE) continue;
                int compras = 0; BigDecimal volume = BigDecimal.ZERO;
                for (Venda v : store.estado().vendas.values()) if (v.getClienteId() == u.getId()) { compras++; volume = volume.add(v.getTotal()); }
                if ("volume".equals(tipo) && compras == 0) continue;
                if ("assiduos".equals(tipo) && compras == 0) continue;
                if ("sem-compras".equals(tipo) && compras != 0) continue;
                lista.add(new ResumoCliente(u.getId(), u.getNome(), u.getEmail(), compras, volume));
            }
            Comparator<ResumoCliente> nome = Comparator.comparing(ResumoCliente::getNome, mz.agrogest.util.Formatos.nomes());
            if ("volume".equals(tipo)) lista.sort(Comparator.comparing(ResumoCliente::getVolume).reversed().thenComparing(nome));
            else if ("assiduos".equals(tipo)) lista.sort(Comparator.comparingInt(ResumoCliente::getCompras).reversed().thenComparing(nome));
            else lista.sort(nome);
            return lista;
        }
    }
}
