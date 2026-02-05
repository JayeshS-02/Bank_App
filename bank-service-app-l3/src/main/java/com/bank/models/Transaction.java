package com.bank.models;

import java.time.Instant;

public class Transaction {
	private final String transactionId;
	private final TransactionType type;
	private final double amount;
	private final Instant timestamp;
	private final String sourceAccountId;
	private final String targetAccountId;
	private final double balanceAfter;

	public Transaction(String transactionId, TransactionType type, double amount, Instant timestamp,
			String sourceAccountId, String targetAccountId, double balanceAfter) {

		if (transactionId == null || transactionId.trim().isEmpty())
			throw new IllegalArgumentException("transactionId required");

		if (type == null)
			throw new IllegalArgumentException("type required");

		if (amount <= 0)
			throw new IllegalArgumentException("amount must be > 0");

		if (timestamp == null)
			throw new IllegalArgumentException("timestamp required");

		switch (type) {
		case DEPOSIT:
			if (targetAccountId == null || targetAccountId.trim().isEmpty())
				throw new IllegalArgumentException("target account required for deposit");
			break;

		case WITHDRAWAL:
			if (sourceAccountId == null || sourceAccountId.trim().isEmpty())
				throw new IllegalArgumentException("source account required for withdrawal");
			break;

		case TRANSFER_IN:
		case TRANSFER_OUT:
			if (sourceAccountId == null || targetAccountId == null || sourceAccountId.trim().isEmpty()
					|| targetAccountId.trim().isEmpty())
				throw new IllegalArgumentException("both accounts required for transfer");
			break;
		}

		this.transactionId = transactionId.trim();
		this.type = type;
		this.amount = amount;
		this.timestamp = timestamp;
		this.sourceAccountId = sourceAccountId;
		this.targetAccountId = targetAccountId;
		this.balanceAfter = balanceAfter;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public TransactionType getType() {
		return type;
	}

	public double getAmount() {
		return amount;
	}

	public Instant  getTimestamp() {
		return timestamp;
	}

	public String getSourceAccountId() {
		return sourceAccountId;
	}

	public String getTargetAccountId() {
		return targetAccountId;
	}

	public double getBalanceAfter() {
		return balanceAfter;
	}
	
}