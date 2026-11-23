package com.ledgercore.service;

import com.ledgercore.model.BankAccount;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BankAccountService {
    private final Map<Long, BankAccount> accounts = new LinkedHashMap<>();
    private long nextId = 1;

    public synchronized BankAccount createAccount(String ownerName) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner name is required");
        }
        BankAccount account = new BankAccount(nextId++, ownerName.trim(), BigDecimal.ZERO);
        accounts.put(account.getId(), account);
        return account;
    }

    public synchronized BankAccount getAccountById(Long id) {
        return accounts.get(id);
    }

    public synchronized BankAccount deposit(Long id, BigDecimal amount) {
        validateAmount(amount);
        BankAccount account = requireAccount(id);
        BankAccount updated = new BankAccount(id, account.getOwnerName(), account.getBalance().add(amount));
        accounts.put(id, updated);
        return updated;
    }

    public synchronized BankAccount withdraw(Long id, BigDecimal amount) {
        validateAmount(amount);
        BankAccount account = requireAccount(id);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");
        }
        BankAccount updated = new BankAccount(id, account.getOwnerName(), account.getBalance().subtract(amount));
        accounts.put(id, updated);
        return updated;
    }

    public synchronized void transfer(Long fromId, Long toId, BigDecimal amount) {
        validateAmount(amount);
        if (fromId == null || toId == null || fromId.equals(toId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Accounts must be different");
        }
        BankAccount source = requireAccount(fromId);
        BankAccount destination = requireAccount(toId);
        if (source.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");
        }
        accounts.put(fromId, new BankAccount(fromId, source.getOwnerName(), source.getBalance().subtract(amount)));
        accounts.put(toId, new BankAccount(toId, destination.getOwnerName(), destination.getBalance().add(amount)));
    }

    private BankAccount requireAccount(Long id) {
        BankAccount account = accounts.get(id);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return account;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive with at most two decimal places");
        }
    }
}
