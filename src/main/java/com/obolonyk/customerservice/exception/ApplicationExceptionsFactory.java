package com.obolonyk.customerservice.exception;

import reactor.core.publisher.Mono;

public class ApplicationExceptionsFactory {
    public static <T> Mono<T> notFound(Integer id){
        return Mono.error(new CustomerNotFoundException(id));
    }
    public static <T> Mono<T> insufficientBalance(Integer id){
        return Mono.error(new InsufficientBalanceException(id));
    }
    public static <T> Mono<T> insufficientShares(Integer id){
        return Mono.error(new InsufficientSharesException(id));
    }
}
