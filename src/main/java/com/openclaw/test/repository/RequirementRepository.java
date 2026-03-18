package com.openclaw.test.repository;

import com.openclaw.test.entity.Requirement;
import com.openclaw.test.entity.RequirementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Long> {

    Optional<Requirement> findByRequirementNumber(String requirementNumber);

    Page<Requirement> findByStatus(RequirementStatus status, Pageable pageable);

    Page<Requirement> findByCreatorContaining(String creator, Pageable pageable);

    Page<Requirement> findByTitleContaining(String title, Pageable pageable);

    Page<Requirement> findByStatusAndTitleContaining(RequirementStatus status, String title, Pageable pageable);

    boolean existsByRequirementNumber(String requirementNumber);
}
