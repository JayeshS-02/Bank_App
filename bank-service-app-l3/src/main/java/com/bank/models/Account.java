package com.bank.models;

public class Account {
    private final String accountId;
    private final Customer customer;
    private double balance;
    private final Transaction[] transactions;
    private int txnCount = 0;

    public Account(String accountId, Customer customer, double openingBalance, int maxTransactions) {
        if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("accountId cannot be null/blank");
        if (customer == null)
            throw new IllegalArgumentException("customer cannot be null");
        if (openingBalance < 0)
            throw new IllegalArgumentException("opening balance cannot be negative");
        if (maxTransactions <= 0)
            throw new IllegalArgumentException("maxTransactions must be > 0");

        this.accountId = accountId.trim();
        this.customer = customer;
        this.balance = openingBalance;
        this.transactions = new Transaction[maxTransactions];
    }

    public String getAccountId() {
        return accountId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        if (balance < 0)
            throw new IllegalArgumentException("balance cannot be negative");
        this.balance = balance;
    }

    public void appendTransaction(Transaction txn) {
        if (txn == null)
            throw new IllegalArgumentException("transaction cannot be null");

        if (txnCount == transactions.length) {
            System.arraycopy(transactions, 1, transactions, 0, transactions.length - 1);
            transactions[transactions.length - 1] = txn;
        } else {
            transactions[txnCount++] = txn;
        }
    }

    public Transaction[] getTransactions() {
        Transaction[] copy = new Transaction[txnCount];
        System.arraycopy(transactions, 0, copy, 0, txnCount);
        return copy;
    }
}