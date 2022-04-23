package com.bank.bootcamp.savingacounts.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.bank.bootcamp.savingacounts.entity.Account;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

  Mono<Account> findByCustomerId(String customerId);

}
