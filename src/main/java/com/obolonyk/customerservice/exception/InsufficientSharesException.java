package com.obolonyk.customerservice.exception;

public class InsufficientSharesException extends RuntimeException{
    public static final String MESSAGE = "Customer [id=%d] does not have enough shares to complete transaction";

    public InsufficientSharesException(Integer customerId) {
        super(MESSAGE.formatted(customerId ));
    }
}
