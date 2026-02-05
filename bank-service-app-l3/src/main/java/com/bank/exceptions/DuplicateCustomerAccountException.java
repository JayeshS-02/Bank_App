package com.bank.exceptions;

public class DuplicateCustomerAccountException extends RuntimeException {

    public DuplicateCustomerAccountException(String message) {
        super(message);
    }
}