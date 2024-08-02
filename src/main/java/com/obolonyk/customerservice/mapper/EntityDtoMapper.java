package com.obolonyk.customerservice.mapper;

import com.obolonyk.customerservice.domain.Ticker;
import com.obolonyk.customerservice.dto.CustomerInformation;
import com.obolonyk.customerservice.dto.Holding;
import com.obolonyk.customerservice.dto.StockTradeRequest;
import com.obolonyk.customerservice.dto.StockTradeResponse;
import com.obolonyk.customerservice.entity.Customer;
import com.obolonyk.customerservice.entity.PortfolioItem;

import java.util.List;
import java.util.stream.Collectors;

public class EntityDtoMapper {

    public static CustomerInformation toCustomerInformation(Customer customer, List<PortfolioItem> items) {
        List<Holding> holdings = items.stream()
                .map(i -> new Holding(i.getTicker(), i.getQuantity()))
                .collect(Collectors.toList());
        return new CustomerInformation(customer.getId(), customer.getName(), customer.getBalance(), holdings);
    }

    public static PortfolioItem toPortfolioItem(Integer customerId, Ticker ticker) {
        PortfolioItem portfolioItem = new PortfolioItem();
        portfolioItem.setCustomerId(customerId);
        portfolioItem.setTicker(ticker);
        portfolioItem.setQuantity(0);
        return portfolioItem;
    }

    public static StockTradeResponse toStockTradeResponse(StockTradeRequest request, Integer customerId, Integer balance) {
        return new StockTradeResponse(
                customerId,
                request.ticker(),
                request.price(),
                request.quantity(),
                request.action(),
                request.getTotalPrice(),
                balance
        );
    }
}
