package com.shoalter.willy.shoaltertools.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@EnableRabbit
@Configuration
public class RabbitMqConfig {
  @Autowired private ObjectMapper objectMapperWithLocalDateTimeSupport;



  @Bean
  public Jackson2JsonMessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter(objectMapperWithLocalDateTimeSupport);
  }

  public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();
    SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
    simpleRetryPolicy.setMaxAttempts(5);
    retryTemplate.setRetryPolicy(simpleRetryPolicy);
    ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialRandomBackOffPolicy();
    exponentialBackOffPolicy.setInitialInterval(5000);
    exponentialBackOffPolicy.setMultiplier(2);
    exponentialBackOffPolicy.setMaxInterval(10000);
    retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);
    return retryTemplate;
  }

  @Primary
  @Bean("defaultRabbitTemplate")
  public RabbitTemplate defaultRabbitTemplate(
      @Qualifier("defaultConnectionFactory") ConnectionFactory connectionFactory) {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMandatory(true);
    rabbitTemplate.setMessageConverter(jsonMessageConverter());
    rabbitTemplate.setRetryTemplate(retryTemplate());
    rabbitTemplate.setConfirmCallback(
        (correlationData, ack, cause) -> {
          if (!ack) {
            log.error(
                "[developer-alert][rabbitTemplate] ConfirmCallback: Send message to exchange fail,"
                    + " Cause[{}], Message[{}]",
                cause,
                correlationData);
          }
        });
    rabbitTemplate.setReturnsCallback(
        returned -> {
          log.error(
              "[developer-alert][rabbitTemplate] ReturnsCallback: Send message to queue fail, "
                  + "ReplyCode[{}], ReplyText[{}], Exchange[{}], RoutingKey[{}], Message[{}]",
              returned.getReplyCode(),
              returned.getReplyText(),
              returned.getExchange(),
              returned.getRoutingKey(),
              returned.getMessage());
        });
    rabbitTemplate.containerAckMode(AcknowledgeMode.MANUAL);
    return rabbitTemplate;
  }

  @Bean
  public RabbitExceptionHandler getRabbitExceptionHandler() {
    return new RabbitExceptionHandler();
  }

  @Bean
  public MappingJackson2MessageConverter mappingJackson2MessageConverter() {
    return new MappingJackson2MessageConverter();
  }

  @Bean
  public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
    DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
    factory.setMessageConverter(mappingJackson2MessageConverter());
    return factory;
  }

  @Bean("defaultRabbitListenerContainerFactory")
  public SimpleRabbitListenerContainerFactory defaultSimpleRabbitListenerContainerFactory(
      @Qualifier("defaultConnectionFactory") ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

    factory.setConnectionFactory(connectionFactory);
    factory.setErrorHandler(getRabbitExceptionHandler());
    factory.setMissingQueuesFatal(false);
    factory.setAutoStartup(true);
    factory.setDefaultRequeueRejected(false);

    return factory;
  }

  @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Primary
  @Bean(name = "defaultConnectionFactory")
  public ConnectionFactory defaultConnectionFactory(
      @Value("${spring.rabbitmq.default.addresses}") String addresses,
      @Value("${spring.rabbitmq.default.username}") String username,
      @Value("${spring.rabbitmq.default.password}") String password) {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
    connectionFactory.setAddresses(addresses);
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);
    connectionFactory.setPublisherReturns(true);
    return connectionFactory;
  }
}
