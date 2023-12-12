package com.shoalter.willy.shoaltertools.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
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

  @Value("${spring.redis.cluster.nodes}")
  private List<String> clusterNodes;

  @Value("${spring.redis.hktv.cluster.nodes}")
  private List<String> clusterHKTVNodes;

  @Value("${spring.redis.lm.cluster.nodes}")
  private List<String> clusterLMNodes;

  @Value("${spring.redis.password}")
  private String redisPassword;

  @Value("${spring.redis.hktv.password}")
  private String redisHKTVPassword;

  @Value("${spring.redis.lm.password}")
  private String redisLMPassword;

  @Primary
  @Bean(name = "redisIIDSConnectionFactory")
  public ReactiveRedisConnectionFactory redisIIDSConnectionFactory() {
    RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration(clusterNodes);
    clusterConfiguration.setPassword(redisPassword);
    return new LettuceConnectionFactory(clusterConfiguration);
  }

  @Bean(name = "redisLMConnectionFactory")
  public ReactiveRedisConnectionFactory redisLMConnectionFactory() {
    RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration(clusterLMNodes);
    clusterConfiguration.setPassword(redisLMPassword);
    return new LettuceConnectionFactory(clusterConfiguration);
  }

  @Bean(name = "redisHKTVConnectionFactory")
  public ReactiveRedisConnectionFactory redisHKTVConnectionFactory() {
    RedisClusterConfiguration clusterConfiguration =
        new RedisClusterConfiguration(clusterHKTVNodes);
    clusterConfiguration.setPassword(redisHKTVPassword);
    return new LettuceConnectionFactory(clusterConfiguration);
  }

  @Primary
  @Bean(name = "redisIIDSTemplate")
  public ReactiveRedisTemplate redisIIDSTemplate(
      @Qualifier("redisIIDSConnectionFactory") ReactiveRedisConnectionFactory connectionFactory,
      RedisSerializationContext redisSerializationContext) {
    ReactiveRedisTemplate reactiveRedisTemplate =
        new ReactiveRedisTemplate(connectionFactory, redisSerializationContext);
    return reactiveRedisTemplate;
  }

  @Bean(name = "redisLMTemplate")
  public ReactiveRedisTemplate redisLMTemplate(
      @Qualifier("redisLMConnectionFactory") ReactiveRedisConnectionFactory connectionFactory,
      RedisSerializationContext redisSerializationContext) {
    ReactiveRedisTemplate reactiveRedisTemplate =
        new ReactiveRedisTemplate(connectionFactory, redisSerializationContext);
    return reactiveRedisTemplate;
  }

  @Bean(name = "redisHKTVTemplate")
  public ReactiveRedisTemplate redisHKTVTemplate(
      @Qualifier("redisHKTVConnectionFactory") ReactiveRedisConnectionFactory connectionFactory,
      RedisSerializationContext redisSerializationContext) {
    ReactiveRedisTemplate reactiveRedisTemplate =
        new ReactiveRedisTemplate(connectionFactory, redisSerializationContext);
    return reactiveRedisTemplate;
  }
}
