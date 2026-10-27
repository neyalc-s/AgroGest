package mz.agrogest.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mz.agrogest.model.*;
import mz.agrogest.service.*;
import mz.agrogest.util.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Controlador da área administrativa. As regras permanecem nos serviços. */
@WebServlet({"/admin", "/admin/*"})
public class AdminServlet extends BaseServlet {
    private String pagina(HttpServletRequest req) {
        String p = req.getPathInfo(); return p == null || p.equals("/") ? "dashboard" : p.substring(1);
    }
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pagina = pagina(req); GestaoService g = gestao();
        req.setAttribute("menu", pagina); req.setAttribute("categorias", CategoriaProduto.values()); req.setAttribute("tipos", TipoFornecedor.values());
        try {
            switch (pagina) {
                case "dashboard":
                    req.setAttribute("numProdutos", g.produtos().size()); req.setAttribute("numFornecedores", g.fornecedores().size()); req.setAttribute("numClientes", g.clientes().size()); req.setAttribute("numCampanhas", g.campanhas().size()); req.setAttribute("numVendas", g.vendas().size());
                    BigDecimal total = BigDecimal.ZERO; for (Venda v : g.vendas()) total = total.add(v.getTotal()); req.setAttribute("totalVendas", total);
                    List<Venda> recentes = g.vendas(); recentes.sort(Comparator.comparing(Venda::getData).reversed()); req.setAttribute("recentes", recentes.subList(0, Math.min(5, recentes.size())));
                    req.setAttribute("campanhas", g.campanhas()); break;
                case "produtos": case "armazem":
                    List<Produto> produtos = g.produtos();
                    if (pagina.equals("armazem")) produtos.removeIf(p -> p.getQuantidade().signum() <= 0);
                    Comparator<Produto> cp = Comparator.comparing(Produto::getNome, mz.agrogest.util.Formatos.nomes());
                    if ("preco".equals(req.getParameter("ordem"))) cp = Comparator.comparing(Produto::getPreco);
                    if ("stock".equals(req.getParameter("ordem"))) cp = Comparator.comparing(Produto::getQuantidade);
                    if ("data".equals(req.getParameter("ordem"))) cp = Comparator.comparing(Produto::getValidade, Comparator.nullsLast(Comparator.naturalOrder()));
                    ordenar(produtos, cp, req); req.setAttribute("produtos", produtos); req.setAttribute("fornecedores", g.fornecedores());
                    if (req.getParameter("editar") != null) req.setAttribute("editar", g.produto(Validacao.id(req.getParameter("editar")))); break;
                case "fornecedores":
                    List<Fornecedor> fornecedores = g.fornecedores(); Comparator<Fornecedor> cf = Comparator.comparing(Fornecedor::getNome, mz.agrogest.util.Formatos.nomes());
                    if ("provincia".equals(req.getParameter("ordem"))) cf = Comparator.comparing(Fornecedor::getProvincia, mz.agrogest.util.Formatos.nomes());
                    if ("tipo".equals(req.getParameter("ordem"))) cf = Comparator.comparing(f -> f.getTipo().name());
                    ordenar(fornecedores, cf, req); req.setAttribute("fornecedores", fornecedores); break;
                case "campanhas":
                    List<Campanha> campanhas = g.campanhas(); Comparator<Campanha> cc = "data".equals(req.getParameter("ordem")) ? Comparator.comparing(Campanha::getInicio) : Comparator.comparing(Campanha::getNome, mz.agrogest.util.Formatos.nomes());
                    ordenar(campanhas, cc, req); req.setAttribute("campanhas", campanhas); break;
                case "cultivos":
                    List<Cultivo> cultivos = g.cultivos(); Comparator<Cultivo> ccu = Comparator.comparing(Cultivo::getCultura, mz.agrogest.util.Formatos.nomes());
                    if ("data".equals(req.getParameter("ordem"))) ccu = Comparator.comparing(Cultivo::getPlantio);
                    if ("hectares".equals(req.getParameter("ordem"))) ccu = Comparator.comparing(Cultivo::getHectares);
                    ordenar(cultivos, ccu, req); req.setAttribute("cultivos", cultivos); req.setAttribute("campanhas", g.campanhas()); break;
                case "colheitas":
                    List<Colheita> colheitas = g.colheitas(); Comparator<Colheita> cco = Comparator.comparing(Colheita::getData);
                    if ("valor".equals(req.getParameter("ordem"))) cco = Comparator.comparing(Colheita::getTotal);
                    if ("quantidade".equals(req.getParameter("ordem"))) cco = Comparator.comparing(Colheita::getQuantidade);
                    ordenar(colheitas, cco, req); req.setAttribute("colheitas", colheitas); req.setAttribute("cultivos", g.cultivos()); break;
                case "clientes":
                    List<Utilizador> clientes = g.clientes(); Comparator<Utilizador> cu = Comparator.comparing(Utilizador::getNome, mz.agrogest.util.Formatos.nomes());
                    if ("data".equals(req.getParameter("ordem"))) cu = Comparator.comparing(Utilizador::getCriadoEm);
                    if ("email".equals(req.getParameter("ordem"))) cu = Comparator.comparing(Utilizador::getEmail);
                    ordenar(clientes, cu, req); req.setAttribute("clientes", clientes); break;
                case "compras":
                    List<Venda> vendas = g.vendas(); Comparator<Venda> cv = Comparator.comparing(Venda::getData);
                    if ("valor".equals(req.getParameter("ordem"))) cv = Comparator.comparing(Venda::getTotal);
                    if ("nome".equals(req.getParameter("ordem"))) cv = Comparator.comparing(Venda::getClienteNome, mz.agrogest.util.Formatos.nomes());
                    ordenar(vendas, cv, req); req.setAttribute("vendas", vendas); break;
                case "relatorios":
                    req.setAttribute("relatorio", new RelatorioService(store()).clientes(req.getParameter("tipo"))); break;
                case "persistencia":
                    req.setAttribute("gravado", ((PersistenciaService)getServletContext().getAttribute("persistencia")).existe()); break;
                case "listagens": break;
                default: resp.sendError(404); return;
            }
            view(req, resp, "admin-" + (pagina.equals("armazem") ? "produtos" : pagina));
        } catch (RegraNegocio e) { erro(req, e.getMessage()); redirect(req, resp, "/admin/produtos"); }
    }
    private <T> void ordenar(List<T> lista, Comparator<T> comparador, HttpServletRequest req) {
        lista.sort("desc".equals(req.getParameter("sentido")) ? comparador.reversed() : comparador);
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pagina = pagina(req); String acao = req.getParameter("acao"); GestaoService g = gestao();
        try {
            if (acao == null) throw new RegraNegocio("Seleccione uma operação.");
            switch (pagina) {
                case "fornecedores":
                    if (acao.equals("remover")) g.removerFornecedor(id(req));
                    else if (acao.equals("criar")) g.criarFornecedor(req.getParameter("nome"), Validacao.opcao(TipoFornecedor.class, req.getParameter("tipo")), req.getParameter("contacto"), req.getParameter("provincia"));
                    else throw new RegraNegocio("Operação inválida."); break;
                case "produtos":
                    if (acao.equals("remover")) g.removerProduto(id(req));
                    else if (acao.equals("repor")) g.reporStock(id(req), numero(req, "quantidade", 3, false));
                    else if (acao.equals("guardar")) {
                        String validade = req.getParameter("validade"); String codigo = req.getParameter("id");
                        Integer produtoId = codigo == null || codigo.isBlank() ? null : Validacao.id(codigo);
                        Integer versao = produtoId == null ? null : Validacao.versao(req.getParameter("versao"));
                        g.guardarProduto(produtoId, versao, req.getParameter("nome"), Validacao.opcao(CategoriaProduto.class, req.getParameter("categoria")), Validacao.id(req.getParameter("fornecedorId")), numero(req, "preco", 2, false), numero(req, "quantidade", 3, true), req.getParameter("unidade"), req.getParameter("descricao"), req.getParameter("foto"), validade == null || validade.isBlank() ? null : Validacao.data(validade, "validade"));
                    } else throw new RegraNegocio("Operação inválida."); break;
                case "campanhas":
                    if (acao.equals("remover")) g.removerCampanha(id(req));
                    else if (acao.equals("fechar")) g.estadoCampanha(id(req), true);
                    else if (acao.equals("reabrir")) g.estadoCampanha(id(req), false);
                    else if (acao.equals("criar")) g.criarCampanha(req.getParameter("nome"), Validacao.data(req.getParameter("inicio"), "início"), Validacao.data(req.getParameter("fim"), "fim"));
                    else throw new RegraNegocio("Operação inválida."); break;
                case "cultivos":
                    if (acao.equals("remover")) g.removerCultivo(id(req));
                    else if (acao.equals("criar")) g.criarCultivo(Validacao.id(req.getParameter("campanhaId")), req.getParameter("cultura"), req.getParameter("variedade"), numero(req, "hectares", 3, false), Validacao.data(req.getParameter("plantio"), "plantio"));
                    else throw new RegraNegocio("Operação inválida."); break;
                case "colheitas":
                    if (!acao.equals("criar")) throw new RegraNegocio("Operação inválida.");
                    g.criarColheita(Validacao.id(req.getParameter("cultivoId")), Validacao.data(req.getParameter("data"), "colheita"), numero(req, "quantidade", 3, false), numero(req, "precoKg", 2, false)); break;
                case "persistencia":
                    PersistenciaService p = (PersistenciaService)getServletContext().getAttribute("persistencia");
                    if (acao.equals("gravar")) p.gravar();
                    else if (acao.equals("eliminar")) { p.eliminar(); sucesso(req, "Ficheiros eliminados. Os dados continuam em memória e serão gravados no encerramento normal."); redirect(req, resp, "/admin/persistencia"); return; }
                    else throw new RegraNegocio("Operação inválida."); break;
                default: resp.sendError(404); return;
            }
            sucesso(req, "Operação concluída com sucesso.");
        } catch (RegraNegocio e) { erro(req, e.getMessage()); }
        catch (IOException e) { getServletContext().log("Falha na persistência", e); erro(req, "Não foi possível gravar/eliminar os ficheiros. Confirme as permissões da pasta de dados e consulte os logs."); }
        redirect(req, resp, "/admin/" + pagina);
    }
    private int id(HttpServletRequest req) { return Validacao.id(req.getParameter("id")); }
    private BigDecimal numero(HttpServletRequest req, String campo, int casas, boolean zero) { return Validacao.numero(req.getParameter(campo), campo, casas, zero); }
}
