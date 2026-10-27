package mz.agrogest.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.Properties;

/** Impede duas instâncias de gravar sobre a mesma pasta de dados. */
public class TravaDados implements AutoCloseable {
    private final FileChannel canal;
    private final FileLock trava;
    private final Properties propriedades = System.getProperties();
    private final String chave;
    private boolean fechada;

    public TravaDados(Path pasta) throws IOException {
        Files.createDirectories(pasta);
        Path real = pasta.toRealPath();
        chave = "mz.agrogest.pasta-em-uso:" + real;
        // O registo na JVM é comum mesmo a dois contextos/classloaders do Tomcat.
        // Evita abrir/fechar outro canal sobre um ficheiro já bloqueado pela própria JVM.
        synchronized (propriedades) {
            if (propriedades.containsKey(chave)) throw ocupada(null);
            FileChannel aberto = FileChannel.open(real.resolve("agrogest.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            try {
                FileLock adquirida = aberto.tryLock();
                if (adquirida == null) throw ocupada(null);
                canal = aberto; trava = adquirida;
                propriedades.setProperty(chave, "em uso");
            } catch (IOException | RuntimeException e) {
                aberto.close();
                throw ocupada(e);
            }
        }
    }
    private IOException ocupada(Exception causa) {
        return new IOException("A pasta de dados já está em uso ou não pode ser bloqueada. Pare a outra instância do AgroGest e tente novamente.", causa);
    }
    @Override public void close() throws IOException {
        synchronized (propriedades) {
            if (fechada) return;
            fechada = true;
            try { if (trava.isValid()) trava.release(); }
            finally { try { canal.close(); } finally { propriedades.remove(chave); } }
        }
    }
}
