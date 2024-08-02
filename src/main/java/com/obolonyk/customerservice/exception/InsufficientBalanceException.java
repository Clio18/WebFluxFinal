package com.obolonyk.customerservice.exception;

public class InsufficientBalanceException extends RuntimeException{
    public static final String MESSAGE = "Customer with ID %d does not have enough funds to complete transaction";

    public InsufficientBalanceException(Integer customerId) {
        super(MESSAGE.formatted(customerId ));
    }
}
