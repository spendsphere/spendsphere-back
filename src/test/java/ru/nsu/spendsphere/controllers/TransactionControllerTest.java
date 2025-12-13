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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.nsu.spendsphere.exceptions.BadRequestException;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.TransactionCreateDTO;
import ru.nsu.spendsphere.models.dto.TransactionDTO;
import ru.nsu.spendsphere.models.dto.TransactionUpdateDTO;
import ru.nsu.spendsphere.models.entities.TransactionType;
import ru.nsu.spendsphere.services.TransactionImageService;
import ru.nsu.spendsphere.services.TransactionService;

/** Юнит-тесты для {@link TransactionController}. */
@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TransactionService transactionService;
  @MockitoBean private TransactionImageService transactionImageService;

  @Autowired private ObjectMapper objectMapper;

  /**
   * Тест успешного получения всех транзакций пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getAllTransactionsSuccess() throws Exception {
    Long userId = 1L;
    List<TransactionDTO> expectedTransactions =
        Arrays.asList(
            new TransactionDTO(
                1L,
                userId,
                TransactionType.EXPENSE,
                5L,
                "Продукты",
                "🍔",
                "#10b981",
                2L,
                "Основная карта",
                null,
                null,
                new BigDecimal("500.00"),
                "Покупка в магазине",
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDateTime.now()),
            new TransactionDTO(
                2L,
                userId,
                TransactionType.INCOME,
                10L,
                "Зарплата",
                "💰",
                "#3b82f6",
                2L,
                "Основная карта",
                null,
                null,
                new BigDecimal("50000.00"),
                "Зарплата за октябрь",
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDateTime.now()));

    when(transactionService.getAllTransactions(userId)).thenReturn(expectedTransactions);

    mockMvc
        .perform(get("/api/v1/users/{userId}/transactions", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].type").value("EXPENSE"))
        .andExpect(jsonPath("$[0].amount").value(500.00))
        .andExpect(jsonPath("$[1].type").value("INCOME"))
        .andExpect(jsonPath("$[1].amount").value(50000.00));
  }

  /**
   * Тест получения транзакций для несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getAllTransactionsUserNotFound() throws Exception {
    Long userId = 999L;
    when(transactionService.getAllTransactions(userId))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc
        .perform(get("/api/v1/users/{userId}/transactions", userId))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного получения транзакции по идентификатору.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getTransactionSuccess() throws Exception {
    Long userId = 1L;
    Long transactionId = 1L;
    TransactionDTO expectedTransaction =
        new TransactionDTO(
            transactionId,
            userId,
            TransactionType.EXPENSE,
            5L,
            "Продукты",
            "🍔",
            "#10b981",
            2L,
            "Основная карта",
            null,
            null,
            new BigDecimal("500.00"),
            "Покупка в магазине",
            LocalDate.now(),
            LocalDateTime.now(),
            LocalDateTime.now());

    when(transactionService.getTransactionById(transactionId, userId))
        .thenReturn(expectedTransaction);

    mockMvc
        .perform(get("/api/v1/users/{userId}/transactions/{transactionId}", userId, transactionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(transactionId))
        .andExpect(jsonPath("$.type").value("EXPENSE"))
        .andExpect(jsonPath("$.categoryName").value("Продукты"))
        .andExpect(jsonPath("$.amount").value(500.00));
  }

  /**
   * Тест получения несуществующей транзакции.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getTransactionNotFound() throws Exception {
    Long userId = 1L;
    Long transactionId = 999L;
    when(transactionService.getTransactionById(transactionId, userId))
        .thenThrow(
            new ResourceNotFoundException(
                "Transaction with id " + transactionId + " not found for user " + userId));

    mockMvc
        .perform(get("/api/v1/users/{userId}/transactions/{transactionId}", userId, transactionId))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного получения транзакций с фильтрами.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getTransactionsWithFiltersSuccess() throws Exception {
    Long userId = 1L;
    List<TransactionDTO> expectedTransactions =
        Arrays.asList(
            new TransactionDTO(
                1L,
                userId,
                TransactionType.EXPENSE,
                5L,
                "Продукты",
                "🍔",
                "#10b981",
                2L,
                "Основная карта",
                null,
                null,
                new BigDecimal("500.00"),
                "Покупка",
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDateTime.now()));

    when(transactionService.getTransactionsWithFilters(
            eq(userId), any(), any(), any(), any(), any()))
        .thenReturn(expectedTransactions);

    mockMvc
        .perform(
            get("/api/v1/users/{userId}/transactions/filter", userId)
                .param("type", "EXPENSE")
                .param("accountId", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].type").value("EXPENSE"));
  }

  /**
   * Тест успешного создания транзакции EXPENSE.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void createExpenseTransactionSuccess() throws Exception {
    Long userId = 1L;
    TransactionCreateDTO createDTO =
        new TransactionCreateDTO(
            TransactionType.EXPENSE,
            5L,
            2L,
            null,
            new BigDecimal("500.00"),
            "Покупка продуктов",
            LocalDate.now());

    TransactionDTO expectedTransaction =
        new TransactionDTO(
            1L,
            userId,
            TransactionType.EXPENSE,
            5L,
            "Продукты",
            "🍔",
            "#10b981",
            2L,
            "Основная карта",
            null,
            null,
            new BigDecimal("500.00"),
            "Покупка продуктов",
            LocalDate.now(),
            LocalDateTime.now(),
            LocalDateTime.now());

    when(transactionService.createTransaction(eq(userId), any(TransactionCreateDTO.class)))
        .thenReturn(expectedTransaction);

    mockMvc
        .perform(
            post("/api/v1/users/{userId}/transactions", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.type").value("EXPENSE"))
        .andExpect(jsonPath("$.amount").value(500.00));
  }

  /**
   * Тест успешного создания транзакции TRANSFER.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void createTransferTransactionSuccess() throws Exception {
    Long userId = 1L;
    TransactionCreateDTO createDTO =
        new TransactionCreateDTO(
            TransactionType.TRANSFER,
            null,
            2L,
            3L,
            new BigDecimal("1000.00"),
            "Перевод на сберегательный счет",
            LocalDate.now());

    TransactionDTO expectedTransaction =
        new TransactionDTO(
            1L,
            userId,
            TransactionType.TRANSFER,
            null,
            null,
            null,
            null,
            2L,
            "Основная карта",
            3L,
            "Сбережения",
            new BigDecimal("1000.00"),
            "Перевод на сберегательный счет",
            LocalDate.now(),
            LocalDateTime.now(),
            LocalDateTime.now());

    when(transactionService.createTransaction(eq(userId), any(TransactionCreateDTO.class)))
        .thenReturn(expectedTransaction);

    mockMvc
        .perform(
            post("/api/v1/users/{userId}/transactions", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.type").value("TRANSFER"))
        .andExpect(jsonPath("$.transferAccountId").value(3L));
  }

  /**
   * Тест создания TRANSFER транзакции без transferAccountId.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void createTransferWithoutTransferAccountFails() throws Exception {
    Long userId = 1L;
    TransactionCreateDTO createDTO =
        new TransactionCreateDTO(
            TransactionType.TRANSFER,
            null,
            2L,
            null,
            new BigDecimal("1000.00"),
            "Перевод",
            LocalDate.now());

    when(transactionService.createTransaction(eq(userId), any(TransactionCreateDTO.class)))
        .thenThrow(
            new BadRequestException("Transfer account is required for TRANSFER transaction"));

    mockMvc
        .perform(
            post("/api/v1/users/{userId}/transactions", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isBadRequest());
  }

  /**
   * Тест создания транзакции для несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void createTransactionUserNotFound() throws Exception {
    Long userId = 999L;
    TransactionCreateDTO createDTO =
        new TransactionCreateDTO(
            TransactionType.EXPENSE,
            5L,
            2L,
            null,
            new BigDecimal("500.00"),
            "Покупка",
            LocalDate.now());

    when(transactionService.createTransaction(eq(userId), any(TransactionCreateDTO.class)))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc
        .perform(
            post("/api/v1/users/{userId}/transactions", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного обновления транзакции.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void updateTransactionSuccess() throws Exception {
    Long userId = 1L;
    Long transactionId = 1L;
    TransactionUpdateDTO updateDTO =
        new TransactionUpdateDTO(
            TransactionType.EXPENSE,
            6L,
            2L,
            null,
            new BigDecimal("750.00"),
            "Обновленное описание",
            LocalDate.now());

    TransactionDTO expectedTransaction =
        new TransactionDTO(
            transactionId,
            userId,
            TransactionType.EXPENSE,
            6L,
            "Транспорт",
            "🚗",
            "#3b82f6",
            2L,
            "Основная карта",
            null,
            null,
            new BigDecimal("750.00"),
            "Обновленное описание",
            LocalDate.now(),
            LocalDateTime.now(),
            LocalDateTime.now());

    when(transactionService.updateTransaction(
            eq(transactionId), eq(userId), any(TransactionUpdateDTO.class)))
        .thenReturn(expectedTransaction);

    mockMvc
        .perform(
            put("/api/v1/users/{userId}/transactions/{transactionId}", userId, transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(transactionId))
        .andExpect(jsonPath("$.amount").value(750.00))
        .andExpect(jsonPath("$.categoryName").value("Транспорт"));
  }

  /**
   * Тест обновления несуществующей транзакции.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void updateTransactionNotFound() throws Exception {
    Long userId = 1L;
    Long transactionId = 999L;
    TransactionUpdateDTO updateDTO =
        new TransactionUpdateDTO(null, null, null, null, new BigDecimal("1000.00"), null, null);

    when(transactionService.updateTransaction(
            eq(transactionId), eq(userId), any(TransactionUpdateDTO.class)))
        .thenThrow(
            new ResourceNotFoundException(
                "Transaction with id " + transactionId + " not found for user " + userId));

    mockMvc
        .perform(
            put("/api/v1/users/{userId}/transactions/{transactionId}", userId, transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного удаления транзакции.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void deleteTransactionSuccess() throws Exception {
    Long userId = 1L;
    Long transactionId = 1L;

    doNothing().when(transactionService).deleteTransaction(transactionId, userId);

    mockMvc
        .perform(
            delete("/api/v1/users/{userId}/transactions/{transactionId}", userId, transactionId))
        .andExpect(status().isNoContent());
  }

  /**
   * Тест удаления несуществующей транзакции.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void deleteTransactionNotFound() throws Exception {
    Long userId = 1L;
    Long transactionId = 999L;

    doThrow(
            new ResourceNotFoundException(
                "Transaction with id " + transactionId + " not found for user " + userId))
        .when(transactionService)
        .deleteTransaction(transactionId, userId);

    mockMvc
        .perform(
            delete("/api/v1/users/{userId}/transactions/{transactionId}", userId, transactionId))
        .andExpect(status().isNotFound());
  }
}
