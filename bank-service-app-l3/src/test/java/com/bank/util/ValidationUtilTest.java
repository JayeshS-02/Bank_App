package com.bank.util;

import com.bank.exceptions.DuplicateCustomerAccountException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.NegativeOrZeroAmountException;
import com.bank.models.Account;
import com.bank.models.Customer;
import com.bank.models.Transaction;
import com.bank.repository.BankRepository;
import com.bank.repository.BankRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
class ValidationUtilTest {

    private BankRepository repoWith(Account... accounts) {
        BankRepository repo = new BankRepositoryImpl(20);
        repo.seedData(accounts);
        return repo;
    }

//    @Test
//    @DisplayName("getValidAccountOrThrow_returns_account_when_present")
//    void getValidAccountOrThrow_returns_account_when_present() {
//        Account a = new Account("A001", new Customer("C1", "Alice"), 0, 10);
//        BankRepository repo = repoWith(a);
//
//        Account found = ValidationUtil.getValidAccountOrThrow(repo, "A001");
//        assertNotNull(found);
//        assertEquals("A001", found.getAccountId());
//    }

    @Test
    @DisplayName("getValidAccountOrThrow_throws_for_null_or_blank_id")
    void getValidAccountOrThrow_throws_for_null_or_blank_id() {
        BankRepository repo = repoWith();
        assertThrows(InvalidAccountException.class, () -> ValidationUtil.getValidAccountOrThrow(repo, null));
        assertThrows(InvalidAccountException.class, () -> ValidationUtil.getValidAccountOrThrow(repo, " "));
    }

    @Test
    @DisplayName("getValidAccountOrThrow_throws_for_unknown_account")
    void getValidAccountOrThrow_throws_for_unknown_account() {
        BankRepository repo = repoWith();
        assertThrows(InvalidAccountException.class, () -> ValidationUtil.getValidAccountOrThrow(repo, "UNKNOWN"));
    }

    @Test
    @DisplayName("checkPositiveAmount_throws_when_amount_is_zero_or_negative")
    void checkPositiveAmount_throws_when_amount_is_zero_or_negative() {
        assertThrows(NegativeOrZeroAmountException.class, () -> ValidationUtil.checkPositiveAmount(0));
        assertThrows(NegativeOrZeroAmountException.class, () -> ValidationUtil.checkPositiveAmount(-1));
    }

    @Test
    @DisplayName("checkPositiveAmount_allows_min_positive_values")
    void checkPositiveAmount_allows_min_positive_values() {
        assertDoesNotThrow(() -> ValidationUtil.checkPositiveAmount(0.01));
    }

    @Test
    @DisplayName("checkNoDuplicateCustomerAccount_detects_duplicate_customer_ids")
    void checkNoDuplicateCustomerAccount_detects_duplicate_customer_ids() {
        Customer[] customers = new Customer[] {
                new Customer("C1", "A"), new Customer("C1", "B")
        };
        Account[] accounts = new Account[] {
                new Account("A1", customers[0], 0, 10), new Account("A2", customers[1], 0, 10)
        };
        assertThrows(DuplicateCustomerAccountException.class,
                () -> ValidationUtil.checkNoDuplicateCustomerAccount(customers, accounts));
    }

    @Test
    @DisplayName("checkNoDuplicateCustomerAccount_detects_duplicate_account_ids")
    void checkNoDuplicateCustomerAccount_detects_duplicate_account_ids() {
        Customer c1 = new Customer("C1", "A");
        Customer c2 = new Customer("C2", "B");
        Customer[] customers = new Customer[] { c1, c2 };
        Account[] accounts = new Account[] {
                new Account("AX", c1, 0, 10), new Account("AX", c2, 0, 10)
        };
        assertThrows(DuplicateCustomerAccountException.class,
                () -> ValidationUtil.checkNoDuplicateCustomerAccount(customers, accounts));
    }

    @Test
    @DisplayName("checkNoDuplicateCustomerAccount_detects_same_customer_used_in_multiple_accounts")
    void checkNoDuplicateCustomerAccount_detects_same_customer_used_in_multiple_accounts() {
        Customer c1 = new Customer("C1", "A");
        Customer c2 = new Customer("C2", "B");
        Customer[] customers = new Customer[] { c1, c2 };
        Account[] accounts = new Account[] {
                new Account("A1", c1, 0, 10), new Account("A2", c1, 0, 10)
        };
        assertThrows(DuplicateCustomerAccountException.class,
                () -> ValidationUtil.checkNoDuplicateCustomerAccount(customers, accounts));
    }

    @Test
    @DisplayName("latest10 returns newest-first: last 10 when len>10, and reversed copy when len<=10")
    void latest10_returns_newest_first_for_all_lengths() {
        // Build an array of 12 transactions with strictly increasing timestamps and amounts = 1..12
        com.bank.models.Transaction[] all = new com.bank.models.Transaction[12];
        java.time.Instant base = java.time.Instant.parse("2026-01-21T00:00:00Z");
        for (int i = 0; i < 12; i++) {
        	all[i] = new com.bank.models.Transaction(
        	        "TXN" + i,                         // transactionId
        	        com.bank.models.TransactionType.DEPOSIT,
        	        i + 1,
        	        base.plusSeconds(i),
        	        null,
        	        "A1",
        	        i + 1
        	);
        }

        // Case 1: length > 10 → most recent 10 in NEWEST-FIRST order
        com.bank.models.Transaction[] last10 = ValidationUtil.latest10(all);
        assertEquals(10, last10.length);

        // NEWEST-FIRST: index 0 should be amount 12, then 11, ..., index 9 should be amount 3
        assertEquals(12.0, last10[0].getAmount(), 0.001);
        assertEquals(11.0, last10[1].getAmount(), 0.001);
        assertEquals(10.0, last10[2].getAmount(), 0.001);
        assertEquals(9.0,  last10[3].getAmount(), 0.001);
        assertEquals(8.0,  last10[4].getAmount(), 0.001);
        assertEquals(7.0,  last10[5].getAmount(), 0.001);
        assertEquals(6.0,  last10[6].getAmount(), 0.001);
        assertEquals(5.0,  last10[7].getAmount(), 0.001);
        assertEquals(4.0,  last10[8].getAmount(), 0.001);
        assertEquals(3.0,  last10[9].getAmount(), 0.001);

        // And timestamps should be non-increasing (newest-first)
        for (int i = 0; i < last10.length - 1; i++) {
            assertTrue(
                    last10[i].getTimestamp().isAfter(last10[i + 1].getTimestamp())
                            || last10[i].getTimestamp().equals(last10[i + 1].getTimestamp()),
                    "Timestamps must be newest-first (non-increasing)"
            );
        }

        // Case 2: length <= 10 → return all in NEWEST-FIRST (i.e., reversed) AND as a new array (defensive copy)
        com.bank.models.Transaction[] two = new com.bank.models.Transaction[] { all[0], all[1] }; // amounts 1,2 (oldest-first)
        com.bank.models.Transaction[] copy = ValidationUtil.latest10(two);
        assertEquals(2, copy.length);
        assertNotSame(two, copy, "Should return a copy, not the same instance");

        // NEWEST-FIRST: index 0 should be amount 2, then amount 1
        assertEquals(2.0, copy[0].getAmount(), 0.001);
        assertEquals(1.0, copy[1].getAmount(), 0.001);

        // And timestamps should be non-increasing
        assertTrue(
                copy[0].getTimestamp().isAfter(copy[1].getTimestamp())
                        || copy[0].getTimestamp().equals(copy[1].getTimestamp()),
                "Two-element slice must be newest-first"
        );
    }

    @Test
    @DisplayName("latest10_returns_empty_array_for_null_input")
    void latest10_returns_empty_array_for_null_input() {
        var result = ValidationUtil.latest10(null);
        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
