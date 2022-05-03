package com.bank.bootcamp.savingacounts.dto;

import lombok.Data;

@Data
public class CreateTransactionDTO {
  
  private String accountId;
  private String agent;
  private String description;
  private Double amount;
  private Boolean createByComission;
  
}
