package com.bank.bootcamp.savingacounts.webclient;

import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import com.bank.bootcamp.savingacounts.dto.BalanceDTO;
import com.bank.bootcamp.savingacounts.exception.BankValidationException;
import reactor.core.publisher.Flux;

@Service
public class CreditWebClient {

  private final ReactiveCircuitBreaker reactiveCircuitBreaker;
  private WebClient webClient;
  
  
  public CreditWebClient(ReactiveResilience4JCircuitBreakerFactory reactiveCircuitBreakerFactory, Environment env) {
    this.reactiveCircuitBreaker = reactiveCircuitBreakerFactory.create("products");
    webClient = WebClient.create(env.getProperty("gateway.url"));
  }

  public Flux<BalanceDTO> getAllBalances(String customerId) {
    if (ObjectUtils.isEmpty(customerId)) {
      return Flux.error(new BankValidationException("Customer ID is required"));
    } else {
      
      var credits = webClient.get()
          .uri("/credits/balanceByCustomer/{customerId}/{creditType}", customerId, "PERSONAL")
          .retrieve()
          .bodyToFlux(BalanceDTO.class)
          .transform(balance -> reactiveCircuitBreaker.run(balance, throwable -> Flux.empty()));
      
      return Flux.merge(credits)
      .parallel()
      .sequential();
    }
  }
}
