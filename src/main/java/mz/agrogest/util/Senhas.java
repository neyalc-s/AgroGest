package mz.agrogest.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Guarda salt + hash, nunca a palavra-passe original. */
public final class Senhas {
    private static final int ITERACOES = 210_000;
    private Senhas() {}
    public static String criarHash(String senha) {
        if (senha == null || senha.length() < 4 || senha.length() > 128) throw new RegraNegocio("A palavra-passe deve ter entre 4 e 128 caracteres.");
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        return ITERACOES + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(derivar(senha, salt, ITERACOES));
    }
    public static boolean verificar(String senha, String guardado) {
        try {
            if (senha == null || senha.length() > 128) return false;
            String[] partes = guardado.split(":");
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            byte[] calculado = derivar(senha, Base64.getDecoder().decode(partes[1]), Integer.parseInt(partes[0]));
            return MessageDigest.isEqual(esperado, calculado);
        } catch (RuntimeException e) { return false; }
    }
    private static byte[] derivar(String senha, byte[] salt, int iteracoes) {
        PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (Exception e) { throw new IllegalStateException("Não foi possível proteger a palavra-passe.", e); }
        finally { spec.clearPassword(); }
    }
}
