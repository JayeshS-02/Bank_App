package com.bank.repository;

import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.InvalidDataException;
import com.bank.exceptions.SameAccountTransferException;
import com.bank.models.Account;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;

import java.time.Instant;

public class BankRepositoryImpl implements BankRepository {

	private Account[] accounts;
	private int count = 0;

	public BankRepositoryImpl(int capacity) {
		if (capacity <= 0)
			throw new IllegalArgumentException("capacity must be > 0");
		this.accounts = new Account[capacity];
	}

	@Override
	public void seedData(Account[] inputAccounts) {

	    this.accounts = new Account[this.accounts.length];
	    count = 0;

	    if (inputAccounts == null) return;

	    for (int i = 0; i < inputAccounts.length && count < this.accounts.length; i++) {
	        if (inputAccounts[i] == null) continue;
	        this.accounts[count++] = inputAccounts[i];
	    }
	}

	@Override
	public Account getAccountById(String accountId) {
		if (accountId == null)
			return null;

		for (int i = 0; i < count; i++) {
			Account acc = accounts[i];
			if (acc != null && accountId.equals(acc.getAccountId())) {
				return acc;
			}
		}
		return null;
	}

	@Override
	public Account[] getAllAccounts() {

		if (accounts.length == 0)
			throw new InvalidDataException("Repository initialized with empty accounts array.");
		for (Account a : accounts) {
			if (a == null)
				throw new InvalidDataException("Null account entry in repository.");
		}
		return accounts;
	}

    @Override
    public void appendTransaction(String accountId, Transaction txn) throws InvalidAccountException {
        Account acc = getAccountById(accountId);
        if (acc == null) {
            throw new InvalidAccountException("Invalid account: " + accountId);
        }

        String src = txn.getSourceAccountId();
        String dst = txn.getTargetAccountId();

        // Null-safe check
        if (src != null && dst != null && src.equals(dst)) {
            throw new SameAccountTransferException("Source and target cannot be the same: " + src);
        }

        acc.appendTransaction(txn);
    }

	@Override
    public Transaction[] getTransactions(String accountId) throws InvalidAccountException {
        Account acc = getAccountById(accountId);
        if (acc == null) {
            throw new InvalidAccountException("Invalid account: " + accountId);
        }
        return acc.getTransactions();
    }

	@Override
    public void saveAccount(Account account) {
        if (account == null || account.getAccountId() == null) return;
        //duplicate check - update existing
        for (int i = 0; i < count; i++) {
            if (accounts[i] != null && account.getAccountId().equals(accounts[i].getAccountId())) {
            	accounts[i] = account;
                return;
            }
        }

        // add new
        if (count < accounts.length) {
        	accounts[count++] = account;
        }
    }

    @Override
    public int size() {
        return count;
    }

//    private void ensureCapacity(int minCapacity) {
//        if (minCapacity <= accounts.length) return;
//        int newCap = Math.max(accounts.length * 2, minCapacity);
//        Account[] newArr = new Account[newCap];
//        System.arraycopy(accounts, 0, newArr, 0, count); // copy only active items
//        accounts = newArr;
//    }

}