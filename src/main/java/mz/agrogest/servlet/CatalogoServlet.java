package mz.agrogest.servlet;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mz.agrogest.model.*;
import java.io.IOException;
import java.util.*;
@WebServlet("/catalogo")
public class CatalogoServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Produto> lista = gestao().produtos(); String categoria = req.getParameter("categoria"); String q = req.getParameter("q");
        if (categoria != null && !categoria.isBlank()) lista.removeIf(p -> !p.getCategoria().name().equals(categoria));
        if (q != null && !q.isBlank()) lista.removeIf(p -> !p.getNome().toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)));
        String ordem = req.getParameter("ordem");
        if ("preco".equals(ordem)) lista.sort(Comparator.comparing(Produto::getPreco));
        else if ("stock".equals(ordem)) lista.sort(Comparator.comparing(Produto::getQuantidade).reversed());
        else lista.sort(Comparator.comparing(Produto::getNome, mz.agrogest.util.Formatos.nomes()));
        req.setAttribute("produtos", lista); req.setAttribute("categorias", CategoriaProduto.values()); req.setAttribute("menu", "catalogo"); view(req, resp, "catalogo");
    }
}
