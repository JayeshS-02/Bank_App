package com.bank.integration;

import com.bank.models.Transaction;
import com.bank.models.TransactionType;
import com.bank.repository.BankRepositoryImpl;
import com.bank.service.BankServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BankServiceIntegrationTest {

    private BankRepositoryImpl repo;
    private BankServiceImpl service;

    @BeforeEach
    void setup() {
        repo = new BankRepositoryImpl(50);
        service = new BankServiceImpl(repo);

        // Initial balances
        service.deposit("A001", 1000.0);
        service.deposit("A002", 500.0);

        System.out.println("[SETUP] A001=" + service.getBalance("A001")
                + ", A002=" + service.getBalance("A002"));
    }

    @Test
    void deposit_withdraw_transfer_endToEnd() {

        // 1) Deposit
        double depositAmount = 200.0;
        System.out.println("Depositing " + depositAmount + " into A001");
        double afterDeposit = service.deposit("A001", depositAmount);
        System.out.println("After deposit A001: " + afterDeposit);

        assertEquals(1200.0, afterDeposit, 1e-9);

        // 2) Withdraw
        double withdrawAmount = 100.0;
        System.out.println("Withdrawing " + withdrawAmount + " from A001");
        double afterWithdraw = service.withdraw("A001", withdrawAmount);
        System.out.println("After withdraw A001: " + afterWithdraw);

        assertEquals(1100.0, afterWithdraw, 1e-9);

        // 3) Transfer
        double transferAmount = 300.0;
        System.out.println("Transferring " + transferAmount + " from A001 to A002");
        double sourceFinal = service.transfer("A001", "A002", transferAmount);

        System.out.println("After transfer A001: " + service.getBalance("A001"));
        System.out.println("After transfer A002: " + service.getBalance("A002"));

        assertEquals(800.0, sourceFinal, 1e-9);
        assertEquals(800.0, service.getBalance("A001"));
        assertEquals(800.0, service.getBalance("A002"));

        // 4) Transaction logs
        Transaction[] a1Txns = service.getLast10Transactions("A001");
        Transaction[] a2Txns = service.getLast10Transactions("A002");

        System.out.println("A001 latest txn type: " + a1Txns[0].getType());
        System.out.println("A002 latest txn type: " + a2Txns[0].getType());

        assertEquals(TransactionType.TRANSFER_OUT, a1Txns[0].getType());
        assertEquals(300.0, a1Txns[0].getAmount(), 1e-9);
        assertEquals(800.0, a1Txns[0].getBalanceAfter(), 1e-9);

        assertEquals(TransactionType.TRANSFER_IN, a2Txns[0].getType());
        assertEquals(300.0, a2Txns[0].getAmount(), 1e-9);
        assertEquals(800.0, a2Txns[0].getBalanceAfter(), 1e-9);
    }
}