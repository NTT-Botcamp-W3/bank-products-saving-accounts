package com.bank.bootcamp.savingacounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import com.bank.bootcamp.savingacounts.dto.AccountType;
import com.bank.bootcamp.savingacounts.dto.BalanceDTO;
import com.bank.bootcamp.savingacounts.dto.CreateAccountDTO;
import com.bank.bootcamp.savingacounts.dto.CreateTransactionDTO;
import com.bank.bootcamp.savingacounts.dto.TransferDTO;
import com.bank.bootcamp.savingacounts.entity.Account;
import com.bank.bootcamp.savingacounts.entity.Transaction;
import com.bank.bootcamp.savingacounts.exception.BankValidationException;
import com.bank.bootcamp.savingacounts.repository.AccountRepository;
import com.bank.bootcamp.savingacounts.repository.TransactionRepository;
import com.bank.bootcamp.savingacounts.service.AccountService;
import com.bank.bootcamp.savingacounts.service.NextSequenceService;
import com.bank.bootcamp.savingacounts.webclient.AccountWebClient;
import com.bank.bootcamp.savingacounts.webclient.CreditWebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class SavingAcountsApplicationTests {

  private static AccountService accountService;
  private static AccountRepository accountRepository;
  private static TransactionRepository transactionRepository;
  private static NextSequenceService nextSequenceService;
  private ModelMapper mapper = new ModelMapper();
  private static Environment env;
  private static CreditWebClient creditWebClient;
  private static AccountWebClient accountWebClient;
  
  @BeforeAll
  public static void setup() {
    accountRepository = mock(AccountRepository.class);
    transactionRepository = mock(TransactionRepository.class);
    nextSequenceService = mock(NextSequenceService.class);
    env = mock(Environment.class);
    creditWebClient = mock(CreditWebClient.class);
    accountWebClient = mock(AccountWebClient.class);
    accountService = new AccountService(accountRepository, transactionRepository, nextSequenceService, env, creditWebClient, accountWebClient);
    when(nextSequenceService.getNextSequence("TransactionSequences")).thenReturn(Mono.just(1));
  }
  
  private Account getAccount() {
    var account = new Account();
    account.setCustomerId("id123456");
    account.setMonthlyMovementLimit(10);
    return account;
  }

  @Test
  public void createAccountWithAllData() throws Exception {
    
    var account = getAccount();
    var accountDTO = mapper.map(account, CreateAccountDTO.class);
    accountDTO.setOpeningAmount(100d);
    
    var savedAccount = mapper.map(account, Account.class);
    savedAccount.setId(UUID.randomUUID().toString());
    
    when(accountRepository.findByCustomerId(account.getCustomerId())).thenReturn(Mono.empty());
    when(accountRepository.save(Mockito.any(Account.class))).thenReturn(Mono.just(savedAccount));
    
    var mono = accountService.createAccount(accountDTO);
    StepVerifier.create(mono).assertNext(acc -> {
      assertThat(acc.getMonthlyMovementLimit()).isEqualTo(10);
      assertThat(acc.getId()).isNotNull();
    }).verifyComplete();
    
  }
  
  @Test
  public void createVIPAccount() throws Exception {
    
    var account = getAccount();
    var accountDTO = mapper.map(account, CreateAccountDTO.class);
    accountDTO.setOpeningAmount(100d);
    accountDTO.setProfile("VIP");
    
    var savedAccount = mapper.map(account, Account.class);
    savedAccount.setId(UUID.randomUUID().toString());
    
    when(accountRepository.findByCustomerId(account.getCustomerId())).thenReturn(Mono.empty());
    when(accountRepository.save(Mockito.any(Account.class))).thenReturn(Mono.just(savedAccount));
    when(creditWebClient.getAllBalances(account.getCustomerId())).thenReturn(Flux.just(new BalanceDTO()));
    
    var mono = accountService.createAccount(accountDTO);
    StepVerifier.create(mono).assertNext(acc -> {
      assertThat(acc.getMonthlyMovementLimit()).isEqualTo(10);
      assertThat(acc.getId()).isNotNull();
    }).verifyComplete();
  }
  
  @Test
  public void createVIPAccountFail() throws Exception {
    
    var account = getAccount();
    var accountDTO = mapper.map(account, CreateAccountDTO.class);
    accountDTO.setOpeningAmount(100d);
    accountDTO.setProfile("VIP");
    
    var savedAccount = mapper.map(account, Account.class);
    savedAccount.setId(UUID.randomUUID().toString());
    
    when(accountRepository.findByCustomerId(account.getCustomerId())).thenReturn(Mono.empty());
    when(accountRepository.save(Mockito.any(Account.class))).thenReturn(Mono.just(savedAccount));
    when(creditWebClient.getAllBalances(account.getCustomerId())).thenReturn(Flux.empty());
    
    var mono = accountService.createAccount(accountDTO);
    StepVerifier.create(mono).expectError(BankValidationException.class).verify();
  }
  
  @Test
  public void createPositiveTransactionWithExistentAccount() throws Exception {
    var accountId = "acc123";
    
    var account = new Account();
    account.setId(accountId);
    account.setMonthlyMovementLimit(1);
    
    var createTransactionDTO = new CreateTransactionDTO();
    createTransactionDTO.setAgent("BCP Huacho - Cajero 021");
    createTransactionDTO.setAmount(100d);
    createTransactionDTO.setAccountId(accountId);
    createTransactionDTO.setDescription("Deposito cajero");
    
    var transactionSaved = mapper.map(createTransactionDTO, Transaction.class);
    transactionSaved.setId(UUID.randomUUID().toString());
    transactionSaved.setOperationNumber(1);
    transactionSaved.setRegisterDate(LocalDateTime.now());
    
    when(nextSequenceService.getNextSequence("TransactionSequences")).thenReturn(Mono.just(1));
    when(transactionRepository.getBalanceByAccountId(accountId)).thenReturn(Mono.just(0d));
    when(accountRepository.findById(accountId)).thenReturn(Mono.just(account));
    Mockito.doReturn(Flux.empty()).when(transactionRepository).findByAccountIdAndRegisterDateBetween(Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class));
    Mockito.doReturn(Mono.just(transactionSaved)).when(transactionRepository).save(Mockito.any());
    
    var mono = accountService.createTransaction(createTransactionDTO);
    StepVerifier.create(mono).assertNext((saved) -> {
      assertThat(saved).isNotNull();
    }).verifyComplete();
  }
  
  @Test
  public void createImposibleTransactionWithExistentAccount() throws Exception {
    var accountId = "acc123";
    var createTransactionDTO = new CreateTransactionDTO();
    createTransactionDTO.setAgent("BCP Huacho - Cajero 021");
    createTransactionDTO.setAmount(-100d); // negative tx with balance 0
    createTransactionDTO.setAccountId(accountId);
    createTransactionDTO.setDescription("Deposito cajero");
    
    var transactionSaved = mapper.map(createTransactionDTO, Transaction.class);
    transactionSaved.setId(UUID.randomUUID().toString());
    transactionSaved.setOperationNumber(1);
    transactionSaved.setRegisterDate(LocalDateTime.now());
    
    when(nextSequenceService.getNextSequence("TransactionSequences")).thenReturn(Mono.just(1));
    when(transactionRepository.getBalanceByAccountId(accountId)).thenReturn(Mono.just(0d));
    when(accountRepository.findById(accountId)).thenReturn(Mono.just(new Account()));
    Mockito.doReturn(Mono.just(transactionSaved)).when(transactionRepository).save(Mockito.any());
    
    var mono = accountService.createTransaction(createTransactionDTO);
    StepVerifier.create(mono).expectError().verify();
  }
  
  @Test
  public void createPositiveTransactionWithInexistentAccount() throws Exception {
    var accountId = "acc123";
    var createTransactionDTO = new CreateTransactionDTO();
    createTransactionDTO.setAgent("BCP Huacho - Cajero 021");
    createTransactionDTO.setAmount(100d);
    createTransactionDTO.setAccountId(accountId);
    createTransactionDTO.setDescription("Deposito cajero");
    
    var transactionSaved = mapper.map(createTransactionDTO, Transaction.class);
    transactionSaved.setId(UUID.randomUUID().toString());
    transactionSaved.setOperationNumber(1);
    transactionSaved.setRegisterDate(LocalDateTime.now());
    
    when(nextSequenceService.getNextSequence("TransactionSequences")).thenReturn(Mono.just(1));
    when(transactionRepository.getBalanceByAccountId(accountId)).thenReturn(Mono.just(0d));
    when(accountRepository.findById(accountId)).thenReturn(Mono.empty()); // inexistent account
    Mockito.doReturn(Mono.just(transactionSaved)).when(transactionRepository).save(Mockito.any());
    
    var mono = accountService.createTransaction(createTransactionDTO);
    StepVerifier.create(mono).expectError().verify();
  }
  
  @Test
  public void getBalanceTest() {
    var accountId = "account_123";
    var account = new Account();
    account.setId(accountId);
    account.setMonthlyMovementLimit(5);
    
    when(transactionRepository.getBalanceByAccountId(accountId)).thenReturn(Mono.just(100d));
    when(accountRepository.findById(accountId)).thenReturn(Mono.just(account));
    var transaction = new Transaction();
    transaction.setAmount(100d);
    when(transactionRepository.findByAccountIdAndRegisterDateBetween(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Flux.just(transaction));
    var mono = accountService.getBalanceByAccountId(accountId);
    StepVerifier.create(mono).assertNext(balance -> {
      assertThat(balance.getAmount()).isEqualTo(100d);
    }).verifyComplete();
  }
  
  @Test
  public void getTransactionsByAccountAndPeriod() {
    
    String accountId = "ACC123";
    when(transactionRepository.findByAccountIdAndRegisterDateBetween(Mockito.any(), Mockito.any(), Mockito.any()))
      .thenReturn(Flux.just(new Transaction()));
    var flux = accountService.getTransactionsByAccountIdAndPeriod(accountId, LocalDate.of(2022, 4, 1));
    StepVerifier.create(flux).assertNext(tx -> {
      assertThat(tx).isNotNull();
    }).verifyComplete();
  }
  
  @Test
  public void transfer() {
    var transferDTO = new TransferDTO();
    transferDTO.setAmount(100d);
    transferDTO.setSourceAccountId("CA-001");
    transferDTO.setTargetAccountType(AccountType.SAVING);
    transferDTO.setTargetAccountId("SA-001");
    var amount = 100d;
    //  /transfer
    when(nextSequenceService.getNextSequence("TransactionSequences")).thenReturn(Mono.just(1));
    when(transactionRepository.getBalanceByAccountId(transferDTO.getSourceAccountId())).thenReturn(Mono.just(amount));
    
    var account = new Account();
    account.setMonthlyMovementLimit(10);
    when(accountRepository.findById(transferDTO.getSourceAccountId())).thenReturn(Mono.just(account));
    
    var existentTransaction = new Transaction();
    existentTransaction.setAmount(100d);
    
    when(transactionRepository.findByAccountIdAndRegisterDateBetween(Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class))).thenReturn(Flux.just(existentTransaction));
    
    var tx = new Transaction();
    tx.setAccountId(transferDTO.getSourceAccountId());
    tx.setAgent("-");
    tx.setOperationNumber(1);
    tx.setAmount(amount);
    tx.setId(UUID.randomUUID().toString());
    tx.setRegisterDate(LocalDateTime.now());
    
    when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(Mono.just(tx));
    when(accountWebClient.createTransaction(Mockito.any(AccountType.class), Mockito.any(CreateTransactionDTO.class))).thenReturn(Mono.just(4));
    var mono = accountService.transfer(transferDTO);
    StepVerifier.create(mono).assertNext(operationNumber -> {
      assertThat(operationNumber).isNotNull();
    }).verifyComplete();
  }
  

}
