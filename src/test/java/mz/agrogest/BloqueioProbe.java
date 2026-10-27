package mz.agrogest;
import mz.agrogest.util.TravaDados;
import java.nio.file.Path;
/** Processo auxiliar usado pelo teste de exclusão entre duas JVMs. */
public class BloqueioProbe {
    public static void main(String[] args) {
        try (TravaDados trava=new TravaDados(Path.of(args[0]))) {System.out.println("LIVRE");}
        catch (Exception e) {System.out.println("OCUPADA");System.exit(2);}
    }
}
