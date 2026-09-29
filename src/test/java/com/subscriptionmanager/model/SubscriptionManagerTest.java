package com.subscriptionmanager.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubscriptionManagerTest {

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    @Test
    void emptyManagerTotalsZero() {
        SubscriptionManager manager = new SubscriptionManager();
        assertEquals(money("0.00"), manager.totalMonthly());
        assertEquals(money("0.00"), manager.totalYearly());
    }

    @Test
    void totalsMonthlySubscriptions() {
        SubscriptionManager manager = new SubscriptionManager();
        manager.add(Subscription.create("Netflix", money("15.49"), BillingCycle.MONTHLY));
        manager.add(Subscription.create("Spotify", money("11.99"), BillingCycle.MONTHLY));
        assertEquals(money("27.48"), manager.totalMonthly());
        assertEquals(money("329.76"), manager.totalYearly());
    }

    @Test
    void convertsOtherCyclesToMonthly() {
        assertEquals(money("10.00"), BillingCycle.YEARLY.toMonthly(money("120")).setScale(2));
        assertEquals(money("10.00"), BillingCycle.QUARTERLY.toMonthly(money("30")).setScale(2));
        // 52 weeks / 12 months
        assertEquals(0, money("43.33").compareTo(
                BillingCycle.WEEKLY.toMonthly(money("10")).setScale(2, java.math.RoundingMode.HALF_UP)));
    }

    @Test
    void mixedCyclesRoundOnlyTheTotal() {
        SubscriptionManager manager = new SubscriptionManager();
        manager.add(Subscription.create("A", money("100"), BillingCycle.YEARLY));   // 8.3333...
        manager.add(Subscription.create("B", money("100"), BillingCycle.YEARLY));   // 8.3333...
        manager.add(Subscription.create("C", money("100"), BillingCycle.YEARLY));   // 8.3333...
        assertEquals(money("25.00"), manager.totalMonthly());
        assertEquals(money("300.00"), manager.totalYearly());
    }

    @Test
    void updateAndRemove() {
        SubscriptionManager manager = new SubscriptionManager();
        Subscription s = Subscription.create("Gym", money("40"), BillingCycle.MONTHLY);
        manager.add(s);
        manager.update(s.withDetails("Gym", money("45"), BillingCycle.MONTHLY));
        assertEquals(money("45.00"), manager.totalMonthly());
        manager.remove(s.id());
        assertEquals(0, manager.getAll().size());
    }

    @Test
    void rejectsInvalidSubscriptions() {
        assertThrows(IllegalArgumentException.class,
                () -> Subscription.create("  ", money("1"), BillingCycle.MONTHLY));
        assertThrows(IllegalArgumentException.class,
                () -> Subscription.create("X", money("-1"), BillingCycle.MONTHLY));
    }

    @Test
    void normalisesWhitespaceInNames() {
        assertEquals("Disney Plus",
                Subscription.create("  Disney \n Plus ", money("1"), BillingCycle.MONTHLY).name());
    }
}
