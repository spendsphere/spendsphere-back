package ru.nsu.spendsphere.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.spendsphere.models.dto.TransactionCreateDTO;
import ru.nsu.spendsphere.models.dto.TransactionDTO;
import ru.nsu.spendsphere.models.dto.TransactionUpdateDTO;
import ru.nsu.spendsphere.models.entities.TransactionType;
import ru.nsu.spendsphere.services.TransactionService;

@Tag(
    name = "Управление транзакциями",
    description = "API для создания, получения, обновления и удаления транзакций пользователей")
@RestController
@RequestMapping("/api/v1/users/{userId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;

  @Operation(
      summary = "Получение всех транзакций пользователя",
      description = "Возвращает список всех транзакций пользователя по его идентификатору")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список транзакций успешно получен",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TransactionDTO.class)))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @GetMapping
  public List<TransactionDTO> getAllTransactions(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId) {
    return transactionService.getAllTransactions(userId);
  }

  @Operation(
      summary = "Получение транзакции по идентификатору",
      description = "Возвращает информацию о конкретной транзакции пользователя")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Транзакция успешно получена",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Транзакция или пользователь не найдены",
            content = @Content)
      })
  @GetMapping("/{transactionId}")
  public TransactionDTO getTransaction(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Идентификатор транзакции", required = true) @PathVariable
          Long transactionId) {
    return transactionService.getTransactionById(transactionId, userId);
  }

  @Operation(
      summary = "Получение транзакций с фильтрами",
      description =
          "Возвращает список транзакций пользователя с применением фильтров по типу, счету,"
              + " категории и дате")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список транзакций успешно получен",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TransactionDTO.class)))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @GetMapping("/filter")
  public List<TransactionDTO> getTransactionsWithFilters(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Тип транзакции", example = "EXPENSE")
          @RequestParam(required = false)
          TransactionType type,
      @Parameter(description = "Идентификатор счета", example = "1") @RequestParam(required = false)
          Long accountId,
      @Parameter(description = "Идентификатор категории", example = "5")
          @RequestParam(required = false)
          Long categoryId,
      @Parameter(description = "Дата начала периода", example = "2025-10-01")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateFrom,
      @Parameter(description = "Дата окончания периода", example = "2025-10-31")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateTo) {
    return transactionService.getTransactionsWithFilters(
        userId, type, accountId, categoryId, dateFrom, dateTo);
  }

  @Operation(
      summary = "Создание новой транзакции",
      description = "Создает новую транзакцию для пользователя с указанными параметрами")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Транзакция успешно создана",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь, счет или категория не найдены",
            content = @Content),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные для создания транзакции",
            content = @Content)
      })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TransactionDTO createTransaction(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Данные для создания новой транзакции", required = true)
          @RequestBody
          @Valid
          TransactionCreateDTO createDTO) {
    return transactionService.createTransaction(userId, createDTO);
  }

  @Operation(
      summary = "Обновление транзакции",
      description = "Обновляет данные существующей транзакции пользователя")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Транзакция успешно обновлена",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Транзакция, счет или категория не найдены",
            content = @Content),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные для обновления транзакции",
            content = @Content)
      })
  @PutMapping("/{transactionId}")
  public TransactionDTO updateTransaction(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Идентификатор транзакции", required = true) @PathVariable
          Long transactionId,
      @Parameter(description = "Данные для обновления транзакции", required = true)
          @RequestBody
          @Valid
          TransactionUpdateDTO updateDTO) {
    return transactionService.updateTransaction(transactionId, userId, updateDTO);
  }

  @Operation(
      summary = "Удаление транзакции",
      description = "Удаляет транзакцию пользователя по идентификатору")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Транзакция успешно удалена"),
        @ApiResponse(
            responseCode = "404",
            description = "Транзакция или пользователь не найдены",
            content = @Content)
      })
  @DeleteMapping("/{transactionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTransaction(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Идентификатор транзакции", required = true) @PathVariable
          Long transactionId) {
    transactionService.deleteTransaction(transactionId, userId);
  }
}
