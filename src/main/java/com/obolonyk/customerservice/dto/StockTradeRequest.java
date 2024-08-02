package com.obolonyk.customerservice.dto;

import com.obolonyk.customerservice.domain.Ticker;
import com.obolonyk.customerservice.domain.TradeAction;

public record StockTradeRequest(Ticker ticker,
                                Integer price,
                                Integer quantity,
                                TradeAction action
) {
    public Integer getTotalPrice(){
        return price*quantity;
    }
}
