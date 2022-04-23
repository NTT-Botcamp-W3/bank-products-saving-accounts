package com.bank.bootcamp.savingacounts.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Document(collection = "AccountSequences")
@Data
@EqualsAndHashCode(callSuper = false)
public class AccountSequences extends Sequence {
  @Id
  private String id;

}
