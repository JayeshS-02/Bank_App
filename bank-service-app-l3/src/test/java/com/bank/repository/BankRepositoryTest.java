package com.bank.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.SameAccountTransferException;
import com.bank.models.Account;
import com.bank.models.Customer;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BankRepositoryTest {

    private BankRepository repo;

    @BeforeEach
    void setUp() {
        repo = new BankRepositoryImpl(50);
    }

    private Account createAccount(String accId, String custId, String name, double balance, int maxTxns) {
        return new Account(accId, new Customer(custId, name), balance, maxTxns);
    }

    private Transaction createTxn(TransactionType type, double amount, String src, String dst, double balanceAfter) {
        return new Transaction(
                "TXN_TEST",
                type,
                amount,
                Instant.parse("2026-01-21T10:00:00Z"),
                src,
                dst,
                balanceAfter
        );
    }

    // ===============           POSITIVE            ===============

    @Test
    @DisplayName("seedData initializes repository count with non-null accounts only")
         
    void seedData_initializes_count_correctly() {
        Account[] initial = new Account[10];
        for (int i = 0; i < 10; i++) {
            initial[i] = createAccount("ACC" + i, "CUST" + i, "Name" + i, 1000.0 + i, 20);
        }

        repo.seedData(initial);

        assertEquals(10, repo.size(), "Repository should contain exactly 10 accounts after seeding");

        assertNotNull(repo.getAccountById("ACC0"));
        assertNotNull(repo.getAccountById("ACC9"));
    }

    @Test
    @DisplayName("seedData: ignores nulls and stores only non-null accounts")
         
    void seedData_ignores_nulls_and_counts_only_non_nulls() {
        Account a0 = createAccount("A0", "C0", "N0", 100, 5);
        Account a2 = createAccount("A2", "C2", "N2", 200, 5);
        Account a4 = createAccount("A4", "C4", "N4", 300, 5);

        Account[] input = new Account[] { a0, null, a2, null, a4 };

        repo.seedData(input);

        assertEquals(3, repo.size());

        assertNotNull(repo.getAccountById("A0"));
        assertNotNull(repo.getAccountById("A2"));
        assertNotNull(repo.getAccountById("A4"));

        assertNull(repo.getAccountById("A1"));
        assertNull(repo.getAccountById("A3"));
    }

    @Test
    @DisplayName("getAccountById returns account when present")
         
    void getAccountById_returns_account_when_present() {
        Account a1 = createAccount("ACC100", "C100", "Alice", 5000, 20);
        repo.seedData(new Account[] { a1 });

        Account found = repo.getAccountById("ACC100");

        assertNotNull(found);
        assertEquals("ACC100", found.getAccountId());
    }

    @Test
    @DisplayName("appendTransaction adds to correct account for DEPOSIT")
         
    void appendTransaction_appends_deposit() {
        Account a1 = createAccount("ACC1", "C1", "A", 100, 10);
        Account a2 = createAccount("ACC2", "C2", "B", 100, 10);
        repo.seedData(new Account[] { a1,a2 });

        Transaction t1 = createTxn(TransactionType.DEPOSIT, 50, "ACC1", "ACC2", 150);

        repo.appendTransaction("ACC1", t1);

        Transaction[] txns = repo.getTransactions("ACC1");
        assertEquals(1, txns.length);
        assertEquals(TransactionType.DEPOSIT, txns[0].getType());
        assertEquals(50, txns[0].getAmount());
    }

    @Test
    @DisplayName("getTransactions returns empty array when account has no txns")
         
    void getTransactions_returns_empty_when_no_txns() {
        repo.seedData(new Account[] { createAccount("ACC1", "C1", "A", 100, 10) });

        Transaction[] txns = repo.getTransactions("ACC1");

        assertNotNull(txns);
        assertEquals(0, txns.length);
    }

    @Test
    @DisplayName("getTransactions returns appended transactions in insertion order")
         
    void getTransactions_returns_all_in_order() {
        repo.seedData(new Account[] { createAccount("ACC1", "C1", "A", 100, 10),
                createAccount("ACC2", "C2", "B", 100, 10)
        });

        repo.appendTransaction("ACC1", createTxn(TransactionType.DEPOSIT, 10, "ACC1", "ACC2", 110));
        repo.appendTransaction("ACC1", createTxn(TransactionType.WITHDRAWAL, 5, "ACC1", "ACC2", 105));
        repo.appendTransaction("ACC1", createTxn(TransactionType.DEPOSIT, 20, "ACC1", "ACC2", 125));

        Transaction[] txns = repo.getTransactions("ACC1");
        assertEquals(3, txns.length);
        assertEquals(TransactionType.DEPOSIT, txns[0].getType());
        assertEquals(TransactionType.WITHDRAWAL, txns[1].getType());
        assertEquals(TransactionType.DEPOSIT, txns[2].getType());
    }

    @Test
    @DisplayName("getTransactions reflects only that account's transactions")
         
    void getTransactions_is_account_scoped() {
        Account a1 = createAccount("ACC1", "C1", "A", 100, 10);
        Account a2 = createAccount("ACC2", "C2", "B", 200, 10);
        repo.seedData(new Account[] { a1, a2 });

        repo.appendTransaction("ACC1", createTxn(TransactionType.DEPOSIT, 10, "ACC1", "ACC2", 110));
        repo.appendTransaction("ACC2", createTxn(TransactionType.DEPOSIT, 50, "ACC2", "ACC1", 250));

        assertEquals(1, repo.getTransactions("ACC1").length);
        assertEquals(1, repo.getTransactions("ACC2").length);

        assertEquals(10, repo.getTransactions("ACC1")[0].getAmount());
        assertEquals(50, repo.getTransactions("ACC2")[0].getAmount());
    }

    @Test
    @DisplayName("saveAccount: saving same ID again does not duplicate (overwrite/no-op)")
         
    void saveAccount_no_duplicate_for_same_id() {
        Account a1 = createAccount("ACC1", "C1", "A", 100, 10);
        repo.seedData(new Account[] { a1 });
        assertEquals(1, repo.size());

        repo.saveAccount(a1);

        assertEquals(1, repo.size());
    }

    @Test
    @DisplayName("saveAccount: updated account is retrievable with new balance")
         
    void saveAccount_updates_visible() {
        Account a1 = createAccount("ACC1", "C1", "A", 100, 10);
        repo.seedData(new Account[] { a1 });

        Account updated = createAccount("ACC1", "C1", "A", 999, 10);

        repo.saveAccount(updated);

        Account fetched = repo.getAccountById("ACC1");
        assertNotNull(fetched);
        assertEquals(999, fetched.getBalance());
    }

    @Test
    @DisplayName("appendTransaction: TRANSFER_OUT recorded on the source account key")
         
    void appendTransaction_appends_TRANSFER_OUT_on_source() {
        Account src = createAccount("SRC1", "C1", "Alice", 1000, 10);
        Account dst = createAccount("DST1", "C2", "Bob", 500, 10);
        repo.seedData(new Account[] { src, dst });

        Transaction tOut = createTxn(TransactionType.TRANSFER_OUT, 200, "SRC1", "DST1", 800);

        repo.appendTransaction("SRC1", tOut);

        Transaction[] srcTxns = repo.getTransactions("SRC1");
        assertEquals(1, srcTxns.length);
        assertEquals(TransactionType.TRANSFER_OUT, srcTxns[0].getType());
        assertEquals("SRC1", srcTxns[0].getSourceAccountId());
        assertEquals("DST1", srcTxns[0].getTargetAccountId());
        assertEquals(200, srcTxns[0].getAmount());
        assertEquals(800, srcTxns[0].getBalanceAfter());
    }

    @Test
    @DisplayName("appendTransaction: TRANSFER_IN recorded on the target account key")
         
    void appendTransaction_appends_TRANSFER_IN_on_target() {
        Account src = createAccount("SRC2", "C1", "Alice", 1000, 10);
        Account dst = createAccount("DST2", "C2", "Bob", 500, 10);
        repo.seedData(new Account[] { src, dst });

        Transaction tIn = createTxn(TransactionType.TRANSFER_IN, 150, "SRC2", "DST2", 650);

        repo.appendTransaction("DST2", tIn);

        Transaction[] dstTxns = repo.getTransactions("DST2");
        assertEquals(1, dstTxns.length);
        assertEquals(TransactionType.TRANSFER_IN, dstTxns[0].getType());
        assertEquals("SRC2", dstTxns[0].getSourceAccountId());
        assertEquals("DST2", dstTxns[0].getTargetAccountId());
        assertEquals(150, dstTxns[0].getAmount());
        assertEquals(650, dstTxns[0].getBalanceAfter());
    }

    // ==============           BOUNDARY            ===============

    @Test
    @DisplayName("seedData: stops at capacity while skipping nulls")
         
    void seedData_respects_capacity_and_skips_nulls() {
        repo = new BankRepositoryImpl(5);

        Account[] input = new Account[] {
                createAccount("A0", "C0", "N0", 100, 5),
                null,
                createAccount("A2", "C2", "N2", 200, 5),
                createAccount("A3", "C3", "N3", 300, 5),
                null,
                createAccount("A5", "C5", "N5", 400, 5),
                createAccount("A6", "C6", "N6", 500, 5),
                createAccount("A7", "C7", "N7", 600, 5)
        };

        repo.seedData(input);

        assertEquals(5, repo.size());

        assertNotNull(repo.getAccountById("A0"));
        assertNotNull(repo.getAccountById("A2"));
        assertNotNull(repo.getAccountById("A3"));
        assertNotNull(repo.getAccountById("A5"));
        assertNotNull(repo.getAccountById("A6"));

        assertNull(repo.getAccountById("A7"));
    }

    @Test
    @DisplayName("getAccountById is case-sensitive")
         
    void getAccountById_is_case_sensitive() {
        repo.seedData(new Account[] { createAccount("AccX", "C1", "A", 100, 10) });

        assertNotNull(repo.getAccountById("AccX"));
        assertNull(repo.getAccountById("ACCX"), "Should be case-sensitive");
    }

    @Test
    @DisplayName("getAccountById requires exact match (no extra whitespace)")
         
    void getAccountById_requires_exact_match() {
        repo.seedData(new Account[] { createAccount("AccX", "C1", "A", 100, 10) });

        assertNull(repo.getAccountById("AccX "), "Should not match with extra whitespace");
    }

    // ==============       NEGATIVE / ERROR        ===============

    @Test
    @DisplayName("getAccountById returns null when absent")
        // [NEGATIVE] (Non-throwing absence handling)
    void getAccountById_returns_null_when_absent() {
        repo.seedData(new Account[] { createAccount("ACC1", "C1", "A", 100, 10) });

        assertNull(repo.getAccountById("DOES_NOT_EXIST"));
    }

    @Test
    @DisplayName("appendTransaction throws InvalidAccountException for unknown account")
        // [NEGATIVE] (Throws)
    void appendTransaction_throws_for_unknown_account() {
        repo.seedData(new Account[] { createAccount("ACC1", "C1", "A", 100, 10) });
        Transaction t1 = createTxn(TransactionType.DEPOSIT, 50, "UNKNOWN", "ACC2", 50);

        InvalidAccountException ex = assertThrows(
                InvalidAccountException.class,
                () -> repo.appendTransaction("UNKNOWN", t1)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("invalid account"));
    }

    @ParameterizedTest(name = "getTransactions throws for invalid id: ''{0}''")
    @ValueSource(strings = { "UNKNOWN", "acc1", " ACC1", "ACC1 " })
    @DisplayName("getTransactions throws InvalidAccountException for invalid IDs")
        // [NEGATIVE] (Throws)
    void getTransactions_throws_for_invalid_ids(String badId) {
        repo.seedData(new Account[] { createAccount("ACC1", "C1", "A", 100, 10) });

        InvalidAccountException ex = assertThrows(
                InvalidAccountException.class,
                () -> repo.getTransactions(badId)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("invalid account"));
    }

    @Test
    @DisplayName("appendTransaction rejects self-transfer where source == target (if enforced)")
        // [NEGATIVE] (Throws)
    void appendTransaction_rejects_self_transfer_if_enforced() {
        Account a = createAccount("A1", "C1", "Alice", 1000, 10);
        repo.seedData(new Account[] { a });

        Transaction selfOut = createTxn(TransactionType.TRANSFER_OUT, 100, "A1", "A1", 900);

        assertThrows(SameAccountTransferException.class,
                () -> repo.appendTransaction("A1", selfOut));
    }
}