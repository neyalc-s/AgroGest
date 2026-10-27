package mz.agrogest.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mz.agrogest.model.*;
import mz.agrogest.service.ContaService;
import mz.agrogest.util.RegraNegocio;
import java.io.IOException;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@WebServlet({"/login", "/registo", "/logout"})
public class AuthServlet extends BaseServlet {
    private static final String ACESSOS_DEMO = "acessosDemonstracao";

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getServletPath().equals("/logout")) { resp.sendError(405); return; }
        if (utilizador(req) != null) { redirect(req, resp, "/inicio"); return; }
        boolean registo = req.getServletPath().equals("/registo");
        if (!registo) prepararDemonstracao(req);
        view(req, resp, registo ? "registo" : "login");
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getServletPath().equals("/logout")) { req.getSession().invalidate(); redirect(req, resp, "/login"); return; }
        ContaService contas = new ContaService(store()); boolean registo = req.getServletPath().equals("/registo");
        try {
            Utilizador u;
            if (registo) {
                if (!java.util.Objects.equals(req.getParameter("senha"), req.getParameter("confirmacao"))) throw new RegraNegocio("As palavras-passe não coincidem.");
                u = contas.registar(req.getParameter("nome"), req.getParameter("email"), req.getParameter("senha"), req.getParameter("telefone"), req.getParameter("endereco"));
            } else u = autenticar(req, contas);
            req.changeSessionId(); HttpSession sessao = req.getSession();
            sessao.setAttribute("utilizadorId", u.getId()); sessao.removeAttribute("carrinho"); sessao.removeAttribute("propostaCompra"); sessao.removeAttribute(ACESSOS_DEMO); sessao.setAttribute("csrf", UUID.randomUUID().toString());
            redirect(req, resp, u.getPerfil() == Perfil.ADMIN ? "/admin" : "/catalogo");
        } catch (RegraNegocio e) {
            req.setAttribute("erro", e.getMessage());
            if (!registo) prepararDemonstracao(req);
            view(req, resp, registo ? "registo" : "login");
        }
    }

    private boolean demonstracaoAtiva() {
        String valor = System.getProperty("agrogest.demoLogin");
        if (valor == null) valor = System.getenv("AGROGEST_DEMO_LOGIN");
        return Boolean.parseBoolean(valor);
    }

    private void prepararDemonstracao(HttpServletRequest req) {
        synchronized (req.getSession()) {
            if (!demonstracaoAtiva()) {
                req.getSession().removeAttribute(ACESSOS_DEMO);
                return;
            }
            List<AcessoDemonstracao> acessos = new ContaService(store()).acessosDemonstracao();
            Map<String, AcessoDemonstracao> porEmail = new LinkedHashMap<>();
            for (AcessoDemonstracao a : acessos) porEmail.put(a.getEmail().toLowerCase(Locale.ROOT), a);
            req.getSession().setAttribute(ACESSOS_DEMO, porEmail);
            req.setAttribute("acessosDemo", acessos);
            req.setAttribute("demoLogin", true);
        }
    }

    @SuppressWarnings("unchecked")
    private Utilizador autenticar(HttpServletRequest req, ContaService contas) {
        String email = req.getParameter("email");
        String senha = req.getParameter("senha");
        if (demonstracaoAtiva() && email != null && senha != null && senha.length() <= 128) {
            synchronized (req.getSession()) {
                Map<String, AcessoDemonstracao> acessos = (Map<String, AcessoDemonstracao>)req.getSession().getAttribute(ACESSOS_DEMO);
                AcessoDemonstracao a = acessos == null ? null : acessos.get(email.trim().toLowerCase(Locale.ROOT));
                if (a != null && MessageDigest.isEqual(a.getCredencial().getBytes(StandardCharsets.UTF_8), senha.getBytes(StandardCharsets.UTF_8))) {
                    return contas.autenticarDemonstracao(a, email, senha);
                }
            }
        }
        return contas.autenticar(email, senha);
    }
}
