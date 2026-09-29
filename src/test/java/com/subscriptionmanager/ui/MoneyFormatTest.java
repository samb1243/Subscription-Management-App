package com.subscriptionmanager.ui;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyFormatTest {

    @Test
    void parsesCommonInputs() {
        assertEquals(new BigDecimal("9.99"), MoneyFormat.parse("9.99"));
        assertEquals(new BigDecimal("9.99"), MoneyFormat.parse(" $9.99 "));
        assertEquals(new BigDecimal("9.99"), MoneyFormat.parse("£9.99"));
        assertEquals(new BigDecimal("9.99"), MoneyFormat.parse("9,99"));
        assertEquals(new BigDecimal("1299.00"), MoneyFormat.parse("1,299.00"));
        assertEquals(new BigDecimal("1299"), MoneyFormat.parse("1,299"));
        assertEquals(new BigDecimal("1299.00"), MoneyFormat.parse("1.299,00"));
        assertEquals(new BigDecimal("10"), MoneyFormat.parse("10"));
    }

    @Test
    void rejectsInvalidInputs() {
        for (String bad : new String[]{"", "   ", "abc", "-5", "9.999", "1.2.3", "12abc", "."}) {
            assertThrows(NumberFormatException.class, () -> MoneyFormat.parse(bad), bad);
        }
    }
}
