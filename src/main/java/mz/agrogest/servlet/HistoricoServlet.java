package mz.agrogest.servlet;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/minhas-compras")
public class HistoricoServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("vendas", vendas().historico(utilizador(req).getId())); req.setAttribute("menu", "compras"); view(req, resp, "historico");
    }
}
