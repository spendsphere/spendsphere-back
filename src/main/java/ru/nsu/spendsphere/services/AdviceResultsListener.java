package ru.nsu.spendsphere.services;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.spendsphere.models.entities.Advice;
import ru.nsu.spendsphere.models.entities.AdviceItem;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.models.messaging.AdviceResultItem;
import ru.nsu.spendsphere.models.messaging.AdviceResultMessage;
import ru.nsu.spendsphere.repositories.AdviceRepository;
import ru.nsu.spendsphere.repositories.UserRepository;

/** Listener для обработки результатов генерации финансовых советов. */
@Service
@ConditionalOnProperty(value = "app.rabbit.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AdviceResultsListener {

  private static final Logger log = LoggerFactory.getLogger(AdviceResultsListener.class);

  private final AdviceService adviceService;
  private final AdviceRepository adviceRepository;
  private final UserRepository userRepository;

  @Value("${app.rabbit.queues.advice-results}")
  private String adviceResultsQueueName;

  @RabbitListener(queues = "${app.rabbit.queues.advice-results}")
  @Transactional
  public void handleAdviceResults(AdviceResultMessage message) {
    log.info(
        "Received advice result: queue={}, taskId={}, status={}",
        adviceResultsQueueName,
        message.taskId(),
        message.status());

    if (!"SUCCESS".equalsIgnoreCase(message.status())) {
      log.error("Advice task {} failed with status: {}", message.taskId(), message.status());
      return;
    }

    if (message.advice() == null || message.advice().isEmpty()) {
      log.warn("Advice result {} has no advice items", message.taskId());
      return;
    }

    Long userId = adviceService.decodeUserId(message.taskId());
    if (userId == null) {
      log.error("Failed to decode userId from taskId: {}", message.taskId());
      return;
    }

    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      log.error("User with id {} not found for advice task {}", userId, message.taskId());
      return;
    }

    log.info(
        "Processing advice result: taskId={}, userId={}, adviceItems={}",
        message.taskId(),
        userId,
        message.advice().size());

    Advice advice =
        Advice.builder()
            .user(user)
            .taskId(message.taskId())
            .goal(message.goal())
            .items(new ArrayList<>())
            .build();

    List<AdviceItem> items = new ArrayList<>();
    for (AdviceResultItem resultItem : message.advice()) {
      AdviceItem item =
          AdviceItem.builder()
              .advice(advice)
              .itemOrder(resultItem.id())
              .title(resultItem.title())
              .priority(resultItem.priority())
              .description(resultItem.description())
              .build();
      items.add(item);
    }

    advice.setItems(items);
    adviceRepository.save(advice);

    log.info(
        "Advice saved successfully: taskId={}, userId={}, adviceItems={}",
        message.taskId(),
        userId,
        items.size());
  }
}
