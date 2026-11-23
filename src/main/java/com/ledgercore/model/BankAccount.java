
package com.ledgercore.model;

import java.math.BigDecimal;

public class BankAccount {

    private Long id;
    private String ownerName;
    private BigDecimal balance;

    public BankAccount(Long id, String ownerName, BigDecimal balance) {
        this.id = id;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
