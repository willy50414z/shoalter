package com.shoalter.willy.shoaltertools.testtool;

import com.shoalter.willy.shoaltertools.dto.ProductInfoDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqUtil {

  @Autowired private RabbitTemplate defaultRabbitTemplate;

  public void sendMsgToIidsQueue(ProductInfoDto productInfoDto) {
    defaultRabbitTemplate.convertAndSend(
        getRabbitMqExchange(), getRabbitMqRoutingKey(), productInfoDto);
  }

  private String getRabbitMqExchange() {
    return "shoalter-see-product-master_topic";
  }

  private String getRabbitMqRoutingKey() {
    return "shoalter-see-product-master.product-info-iids";
  }
}
