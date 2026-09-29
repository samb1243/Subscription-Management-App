package com.subscriptionmanager.model;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * How often a subscription is charged. Each cycle knows how to convert its
 * price into an equivalent monthly cost so subscriptions can be compared and
 * totalled on the same basis.
 */
public enum BillingCycle {
    WEEKLY("Weekly", new BigDecimal("52"), new BigDecimal("12")),
    MONTHLY("Monthly", BigDecimal.ONE, BigDecimal.ONE),
    QUARTERLY("Quarterly", BigDecimal.ONE, new BigDecimal("3")),
    YEARLY("Yearly", BigDecimal.ONE, new BigDecimal("12"));

    private final String displayName;
    private final BigDecimal numerator;
    private final BigDecimal denominator;

    BillingCycle(String displayName, BigDecimal numerator, BigDecimal denominator) {
        this.displayName = displayName;
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /** Converts a price charged once per this cycle into a monthly amount (unrounded). */
    public BigDecimal toMonthly(BigDecimal price) {
        return price.multiply(numerator).divide(denominator, MathContext.DECIMAL64);
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
