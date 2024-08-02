package com.obolonyk.customerservice.service;

import com.obolonyk.customerservice.dto.StockTradeRequest;
import com.obolonyk.customerservice.dto.StockTradeResponse;
import com.obolonyk.customerservice.entity.Customer;
import com.obolonyk.customerservice.entity.PortfolioItem;
import com.obolonyk.customerservice.exception.ApplicationExceptionsFactory;
import com.obolonyk.customerservice.mapper.EntityDtoMapper;
import com.obolonyk.customerservice.repository.CustomerRepository;
import com.obolonyk.customerservice.repository.PortfolioItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class TradeService {

    private final CustomerRepository customerRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public TradeService(CustomerRepository customerRepository, PortfolioItemRepository portfolioItemRepository) {
        this.customerRepository = customerRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    @Transactional
    public Mono<StockTradeResponse> trade(Integer customerId, StockTradeRequest request) {
        return switch (request.action()) {
            case BUY -> buyStock(customerId, request);
            case SELL -> sell(customerId, request);
        };
    }

    private Mono<StockTradeResponse> sell(Integer customerId, StockTradeRequest request) {
        var customerMono = customerRepository.findById(customerId)
                .switchIfEmpty(ApplicationExceptionsFactory.notFound(customerId));

        var portfolioItemMono = portfolioItemRepository.findByCustomerIdAndTicker(customerId, request.ticker())
                .filter(p -> p.getQuantity() >= request.quantity())
                .switchIfEmpty(ApplicationExceptionsFactory.insufficientShares(customerId));


        return customerMono.zipWhen(customer -> portfolioItemMono)
                .flatMap(t -> executeSell(t.getT1(), t.getT2(), request));

    }

    private Mono<StockTradeResponse> buyStock(Integer customerId, StockTradeRequest request) {
        // find customer, if not -> exception
        var customerMono = customerRepository.findById(customerId)
                .switchIfEmpty(ApplicationExceptionsFactory.notFound(customerId))
                .filter(c -> c.getBalance() >= request.getTotalPrice())
                .switchIfEmpty(ApplicationExceptionsFactory.insufficientBalance(customerId));

        // check if customer has Ticker:
        // if not -> create new portfolioItemMono
        // if yes - get it
        var portfolioItemMono = portfolioItemRepository.findByCustomerIdAndTicker(customerId, request.ticker())
                .defaultIfEmpty(EntityDtoMapper.toPortfolioItem(customerId, request.ticker()));

        // firstly subscribe to customerMono if its gives customer
        // then subscribe to portfolioItemMono
        // to create a Tuple -> contains both
        // and do it in sequential manner, because we do not need to continue if we did not found customer
        return customerMono.zipWhen(customer -> portfolioItemMono)
                .flatMap(t -> executeBuy(t.getT1(), t.getT2(), request));
    }

    private Mono<StockTradeResponse> executeBuy(Customer customer, PortfolioItem portfolioItem, StockTradeRequest request) {
        customer.setBalance(customer.getBalance() - request.getTotalPrice());
        portfolioItem.setQuantity(portfolioItem.getQuantity() + request.quantity());
        return getStockTradeResponseMono(customer, portfolioItem, request);
    }

    private Mono<StockTradeResponse> executeSell(Customer customer, PortfolioItem portfolioItem, StockTradeRequest request) {
        customer.setBalance(customer.getBalance() + request.getTotalPrice());
        portfolioItem.setQuantity(portfolioItem.getQuantity() - request.quantity());
        return getStockTradeResponseMono(customer, portfolioItem, request);
    }

    private Mono<StockTradeResponse> getStockTradeResponseMono(Customer customer, PortfolioItem portfolioItem, StockTradeRequest request) {
        var response = EntityDtoMapper.toStockTradeResponse(request, customer.getId(), customer.getBalance());
        return Mono.zip(customerRepository.save(customer), portfolioItemRepository.save(portfolioItem))
                .thenReturn(response);
    }


}
