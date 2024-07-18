package com.shoalter.willy.shoaltertools.repository;

import com.shoalter.willy.shoaltertools.entity.ProductEntity;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

  @Query(value = "select * from product where uuid = :uuid", nativeQuery = true)
  public ProductEntity findByUuid(String uuid);

  @Modifying
  @Query(value = "delete from product where uuid = :uuid", nativeQuery = true)
  public void deleteByUuid(@Param("uuid") String uuid);
}
