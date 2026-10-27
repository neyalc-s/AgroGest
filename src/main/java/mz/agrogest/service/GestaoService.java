package mz.agrogest.service;

import mz.agrogest.model.*;
import mz.agrogest.repo.*;
import mz.agrogest.util.*;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** CRUD e validações. Nenhuma alteração das listas fica fora do mesmo monitor. */
public class GestaoService {
    private final DataStore store;
    public GestaoService(DataStore store) { this.store = store; }

    public Fornecedor criarFornecedor(String nome, TipoFornecedor tipo, String contacto, String provincia) {
        nome = Validacao.texto(nome, "nome", 120); contacto = Validacao.texto(contacto, "contacto", 80); provincia = Validacao.texto(provincia, "província", 80);
        if (tipo == null) throw new RegraNegocio("Seleccione o tipo de fornecedor.");
        synchronized (store) {
            Estado e = store.estado();
            Fornecedor f = new Fornecedor(e.fornecedorId.incrementAndGet(), nome, tipo, contacto, provincia);
            e.fornecedores.put(f.getId(), f); return f;
        }
    }
    public void removerFornecedor(int id) {
        synchronized (store) {
            if (!store.estado().fornecedores.containsKey(id)) throw new RegraNegocio("Fornecedor inexistente.");
            for (Produto p : store.estado().produtos.values()) if (p.getFornecedorId() == id) throw new RegraNegocio("Remova primeiro os produtos deste fornecedor.");
            store.estado().fornecedores.remove(id);
        }
    }
    public Produto guardarProduto(Integer id, Integer versaoEsperada, String nome, CategoriaProduto categoria, int fornecedorId, BigDecimal preco, BigDecimal quantidade, String unidade, String descricao, String foto, LocalDate validade) {
        nome = Validacao.texto(nome, "nome do produto", 120); unidade = Validacao.texto(unidade, "unidade", 30); descricao = Validacao.texto(descricao, "descrição", 1000);
        preco = Validacao.numero(preco.toPlainString(), "preço", 2, false); quantidade = Validacao.numero(quantidade.toPlainString(), "stock", 3, true);
        if (categoria == null) throw new RegraNegocio("Seleccione a categoria.");
        foto = foto == null ? "" : foto.trim();
        if (foto.isEmpty()) foto = "assets/img/" + categoria.name().toLowerCase() + ".svg";
        if (!foto.startsWith("assets/img/")) {
            try { URI uri = URI.create(foto); if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) || uri.getHost() == null || foto.length() > 1500) throw new IllegalArgumentException(); }
            catch (Exception ex) { throw new RegraNegocio("A foto deve ser um URL HTTP/HTTPS válido."); }
        }
        synchronized (store) {
            Estado e = store.estado(); Fornecedor f = e.fornecedores.get(fornecedorId);
            if (f == null) throw new RegraNegocio("Fornecedor inexistente.");
            Produto anterior = id == null ? null : e.produtos.get(id);
            if (id != null && anterior == null) throw new RegraNegocio("Produto inexistente.");
            // Uma compra/reposição pode ter acontecido enquanto o formulário estava aberto.
            if (anterior != null && (versaoEsperada == null || anterior.getVersao() != versaoEsperada)) {
                throw new RegraNegocio("O produto foi alterado desde que abriu o formulário. Abra novamente a edição e confira os dados antes de guardar.");
            }
            int codigo = id == null ? e.produtoId.incrementAndGet() : id;
            Produto p = new Produto(codigo, nome, categoria, f.getId(), f.getNome(), preco, quantidade, unidade, descricao, foto, validade);
            if (anterior != null) p.setVersao(anterior.getVersao() + 1);
            e.produtos.put(codigo, p); return copiarProduto(p);
        }
    }
    public void reporStock(int id, BigDecimal quantidade) {
        quantidade = Validacao.numero(quantidade.toPlainString(), "quantidade a repor", 3, false);
        synchronized (store) {
            Produto p = store.estado().produtos.get(id);
            if (p == null) throw new RegraNegocio("Produto inexistente.");
            BigDecimal novo = Validacao.numero(p.getQuantidade().add(quantidade).toPlainString(), "stock final", 3, true);
            p.setQuantidade(novo);
            p.setVersao(p.getVersao() + 1);
        }
    }
    public void removerProduto(int id) { synchronized (store) { produto(id); store.estado().produtos.remove(id); } }
    public Produto produto(int id) {
        synchronized (store) {
            Produto p = store.estado().produtos.get(id); if (p == null) throw new RegraNegocio("Produto inexistente."); return copiarProduto(p);
        }
    }
    public Campanha criarCampanha(String nome, LocalDate inicio, LocalDate fim) {
        nome = Validacao.texto(nome, "nome da campanha", 120);
        if (inicio == null || fim == null || fim.isBefore(inicio)) throw new RegraNegocio("A data final não pode ser anterior à inicial.");
        synchronized (store) {
            Estado e = store.estado(); Campanha c = new Campanha(e.campanhaId.incrementAndGet(), nome, inicio, fim, false); e.campanhas.put(c.getId(), c); return c;
        }
    }
    public void estadoCampanha(int id, boolean fechada) { synchronized (store) { campanha(id).setFechada(fechada); } }
    public void removerCampanha(int id) {
        synchronized (store) {
            campanha(id);
            for (Cultivo c : store.estado().cultivos.values()) if (c.getCampanhaId() == id) throw new RegraNegocio("A campanha tem cultivos associados e não pode ser removida.");
            store.estado().campanhas.remove(id);
        }
    }
    private Campanha campanha(int id) { Campanha c = store.estado().campanhas.get(id); if (c == null) throw new RegraNegocio("Campanha inexistente."); return c; }
    public Cultivo criarCultivo(int campanhaId, String cultura, String variedade, BigDecimal hectares, LocalDate plantio) {
        cultura = Validacao.texto(cultura, "cultura", 120); variedade = Validacao.texto(variedade, "variedade", 120);
        hectares = Validacao.numero(hectares.toPlainString(), "hectares", 3, false);
        synchronized (store) {
            Campanha c = campanha(campanhaId);
            if (c.isFechada()) throw new RegraNegocio("Reabra a campanha antes de registar cultivos.");
            if (plantio == null || plantio.isBefore(c.getInicio()) || plantio.isAfter(c.getFim())) throw new RegraNegocio("O plantio deve estar dentro das datas da campanha.");
            Estado e = store.estado(); Cultivo cultivo = new Cultivo(e.cultivoId.incrementAndGet(), c.getId(), c.getNome(), cultura, variedade, hectares, plantio); e.cultivos.put(cultivo.getId(), cultivo); return cultivo;
        }
    }
    public void removerCultivo(int id) {
        synchronized (store) {
            if (!store.estado().cultivos.containsKey(id)) throw new RegraNegocio("Cultivo inexistente.");
            for (Colheita c : store.estado().colheitas.values()) if (c.getCultivoId() == id) throw new RegraNegocio("O cultivo tem colheitas associadas e não pode ser removido.");
            store.estado().cultivos.remove(id);
        }
    }
    public Colheita criarColheita(int cultivoId, LocalDate data, BigDecimal quantidade, BigDecimal precoKg) {
        quantidade = Validacao.numero(quantidade.toPlainString(), "quantidade colhida", 3, false); precoKg = Validacao.numero(precoKg.toPlainString(), "preço por kg", 2, false);
        synchronized (store) {
            Estado e = store.estado(); Cultivo cultivo = e.cultivos.get(cultivoId);
            if (cultivo == null) throw new RegraNegocio("Cultivo inexistente."); Campanha c = campanha(cultivo.getCampanhaId());
            if (c.isFechada()) throw new RegraNegocio("Reabra a campanha antes de registar colheitas.");
            if (data == null || data.isBefore(cultivo.getPlantio()) || data.isAfter(c.getFim())) throw new RegraNegocio("A colheita deve ocorrer entre o plantio e o fim da campanha.");
            Colheita colheita = new Colheita(e.colheitaId.incrementAndGet(), cultivo.getId(), cultivo.getCultura(), data, quantidade, precoKg, Validacao.dinheiro(quantidade.multiply(precoKg))); e.colheitas.put(colheita.getId(), colheita); return colheita;
        }
    }
    public List<Fornecedor> fornecedores() { synchronized (store) { return new ArrayList<>(store.estado().fornecedores.values()); } }
    public List<Produto> produtos() {
        synchronized (store) {
            List<Produto> lista = new ArrayList<>();
            for (Produto p : store.estado().produtos.values()) lista.add(copiarProduto(p));
            return lista;
        }
    }
    /** A página recebe uma cópia estável, sem permitir alterar o stock por fora do serviço. */
    private Produto copiarProduto(Produto p) {
        Produto copia = new Produto(p.getId(), p.getNome(), p.getCategoria(), p.getFornecedorId(), p.getFornecedorNome(), p.getPreco(), p.getQuantidade(), p.getUnidade(), p.getDescricao(), p.getFoto(), p.getValidade());
        copia.setVersao(p.getVersao());
        return copia;
    }
    public List<Campanha> campanhas() { synchronized (store) { return new ArrayList<>(store.estado().campanhas.values()); } }
    public List<Cultivo> cultivos() { synchronized (store) { return new ArrayList<>(store.estado().cultivos.values()); } }
    public List<Colheita> colheitas() { synchronized (store) { return new ArrayList<>(store.estado().colheitas.values()); } }
    public List<Utilizador> clientes() { synchronized (store) { List<Utilizador> r = new ArrayList<>(); for (Utilizador u : store.estado().utilizadores.values()) if (u.getPerfil() == Perfil.CLIENTE) r.add(u); return r; } }
    public List<Venda> vendas() { synchronized (store) { return new ArrayList<>(store.estado().vendas.values()); } }
}
