package mz.agrogest.util;

/** Erro esperado, apresentado em português ao utilizador. */
public class RegraNegocio extends RuntimeException {
    public RegraNegocio(String mensagem) { super(mensagem); }
}
