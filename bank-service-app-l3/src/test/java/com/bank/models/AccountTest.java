package com.bank.models;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

public class AccountTest {

    // NEGATIVE
    @Test
    void constructorShouldRejectNullAccountId() {
        Customer c = new Customer("C1", "Alice");
        assertThrows(IllegalArgumentException.class, () -> new Account(null, c, 0.0, 10));
    }

    // NEGATIVE
    @Test
    void constructorShouldRejectBlankAccountId() {
        Customer c = new Customer("C1", "Alice");
        assertThrows(IllegalArgumentException.class, () -> new Account("   ", c, 0.0, 10));
    }

    // NEGATIVE
    @Test
    void constructorShouldRejectNullCustomer() {
        assertThrows(IllegalArgumentException.class, () -> new Account("A1", null, 0.0, 10));
    }

    // NEGATIVE
    @Test
    void constructorShouldRejectNegativeOpeningBalance() {
        Customer c = new Customer("C1", "Alice");
        assertThrows(IllegalArgumentException.class, () -> new Account("A1", c, -1.0, 10));
    }

    // NEGATIVE
    @Test
    void constructorShouldRejectNonPositiveTxnCapacity() {
        Customer c = new Customer("C1", "Alice");
        assertThrows(IllegalArgumentException.class, () -> new Account("A1", c, 0.0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Account("A1", c, 0.0, -5));
    }

    // NEGATIVE
    @Test
    void setBalanceShouldRejectNegative() {
        Customer c = new Customer("C1", "Alice");
        Account a = new Account("A1", c, 100.0, 3);
        assertThrows(IllegalArgumentException.class, () -> a.setBalance(-10.0));
    }

    // POSITIVE
    @Test
    void getBalanceShouldReturnOpeningBalance() {
        Customer c = new Customer("C1", "Alice");
        Account a = new Account("A1", c, 500.0, 3);
        assertEquals(500.0, a.getBalance());
    }

    // POSITIVE
    @Test
    void setBalanceShouldUpdateWhenValid() {
        Customer c = new Customer("C1", "Alice");
        Account a = new Account("A1", c, 100.0, 3);
        a.setBalance(250.0);
        assertEquals(250.0, a.getBalance());
    }

    // POSITIVE: Append under capacity, order maintained  --->Remark
    @Test
    void appendTransactionMaintainsOrderUntilFull() {
        Customer c = new Customer("C1", "Alice");
        Account a = new Account("A1", c, 0.0, 3);

        Transaction t1 = new Transaction("T1", TransactionType.DEPOSIT, 100, Instant.now(), null, "A1", 100.0);
        Transaction t2 = new Transaction("T2", TransactionType.DEPOSIT, 200, Instant.now(), null, "A1", 300.0);

        a.appendTransaction(t1);
        a.appendTransaction(t2);

        Transaction[] txns = a.getTransactions();

        assertEquals(2, txns.length);
        assertEquals("T1", txns[0].getTransactionId());
        assertEquals("T2", txns[1].getTransactionId());
    }

   

    
}