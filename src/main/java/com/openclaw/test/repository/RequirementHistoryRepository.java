package com.openclaw.test.repository;

import com.openclaw.test.entity.RequirementHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementHistoryRepository extends JpaRepository<RequirementHistory, Long> {

    List<RequirementHistory> findByRequirementIdOrderByCreatedAtDesc(Long requirementId);

    List<RequirementHistory> findByRequirementIdAndVersion(Long requirementId, Integer version);
}
