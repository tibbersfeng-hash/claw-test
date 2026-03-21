package com.openclaw.test.repository;

import com.openclaw.test.entity.Identity;
import com.openclaw.test.entity.IdentityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentityRepository extends JpaRepository<Identity, Long> {

    Optional<Identity> findByApiKey(String apiKey);

    Page<Identity> findByType(IdentityType type, Pageable pageable);

    boolean existsByApiKey(String apiKey);

    Optional<Identity> findFirstByTypeOrderByIdAsc(IdentityType type);

    /**
     * 根据身份标识查找（如 "PM-1", "DEV-3"）
     */
    Identity findByIdentityId(String identityId);

    /**
     * 检查身份标识是否已存在
     */
    boolean existsByIdentityId(String identityId);
}
