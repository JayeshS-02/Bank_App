package com.bank.util;

import com.bank.models.Account;
import com.bank.models.Customer;



public class SampleDataUtil {

	private static final int DEFAULT_ACCOUNT_COUNT = 10;
    private static final int MAX_TRANSACTIONS_PER_ACCOUNT = 10;
    private static final double INITIAL_BALANCE = 0.0;


    public static Customer[] createSampleCustomers() {
        Customer[] customers = new Customer[DEFAULT_ACCOUNT_COUNT];

        for (int i = 0; i < DEFAULT_ACCOUNT_COUNT; i++) {
            String customerId = "C" + (i + 1);
            String customerName = "Customer " + (i + 1);
            customers[i] = new Customer(customerId, customerName);
        }

        return customers;
    }


    public static Account[] createSampleAccounts() {
        Customer[] customers = createSampleCustomers();
        Account[] accounts = new Account[DEFAULT_ACCOUNT_COUNT];

        for (int i = 0; i < DEFAULT_ACCOUNT_COUNT; i++) {
            String accountId = String.format("A%03d", i + 1); // A001, A002, ..., A010
            accounts[i] = new Account(accountId, customers[i], INITIAL_BALANCE, MAX_TRANSACTIONS_PER_ACCOUNT);
        }

        return accounts;
    }


    public static Account[] createSampleAccountsWithBalances(double[] balances) {
        if (balances == null || balances.length != DEFAULT_ACCOUNT_COUNT) {
            throw new IllegalArgumentException("Balances array must contain exactly " + DEFAULT_ACCOUNT_COUNT + " elements");
        }

        Customer[] customers = createSampleCustomers();
        Account[] accounts = new Account[DEFAULT_ACCOUNT_COUNT];

        for (int i = 0; i < DEFAULT_ACCOUNT_COUNT; i++) {
            String accountId = String.format("A%03d", i + 1);
            accounts[i] = new Account(accountId, customers[i], balances[i], MAX_TRANSACTIONS_PER_ACCOUNT);
        }

        return accounts;
    }


    public static void validateNoDuplicates(Customer[] customers, Account[] accounts) {
        ValidationUtil.checkNoDuplicateCustomerAccount(customers, accounts);
    }
}
