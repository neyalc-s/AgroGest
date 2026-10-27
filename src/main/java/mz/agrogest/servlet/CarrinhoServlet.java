package mz.agrogest.servlet;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mz.agrogest.model.*;
import mz.agrogest.util.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
@WebServlet({"/carrinho", "/carrinho/*"})
public class CarrinhoServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        totaisCarrinho(req); req.setAttribute("menu", "carrinho"); view(req, resp, "carrinho");
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean adicionar = "adicionar".equals(req.getParameter("acao"));
        try {
            if (utilizador(req).getPerfil() != Perfil.CLIENTE) { resp.sendError(403); return; }
            synchronized (req.getSession()) {
                Map<Integer, BigDecimal> c = carrinho(req.getSession()); String acao = req.getParameter("acao");
                if ("esvaziar".equals(acao)) c.clear();
                else {
                    int id = Validacao.id(req.getParameter("id"));
                    if ("remover".equals(acao)) c.remove(id);
                    else if (adicionar || "atualizar".equals(acao)) {
                        Produto p = gestao().produto(id); BigDecimal q = Validacao.numero(req.getParameter("quantidade"), "quantidade", 3, false);
                        if (adicionar) q = q.add(c.getOrDefault(id, BigDecimal.ZERO));
                        if (p.isExpirado()) throw new RegraNegocio("Este produto está fora de validade.");
                        if (q.compareTo(p.getQuantidade()) > 0) throw new RegraNegocio("Stock insuficiente para a quantidade pedida.");
                        c.put(id, q);
                    } else throw new RegraNegocio("Operação inválida.");
                }
            }
            sucesso(req, adicionar ? "Produto adicionado ao carrinho." : "Carrinho actualizado.");
        } catch (RegraNegocio e) { erro(req, e.getMessage()); }
        redirect(req, resp, adicionar ? "/catalogo" : "/carrinho");
    }
}
