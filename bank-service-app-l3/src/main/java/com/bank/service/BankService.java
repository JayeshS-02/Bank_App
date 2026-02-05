package com.bank.service;

import com.bank.models.Transaction;

public interface BankService {

	double getBalance(String accountId);

    double deposit(String accountId, double amount);

    double withdraw(String accountId, double amount);

    double transfer(String sourceId, String targetId, double amount);

    Transaction[] getLast10Transactions(String accountId);
}