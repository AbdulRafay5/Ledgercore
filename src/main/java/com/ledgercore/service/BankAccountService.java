package com.ledgercore.service;

import com.ledgercore.model.BankAccount;
import com.ledgercore.model.TransactionRecord;
import com.ledgercore.repository.TransactionRecordRepository;
import java.util.List;
import com.ledgercore.repository.BankAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class BankAccountService {
    private final BankAccountRepository repository;
    private final TransactionRecordRepository transactions;

    public BankAccountService(BankAccountRepository repository, TransactionRecordRepository transactions) {
        this.repository = repository;
        this.transactions = transactions;
    }

    @Transactional
    public BankAccount createAccount(String ownerName) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner name is required");
        }
        return repository.save(new BankAccount(null, ownerName.trim(), BigDecimal.ZERO.setScale(2)));
    }

    @Transactional(readOnly = true)
    public BankAccount getAccountById(Long id) {
        return id == null ? null : repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<TransactionRecord> getTransactions(Long accountId) {
        if (accountId == null || !repository.existsById(accountId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return transactions.findByFromAccountIdOrToAccountIdOrderByCreatedAtDescIdDesc(accountId, accountId);
    }

    @Transactional
    public BankAccount deposit(Long id, BigDecimal amount) {
        validateAmount(amount);
        BankAccount account = requireLockedAccount(id);
        account.setBalance(account.getBalance().add(amount));
        BankAccount saved = repository.save(account);
        transactions.save(new TransactionRecord(TransactionRecord.Type.DEPOSIT, null, id, amount));
        return saved;
    }

    @Transactional
    public BankAccount withdraw(Long id, BigDecimal amount) {
        validateAmount(amount);
        BankAccount account = requireLockedAccount(id);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        BankAccount saved = repository.save(account);
        transactions.save(new TransactionRecord(TransactionRecord.Type.WITHDRAWAL, id, null, amount));
        return saved;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        validateAmount(amount);
        if (fromId == null || toId == null || fromId.equals(toId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Accounts must be different");
        }
        // Acquire locks in a stable order to reduce the chance of transfer deadlocks.
        BankAccount first = requireLockedAccount(Math.min(fromId, toId));
        BankAccount second = requireLockedAccount(Math.max(fromId, toId));
        BankAccount source = fromId.equals(first.getId()) ? first : second;
        BankAccount destination = toId.equals(first.getId()) ? first : second;
        if (source.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");
        }
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));
        repository.save(source);
        repository.save(destination);
        transactions.save(new TransactionRecord(TransactionRecord.Type.TRANSFER, fromId, toId, amount));
    }

    private BankAccount requireLockedAccount(Long id) {
        return repository.findByIdForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive with at most two decimal places");
        }
    }
}
