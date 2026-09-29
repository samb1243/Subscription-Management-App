package com.subscriptionmanager.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** A single recurring subscription, e.g. "Netflix, 15.49, Monthly". */
public record Subscription(String id, String name, BigDecimal price, BillingCycle cycle) {

    public Subscription {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(cycle, "cycle");
        // Collapse runs of whitespace (including newlines) so names stay on one line.
        name = name.strip().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }
    }

    /** Creates a new subscription with a freshly generated id. */
    public static Subscription create(String name, BigDecimal price, BillingCycle cycle) {
        return new Subscription(UUID.randomUUID().toString(), name, price, cycle);
    }

    /** Returns a copy with the same id but updated details. */
    public Subscription withDetails(String name, BigDecimal price, BillingCycle cycle) {
        return new Subscription(id, name, price, cycle);
    }

    /** The equivalent cost of this subscription per month (unrounded). */
    public BigDecimal monthlyCost() {
        return cycle.toMonthly(price);
    }
}
