package com.bank.bootcamp.savingacounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class SavingAcountsApplication {

  public static void main(String[] args) {
    System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
    SpringApplication.run(SavingAcountsApplication.class, args);
  }

}
