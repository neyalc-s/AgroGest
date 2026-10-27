package mz.agrogest.listener;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import mz.agrogest.repo.*;
import mz.agrogest.service.*;
import java.nio.file.Path;
import mz.agrogest.util.TravaDados;

@WebListener
public class AplicacaoListener implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent evento) {
        ServletContext ctx = evento.getServletContext();
        String pasta = System.getProperty("agrogest.dataDir");
        if (pasta == null || pasta.isBlank()) pasta = System.getenv("AGROGEST_DATA_DIR");
        if (pasta == null || pasta.isBlank()) pasta = Path.of(System.getProperty("catalina.base", "."), "data", "agrogest").toString();
        try {
            ctx.setAttribute("travaDados", new TravaDados(Path.of(pasta)));
            Estado estado = PersistenciaService.carregar(Path.of(pasta));
            DataStore store = estado == null ? new DataStore() : new DataStore(estado);
            if (estado == null) DadosExemplo.criar(store);
            new ContaService(store).garantirAdministrador();
            ctx.setAttribute("store", store); ctx.setAttribute("persistencia", new PersistenciaService(store, Path.of(pasta)));
            ctx.log("AgroGest iniciado. Estado " + (estado == null ? "de demonstração" : "restaurado") + ". Pasta de dados: " + pasta);
        } catch (Exception e) {
            libertarPasta(ctx);
            throw new IllegalStateException("Falha no arranque do AgroGest. Os dados existentes não foram substituídos.", e);
        }
    }
    @Override public void contextDestroyed(ServletContextEvent evento) {
        ServletContext ctx = evento.getServletContext(); PersistenciaService p = (PersistenciaService) ctx.getAttribute("persistencia");
        try { if (p != null) { p.gravar(); ctx.log("AgroGest: estado gravado ao encerrar."); } }
        catch (Exception e) { ctx.log("ERRO: não foi possível gravar o estado ao encerrar.", e); }
        finally { libertarPasta(ctx); }
    }
    private void libertarPasta(ServletContext ctx) {
        TravaDados trava = (TravaDados)ctx.getAttribute("travaDados");
        if (trava != null) {
            try { trava.close(); }
            catch (Exception e) { ctx.log("Não foi possível libertar a pasta de dados.", e); }
            finally { ctx.removeAttribute("travaDados"); }
        }
    }
}
