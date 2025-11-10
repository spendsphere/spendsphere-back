package ru.nsu.spendsphere.configurations;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "app.rabbit.enabled", havingValue = "true")
public class RabbitMQConfig {

  @Value("${app.rabbit.queues.image}")
  private String imageUploadQueueName;

  @Value("${app.rabbit.queues.parsed}")
  private String parsedResultsQueueName;

  @Bean
  public Queue imageUploadQueue() {
    return new Queue(imageUploadQueueName, true);
  }

  @Bean
  public Queue parsedResultsQueue() {
    return new Queue(parsedResultsQueueName, true);
  }

  @Bean
  public MessageConverter jacksonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, MessageConverter messageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter);
    return template;
  }
}
