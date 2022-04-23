package com.bank.bootcamp.savingacounts.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document("Transactions")
@Data
public class Transaction {

  @Id
  private String id;
  private Integer operationNumber;
  private LocalDateTime registerDate;
  private String accountId;
  private String agent;
  private String description;
  private Double amount;
  
}