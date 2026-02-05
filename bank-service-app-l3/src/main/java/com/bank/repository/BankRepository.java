package com.bank.repository;

import com.bank.exceptions.InvalidAccountException;
import com.bank.models.Account;
import com.bank.models.Transaction;

public interface BankRepository {

	void seedData(Account[] accounts);

	Account getAccountById(String accountId);

	Account[] getAllAccounts();

	void appendTransaction(String accountId, Transaction txn) throws InvalidAccountException;

	Transaction[] getTransactions(String accountId) throws InvalidAccountException;

	void saveAccount(Account account);

	int size();

}