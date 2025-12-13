package ru.nsu.spendsphere.services;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.AdviceItemDTO;
import ru.nsu.spendsphere.models.dto.AdviceRequestDTO;
import ru.nsu.spendsphere.models.dto.AdviceResponseDTO;
import ru.nsu.spendsphere.models.entities.AdviceItem;
import ru.nsu.spendsphere.models.entities.Transaction;
import ru.nsu.spendsphere.models.entities.TransactionType;
import ru.nsu.spendsphere.models.messaging.AdviceGoal;
import ru.nsu.spendsphere.models.messaging.AdviceTaskMessage;
import ru.nsu.spendsphere.models.messaging.MonthlyStats;
import ru.nsu.spendsphere.repositories.AdviceRepository;
import ru.nsu.spendsphere.repositories.TransactionRepository;
import ru.nsu.spendsphere.repositories.UserRepository;

/**
 * Сервис для работы с финансовыми советами.
 */
@Service
@RequiredArgsConstructor
public class AdviceService {

  private static final Logger log = LoggerFactory.getLogger(AdviceService.class);
  private static final int MAX_MONTHS_FOR_STATS = 3;

  private final RabbitTemplate rabbitTemplate;
  private final AdviceRepository adviceRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;

  @Value("${app.rabbit.queues.advice-tasks}")
  private String adviceTasksQueueName;

  /**
   * Создает задачу на получение финансовых советов.
   *
   * @param userId идентификатор пользователя
   * @param requestDTO данные запроса
   */
  public void requestAdvice(Long userId, AdviceRequestDTO requestDTO) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }

    String taskId = generateTaskId(userId);

    Map<String, MonthlyStats> monthlyStats = collectMonthlyStats(userId);

    AdviceGoal goal = new AdviceGoal(requestDTO.goal(), requestDTO.targetDate());

    AdviceTaskMessage message = new AdviceTaskMessage(taskId, goal, monthlyStats);

    log.info(
        "Sending advice task to RabbitMQ: queue={}, taskId={}, userId={}, goal={}",
        adviceTasksQueueName,
        taskId,
        userId,
        requestDTO.goal());

    rabbitTemplate.convertAndSend(adviceTasksQueueName, message);

    log.info("Advice task message sent: queue={}, taskId={}", adviceTasksQueueName, taskId);
  }

  /**
   * Получает советы пользователя за последний месяц.
   *
   * @param userId идентификатор пользователя
   * @return список советов за последний месяц
   */
  public List<AdviceResponseDTO> getRecentAdvices(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }

    LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);

    return adviceRepository.findByUserIdWithItems(userId).stream()
        .filter(advice -> advice.getCreatedAt().toLocalDate().isAfter(oneMonthAgo))
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
  }

  private AdviceResponseDTO toResponseDTO(ru.nsu.spendsphere.models.entities.Advice advice) {
    List<AdviceItemDTO> items =
        advice.getItems().stream()
            .sorted(Comparator.comparing(AdviceItem::getItemOrder))
            .map(
                item ->
                    new AdviceItemDTO(
                        item.getItemOrder(),
                        item.getTitle(),
                        item.getPriority(),
                        item.getDescription()))
            .collect(Collectors.toList());

    return new AdviceResponseDTO(
        advice.getId(),
        advice.getUser().getId(),
        advice.getGoal(),
        advice.getTargetDate(),
        items,
        advice.getCreatedAt());
  }

  private String generateTaskId(Long userId) {
    String userIdStr = "user_" + userId + "_" + System.currentTimeMillis();
    return Base64.getUrlEncoder().withoutPadding().encodeToString(userIdStr.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Декодирует userId из taskId.
   *
   * @param taskId идентификатор задачи
   * @return идентификатор пользователя
   */
  public Long decodeUserId(String taskId) {
    try {
      byte[] decoded = Base64.getUrlDecoder().decode(taskId);
      String decodedStr = new String(decoded, StandardCharsets.UTF_8);
      String[] parts = decodedStr.split("_");
      if (parts.length >= 2 && "user".equals(parts[0])) {
        return Long.parseLong(parts[1]);
      }
    } catch (Exception e) {
      log.error("Failed to decode userId from taskId: {}", taskId, e);
    }
    return null;
  }

  private Map<String, MonthlyStats> collectMonthlyStats(Long userId) {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusMonths(MAX_MONTHS_FOR_STATS);

    List<Transaction> transactions =
        transactionRepository.findByUserIdWithFilters(userId, null, null, null, startDate, endDate);

    Map<String, MonthlyStats> result = new LinkedHashMap<>();

    for (int i = 0; i < MAX_MONTHS_FOR_STATS; i++) {
      YearMonth month = YearMonth.from(endDate.minusMonths(i));
      String monthKey = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));

      LocalDate monthStart = month.atDay(1);
      LocalDate monthEnd = month.atEndOfMonth();

      List<Transaction> monthTransactions =
          transactions.stream()
              .filter(
                  t ->
                      !t.getDate().isBefore(monthStart)
                          && !t.getDate().isAfter(monthEnd))
              .collect(Collectors.toList());

      MonthlyStats stats = buildMonthlyStats(monthTransactions);
      result.put(monthKey, stats);
    }

    return result;
  }

  private MonthlyStats buildMonthlyStats(List<Transaction> transactions) {
    Map<String, BigDecimal> expensesByCategory =
        transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategory() != null)
            .collect(
                Collectors.groupingBy(
                    t -> t.getCategory().getName(),
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

    Map<String, BigDecimal> incomeBySource =
        transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME && t.getCategory() != null)
            .collect(
                Collectors.groupingBy(
                    t -> t.getCategory().getName(),
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

    Map<String, BigDecimal> averageByCategory =
        transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategory() != null)
            .collect(
                Collectors.groupingBy(
                    t -> t.getCategory().getName(),
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                          if (list.isEmpty()) return BigDecimal.ZERO;
                          BigDecimal sum =
                              list.stream()
                                  .map(Transaction::getAmount)
                                  .reduce(BigDecimal.ZERO, BigDecimal::add);
                          return sum.divide(
                              BigDecimal.valueOf(list.size()),
                              2,
                              java.math.RoundingMode.HALF_UP);
                        })));

    return new MonthlyStats(expensesByCategory, incomeBySource, averageByCategory);
  }
}

