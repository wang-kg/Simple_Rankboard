package com.wangdai.rankboard;

final class NumberFormatter {
    private NumberFormatter() {
    }

    static String format(long value, String unit, boolean compact) {
        String number = compact ? compact(value) : Long.toString(value);
        return unit == null || unit.isBlank() ? number : number + " " + unit;
    }

    private static String compact(long value) {
        if (value >= 1_000_000_000L) {
            return trim(value / 1_000_000_000.0D) + "b";
        }
        if (value >= 1_000_000L) {
            return trim(value / 1_000_000.0D) + "m";
        }
        if (value >= 10_000L) {
            return trim(value / 1_000.0D) + "k";
        }
        return Long.toString(value);
    }

    private static String trim(double value) {
        String formatted = String.format(java.util.Locale.ROOT, "%.1f", value);
        return formatted.endsWith(".0") ? formatted.substring(0, formatted.length() - 2) : formatted;
    }
}
