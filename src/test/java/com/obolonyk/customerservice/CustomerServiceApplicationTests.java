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
        var buyReq = new StockTradeRequest(Ticker.GOOGLE, 100, 5, TradeAction.BUY);
        trade(2, buyReq, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo("9500")
                .jsonPath("$.totalPrice").isEqualTo("500");
    }

    @Test
    void buyAndSellFlow() {
        //buy one
        var buyReq1 = new StockTradeRequest(Ticker.GOOGLE, 100, 5, TradeAction.BUY);
        trade(2, buyReq1, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo("9500")
                .jsonPath("$.totalPrice").isEqualTo("500");
        //buy second
        var buyReq2 = new StockTradeRequest(Ticker.GOOGLE, 100, 10, TradeAction.BUY);
        trade(2, buyReq2, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo("8500")
                .jsonPath("$.totalPrice").isEqualTo("1000");

        //get info
        getCustomer(2, HttpStatus.OK)
                .jsonPath("$.holdings").isNotEmpty()
                .jsonPath("$.holdings.length()").isEqualTo(1)
                .jsonPath("$.holdings[0].ticker").isEqualTo("GOOGLE")
                .jsonPath("$.holdings[0].quantity").isEqualTo(15);

        //sell
        var req3 = new StockTradeRequest(Ticker.GOOGLE, 100, 10, TradeAction.SELL);
        trade(2, req3, HttpStatus.OK)
                .jsonPath("$.balance").isEqualTo("9500")
                .jsonPath("$.totalPrice").isEqualTo("1000");

        //get info
        getCustomer(2, HttpStatus.OK)
                .jsonPath("$.holdings").isNotEmpty()
                .jsonPath("$.holdings.length()").isEqualTo(1)
                .jsonPath("$.holdings[0].ticker").isEqualTo("GOOGLE")
                .jsonPath("$.holdings[0].quantity").isEqualTo(5);
    }

    @Test
    void customerNotFound() {
        getCustomer(100, HttpStatus.NOT_FOUND)
                .jsonPath("$.detail").isEqualTo("Customer with ID 100 not found")
                .jsonPath("$.type").isEqualTo("http://example.com/customer-not-found")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.instance").isEqualTo("/customers/100")
                .jsonPath("$.title").isEqualTo("Customer not found");

        var req3 = new StockTradeRequest(Ticker.GOOGLE, 100, 10, TradeAction.SELL);
        trade(100, req3, HttpStatus.NOT_FOUND)
                .jsonPath("$.detail").isEqualTo("Customer with ID 100 not found")
                .jsonPath("$.type").isEqualTo("http://example.com/customer-not-found")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.instance").isEqualTo("/customers/100/trade")
                .jsonPath("$.title").isEqualTo("Customer not found");
    }

    @Test
    void insufficientBalance() {
        var req = new StockTradeRequest(Ticker.GOOGLE, 100, 10_000, TradeAction.BUY);
        trade(1, req, HttpStatus.BAD_REQUEST)
                .jsonPath("$.detail").isEqualTo("Customer with ID 1 does not have enough funds to complete transaction")
                .jsonPath("$.type").isEqualTo("http://example.com/insufficient-balance")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.instance").isEqualTo("/customers/1/trade")
                .jsonPath("$.title").isEqualTo("Insufficient balance");
    }

    @Test
    void insufficientShares() {
        var req = new StockTradeRequest(Ticker.GOOGLE, 100, 10_000, TradeAction.SELL);
        trade(1, req, HttpStatus.BAD_REQUEST)
                .jsonPath("$.detail").isEqualTo("Customer [id=1] does not have enough shares to complete transaction")
                .jsonPath("$.type").isEqualTo("http://example.com/insufficient-shares")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.instance").isEqualTo("/customers/1/trade")
                .jsonPath("$.title").isEqualTo("Insufficient shares");
    }


    private WebTestClient.BodyContentSpec getCustomer(Integer id, HttpStatus expectedStatus) {
        return client.get()
                .uri("/customers/{customerId}", id)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
                .consumeWith(e -> log.info("{}", new String(Objects.requireNonNull(e.getResponseBody()))));
    }

    private WebTestClient.BodyContentSpec trade(Integer id, StockTradeRequest request, HttpStatus expectedStatus) {
        return client.post()
                .uri("/customers/{customerId}/trade", id)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
                .consumeWith(e -> log.info("{}", new String(Objects.requireNonNull(e.getResponseBody()))));
    }


}
