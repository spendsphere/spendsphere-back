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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.spendsphere.models.dto.AdviceRequestDTO;
import ru.nsu.spendsphere.models.dto.AdviceResponseDTO;
import ru.nsu.spendsphere.services.AdviceService;

/**
 * Контроллер для работы с финансовыми советами.
 */
@Tag(name = "Финансовые советы", description = "API для получения персональных финансовых советов")
@RestController
@RequestMapping("/api/v1/users/{userId}/advices")
@RequiredArgsConstructor
public class AdviceController {

  private final AdviceService adviceService;

  @Operation(
      summary = "Запросить генерацию финансовых советов",
      description =
          "Отправляет запрос на генерацию персональных финансовых советов на основе истории"
              + " транзакций пользователя")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "202",
            description = "Запрос принят и поставлен в очередь на обработку"),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные запроса",
            content = @Content)
      })
  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void requestAdvice(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Данные для генерации советов", required = true) @RequestBody
          @Valid
          AdviceRequestDTO requestDTO) {
    adviceService.requestAdvice(userId, requestDTO);
  }

  @Operation(
      summary = "Получить финансовые советы за последний месяц",
      description =
          "Возвращает список финансовых советов, созданных за последний месяц для пользователя")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список советов успешно получен",
            content =
                @Content(
                    mediaType = "application/json",
                    array =
                        @ArraySchema(schema = @Schema(implementation = AdviceResponseDTO.class)))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @GetMapping("/recent")
  public List<AdviceResponseDTO> getRecentAdvices(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId) {
    return adviceService.getRecentAdvices(userId);
  }
}

