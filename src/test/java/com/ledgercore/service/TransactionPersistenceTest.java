package com.ledgercore.service;

import com.ledgercore.model.BankAccount;
import com.ledgercore.model.TransactionRecord;
import com.ledgercore.repository.BankAccountRepository;
import com.ledgercore.repository.TransactionRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TransactionPersistenceTest {
    @Autowired BankAccountService service;
    @Autowired BankAccountRepository accounts;
    @Autowired TransactionRecordRepository transactions;

    @Test
    void savesOneLedgerEntryForEachSuccessfulOperation() {
        BankAccount alex = service.createAccount("Alex");
        BankAccount sam = service.createAccount("Sam");

        service.deposit(alex.getId(), new BigDecimal("75.00"));
        service.transfer(alex.getId(), sam.getId(), new BigDecimal("20.00"));
        service.withdraw(sam.getId(), new BigDecimal("5.00"));

        assertEquals(3, transactions.count());
        assertEquals(0, new BigDecimal("55.00").compareTo(accounts.findById(alex.getId()).orElseThrow().getBalance()));
        assertEquals(0, new BigDecimal("15.00").compareTo(accounts.findById(sam.getId()).orElseThrow().getBalance()));
        assertEquals(2, service.getTransactions(alex.getId()).size());
        assertEquals(2, service.getTransactions(sam.getId()).size());
        assertTrue(service.getTransactions(sam.getId()).stream()
            .anyMatch(t -> t.getType() == TransactionRecord.Type.TRANSFER
                && t.getFromAccountId().equals(alex.getId())
                && t.getToAccountId().equals(sam.getId())));
    }

    @Test
    void rejectedTransferLeavesBalancesAndLedgerUnchanged() {
        BankAccount alex = service.createAccount("Alex");
        BankAccount sam = service.createAccount("Sam");
        service.deposit(alex.getId(), new BigDecimal("10.00"));
        long before = transactions.count();

        assertThrows(ResponseStatusException.class,
            () -> service.transfer(alex.getId(), sam.getId(), new BigDecimal("20.00")));

        assertEquals(before, transactions.count());
        assertEquals(0, new BigDecimal("10.00").compareTo(accounts.findById(alex.getId()).orElseThrow().getBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(accounts.findById(sam.getId()).orElseThrow().getBalance()));
    }
}
