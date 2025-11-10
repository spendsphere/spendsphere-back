package ru.nsu.spendsphere.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.spendsphere.models.messaging.CategoryShortMessage;
import ru.nsu.spendsphere.models.messaging.ImageTransactionUploadMessage;
import ru.nsu.spendsphere.repositories.CategoryRepository;

@Service
@RequiredArgsConstructor
public class TransactionImageService {

  private static final Logger log = LoggerFactory.getLogger(TransactionImageService.class);

  private final RabbitTemplate rabbitTemplate;
  private final CategoryRepository categoryRepository;

  @Value("${app.rabbit.queues.image}")
  private String imageUploadQueueName;

  public void sendImageForRecognition(
      Long userId, Long accountId, String filename, String contentType, byte[] data) {
    List<CategoryShortMessage> categories =
        categoryRepository.findAllByUserIdOrDefault(userId).stream()
            .map(c -> new CategoryShortMessage(c.getId(), c.getName()))
            .toList();

    ImageTransactionUploadMessage message =
        new ImageTransactionUploadMessage(accountId, filename, contentType, data, categories);

    log.info(
        "Sending image to RabbitMQ: queue={}, userId={}, accountId={}, file={}, contentType={}, size={}, categories={}",
        imageUploadQueueName,
        userId,
        accountId,
        filename,
        contentType,
        data != null ? data.length : 0,
        categories.size());

    rabbitTemplate.convertAndSend(imageUploadQueueName, message);

    log.info(
        "Image message sent: queue={}, accountId={}, file={}",
        imageUploadQueueName,
        accountId,
        filename);
  }
}
