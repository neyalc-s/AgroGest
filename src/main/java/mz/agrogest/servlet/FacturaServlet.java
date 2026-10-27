package mz.agrogest.servlet;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mz.agrogest.model.Venda;
import mz.agrogest.util.*;
import java.io.IOException;
@WebServlet("/factura")
public class FacturaServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Venda v = vendas().factura(Validacao.id(req.getParameter("id")), utilizador(req));
            if (v == null) { resp.sendError(404); return; }
            req.setAttribute("venda", v); req.setAttribute("menu", "compras"); view(req, resp, "factura");
        } catch (SecurityException e) { resp.sendError(403); }
        catch (RegraNegocio e) { resp.sendError(400); }
    }
}
