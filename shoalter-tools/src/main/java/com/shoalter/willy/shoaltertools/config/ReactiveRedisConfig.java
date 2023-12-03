package com.shoalter.willy.shoaltertools.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class ReactiveRedisConfig {
  @Autowired ObjectMapper objectMapper;

  /**
   * ReactiveRedisTemplate默认使用的序列化是Jdk序列化，我们可以配置为json序列化
   *
   * @return
   */
  @Bean
  public RedisSerializationContext redisSerializationContext() {
    RedisSerializationContext.RedisSerializationContextBuilder builder =
        RedisSerializationContext.newSerializationContext();
    builder.key(StringRedisSerializer.UTF_8);
    // we cannot use default jackson since java 8 LocalDateTime need additional support
    builder.value(StringRedisSerializer.UTF_8);
    builder.hashKey(StringRedisSerializer.UTF_8);
    builder.hashValue(StringRedisSerializer.UTF_8);

    return builder.build();
  }

  @Bean
  public ReactiveRedisTemplate reactiveRedisTemplate(
      ReactiveRedisConnectionFactory connectionFactory,
      RedisSerializationContext redisSerializationContext) {
    ReactiveRedisTemplate reactiveRedisTemplate =
        new ReactiveRedisTemplate(connectionFactory, redisSerializationContext);
    return reactiveRedisTemplate;
  }
}
