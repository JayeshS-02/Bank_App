package com.bank.service;

import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.SameAccountTransferException;
import com.bank.models.Account;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;
import com.bank.repository.BankRepository;
import com.bank.repository.BankRepositoryImpl;
import com.bank.util.SampleDataUtil;
import com.bank.util.ValidationUtil;

import java.time.Instant;
import java.util.UUID;

public class BankServiceImpl implements BankService {

	private static final int REPOSITORY_CAPACITY = 50;
	private final BankRepository repository;

	public BankServiceImpl() {
		this.repository = new BankRepositoryImpl(REPOSITORY_CAPACITY);
		initializeAccounts();
	}

	public BankServiceImpl(BankRepository repository) {
		this.repository = repository;
		initializeAccounts();
	}

	@Override
	public double getBalance(String accountId) {
		Account acc = ValidationUtil.getValidAccountOrThrow(repository, accountId);
		return acc.getBalance();
	}

	@Override
	public double deposit(String accountId, double amount) {
		ValidationUtil.checkPositiveAmount(amount);
		Account acc = ValidationUtil.getValidAccountOrThrow(repository, accountId);

		double newBalance = acc.getBalance() + amount;
		acc.setBalance(newBalance);

		Transaction txn = new Transaction(UUID.randomUUID().toString(), // transactionId
				TransactionType.DEPOSIT, amount, Instant.now(), null, // sourceAccountId
				acc.getAccountId(), // targetAccountId
				newBalance // balanceAfter
		);

		repository.appendTransaction(accountId, txn);
		repository.saveAccount(acc);

		return newBalance;
	}

	@Override
	public double withdraw(String accountId, double amount) {
		ValidationUtil.checkPositiveAmount(amount);
		Account acc = ValidationUtil.getValidAccountOrThrow(repository, accountId);

		if (acc.getBalance() < amount) {
			throw new InsufficientFundsException(
					"Insufficient funds in account " + accountId + " for amount " + amount);
		}

		double newBalance = acc.getBalance() - amount;
		acc.setBalance(newBalance);

		Transaction txn = new Transaction(UUID.randomUUID().toString(), TransactionType.WITHDRAWAL, amount,
				Instant.now(), acc.getAccountId(), // sourceAccountId
				null, // targetAccountId
				newBalance);

		repository.appendTransaction(accountId, txn);
		repository.saveAccount(acc);

		return newBalance;
	}

	@Override
	public double transfer(String sourceId, String targetId, double amount) {
		if (sourceId == null || targetId == null) {
			throw new InvalidAccountException("Source/Target account id cannot be null");
		}
		if (sourceId.equals(targetId)) {
			throw new SameAccountTransferException("Source and target cannot be the same: " + sourceId);
		}

		ValidationUtil.checkPositiveAmount(amount);
		Account src = ValidationUtil.getValidAccountOrThrow(repository, sourceId);
		Account dst = ValidationUtil.getValidAccountOrThrow(repository, targetId);

		if (src.getBalance() < amount) {
			throw new InsufficientFundsException(
					"Insufficient funds in source account " + sourceId + " for amount " + amount);
		}

		double srcNewBal = src.getBalance() - amount;
		double dstNewBal = dst.getBalance() + amount;
		src.setBalance(srcNewBal);
		dst.setBalance(dstNewBal);

		Instant now = Instant.now();

		// This is to Record TRANSFER_OUT on source
		Transaction outTxn = new Transaction(UUID.randomUUID().toString(), TransactionType.TRANSFER_OUT, amount, now,
				sourceId, targetId, srcNewBal);
		repository.appendTransaction(sourceId, outTxn);
		repository.saveAccount(src);

		// This is to Record TRANSFER_IN on destination
		Transaction inTxn = new Transaction(UUID.randomUUID().toString(), TransactionType.TRANSFER_IN, amount, now,
				sourceId, targetId, dstNewBal);
		repository.appendTransaction(targetId, inTxn);
		repository.saveAccount(dst);
		return srcNewBal;
	}

	@Override
	public Transaction[] getLast10Transactions(String accountId) {
		ValidationUtil.checkAccountExists(repository, accountId);
		Transaction[] allTransactions = repository.getTransactions(accountId);
		return ValidationUtil.latest10(allTransactions);
	}

	// This is for Initialization
	private void initializeAccounts() {
		Account[] accounts = SampleDataUtil.createSampleAccounts();
		SampleDataUtil.validateNoDuplicates(SampleDataUtil.createSampleCustomers(), accounts);
		repository.seedData(accounts);
	}

}