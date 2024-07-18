package com.shoalter.willy.shoaltertools.entity;

import java.util.Date;
import java.util.Set;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "share_group")
public class ShareGroupEntity {
  @javax.persistence.Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Date createTime;
  private Date modifiedTime;

  @OneToMany private Set<ProductShareGroupEntity> productShareGroupEntities;
}
