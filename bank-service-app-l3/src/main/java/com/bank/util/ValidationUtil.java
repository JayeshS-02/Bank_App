package com.bank.util;


import com.bank.exceptions.DuplicateCustomerAccountException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.NegativeOrZeroAmountException;
import com.bank.models.Account;
import com.bank.models.Customer;
import com.bank.repository.BankRepository;

public class ValidationUtil {


    public static Account getValidAccountOrThrow(BankRepository repo, String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new InvalidAccountException("Account ID cannot be null or blank");
        }

        Account account = repo.getAccountById(accountId);
        if (account == null) {
            throw new InvalidAccountException("Account not found: " + accountId);
        }

        return account;
    }


    public static void checkAccountExists(BankRepository repo, String accountId) {
        getValidAccountOrThrow(repo, accountId);
    }

    public static void checkPositiveAmount(double amount) {
        if (amount <= 0) {
            throw new NegativeOrZeroAmountException("Amount must be greater than zero. Provided: " + amount);
        }
    }


    public static void checkNoDuplicateCustomerAccount(Customer[] customers, Account[] accounts) {
        if (customers == null || accounts == null) {
            return;
        }

        // Check for duplicate customer IDs
        for (int i = 0; i < customers.length; i++) {
            if (customers[i] == null) continue;

            for (int j = i + 1; j < customers.length; j++) {
                if (customers[j] == null) continue;

                if (customers[i].getCustomerId().equals(customers[j].getCustomerId())) {
                    throw new DuplicateCustomerAccountException(
                            "Duplicate customer ID found: " + customers[i].getCustomerId());
                }
            }
        }

        // Check for duplicate account IDs
        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i] == null) continue;

            for (int j = i + 1; j < accounts.length; j++) {
                if (accounts[j] == null) continue;

                if (accounts[i].getAccountId().equals(accounts[j].getAccountId())) {
                    throw new DuplicateCustomerAccountException(
                            "Duplicate account ID found: " + accounts[i].getAccountId());
                }
            }
        }

        // Check if the same customer is used in multiple accounts
        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i] == null || accounts[i].getCustomer() == null) continue;

            for (int j = i + 1; j < accounts.length; j++) {
                if (accounts[j] == null || accounts[j].getCustomer() == null) continue;

                // Check if the same customer object or same customer ID is used
                if (accounts[i].getCustomer() == accounts[j].getCustomer() ||
                        accounts[i].getCustomer().getCustomerId().equals(accounts[j].getCustomer().getCustomerId())) {
                    throw new DuplicateCustomerAccountException(
                            "Same customer cannot be used in multiple accounts: " +
                                    accounts[i].getCustomer().getCustomerId());
                }
            }
        }
    }


    public static com.bank.models.Transaction[] latest10(com.bank.models.Transaction[] transactions) {
        if (transactions == null || transactions.length == 0) {
            return new com.bank.models.Transaction[0];
        }

        int n = transactions.length;
        int from = Math.max(0, n - 10);
        int len = n - from;

        // Copy the tail [from, n) into a new array (currently oldest-first)
        com.bank.models.Transaction[] last10 = new com.bank.models.Transaction[len];
        System.arraycopy(transactions, from, last10, 0, len);

        // Reverse to newest-first
        for (int i = 0, j = len - 1; i < j; i++, j--) {
            var tmp = last10[i];
            last10[i] = last10[j];
            last10[j] = tmp;
        }
        return last10;
    }
}