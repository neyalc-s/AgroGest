package mz.agrogest.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class Validacao {
    private Validacao() {}
    public static String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.trim().isEmpty()) throw new RegraNegocio("Preencha: " + campo + ".");
        String texto = valor.trim();
        if (texto.length() > maximo) throw new RegraNegocio(campo + ": máximo de " + maximo + " caracteres.");
        return texto;
    }
    public static BigDecimal numero(String valor, String campo, int casas, boolean permiteZero) {
        try {
            BigDecimal n = new BigDecimal(valor.trim().replace(',', '.')).setScale(casas, RoundingMode.UNNECESSARY);
            if (n.signum() < 0 || (!permiteZero && n.signum() == 0) || n.compareTo(new BigDecimal("999999999")) > 0) throw new NumberFormatException();
            return n;
        } catch (Exception e) { throw new RegraNegocio(campo + ": indique um número " + (permiteZero ? "igual ou superior a zero" : "positivo") + " com até " + casas + " casas decimais."); }
    }
    public static int id(String valor) {
        try { int id = Integer.parseInt(valor); if (id < 1) throw new NumberFormatException(); return id; }
        catch (Exception e) { throw new RegraNegocio("Identificador inválido."); }
    }
    public static int versao(String valor) {
        try { int n = Integer.parseInt(valor); if (n < 0) throw new NumberFormatException(); return n; }
        catch (Exception e) { throw new RegraNegocio("Reabra a edição do produto antes de guardar."); }
    }
    public static LocalDate data(String valor, String campo) {
        try { return LocalDate.parse(valor); }
        catch (Exception e) { throw new RegraNegocio("Indique uma data válida para " + campo + "."); }
    }
    public static <T extends Enum<T>> T opcao(Class<T> tipo, String valor) {
        try { return Enum.valueOf(tipo, valor); }
        catch (Exception e) { throw new RegraNegocio("Opção inválida."); }
    }
    public static BigDecimal dinheiro(BigDecimal valor) { return valor.setScale(2, RoundingMode.HALF_UP); }
}
