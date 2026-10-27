package mz.agrogest.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Comparator;
import java.text.Collator;

public final class Formatos {
    private Formatos() {}
    /** Ordem alfabética portuguesa, incluindo nomes com acentos. */
    public static Comparator<String> nomes() {
        Collator c = Collator.getInstance(Locale.forLanguageTag("pt-MZ"));
        c.setStrength(Collator.PRIMARY);
        return c::compare;
    }
    public static String moeda(BigDecimal n) { NumberFormat f = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-MZ")); f.setMinimumFractionDigits(2); f.setMaximumFractionDigits(2); return f.format(n == null ? BigDecimal.ZERO : n); }
    public static String quantidade(BigDecimal n) { NumberFormat f = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-MZ")); f.setMaximumFractionDigits(3); return f.format(n == null ? BigDecimal.ZERO : n); }
    public static String data(Object n) {
        if (n == null) return "Sem validade";
        if (n instanceof LocalDateTime) return ((LocalDateTime)n).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        if (n instanceof LocalDate) return ((LocalDate)n).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return n.toString();
    }
}
