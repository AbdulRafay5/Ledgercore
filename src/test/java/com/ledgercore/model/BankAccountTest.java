
package com.ledgercore.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankAccountTest {

    @Test
    void shouldCreateBankAccountWithCorrectValues() {

        BankAccount account = new BankAccount(
            1L,
            "Alex",
            new BigDecimal("1000.00")
        );

        assertEquals(1L, account.getId());
        assertEquals("Alex", account.getOwnerName());
        assertEquals(
            new BigDecimal("1000.00"),
            account.getBalance()
        );
    }
}

