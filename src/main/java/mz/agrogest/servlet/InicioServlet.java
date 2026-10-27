package mz.agrogest.servlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mz.agrogest.model.Perfil;
import java.io.IOException;
@WebServlet("/inicio")
public class InicioServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException { redirect(req, resp, utilizador(req).getPerfil() == Perfil.ADMIN ? "/admin" : "/catalogo"); }
}
