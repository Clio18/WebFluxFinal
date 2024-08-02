package com.obolonyk.customerservice.controller;


import com.obolonyk.customerservice.dto.CustomerInformation;
import com.obolonyk.customerservice.dto.StockTradeRequest;
import com.obolonyk.customerservice.dto.StockTradeResponse;
import com.obolonyk.customerservice.entity.Customer;
import com.obolonyk.customerservice.service.CustomerService;
import com.obolonyk.customerservice.service.TradeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("customers")
public class CustomerController {
    private final CustomerService customerService;
    private final TradeService tradeService;

    public CustomerController(CustomerService customerService, TradeService tradeService) {
        this.customerService = customerService;
        this.tradeService = tradeService;
    }

    @GetMapping("/{customerId}")
    public Mono<CustomerInformation> getCustomerInformation(@PathVariable Integer customerId){
        return customerService.getCustomerInformation(customerId);
    }

    @PostMapping("/{customerId}/trade")
    public Mono<StockTradeResponse> trade(@PathVariable Integer customerId, @RequestBody Mono<StockTradeRequest> mono){
        return mono.flatMap(req -> this.tradeService.trade(customerId, req));
    }


    // check
    @GetMapping
    public Flux<Customer> getAll(){
        return customerService.getAll();
    }

    @GetMapping("/one/{id}")
    public Mono<Customer> getOne(@PathVariable Integer id){
        return customerService.getOne(id);
    }
}
