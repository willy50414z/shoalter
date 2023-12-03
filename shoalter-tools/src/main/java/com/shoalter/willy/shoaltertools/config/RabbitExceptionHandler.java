package com.shoalter.willy.shoaltertools.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.util.ErrorHandler;

@Slf4j
public class RabbitExceptionHandler implements ErrorHandler {

  @Override
  public void handleError(Throwable t) {
    log.error("[developer-alert][handleError] RabbitExceptionHandler Exception: ", t);
    throw new AmqpRejectAndDontRequeueException(t);
  }
}
