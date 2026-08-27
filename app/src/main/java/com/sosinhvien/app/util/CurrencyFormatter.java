package com.sosinhvien.app.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {

    private CurrencyFormatter() {
    }

    public static String format(long amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setGroupingUsed(true);
        return nf.format(amount) + " đ";
    }

    public static String formatShort(long amount) {
        if (amount >= 1_000_000) {
            double m = amount / 1_000_000.0;
            if (m == (long) m) {
                return (long) m + "M";
            }
            return String.format(Locale.US, "%.1fM", m);
        }
        if (amount >= 1_000) {
            double k = amount / 1_000.0;
            if (k == (long) k) {
                return (long) k + "k";
            }
            return String.format(Locale.US, "%.0fk", k);
        }
        return String.valueOf(amount);
    }
}
