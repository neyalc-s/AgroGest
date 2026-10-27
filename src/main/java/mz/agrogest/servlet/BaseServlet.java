package mz.agrogest.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import mz.agrogest.model.*;
import mz.agrogest.repo.DataStore;
import mz.agrogest.service.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public abstract class BaseServlet extends HttpServlet {
    protected DataStore store() { return (DataStore)getServletContext().getAttribute("store"); }
    protected GestaoService gestao() { return new GestaoService(store()); }
    protected VendaService vendas() { return new VendaService(store()); }
    protected Utilizador utilizador(HttpServletRequest req) { return (Utilizador)req.getAttribute("utilizador"); }
    @SuppressWarnings("unchecked")
    protected Map<Integer, BigDecimal> carrinho(HttpSession sessao) {
        // Os chamadores sincronizam na sessão para evitar alterações entre separadores.
        Map<Integer, BigDecimal> c = (Map<Integer, BigDecimal>)sessao.getAttribute("carrinho");
        if (c == null) { c = new LinkedHashMap<>(); sessao.setAttribute("carrinho", c); } return c;
    }
    protected void totaisCarrinho(HttpServletRequest req) {
        List<LinhaCarrinho> linhas;
        synchronized (req.getSession()) { linhas = vendas().linhas(carrinho(req.getSession())); }
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LinhaCarrinho l : linhas) subtotal = subtotal.add(l.getSubtotal());
        BigDecimal iva = mz.agrogest.util.Validacao.dinheiro(subtotal.multiply(VendaService.TAXA_IVA));
        req.setAttribute("linhas", linhas); req.setAttribute("subtotal", subtotal); req.setAttribute("iva", iva); req.setAttribute("total", subtotal.add(iva));
    }
    protected void view(HttpServletRequest req, HttpServletResponse resp, String nome) throws ServletException, IOException {
        HttpSession sessao = req.getSession();
        synchronized (sessao) {
            req.setAttribute("cartCount", carrinho(sessao).size());
            for (String tipo : List.of("sucesso", "erro")) {
                Object aviso = sessao.getAttribute(tipo); if (aviso != null) { req.setAttribute(tipo, aviso); sessao.removeAttribute(tipo); }
            }
        }
        req.getRequestDispatcher("/WEB-INF/views/" + nome + ".jsp").forward(req, resp);
    }
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String caminho) throws IOException { resp.sendRedirect(req.getContextPath() + caminho); }
    protected void sucesso(HttpServletRequest req, String texto) { req.getSession().setAttribute("sucesso", texto); }
    protected void erro(HttpServletRequest req, String texto) { req.getSession().setAttribute("erro", texto); }
}
