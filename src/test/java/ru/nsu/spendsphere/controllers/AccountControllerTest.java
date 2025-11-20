package ru.nsu.spendsphere.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.AccountBalanceDTO;
import ru.nsu.spendsphere.models.dto.AccountCreateDTO;
import ru.nsu.spendsphere.models.dto.AccountDTO;
import ru.nsu.spendsphere.models.dto.AccountUpdateDTO;
import ru.nsu.spendsphere.models.entities.AccountType;
import ru.nsu.spendsphere.models.entities.Currency;
import ru.nsu.spendsphere.services.AccountService;

/** Юнит-тесты для {@link AccountController}. */
@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AccountService accountService;

  @Autowired private ObjectMapper objectMapper;

  /**
   * Тест успешного получения всех счетов пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getUserAccountsSuccess() throws Exception {
    Long userId = 1L;
    List<AccountDTO> expectedAccounts =
        Arrays.asList(
            new AccountDTO(
                1L,
                userId,
                AccountType.CARD,
                new BigDecimal("1000.00"),
                Currency.RUB,
                "Основная карта",
                "http://example.com/icon1.png",
                BigDecimal.ZERO,
                true,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()),
            new AccountDTO(
                2L,
                userId,
                AccountType.CASH,
                new BigDecimal("500.00"),
                Currency.RUB,
                "Наличные",
                "http://example.com/icon2.png",
                BigDecimal.ZERO,
                true,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()));

    when(accountService.getUserAccounts(userId)).thenReturn(expectedAccounts);

    mockMvc
        .perform(get("/api/v1/users/{userId}/accounts", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].name").value("Основная карта"))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].name").value("Наличные"));
  }

  /**
   * Тест получения счетов для несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getUserAccountsUserNotFound() throws Exception {
    Long userId = 999L;
    when(accountService.getUserAccounts(userId))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc
        .perform(get("/api/v1/users/{userId}/accounts", userId))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного получения счета по идентификатору.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getAccountSuccess() throws Exception {
    Long userId = 1L;
    Long accountId = 1L;
    AccountDTO expectedAccount =
        new AccountDTO(
            accountId,
            userId,
            AccountType.CARD,
            new BigDecimal("1000.00"),
            Currency.RUB,
            "Основная карта",
            "http://example.com/icon.png",
            new BigDecimal("50000.00"),
            true,
            true,
            LocalDateTime.now(),
            LocalDateTime.now());

    when(accountService.getAccountById(accountId, userId)).thenReturn(expectedAccount);

    mockMvc
        .perform(get("/api/v1/users/{userId}/accounts/{accountId}", userId, accountId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(accountId))
        .andExpect(jsonPath("$.userId").value(userId))
        .andExpect(jsonPath("$.accountType").value("CARD"))
        .andExpect(jsonPath("$.balance").value(1000.00))
        .andExpect(jsonPath("$.currency").value("RUB"))
        .andExpect(jsonPath("$.name").value("Основная карта"))
        .andExpect(jsonPath("$.creditLimit").value(50000.00))
        .andExpect(jsonPath("$.isActive").value(true))
        .andExpect(jsonPath("$.includeInTotal").value(true));
  }

  /**
   * Тест получения несуществующего счета.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getAccountNotFound() throws Exception {
    Long userId = 1L;
    Long accountId = 999L;
    when(accountService.getAccountById(accountId, userId))
        .thenThrow(
            new ResourceNotFoundException(
                "Account with id " + accountId + " not found for user " + userId));

    mockMvc
        .perform(get("/api/v1/users/{userId}/accounts/{accountId}", userId, accountId))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного создания счета.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void createAccountSuccess() throws Exception {
    Long userId = 1L;
    AccountCreateDTO createDTO =
        new AccountCreateDTO(
            AccountType.CARD,
            new BigDecimal("1000.00"),
            Currency.RUB,
            "Основная карта",
            "http://example.com/icon.png",
            BigDecimal.ZERO,
            true,
            true);

    AccountDTO expectedAccount =
        new AccountDTO(
            1L,
            userId,
            AccountType.CARD,
            new BigDecimal("1000.00"),
            Currency.RUB,
            "Основная карта",
            "http://example.com/icon.png",
            BigDecimal.ZERO,
            true,
            true,
            LocalDateTime.now(),
            LocalDateTime.now());

    when(accountService.createAccount(eq(userId), any(AccountCreateDTO.class)))
        .thenReturn(expectedAccount);

    mockMvc
        .perform(
            post("/api/v1/users/{userId}/accounts", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.userId").value(userId))
        .andExpect(jsonPath("$.accountType").value("CARD"))
        .andExpect(jsonPath("$.name").value("Основная карта"))
        .andExpect(jsonPath("$.balance").value(1000.00));
  }

  /**
   * Тест создания счета для несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void createAccountUserNotFound() throws Exception {
    Long userId = 999L;
    AccountCreateDTO createDTO =
        new AccountCreateDTO(
            AccountType.CARD,
            BigDecimal.ZERO,
            Currency.RUB,
            "Тестовая карта",
            null,
            null,
            true,
            true);

    when(accountService.createAccount(eq(userId), any(AccountCreateDTO.class)))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc
        .perform(
            post("/api/v1/users/{userId}/accounts", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного обновления счета.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void updateAccountSuccess() throws Exception {
    Long userId = 1L;
    Long accountId = 1L;
    AccountUpdateDTO updateDTO =
        new AccountUpdateDTO(
            AccountType.SAVINGS,
            new BigDecimal("2000.00"),
            Currency.USD,
            "Сберегательный счет",
            "http://example.com/new-icon.png",
            BigDecimal.ZERO,
            false,
            true);

    AccountDTO expectedAccount =
        new AccountDTO(
            accountId,
            userId,
            AccountType.SAVINGS,
            new BigDecimal("2000.00"),
            Currency.USD,
            "Сберегательный счет",
            "http://example.com/new-icon.png",
            BigDecimal.ZERO,
            false,
            true,
            LocalDateTime.now(),
            LocalDateTime.now());

    when(accountService.updateAccount(eq(accountId), eq(userId), any(AccountUpdateDTO.class)))
        .thenReturn(expectedAccount);

    mockMvc
        .perform(
            put("/api/v1/users/{userId}/accounts/{accountId}", userId, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(accountId))
        .andExpect(jsonPath("$.accountType").value("SAVINGS"))
        .andExpect(jsonPath("$.balance").value(2000.00))
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.name").value("Сберегательный счет"))
        .andExpect(jsonPath("$.isActive").value(false));
  }

  /**
   * Тест обновления несуществующего счета.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void updateAccountNotFound() throws Exception {
    Long userId = 1L;
    Long accountId = 999L;
    AccountUpdateDTO updateDTO =
        new AccountUpdateDTO(
            AccountType.CARD, BigDecimal.ZERO, Currency.RUB, "Тест", null, null, true, true);

    when(accountService.updateAccount(eq(accountId), eq(userId), any(AccountUpdateDTO.class)))
        .thenThrow(
            new ResourceNotFoundException(
                "Account with id " + accountId + " not found for user " + userId));

    mockMvc
        .perform(
            put("/api/v1/users/{userId}/accounts/{accountId}", userId, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного удаления счета.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void deleteAccountSuccess() throws Exception {
    Long userId = 1L;
    Long accountId = 1L;

    doNothing().when(accountService).deleteAccount(accountId, userId);

    mockMvc
        .perform(delete("/api/v1/users/{userId}/accounts/{accountId}", userId, accountId))
        .andExpect(status().isNoContent());
  }

  /**
   * Тест удаления несуществующего счета.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void deleteAccountNotFound() throws Exception {
    Long userId = 1L;
    Long accountId = 999L;

    doThrow(
            new ResourceNotFoundException(
                "Account with id " + accountId + " not found for user " + userId))
        .when(accountService)
        .deleteAccount(accountId, userId);

    mockMvc
        .perform(delete("/api/v1/users/{userId}/accounts/{accountId}", userId, accountId))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного получения баланса по активным счетам.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getUserAccountsBalanceSuccess() throws Exception {
    Long userId = 1L;
    Map<Currency, BigDecimal> balances = new HashMap<>();
    balances.put(Currency.RUB, new BigDecimal("5000.00"));
    balances.put(Currency.USD, new BigDecimal("1000.00"));

    AccountBalanceDTO expectedBalance = new AccountBalanceDTO(3, balances);

    when(accountService.getUserAccountsBalance(userId)).thenReturn(expectedBalance);

    mockMvc
        .perform(get("/api/v1/users/{userId}/accounts/balance", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalAccounts").value(3))
        .andExpect(jsonPath("$.balancesByCurrency.RUB").value(5000.00))
        .andExpect(jsonPath("$.balancesByCurrency.USD").value(1000.00));
  }

  /**
   * Тест получения баланса для несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getUserAccountsBalanceUserNotFound() throws Exception {
    Long userId = 999L;
    when(accountService.getUserAccountsBalance(userId))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc
        .perform(get("/api/v1/users/{userId}/accounts/balance", userId))
        .andExpect(status().isNotFound());
  }
}
