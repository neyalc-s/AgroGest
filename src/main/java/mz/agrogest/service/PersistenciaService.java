package mz.agrogest.service;

import mz.agrogest.repo.*;
import java.io.*;
import java.nio.file.*;

public class PersistenciaService {
    // A escrita e a leitura devem aceitar exactamente a mesma política.
    private static final long MAX_BYTES = 64L * 1024 * 1024;
    private static final long MAX_REFERENCIAS = 2_000_000;
    private static final long MAX_ARRAY = 1_000_000;
    private final Path ficheiro;
    private final Path backup;
    private final DataStore store;
    public PersistenciaService(DataStore store, Path pasta) {
        this.store = store; this.ficheiro = pasta.resolve("agrogest.ser"); this.backup = pasta.resolve("agrogest.ser.bak");
    }
    public Path getFicheiro() { return ficheiro; }
    public boolean existe() { return Files.exists(ficheiro); }

    public static Estado carregar(Path pasta) throws IOException {
        Path principal = pasta.resolve("agrogest.ser"); Path copia = pasta.resolve("agrogest.ser.bak");
        if (!Files.exists(principal) && !Files.exists(copia)) return null;
        try { return ler(principal); }
        catch (IOException erro) {
            if (Files.exists(copia)) {
                try {
                    Estado recuperado = ler(copia);
                    java.util.logging.Logger.getLogger(PersistenciaService.class.getName()).warning("AgroGest: estado recuperado do backup anterior, porque o ficheiro principal não pôde ser lido.");
                    return recuperado;
                }
                catch (IOException secundario) { erro.addSuppressed(secundario); }
            }
            throw new IOException("Não foi possível ler o estado. Os ficheiros foram preservados; consulte os logs e restaure uma cópia válida.", erro);
        }
    }
    private static Estado ler(Path origem) throws IOException {
        if (Files.size(origem) > MAX_BYTES) throw new IOException("O estado excede o limite suportado de 64 MiB.");
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(origem))) {
            // Só se lê o ficheiro local do sistema e os tipos esperados.
            in.setObjectInputFilter(info -> {
                if (info.depth() > 40 || info.references() > MAX_REFERENCIAS || info.arrayLength() > MAX_ARRAY || info.streamBytes() > MAX_BYTES) return ObjectInputFilter.Status.REJECTED;
                Class<?> c = info.serialClass(); if (c == null) return ObjectInputFilter.Status.UNDECIDED;
                while (c.isArray()) c = c.getComponentType();
                if (c.isPrimitive() || c.getName().startsWith("mz.agrogest.") || c.getName().startsWith("java.lang.") || c.getName().startsWith("java.util.") || c.getName().startsWith("java.math.") || c.getName().startsWith("java.time.")) return ObjectInputFilter.Status.ALLOWED;
                return ObjectInputFilter.Status.REJECTED;
            });
            Object objecto = in.readObject();
            if (!(objecto instanceof Estado)) throw new IOException("Formato de estado inválido.");
            return (Estado) objecto;
        } catch (ClassNotFoundException | RuntimeException e) { throw new IOException("Estado incompatível ou danificado.", e); }
    }
    public void gravar() throws IOException {
        synchronized (store) {
            Files.createDirectories(ficheiro.getParent());
            Path temporario = Files.createTempFile(ficheiro.getParent(), "agrogest-", ".tmp");
            Path tempBackup = null;
            try {
                try (FileOutputStream file = new FileOutputStream(temporario.toFile()); ObjectOutputStream out = new ObjectOutputStream(file)) {
                    out.writeObject(store.estado()); out.flush(); file.getFD().sync();
                }
                // Uma gravação só é publicada se o leitor do arranque a aceitar.
                // A validação acontece antes de alterar o principal OU o backup.
                ler(temporario);
                if (Files.exists(ficheiro)) {
                    // Uma origem danificada nunca substitui um backup válido.
                    boolean principalValido;
                    try {
                        ler(ficheiro);
                        principalValido = true;
                    } catch (IOException e) { principalValido = false; }
                    if (principalValido) {
                        tempBackup = Files.createTempFile(ficheiro.getParent(), "agrogest-bak-", ".tmp");
                        Files.copy(ficheiro, tempBackup, StandardCopyOption.REPLACE_EXISTING);
                        Files.move(tempBackup, backup, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                // Sem fallback não atómico: se não houver suporte, mantém-se a versão anterior.
                Files.move(temporario, ficheiro, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporario); if (tempBackup != null) Files.deleteIfExists(tempBackup);
            }
        }
    }
    /** Elimina apenas os ficheiros; os dados em memória continuam disponíveis. */
    public void eliminar() throws IOException {
        synchronized (store) { Files.deleteIfExists(ficheiro); Files.deleteIfExists(backup); }
    }
}
