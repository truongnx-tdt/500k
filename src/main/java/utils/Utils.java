package utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class Utils {
    public static String formatPriceOnly(BigDecimal amount) {
        if (amount == null) return "0";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(amount);
    }
}
