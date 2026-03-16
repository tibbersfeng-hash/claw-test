package com.openclaw.test.repository;

import com.openclaw.test.entity.DesignDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DesignDocRepository extends JpaRepository<DesignDoc, Long> {

    Page<DesignDoc> findByTaskId(Long taskId, Pageable pageable);

    Optional<DesignDoc> findByTaskId(Long taskId);
}
