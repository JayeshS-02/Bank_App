package com.bank.models;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

public class TransactionTest {

	// NEGATIVE
	@Test
	void shouldRejectNullTransactionId() {
		assertThrows(IllegalArgumentException.class,
				() -> new Transaction(null, TransactionType.DEPOSIT, 100, Instant.now(), null, "A1", 100.0));
	}

	// NEGATIVE
	@Test
	void shouldRejectNullType() {
		assertThrows(IllegalArgumentException.class,
				() -> new Transaction("T1", null, 100, Instant.now(), null, "A1", 100.0));
	}

	// NEGATIVE
	@Test
	void shouldRejectZeroOrNegativeAmount() {
		assertThrows(IllegalArgumentException.class,
				() -> new Transaction("T1", TransactionType.DEPOSIT, 0, Instant.now(), null, "A1", 100.0));
		assertThrows(IllegalArgumentException.class,
				() -> new Transaction("T2", TransactionType.DEPOSIT, -50, Instant.now(), null, "A1", 100.0));
	}

	// NEGATIVE
	@Test
	void shouldRejectNullTimestamp() {
		assertThrows(IllegalArgumentException.class,
				() -> new Transaction("T1", TransactionType.DEPOSIT, 100, null, null, "A1", 100.0));
	}

	// NEGATIVE
	@Test
	void transferMustHaveBothAccounts() {
		assertThrows(IllegalArgumentException.class,
				() -> new Transaction("T3", TransactionType.TRANSFER_IN, 100, Instant.now(), null, "A2", 200.0));
		assertThrows(IllegalArgumentException.class,
				() -> new Transaction("T4", TransactionType.TRANSFER_OUT, 100, Instant.now(), "A1", null, 200.0));
	}

	// POSITIVE
	@Test
	void depositHasTargetAndNullSource() {
		Transaction t = new Transaction("T2", TransactionType.DEPOSIT, 150, Instant.now(), null, "A2", 250.0);
		assertEquals("A2", t.getTargetAccountId());
		assertNull(t.getSourceAccountId());
		assertEquals(150, t.getAmount());
	}

	// POSITIVE
	@Test
	void withdrawalHasSourceAndNullTarget() {
		Transaction t = new Transaction("T3", TransactionType.WITHDRAWAL, 75, Instant.now(), "A1", null, 225.0);
		assertEquals("A1", t.getSourceAccountId());
		assertNull(t.getTargetAccountId());
		assertEquals(75, t.getAmount());
	}

	// POSITIVE
	@Test
	void transferInHasBothAccounts() {
		Transaction t = new Transaction("T4", TransactionType.TRANSFER_IN, 200, Instant.now(), "A1", "A2", 500.0);
		assertEquals("A1", t.getSourceAccountId());
		assertEquals("A2", t.getTargetAccountId());
	}

	// POSITIVE
	@Test
	void transferOutHasBothAccounts() {
		Transaction t = new Transaction("T5", TransactionType.TRANSFER_OUT, 300, Instant.now(), "A2", "A3", 700.0);
		assertEquals("A2", t.getSourceAccountId());
		assertEquals("A3", t.getTargetAccountId());
	}

	// POSITIVE
	@Test
	void gettersReturnValues() {
		Instant ts = Instant.now();
		Transaction t = new Transaction("T6", TransactionType.DEPOSIT, 50, ts, null, "A9", 150.0);
		assertEquals("T6", t.getTransactionId());
		assertEquals(TransactionType.DEPOSIT, t.getType());
		assertEquals(50, t.getAmount());
		assertEquals(ts, t.getTimestamp());
		assertNull(t.getSourceAccountId());
		assertEquals("A9", t.getTargetAccountId());
		assertEquals(150.0, t.getBalanceAfter());
	}
}