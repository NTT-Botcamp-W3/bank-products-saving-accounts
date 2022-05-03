package com.bank.bootcamp.savingacounts.dto;

public enum AccountType {

  SAVING("savingAccounts"), 
  FIXED_TERM("fixedAccounts"), 
  CURRENT("currentAccount");
  
  private String resource;
  
  private AccountType(String resource) {
    this.resource = resource;
  }
  
  public String getResource() {
    return this.resource;
  }
}
