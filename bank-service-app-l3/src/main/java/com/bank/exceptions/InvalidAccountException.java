package com.bank.exceptions;

public class InvalidAccountException extends RuntimeException{
    public InvalidAccountException(String msg) {
        super(msg);
    }
}