package mz.agrogest.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import mz.agrogest.model.*;
import mz.agrogest.repo.DataStore;
import mz.agrogest.service.ContaService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@WebFilter("/*")
public class AuthFilter implements Filter {
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request; HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8"); resp.setCharacterEncoding("UTF-8");
        resp.setHeader("X-Content-Type-Options", "nosniff"); resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "same-origin");
        // O caminho já descodificado pelo Tomcat evita contornar o perfil com %xx.
        String rota = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        if (rota.startsWith("/assets/")) { chain.doFilter(req, resp); return; }
        resp.setHeader("Cache-Control", "no-store");
        HttpSession sessao = req.getSession();
        if (sessao.getAttribute("csrf") == null) sessao.setAttribute("csrf", UUID.randomUUID().toString());
        req.setAttribute("csrf", sessao.getAttribute("csrf"));
        req.setAttribute("cp", req.getContextPath());
        Integer id = (Integer) sessao.getAttribute("utilizadorId");
        DataStore store = (DataStore) req.getServletContext().getAttribute("store");
        Utilizador u = id == null ? null : new ContaService(store).procurar(id);
        if (u != null && !u.isAtivo()) u = null;
        req.setAttribute("utilizador", u);
        boolean admin = rota.equals("/admin") || rota.startsWith("/admin/");
        if (admin && (u == null || u.getPerfil() != Perfil.ADMIN)) { resp.sendError(403); return; }
        boolean publica = rota.equals("/login") || rota.equals("/registo");
        if (!publica && u == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }
        if ("POST".equals(req.getMethod())) {
            String esperado = (String)sessao.getAttribute("csrf"); String recebido = req.getParameter("csrf");
            if (recebido == null || !MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8), recebido.getBytes(StandardCharsets.UTF_8))) { resp.sendError(403); return; }
        }
        chain.doFilter(req, resp);
    }
}
