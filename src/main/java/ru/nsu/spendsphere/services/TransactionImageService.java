package ru.nsu.spendsphere.services;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.spendsphere.models.entities.Category;
import ru.nsu.spendsphere.models.entities.OcrTask;
import ru.nsu.spendsphere.models.messaging.OcrTaskMessage;
import ru.nsu.spendsphere.repositories.CategoryRepository;
import ru.nsu.spendsphere.repositories.OcrTaskRepository;

@Service
@RequiredArgsConstructor
public class TransactionImageService {

  private static final Logger log = LoggerFactory.getLogger(TransactionImageService.class);

  private final RabbitTemplate rabbitTemplate;
  private final CategoryRepository categoryRepository;
  private final OcrTaskRepository ocrTaskRepository;

  @Value("${app.rabbit.queues.image}")
  private String imageUploadQueueName;

  public void sendImageForRecognition(
      Long userId, Long accountId, String filename, String contentType, byte[] data) {
    UUID taskId = UUID.randomUUID();

    OcrTask ocrTask =
        OcrTask.builder().taskId(taskId).userId(userId).accountId(accountId).build();
    ocrTaskRepository.save(ocrTask);

    List<String> categories =
        categoryRepository.findAllByUserIdOrDefault(userId).stream()
            .map(Category::getName)
            .toList();

    String imageB64 = Base64.getEncoder().encodeToString(data);

    OcrTaskMessage message = new OcrTaskMessage(taskId.toString(), imageB64, categories);

    log.info(
        "Sending OCR task to RabbitMQ: queue={}, taskId={}, userId={}, accountId={}, "
            + "file={}, contentType={}, size={}, categories={}",
        imageUploadQueueName,
        taskId,
        userId,
        accountId,
        filename,
        contentType,
        data != null ? data.length : 0,
        categories);

    rabbitTemplate.convertAndSend(imageUploadQueueName, message);

    log.info(
        "OCR task message sent: queue={}, taskId={}, accountId={}, file={}",
        imageUploadQueueName,
        taskId,
        accountId,
        filename);
  }
}
