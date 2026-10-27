package mz.agrogest.servlet;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mz.agrogest.model.*;
import mz.agrogest.util.RegraNegocio;
import java.io.IOException;
@WebServlet("/compra")
public class CompraServlet extends BaseServlet {
    private static final String PROPOSTA = "propostaCompra";

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (utilizador(req).getPerfil() != Perfil.CLIENTE) { resp.sendError(403); return; }
        try {
            synchronized (req.getSession()) {
                if (carrinho(req.getSession()).isEmpty()) { redirect(req, resp, "/carrinho"); return; }
                PropostaCompra proposta = vendas().prepararCompra(utilizador(req).getId(), carrinho(req.getSession()));
                req.getSession().setAttribute(PROPOSTA, proposta);
                req.setAttribute("linhas", proposta.getItens());
                req.setAttribute("subtotal", proposta.getSubtotal());
                req.setAttribute("iva", proposta.getIva());
                req.setAttribute("total", proposta.getTotal());
                req.setAttribute("confirmacaoCompra", proposta.getToken());
            }
            req.setAttribute("menu", "carrinho");
            view(req, resp, "compra");
        } catch (RegraNegocio e) {
            erro(req, e.getMessage());
            redirect(req, resp, "/carrinho");
        }
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (utilizador(req).getPerfil() != Perfil.CLIENTE) { resp.sendError(403); return; }
        try {
            Venda venda;
            synchronized (req.getSession()) {
                if (carrinho(req.getSession()).isEmpty()) throw new RegraNegocio("O carrinho está vazio.");
                PropostaCompra proposta = (PropostaCompra)req.getSession().getAttribute(PROPOSTA);
                if (proposta == null || !proposta.getToken().equals(req.getParameter("confirmacaoCompra"))) {
                    throw new RegraNegocio("Esta confirmação já não está disponível. Confira o resumo e confirme novamente. Nenhuma compra foi registada.");
                }
                venda = vendas().confirmarCompra(utilizador(req).getId(), carrinho(req.getSession()), proposta);
                carrinho(req.getSession()).clear();
                req.getSession().removeAttribute(PROPOSTA);
            }
            sucesso(req, "Compra confirmada. A sua factura está pronta."); redirect(req, resp, "/factura?id=" + venda.getId());
        } catch (RegraNegocio e) { erro(req, e.getMessage()); redirect(req, resp, "/carrinho"); }
    }
}
