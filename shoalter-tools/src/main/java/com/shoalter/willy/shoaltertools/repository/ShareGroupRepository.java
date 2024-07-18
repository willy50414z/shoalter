package com.shoalter.willy.shoaltertools.repository;

import com.shoalter.willy.shoaltertools.entity.ShareGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareGroupRepository extends JpaRepository<ShareGroupEntity, Long> {}
