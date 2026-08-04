package com.app.returns.domain.exception;

public class RegistrableStockNotFoundException extends RuntimeException{

    public RegistrableStockNotFoundException(String message) {
        super(message);
    }
}
