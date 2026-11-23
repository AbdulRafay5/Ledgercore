package com.ledgercore.controller;

import com.ledgercore.model.BankAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import com.ledgercore.model.TransactionRecord;
import java.util.List;
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

    public record CreateAccountRequest(@NotBlank(message = "Owner name is required") String ownerName) {}
    public record AmountRequest(@NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be positive") @Digits(integer = 17, fraction = 2, message = "Amount must have at most two decimal places") BigDecimal amount) {}
    public record TransferRequest(@NotNull(message = "Source account is required") Long fromAccountId, @NotNull(message = "Destination account is required") Long toAccountId, @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be positive") @Digits(integer = 17, fraction = 2, message = "Amount must have at most two decimal places") BigDecimal amount) {}

    @PostMapping
    public ResponseEntity<BankAccount> create(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(request.ownerName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> get(@PathVariable Long id) {
        BankAccount account = service.getAccountById(id);
        return account == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(account);
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionRecord> transactions(@PathVariable Long id) {
        return service.getTransactions(id);
    }

    @PostMapping("/{id}/deposits")
    public BankAccount deposit(@PathVariable Long id, @Valid @RequestBody AmountRequest request) {
        return service.deposit(id, request.amount());
    }

    @PostMapping("/{id}/withdrawals")
    public BankAccount withdraw(@PathVariable Long id, @Valid @RequestBody AmountRequest request) {
        return service.withdraw(id, request.amount());
    }

    @PostMapping("/transfers")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        service.transfer(request.fromAccountId(), request.toAccountId(), request.amount());
        return ResponseEntity.noContent().build();
    }
}
