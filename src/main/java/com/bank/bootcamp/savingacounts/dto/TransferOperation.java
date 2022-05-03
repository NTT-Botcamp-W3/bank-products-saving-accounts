package com.bank.bootcamp.savingacounts.dto;

import lombok.Data;

@Data
public class TransferOperation {

  private String sourceTransactionId;
  private String targetTransactionId;
}
