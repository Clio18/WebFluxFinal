package com.obolonyk.customerservice.advice;

import com.obolonyk.customerservice.exception.CustomerNotFoundException;
import com.obolonyk.customerservice.exception.InsufficientBalanceException;
import com.obolonyk.customerservice.exception.InsufficientSharesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.util.function.Consumer;

@ControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleException(CustomerNotFoundException exception){
        return problemDetailBuilder(HttpStatus.NOT_FOUND, exception, problem -> {
            problem.setType(URI.create("http://example.com/customer-not-found"));
            problem.setTitle("Customer not found");
        } );
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ProblemDetail handleException(InsufficientBalanceException exception){
        return problemDetailBuilder(HttpStatus.BAD_REQUEST, exception, problem -> {
            problem.setType(URI.create("http://example.com/insufficient-balance"));
            problem.setTitle("Insufficient balance");
        } );
    }

    @ExceptionHandler(InsufficientSharesException.class)
    public ProblemDetail handleException(InsufficientSharesException exception){
        return problemDetailBuilder(HttpStatus.BAD_REQUEST, exception, problem -> {
            problem.setType(URI.create("http://example.com/insufficient-shares"));
            problem.setTitle("Insufficient shares");
        } );
    }

    private ProblemDetail problemDetailBuilder(HttpStatus status, Exception ex, Consumer<ProblemDetail> consumer){
        var problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        consumer.accept(problemDetail);
        return problemDetail;
    }
}
