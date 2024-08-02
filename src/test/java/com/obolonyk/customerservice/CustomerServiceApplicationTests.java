package com.obolonyk.customerservice;

import com.obolonyk.customerservice.domain.Ticker;
import com.obolonyk.customerservice.domain.TradeAction;
import com.obolonyk.customerservice.dto.StockTradeRequest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

@SpringBootTest
@AutoConfigureWebTestClient
class CustomerServiceApplicationTests {
    public static final Logger log = LoggerFactory.getLogger(CustomerServiceApplicationTests.class);

    @Autowired
    private WebTestClient client;

    @Test
    void getCustomerInfo() {
        getCustomer(1, HttpStatus.OK)
                .jsonPath("$.name").isEqualTo("Sam")
                .jsonPath("$.balance").isEqualTo("10000")
                .jsonPath("$.holdings").isEmpty();
    }

    @Test
    void buyAndSell() {
        //buy
        var buyReq = new StockTradeRequest(Ticker.GOOGLE, 100 , 5, TradeAction.BUY);
        trade(2, buyReq, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo("9500");
    }

    private WebTestClient.BodyContentSpec getCustomer(Integer id, HttpStatus expectedStatus){
        return client.get()
                .uri("/customers/{customerId}", id)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
                .consumeWith(e -> log.info("{}", new String(Objects.requireNonNull(e.getResponseBody()))));
    }

    private WebTestClient.BodyContentSpec trade (Integer id, StockTradeRequest request, HttpStatus expectedStatus){
        return client.post()
                .uri("/customers/{customerId}/trade", id)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
                .consumeWith(e -> log.info("{}", new String(Objects.requireNonNull(e.getResponseBody()))));
    }

}
