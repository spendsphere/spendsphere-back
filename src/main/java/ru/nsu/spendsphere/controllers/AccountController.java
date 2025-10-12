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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.spendsphere.models.dto.AccountBalanceDTO;
import ru.nsu.spendsphere.models.dto.AccountCreateDTO;
import ru.nsu.spendsphere.models.dto.AccountDTO;
import ru.nsu.spendsphere.models.dto.AccountUpdateDTO;
import ru.nsu.spendsphere.services.AccountService;

@Tag(
    name = "Управление счетами",
    description = "API для создания, получения, обновления и удаления счетов пользователей")
@RestController
@RequestMapping("/api/v1/users/{userId}/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @Operation(
      summary = "Получение всех счетов пользователя",
      description = "Возвращает список всех счетов пользователя по его идентификатору")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список счетов успешно получен",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AccountDTO.class)))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @GetMapping
  public List<AccountDTO> getUserAccounts(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId) {
    return accountService.getUserAccounts(userId);
  }

  @Operation(
      summary = "Получение счета по идентификатору",
      description = "Возвращает информацию о конкретном счете пользователя")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Счет успешно получен",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Счет или пользователь не найден",
            content = @Content)
      })
  @GetMapping("/{accountId}")
  public AccountDTO getAccount(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Идентификатор счета", required = true) @PathVariable
          Long accountId) {
    return accountService.getAccountById(accountId, userId);
  }

  @Operation(
      summary = "Создание нового счета",
      description = "Создает новый счет для пользователя с указанными параметрами")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Счет успешно создан",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные для создания счета",
            content = @Content)
      })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AccountDTO createAccount(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Данные для создания нового счета", required = true)
          @RequestBody
          @Valid
          AccountCreateDTO createDTO) {
    return accountService.createAccount(userId, createDTO);
  }

  @Operation(
      summary = "Обновление счета",
      description = "Обновляет данные существующего счета пользователя")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Счет успешно обновлен",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Счет не найден или не принадлежит пользователю",
            content = @Content)
      })
  @PutMapping("/{accountId}")
  public AccountDTO updateAccount(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Идентификатор счета", required = true) @PathVariable Long accountId,
      @Parameter(description = "Данные для обновления счета", required = true) @RequestBody
          AccountUpdateDTO updateDTO) {
    return accountService.updateAccount(accountId, userId, updateDTO);
  }

  @Operation(
      summary = "Удаление счета",
      description = "Удаляет счет пользователя по идентификатору")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Счет успешно удален"),
        @ApiResponse(
            responseCode = "404",
            description = "Счет не найден или не принадлежит пользователю",
            content = @Content)
      })
  @DeleteMapping("/{accountId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAccount(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Идентификатор счета", required = true) @PathVariable
          Long accountId) {
    accountService.deleteAccount(accountId, userId);
  }

  @Operation(
      summary = "Получение общего баланса по активным счетам",
      description =
          "Возвращает общий баланс по всем активным счетам пользователя, сгруппированный по валютам")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Баланс успешно получен",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBalanceDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @GetMapping("/balance")
  public AccountBalanceDTO getUserAccountsBalance(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId) {
    return accountService.getUserAccountsBalance(userId);
  }
}
