package com.subscriptionmanager.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/** Formats and parses money amounts for display and input. */
public final class MoneyFormat {

    private static final NumberFormat FORMAT = createFormat();

    private MoneyFormat() {
    }

    private static NumberFormat createFormat() {
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        if (!locale.getCountry().isEmpty()) {
            NumberFormat currency = NumberFormat.getCurrencyInstance(locale);
            currency.setMinimumFractionDigits(2);
            currency.setMaximumFractionDigits(2);
            return currency;
        }
        // No country in the locale means no known currency symbol; show plain amounts.
        return new DecimalFormat("#,##0.00");
    }

    public static String format(BigDecimal amount) {
        synchronized (FORMAT) {
            return FORMAT.format(amount.setScale(2, RoundingMode.HALF_UP));
        }
    }

    /**
     * Parses user input such as {@code 9.99}, {@code $9.99}, {@code £1,299.00} or {@code 9,99}.
     *
     * @throws NumberFormatException if the input is not a valid non-negative amount
     */
    public static BigDecimal parse(String input) {
        String cleaned = input.strip().replaceAll("[^0-9.,]", "");
        if (cleaned.isEmpty() || !cleaned.equals(input.strip().replaceAll("[\\s\\p{Sc}]", ""))) {
            throw new NumberFormatException("Enter a price like 9.99");
        }
        int lastComma = cleaned.lastIndexOf(',');
        int lastDot = cleaned.lastIndexOf('.');
        if (lastComma > lastDot && cleaned.length() - lastComma - 1 <= 2) {
            // Comma used as the decimal separator, e.g. "9,99" or "1.299,00".
            cleaned = cleaned.replace(".", "").replace(',', '.');
        } else {
            cleaned = cleaned.replace(",", "");
        }
        BigDecimal value = new BigDecimal(cleaned);
        if (value.scale() > 2) {
            throw new NumberFormatException("Price can have at most 2 decimal places");
        }
        return value;
    }
}
