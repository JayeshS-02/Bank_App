package com.bank.exceptions;

public class NegativeOrZeroAmountException extends RuntimeException {

    public NegativeOrZeroAmountException(String message) {
        super(message);
    }
}
