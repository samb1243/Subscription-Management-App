package com.subscriptionmanager.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * A single recurring subscription, e.g. "Netflix, 15.49, Monthly, Streaming".
 * The category is optional; an empty string means the subscription has none.
 */
public record Subscription(String id, String name, BigDecimal price, BillingCycle cycle, String category) {

    public Subscription {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(cycle, "cycle");
        // Collapse runs of whitespace (including newlines) so text stays on one line.
        name = name.strip().replaceAll("\\s+", " ");
        category = category == null ? "" : category.strip().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }
    }

    /** Creates a new subscription with a freshly generated id. */
    public static Subscription create(String name, BigDecimal price, BillingCycle cycle, String category) {
        return new Subscription(UUID.randomUUID().toString(), name, price, cycle, category);
    }

    /** Returns a copy with the same id but updated details. */
    public Subscription withDetails(String name, BigDecimal price, BillingCycle cycle, String category) {
        return new Subscription(id, name, price, cycle, category);
    }

    /** The equivalent cost of this subscription per month (unrounded). */
    public BigDecimal monthlyCost() {
        return cycle.toMonthly(price);
    }
}
