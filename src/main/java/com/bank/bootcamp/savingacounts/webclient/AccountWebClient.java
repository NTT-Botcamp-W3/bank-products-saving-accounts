package com.bank.bootcamp.savingacounts.webclient;

import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.bank.bootcamp.savingacounts.dto.AccountType;
import com.bank.bootcamp.savingacounts.dto.CreateTransactionDTO;
import reactor.core.publisher.Mono;

@Service
public class AccountWebClient {
  private final ReactiveCircuitBreaker reactiveCircuitBreaker;
  private WebClient webClient;
  
  public AccountWebClient(ReactiveResilience4JCircuitBreakerFactory reactiveCircuitBreakerFactory, Environment env) {
    this.reactiveCircuitBreaker = reactiveCircuitBreakerFactory.create("products");
    webClient = WebClient.create(env.getProperty("gateway.url"));
  }
  
  public Mono<Integer> createTransaction(AccountType accountType, CreateTransactionDTO dto) {

    return webClient.post()
        .uri(String.format("/%s/transaction", accountType.getResource()))
        .bodyValue(dto).retrieve().bodyToMono(Integer.class)
        .transform(balance -> reactiveCircuitBreaker.run(balance, throwable -> Mono.empty()));
  }
}
