package com.subscriptionmanager.storage;

import com.subscriptionmanager.model.BillingCycle;
import com.subscriptionmanager.model.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionStoreTest {

    @TempDir
    Path dir;

    @Test
    void missingFileLoadsEmpty() throws IOException {
        assertTrue(new SubscriptionStore(dir.resolve("none.csv")).load().isEmpty());
    }

    @Test
    void roundTripsSubscriptions() throws IOException {
        SubscriptionStore store = new SubscriptionStore(dir.resolve("nested/subs.csv"));
        List<Subscription> original = List.of(
                Subscription.create("Netflix", new BigDecimal("15.49"), BillingCycle.MONTHLY, "Streaming"),
                Subscription.create("Tricky, \"quoted\" name", new BigDecimal("99.00"), BillingCycle.YEARLY,
                        "Work, \"misc\""),
                Subscription.create("Café ☕", new BigDecimal("3.50"), BillingCycle.WEEKLY, ""));
        store.save(original);
        assertEquals(original, store.load());
    }

    @Test
    void saveOverwritesPreviousContents() throws IOException {
        SubscriptionStore store = new SubscriptionStore(dir.resolve("subs.csv"));
        store.save(List.of(Subscription.create("Old", BigDecimal.ONE, BillingCycle.MONTHLY, "")));
        store.save(List.of());
        assertTrue(store.load().isEmpty());
    }

    @Test
    void malformedFileThrows() throws IOException {
        Path file = dir.resolve("bad.csv");
        Files.writeString(file, "id,name,price,cycle\nabc,Netflix,not-a-number,MONTHLY\n");
        assertThrows(IOException.class, () -> new SubscriptionStore(file).load());
    }

    @Test
    void parsesQuotedFields() {
        assertEquals(List.of("a", "b,c", "d\"e", ""), SubscriptionStore.parseLine("a,\"b,c\",\"d\"\"e\","));
    }

    @Test
    void loadsFilesSavedBeforeCategoriesExisted() throws IOException {
        Path file = dir.resolve("old.csv");
        Files.writeString(file, "id,name,price,cycle\nabc,Netflix,15.49,MONTHLY\n");
        SubscriptionStore store = new SubscriptionStore(file);

        List<Subscription> loaded = store.load();
        assertEquals(List.of(new Subscription("abc", "Netflix", new BigDecimal("15.49"), BillingCycle.MONTHLY, "")),
                loaded);

        // Saving upgrades the file to the new format, which then loads the same way.
        store.save(loaded);
        assertEquals("id,name,price,cycle,category", Files.readAllLines(file).get(0));
        assertEquals(loaded, store.load());
    }

    @Test
    void loadsFilesWithWindowsLineEndings() throws IOException {
        Path file = dir.resolve("windows.csv");
        Files.writeString(file, "id,name,price,cycle,category\r\nabc,Netflix,15.49,MONTHLY,Streaming\r\n");
        assertEquals(List.of(new Subscription("abc", "Netflix", new BigDecimal("15.49"), BillingCycle.MONTHLY, "Streaming")),
                new SubscriptionStore(file).load());
    }
}
