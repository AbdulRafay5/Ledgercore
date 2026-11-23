package com.ledgercore.service;

import com.ledgercore.model.BankAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BankAccountServiceTest {
    @Autowired
    private BankAccountService service;
    @Test
    void createsAccountsWithUniqueIdsAndZeroBalances() {
        BankAccount alex = service.createAccount("Alex");
        BankAccount sarah = service.createAccount("Sarah");
        assertNotEquals(alex.getId(), sarah.getId());
        assertNotNull(sarah.getId());
        assertEquals(BigDecimal.ZERO, alex.getBalance());
        assertEquals("Sarah", service.getAccountById(sarah.getId()).getOwnerName());
        assertNull(service.getAccountById(999L));
    }

    @Test
    void depositsAndWithdrawsMoney() {
        BankAccount account = service.createAccount("Alex");
        service.deposit(account.getId(), new BigDecimal("100.00"));
        BankAccount updated = service.withdraw(account.getId(), new BigDecimal("35.50"));
        assertEquals(0, new BigDecimal("64.50").compareTo(updated.getBalance()));
    }

    @Test
    void transfersMoneyBetweenAccounts() {
        BankAccount alex = service.createAccount("Alex");
        BankAccount sarah = service.createAccount("Sarah");
        service.deposit(alex.getId(), new BigDecimal("100.00"));
        service.transfer(alex.getId(), sarah.getId(), new BigDecimal("40.00"));
        assertEquals(0, new BigDecimal("60.00").compareTo(service.getAccountById(alex.getId()).getBalance()));
        assertEquals(0, new BigDecimal("40.00").compareTo(service.getAccountById(sarah.getId()).getBalance()));
    }

    @Test
    void rejectsInvalidOperationsWithoutChangingBalances() {
        BankAccount alex = service.createAccount("Alex");
        BankAccount sarah = service.createAccount("Sarah");
        assertThrows(ResponseStatusException.class, () -> service.deposit(alex.getId(), new BigDecimal("-1")));
        assertThrows(ResponseStatusException.class, () -> service.deposit(alex.getId(), new BigDecimal("0.001")));
        assertThrows(ResponseStatusException.class, () -> service.withdraw(alex.getId(), BigDecimal.ONE));
        assertThrows(ResponseStatusException.class, () -> service.transfer(alex.getId(), sarah.getId(), BigDecimal.ONE));
        assertThrows(ResponseStatusException.class, () -> service.transfer(alex.getId(), alex.getId(), BigDecimal.ONE));
        assertThrows(ResponseStatusException.class, () -> service.createAccount(" "));
        assertEquals(BigDecimal.ZERO, service.getAccountById(alex.getId()).getBalance());
        assertEquals(BigDecimal.ZERO, service.getAccountById(sarah.getId()).getBalance());
    }
}
