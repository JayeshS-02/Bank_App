package com.bank.service;

import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.NegativeOrZeroAmountException;
import com.bank.exceptions.SameAccountTransferException;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("BankServiceImpl - Full Functional Suite (Newest-First History)")
public class BankServiceTest {

    private BankService service;

    @BeforeEach
    void setup() {
        service = new BankServiceImpl();
    }

    @Nested
    @DisplayName("Balance - getBalance()")
    class BalanceTests {

        @Test
        @DisplayName("Init has zero balances for A001 and A010")
        void init_has_zero_balances() {
            assertEquals(0.0, service.getBalance("A001"), 0.001);
            assertEquals(0.0, service.getBalance("A010"), 0.001);
        }

        @Test
        @DisplayName("Returns current balance for a valid account")
        void returns_current_balance() {
            assertEquals(0.0, service.getBalance("A001"), 0.001);
        }

        @Test
        @DisplayName("After deposit, balance reflects increment")
        void balance_updates_after_deposit() {
            service.deposit("A001", 50.0);
            assertEquals(50.0, service.getBalance("A001"), 0.001);
        }

        @Test
        @DisplayName("After withdraw, balance reflects decrement")
        void balance_updates_after_withdraw() {
            service.deposit("A001", 100.0);
            service.withdraw("A001", 40.0);
            assertEquals(60.0, service.getBalance("A001"), 0.001);
        }

        @Test
        @DisplayName("Boundary: withdraw full balance results in zero")
        void full_withdraw_results_zero() {
            service.deposit("A002", 200.0);
            service.withdraw("A002", 200.0);
            assertEquals(0.0, service.getBalance("A002"), 0.001);
        }

        // Negative
        @Test
        @DisplayName("Invalid account → InvalidAccountException")
        void invalid_account_throws() {
            assertThrows(InvalidAccountException.class, () -> service.getBalance("INVALID"));
        }
    }

    @Nested
    @DisplayName("Deposit - deposit(accountId, amount)")
    class DepositTests {

        @Test
        @DisplayName("Positive amount increases balance")
        void deposit_increases_balance() {
            double newBal = service.deposit("A001", 50.0);
            assertEquals(50.0, newBal, 0.001);
            assertEquals(50.0, service.getBalance("A001"), 0.001);
        }

        @Test
        @DisplayName("Records DEPOSIT transaction with correct balanceAfter (latest at index 0)")
        void records_deposit_transaction() {
            double before = service.getBalance("A001");
            double after = service.deposit("A001", 125.0);

            Transaction[] txns = service.getLast10Transactions("A001");
            assertTrue(txns.length >= 1);

            Transaction latest = txns[0]; // NEWEST-FIRST
            assertEquals(TransactionType.DEPOSIT, latest.getType());
            assertEquals(125.0, latest.getAmount(), 0.001);
            assertEquals(after, latest.getBalanceAfter(), 0.001);
            assertEquals(before + 125.0, after, 0.001);
        }

        @Test
        @DisplayName("Allows minimal positive amount like 0.01")
        void minimal_positive_allowed() {
            service.deposit("A001", 1000.0);
            double newBal = service.deposit("A001", 0.01);
            assertEquals(1000.01, newBal, 0.001);
        }

        @Test
        @DisplayName("Multiple deposits accumulate correctly")
        void multiple_deposits_accumulate() {
            service.deposit("A001", 10.0);
            service.deposit("A001", 15.5);
            service.deposit("A001", 24.5);
            assertEquals(50.0, service.getBalance("A001"), 0.001);
        }

        @Test
        @DisplayName("History top is most recent DEPOSIT (newest-first)")
        void history_newest_first() {
            service.deposit("A002", 10.0);
            service.deposit("A002", 20.0);

            Transaction[] txns = service.getLast10Transactions("A002");
            assertEquals(2, txns.length);
            assertEquals(20.0, txns[0].getAmount(), 0.001); // latest
            assertEquals(TransactionType.DEPOSIT, txns[0].getType());
            assertEquals(10.0, txns[1].getAmount(), 0.001); // older
        }

        // Negative
        @Test
        @DisplayName("Zero deposit amount → NegativeOrZeroAmountException")
        void zero_amount_throws() {
            assertThrows(NegativeOrZeroAmountException.class, () -> service.deposit("A001", 0.0));
        }
    }

    @Nested
    @DisplayName("Withdraw - withdraw(accountId, amount)")
    class WithdrawTests {

        @BeforeEach
        void seed() {
            // Ensure A001 has funds before each withdraw test
            service.deposit("A001", 1000.0);
        }

        @Test
        @DisplayName("Withdrawing a positive amount decreases balance")
        void withdraw_decreases_balance() {
            double newBal = service.withdraw("A001", 200.0);
            assertEquals(800.0, newBal, 0.001);
        }

        @Test
        @DisplayName("Withdrawing exactly the balance results in zero")
        void exact_withdraw_to_zero() {
            service.deposit("A002", 300.0);
            double newBal = service.withdraw("A002", 300.0);
            assertEquals(0.0, newBal, 0.001);
        }

        @Test
        @DisplayName("Records WITHDRAWAL transaction as latest at index 0")
        void records_withdrawal_transaction() {
            double before = service.getBalance("A001");
            double after = service.withdraw("A001", 150.0);

            Transaction[] txns = service.getLast10Transactions("A001");
            Transaction latest = txns[0]; // NEWEST-FIRST
            assertEquals(TransactionType.WITHDRAWAL, latest.getType());
            assertEquals(150.0, latest.getAmount(), 0.001);
            assertEquals(after, latest.getBalanceAfter(), 0.001);
            assertEquals(before - 150.0, after, 0.001);
        }

        @Test
        @DisplayName("Multiple withdrawals stack correctly")
        void multiple_withdrawals_stack() {
            service.withdraw("A001", 100.0);
            service.withdraw("A001", 200.0);
            assertEquals(700.0, service.getBalance("A001"), 0.001);
        }

        @Test
        @DisplayName("History is newest-first for withdrawals")
        void withdrawals_history_newest_first() {
            service.withdraw("A001", 10.0);
            service.withdraw("A001", 20.0);
            Transaction[] txns = service.getLast10Transactions("A001");
            assertTrue(txns.length >= 2);
            assertEquals(TransactionType.WITHDRAWAL, txns[0].getType());
            assertEquals(20.0, txns[0].getAmount(), 0.001); // latest
            assertEquals(TransactionType.WITHDRAWAL, txns[1].getType());
            assertEquals(10.0, txns[1].getAmount(), 0.001); // older
        }

        // Negative
        @Test
        @DisplayName("Insufficient funds → throws and leaves balance unchanged")
        void insufficient_funds_throws_no_change() {
            double before = service.getBalance("A001");
            assertThrows(InsufficientFundsException.class, () -> service.withdraw("A001", before + 0.01));
            assertEquals(before, service.getBalance("A001"), 0.001);
        }
    }

    @Nested
    @DisplayName("Transfer - transfer(srcId, dstId, amount)")
    class TransferTests {

        @BeforeEach
        void seed() {
            service.deposit("A001", 500.0);
            service.deposit("A002", 100.0);
        }

        @Test
        @DisplayName("Happy path: funds move from source to destination")
        void moves_funds() {
            service.transfer("A001", "A002", 150.0);
            assertEquals(350.0, service.getBalance("A001"), 0.001);
            assertEquals(250.0, service.getBalance("A002"), 0.001);
        }

        @Test
        @DisplayName("Records TRANSFER_OUT (src) and TRANSFER_IN (dst) with latest at index 0")
        void records_two_transactions() {
            service.transfer("A001", "A002", 200.0);

            Transaction[] srcTx = service.getLast10Transactions("A001");
            Transaction[] dstTx = service.getLast10Transactions("A002");

            Transaction srcLatest = srcTx[0];
            Transaction dstLatest = dstTx[0];

            assertEquals(TransactionType.TRANSFER_OUT, srcLatest.getType());
            assertEquals(200.0, srcLatest.getAmount(), 0.001);

            assertEquals(TransactionType.TRANSFER_IN, dstLatest.getType());
            assertEquals(200.0, dstLatest.getAmount(), 0.001);
        }

        @Test
        @DisplayName("Transfer full source balance results in source=0 and dst+=amount")
        void transfer_full_balance_to_zero() {
            service.deposit("A003", 300.0);
            double srcAfter = service.transfer("A003", "A004", 300.0);
            assertEquals(0.0, srcAfter, 0.001);
            assertEquals(300.0, service.getBalance("A004"), 0.001);
        }

        @Test
        @DisplayName("Multiple transfers accumulate correctly for both accounts")
        void multiple_transfers_accumulate() {
            service.transfer("A001", "A002", 50.0);
            service.transfer("A001", "A002", 25.0);
            assertEquals(425.0, service.getBalance("A001"), 0.001);
            assertEquals(175.0, service.getBalance("A002"), 0.001);
        }

        @Test
        @DisplayName("History is newest-first for both src and dst (latest at index 0)")
        void history_newest_first_for_transfers() {
            service.transfer("A001", "A002", 10.0);
            service.transfer("A001", "A002", 20.0);

            Transaction[] srcTx = service.getLast10Transactions("A001");
            Transaction[] dstTx = service.getLast10Transactions("A002");

            assertEquals(TransactionType.TRANSFER_OUT, srcTx[0].getType());
            assertEquals(20.0, srcTx[0].getAmount(), 0.001);
            assertEquals(TransactionType.TRANSFER_OUT, srcTx[1].getType());
            assertEquals(10.0, srcTx[1].getAmount(), 0.001);

            assertEquals(TransactionType.TRANSFER_IN, dstTx[0].getType());
            assertEquals(20.0, dstTx[0].getAmount(), 0.001);
            assertEquals(TransactionType.TRANSFER_IN, dstTx[1].getType());
            assertEquals(10.0, dstTx[1].getAmount(), 0.001);
        }

        // Negative
        @Test
        @DisplayName("Same source and destination → SameAccountTransferException")
        void same_account_throws() {
            assertThrows(SameAccountTransferException.class, () -> service.transfer("A001", "A001", 10.0));
        }
    }

    @Nested
    @DisplayName("History - getLast10Transactions(accountId)")
    class Last10TransactionsTests {

        @Test
        @DisplayName("No transactions yet → empty array")
        void empty_history_returns_empty() {
            Transaction[] txns = service.getLast10Transactions("A001");
            assertNotNull(txns);
            assertEquals(0, txns.length);
        }

        @Test
        @DisplayName("Exactly 10 transactions → returns 10 (newest-first)")
        void exactly_10_returns_10() {
            for (int i = 0; i < 10; i++) service.deposit("A001", 1.0);
            Transaction[] txns = service.getLast10Transactions("A001");
            assertEquals(10, txns.length);
        }

        @Test
        @DisplayName("More than 10 → returns the most recent 10 (newest-first)")
        void more_than_10_returns_recent_10() {
            // amounts 1..15
            for (int i = 0; i < 15; i++) service.deposit("A001", i + 1);
            Transaction[] txns = service.getLast10Transactions("A001");
            assertEquals(10, txns.length);

            // NEWEST-FIRST: index 0 = 15, last index = 6
            assertEquals(15.0, txns[0].getAmount(), 0.001);
            assertEquals(6.0, txns[txns.length - 1].getAmount(), 0.001);
            assertEquals(TransactionType.DEPOSIT, txns[0].getType());
            assertEquals(TransactionType.DEPOSIT, txns[txns.length - 1].getType());
        }

        @Test
        @DisplayName("Ordering is newest-first (non-increasing timestamps)")
        void ordering_newest_first() throws InterruptedException {
            service.deposit("A001", 100.0); // older
            Thread.sleep(2);                // ensure different timestamps
            service.deposit("A001", 200.0); // newer

            Transaction[] txns = service.getLast10Transactions("A001");
            assertEquals(2, txns.length);

            // newest-first means each next timestamp is <= previous
            for (int i = 0; i < txns.length - 1; i++) {
                assertTrue(
                        txns[i].getTimestamp().isAfter(txns[i + 1].getTimestamp())
                                || txns[i].getTimestamp().equals(txns[i + 1].getTimestamp())
                );
            }

            assertEquals(200.0, txns[0].getAmount(), 0.001);
            assertEquals(100.0, txns[1].getAmount(), 0.001);
        }

        @Test
        @DisplayName("Mixed types appear in newest-first order")
        void mixed_types_in_order() {
            service.deposit("A001", 300.0);   // older
            service.withdraw("A001", 50.0);   // newer

            Transaction[] txns = service.getLast10Transactions("A001");
            assertEquals(2, txns.length);
            assertEquals(TransactionType.WITHDRAWAL, txns[0].getType());   // latest
            assertEquals(TransactionType.DEPOSIT, txns[1].getType());      // older
        }

        // Negative
        @Test
        @DisplayName("Invalid account → InvalidAccountException")
        void invalid_account_throws() {
            assertThrows(InvalidAccountException.class, () -> service.getLast10Transactions("INVALID"));
        }
    }
}