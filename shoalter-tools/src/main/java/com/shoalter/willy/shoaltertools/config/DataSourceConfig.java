package com.shoalter.willy.shoaltertools.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

  @Bean
  public DataSource dataSource() {
    DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
    dataSourceBuilder.url("jdbc:mysql://localhost:3416/inventory?serverZoneId=Asia/Hong_Kong");
    dataSourceBuilder.username("root");
    dataSourceBuilder.password("HKtv2014");
    return dataSourceBuilder.build();
  }
}
