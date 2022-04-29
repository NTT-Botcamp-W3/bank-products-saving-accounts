package com.bank.bootcamp.savingacounts.dto;

import lombok.Data;

@Data
public class CreateAccountDTO {

  private String customerId;
  private Integer monthlyMovementLimit;
  private Double openingAmount;
  private String profile;
}
