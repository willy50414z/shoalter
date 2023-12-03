package com.shoalter.willy.shoaltertools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@Slf4j
@SpringBootApplication
public class ShoalterToolsApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(ShoalterToolsApplication.class, args);
  }

  @Autowired RabbitTemplate rabbitTempl;
  @Autowired ReactiveRedisTemplate<String, String> redisTempl;

  @Override
  public void run(String... args) throws Exception {
    List<String> uuidList = List.of("");
    for(String uuid : uuidList) {
      Map<String, String> oldStockLevelMap = redisTempl.<String, String>opsForHash().entries(uuid).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).block();
      Map<String, String> newStockLevelMap = redisTempl.<String, String>opsForHash().entries("inventory:" + uuid).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).block();
      Map<String, String> iimsStockLevelMap = redisTempl.<String, String>opsForHash().entries(newStockLevelMap.get("hktv_sku")).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).block();

    }
  }
}
