package com.obolonyk.customerservice.dto;

import com.obolonyk.customerservice.domain.Ticker;

public record Holding(Ticker ticker,
                      Integer quantity
) {
}
