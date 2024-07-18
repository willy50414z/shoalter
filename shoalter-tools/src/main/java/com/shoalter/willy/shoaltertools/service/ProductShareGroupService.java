package com.shoalter.willy.shoaltertools.service;

import com.shoalter.willy.shoaltertools.entity.ProductEntity;
import com.shoalter.willy.shoaltertools.entity.ProductShareGroupEntity;
import com.shoalter.willy.shoaltertools.entity.ShareGroupEntity;
import com.shoalter.willy.shoaltertools.repository.ProductRepository;
import com.shoalter.willy.shoaltertools.repository.ProductShareGroupRepository;
import com.shoalter.willy.shoaltertools.repository.ShareGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductShareGroupService {

  @Autowired private ProductRepository productRepository;

  @Autowired private ProductShareGroupRepository productShareGroupRepository;

  @Autowired private ShareGroupRepository shareGroupRepository;

  public ProductEntity saveProduct(ProductEntity product) {
    return productRepository.save(product);
  }

  public ShareGroupEntity saveShareGroup(ShareGroupEntity shareGroup) {
    return shareGroupRepository.save(shareGroup);
  }

  public ProductShareGroupEntity saveProductShareGroup(
      ProductEntity productEntity, ShareGroupEntity shareGroupEntity) {

    ProductShareGroupEntity build =
        ProductShareGroupEntity.builder()
            .product(productEntity)
            .shareGroup(shareGroupEntity)
            .build();
    return productShareGroupRepository.save(build);
  }

  public void deleteProduct(String uuid) {

    productRepository.deleteByUuid(uuid);
  }

  public void deleteShareGroup(ShareGroupEntity shareGroup) {
    shareGroupRepository.delete(shareGroup);
  }

  public void updateByProductAndShareGroup(
      ShareGroupEntity shareGroup, ProductEntity productEntity) {
    productShareGroupRepository.updateByProductAndShareGroup(
        shareGroup.getId(), productEntity.getId());
  }

  public void deleteProductShareGroupByProduct(ProductEntity productEntity) {
    productShareGroupRepository.deleteByProduct(productEntity.getId());
  }
}
