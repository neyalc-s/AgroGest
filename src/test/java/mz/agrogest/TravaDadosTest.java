package mz.agrogest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import mz.agrogest.util.TravaDados;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class TravaDadosTest {
    @Test void naoPermiteDuasInstanciasNaMesmaPasta(@TempDir Path pasta) throws Exception {
        try (TravaDados primeira=new TravaDados(pasta)) {
            assertThrows(IOException.class, () -> new TravaDados(pasta));
        }
        // O ficheiro fica na pasta, mas o bloqueio foi libertado.
        try (TravaDados seguinte=new TravaDados(pasta)) { assertNotNull(seguinte); }
    }
    @Test void fecharDuasVezesNaoLibertaInstanciaSeguinte(@TempDir Path pasta) throws Exception {
        TravaDados antiga=new TravaDados(pasta);antiga.close();
        try (TravaDados nova=new TravaDados(pasta)) {
            antiga.close();assertThrows(IOException.class, () -> new TravaDados(pasta));
        }
    }
    @Test void pastasDiferentesPodemSerUsadas(@TempDir Path pasta) throws Exception {
        try (TravaDados a=new TravaDados(pasta.resolve("a"));TravaDados b=new TravaDados(pasta.resolve("b"))) {
            assertNotNull(a);assertNotNull(b);
        }
    }
    @Test void bloqueiaOutraJvmELibertaDepoisDeEncerrar(@TempDir Path pasta) throws Exception {
        try (TravaDados primeira=new TravaDados(pasta)) {assertEquals(2,executarOutraJvm(pasta));}
        assertEquals(0,executarOutraJvm(pasta));
    }
    private int executarOutraJvm(Path pasta) throws Exception {
        String executavel=Path.of(System.getProperty("java.home"),"bin","java").toString();
        String classes=Path.of(TravaDados.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        String testes=Path.of(BloqueioProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        Process p=new ProcessBuilder(executavel,"-cp",classes+java.io.File.pathSeparator+testes,BloqueioProbe.class.getName(),pasta.toString()).redirectErrorStream(true).start();
        if (!p.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)) {p.destroyForcibly();fail("A JVM auxiliar não terminou.");}
        return p.exitValue();
    }
}
