package com.ledgercore.controller;

import com.ledgercore.model.BankAccount;
import com.ledgercore.service.BankAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {
    private final BankAccountService service;

    public BankAccountController(BankAccountService service) {
        this.service = service;
    }

    public record CreateAccountRequest(String ownerName) {}
    public record AmountRequest(BigDecimal amount) {}
    public record TransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {}

    @PostMapping
    public ResponseEntity<BankAccount> create(@RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(request.ownerName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> get(@PathVariable Long id) {
        BankAccount account = service.getAccountById(id);
        return account == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(account);
    }

    @PostMapping("/{id}/deposits")
    public BankAccount deposit(@PathVariable Long id, @RequestBody AmountRequest request) {
        return service.deposit(id, request.amount());
    }

    @PostMapping("/{id}/withdrawals")
    public BankAccount withdraw(@PathVariable Long id, @RequestBody AmountRequest request) {
        return service.withdraw(id, request.amount());
    }

    @PostMapping("/transfers")
    public ResponseEntity<Void> transfer(@RequestBody TransferRequest request) {
        service.transfer(request.fromAccountId(), request.toAccountId(), request.amount());
        return ResponseEntity.noContent().build();
    }
}
