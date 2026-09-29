package com.subscriptionmanager.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/** Holds the list of subscriptions and calculates totals. */
public class SubscriptionManager {

    private final List<Subscription> subscriptions = new ArrayList<>();

    public SubscriptionManager() {
    }

    public SubscriptionManager(List<Subscription> initial) {
        subscriptions.addAll(initial);
    }

    public List<Subscription> getAll() {
        return Collections.unmodifiableList(subscriptions);
    }

    public void add(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public void update(Subscription updated) {
        subscriptions.set(indexOf(updated.id()), updated);
    }

    public void remove(String id) {
        subscriptions.remove(indexOf(id));
    }

    /** Replaces every subscription with the given list, e.g. to roll back a failed change. */
    public void replaceAll(List<Subscription> replacement) {
        subscriptions.clear();
        subscriptions.addAll(replacement);
    }

    /** Total cost per month across all subscriptions, rounded to cents. */
    public BigDecimal totalMonthly() {
        return subscriptions.stream()
                .map(Subscription::monthlyCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Total cost per year across all subscriptions, rounded to cents. */
    public BigDecimal totalYearly() {
        return subscriptions.stream()
                .map(Subscription::monthlyCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(12))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private int indexOf(String id) {
        for (int i = 0; i < subscriptions.size(); i++) {
            if (subscriptions.get(i).id().equals(id)) {
                return i;
            }
        }
        throw new NoSuchElementException("No subscription with id " + id);
    }
}
