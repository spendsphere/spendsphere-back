package ru.nsu.spendsphere.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.spendsphere.exceptions.BadRequestException;
import ru.nsu.spendsphere.models.dto.TransactionCreateDTO;
import ru.nsu.spendsphere.models.dto.TransactionUpdateDTO;
import ru.nsu.spendsphere.models.entities.Account;
import ru.nsu.spendsphere.models.entities.AccountType;
import ru.nsu.spendsphere.models.entities.TransactionType;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.repositories.AccountRepository;
import ru.nsu.spendsphere.repositories.UserRepository;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class TransactionServiceIntegrationTest {

  @Autowired private TransactionService transactionService;
  @Autowired private UserRepository userRepository;
  @Autowired private AccountRepository accountRepository;

  // ---------- CREATE ----------

  @Test
  void createIncomeUpdatesBalance() {
    User user = createUser();
    Account account = createAccount(user, "Основная карта", new BigDecimal("1000.00"));

    transactionService.createTransaction(
        user.getId(),
        new TransactionCreateDTO(
            TransactionType.INCOME,
            null,
            account.getId(),
            null,
            new BigDecimal("200.00"),
            "Пополнение",
            LocalDate.now()));

    Account reloaded = accountRepository.findById(account.getId()).orElseThrow();
    assertEquals(new BigDecimal("1200.00"), reloaded.getBalance());
  }

  @Test
  void createExpenseUpdatesBalance() {
    User user = createUser();
    Account account = createAccount(user, "Основная карта", new BigDecimal("1000.00"));

    transactionService.createTransaction(
        user.getId(),
        new TransactionCreateDTO(
            TransactionType.EXPENSE,
            null,
            account.getId(),
            null,
            new BigDecimal("200.00"),
            "Покупка",
            LocalDate.now()));

    Account reloaded = accountRepository.findById(account.getId()).orElseThrow();
    assertEquals(new BigDecimal("800.00"), reloaded.getBalance());
  }

  @Test
  void createTransferUpdatesBothBalances() {
    User user = createUser();
    Account source = createAccount(user, "Дебетовая", new BigDecimal("1000.00"));
    Account target = createAccount(user, "Сбережения", new BigDecimal("300.00"));

    transactionService.createTransaction(
        user.getId(),
        new TransactionCreateDTO(
            TransactionType.TRANSFER,
            null,
            source.getId(),
            target.getId(),
            new BigDecimal("250.00"),
            "Перевод",
            LocalDate.now()));

    Account reloadedSource = accountRepository.findById(source.getId()).orElseThrow();
    Account reloadedTarget = accountRepository.findById(target.getId()).orElseThrow();
    assertEquals(new BigDecimal("750.00"), reloadedSource.getBalance());
    assertEquals(new BigDecimal("550.00"), reloadedTarget.getBalance());
  }

  @Test
  void createExpenseInsufficientFundsThrowsAndNoChange() {
    User user = createUser();
    Account account = createAccount(user, "Основная карта", new BigDecimal("100.00"));

    assertThrows(
        BadRequestException.class,
        () ->
            transactionService.createTransaction(
                user.getId(),
                new TransactionCreateDTO(
                    TransactionType.EXPENSE,
                    null,
                    account.getId(),
                    null,
                    new BigDecimal("200.00"),
                    "Покупка",
                    LocalDate.now())));

    Account reloaded = accountRepository.findById(account.getId()).orElseThrow();
    assertEquals(new BigDecimal("100.00"), reloaded.getBalance());
  }

  // ---------- UPDATE ----------

  @Test
  void updateExpenseChangeAmountAppliesDeltaCorrectly() {
    User user = createUser();
    Account account = createAccount(user, "Основная карта", new BigDecimal("1000.00"));

    var created =
        transactionService.createTransaction(
            user.getId(),
            new TransactionCreateDTO(
                TransactionType.EXPENSE,
                null,
                account.getId(),
                null,
                new BigDecimal("200.00"),
                "Покупка",
                LocalDate.now()));

    // Было 1000 -> после создания 800. После обновления на 300: 800 + 200 (revert) - 300 = 700
    transactionService.updateTransaction(
        created.id(),
        user.getId(),
        new TransactionUpdateDTO(
            TransactionType.EXPENSE,
            null,
            account.getId(),
            null,
            new BigDecimal("300.00"),
            null,
            null));

    Account reloaded = accountRepository.findById(account.getId()).orElseThrow();
    assertEquals(new BigDecimal("700.00"), reloaded.getBalance());
  }

  @Test
  void updateExpenseChangeAccountMovesEffectBetweenAccounts() {
    User user = createUser();
    Account a = createAccount(user, "A", new BigDecimal("1000.00"));
    Account b = createAccount(user, "B", new BigDecimal("500.00"));

    var created =
        transactionService.createTransaction(
            user.getId(),
            new TransactionCreateDTO(
                TransactionType.EXPENSE,
                null,
                a.getId(),
                null,
                new BigDecimal("100.00"),
                null,
                LocalDate.now()));

    // A: 1000 -> 900
    transactionService.updateTransaction(
        created.id(),
        user.getId(),
        new TransactionUpdateDTO(
            TransactionType.EXPENSE, null, b.getId(), null, new BigDecimal("100.00"), null, null));

    // Revert from A (+100): A=1000; apply to B (-100): B=400
    Account reloadedA = accountRepository.findById(a.getId()).orElseThrow();
    Account reloadedB = accountRepository.findById(b.getId()).orElseThrow();
    assertEquals(new BigDecimal("1000.00"), reloadedA.getBalance());
    assertEquals(new BigDecimal("400.00"), reloadedB.getBalance());
  }

  @Test
  void updateToTransferAppliesTransferEffect() {
    User user = createUser();
    Account a = createAccount(user, "A", new BigDecimal("1000.00"));
    Account b = createAccount(user, "B", new BigDecimal("500.00"));

    var created =
        transactionService.createTransaction(
            user.getId(),
            new TransactionCreateDTO(
                TransactionType.EXPENSE,
                null,
                a.getId(),
                null,
                new BigDecimal("100.00"),
                null,
                LocalDate.now()));

    // Update type -> TRANSFER to B with same amount
    transactionService.updateTransaction(
        created.id(),
        user.getId(),
        new TransactionUpdateDTO(
            TransactionType.TRANSFER,
            null,
            a.getId(),
            b.getId(),
            new BigDecimal("100.00"),
            null,
            null));

    // Revert expense on A: +100 => 1000; Apply transfer: A -100 => 900; B +100 => 600
    Account reloadedA = accountRepository.findById(a.getId()).orElseThrow();
    Account reloadedB = accountRepository.findById(b.getId()).orElseThrow();
    assertEquals(new BigDecimal("900.00"), reloadedA.getBalance());
    assertEquals(new BigDecimal("600.00"), reloadedB.getBalance());
  }

  // ---------- DELETE ----------

  @Test
  void deleteExpenseRevertsBalance() {
    User user = createUser();
    Account account = createAccount(user, "Основная карта", new BigDecimal("1000.00"));

    var created =
        transactionService.createTransaction(
            user.getId(),
            new TransactionCreateDTO(
                TransactionType.EXPENSE,
                null,
                account.getId(),
                null,
                new BigDecimal("200.00"),
                null,
                LocalDate.now()));

    transactionService.deleteTransaction(created.id(), user.getId());

    Account reloaded = accountRepository.findById(account.getId()).orElseThrow();
    assertEquals(new BigDecimal("1000.00"), reloaded.getBalance());
  }

  @Test
  void deleteTransferRevertsBoth() {
    User user = createUser();
    Account a = createAccount(user, "A", new BigDecimal("1000.00"));
    Account b = createAccount(user, "B", new BigDecimal("300.00"));

    var created =
        transactionService.createTransaction(
            user.getId(),
            new TransactionCreateDTO(
                TransactionType.TRANSFER,
                null,
                a.getId(),
                b.getId(),
                new BigDecimal("250.00"),
                null,
                LocalDate.now()));

    transactionService.deleteTransaction(created.id(), user.getId());

    Account reloadedA = accountRepository.findById(a.getId()).orElseThrow();
    Account reloadedB = accountRepository.findById(b.getId()).orElseThrow();
    assertEquals(new BigDecimal("1000.00"), reloadedA.getBalance());
    assertEquals(new BigDecimal("300.00"), reloadedB.getBalance());
  }

  private User createUser() {
    User user =
        User.builder()
            .email("test@example.com")
            .password("pass")
            .name("Test")
            .surname("User")
            .build();
    return userRepository.save(user);
  }

  private Account createAccount(User user, String name, BigDecimal balance) {
    Account account =
        Account.builder()
            .user(user)
            .accountType(AccountType.CARD)
            .name(name)
            .balance(balance)
            .build();
    return accountRepository.save(account);
  }
}
