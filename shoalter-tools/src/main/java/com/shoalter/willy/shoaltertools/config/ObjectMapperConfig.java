package com.shoalter.willy.shoaltertools.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapperConfig {
  @Bean
  public ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }
}
