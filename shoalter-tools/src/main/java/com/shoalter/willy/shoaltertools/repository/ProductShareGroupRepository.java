package com.shoalter.willy.shoaltertools.repository;

import com.shoalter.willy.shoaltertools.entity.ProductShareGroupEntity;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductShareGroupRepository extends JpaRepository<ProductShareGroupEntity, Long> {
  @Transactional
  @Modifying
  @Query(
      value = "delete from product_share_group where product_id = :product_id",
      nativeQuery = true)
  void deleteByProduct(Long product_id);

  @Transactional
  @Modifying
  @Query(
      value =
          "INSERT INTO product_share_group (group_id, product_id) VALUES (:group_id, :product_id)",
      nativeQuery = true)
  void updateByProductAndShareGroup(Long group_id, Long product_id);
}
