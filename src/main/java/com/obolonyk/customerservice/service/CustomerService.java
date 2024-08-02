package com.obolonyk.customerservice.service;

import com.obolonyk.customerservice.dto.CustomerInformation;
import com.obolonyk.customerservice.entity.Customer;
import com.obolonyk.customerservice.exception.ApplicationExceptionsFactory;
import com.obolonyk.customerservice.mapper.EntityDtoMapper;
import com.obolonyk.customerservice.repository.CustomerRepository;
import com.obolonyk.customerservice.repository.PortfolioItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public CustomerService(CustomerRepository customerRepository, PortfolioItemRepository portfolioItemRepository) {
        this.customerRepository = customerRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }


    public Mono<CustomerInformation> getCustomerInformation(Integer id) {
        return this.customerRepository.findById(id)
                .switchIfEmpty(ApplicationExceptionsFactory.notFound(id))
                .flatMap(this::buildCustomerInformation);
    }

    private Mono<CustomerInformation> buildCustomerInformation(Customer customer) {
        return this.portfolioItemRepository.findAllByCustomerId(customer.getId())
                .collectList()
                .map(list -> EntityDtoMapper.toCustomerInformation(customer, list));
    }


    public Flux<Customer> getAll() {
        return customerRepository.findAll();
    }

    public Mono<Customer> getOne(Integer id) {
        return customerRepository.findById(id);
    }
}
