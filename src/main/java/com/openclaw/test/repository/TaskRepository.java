package com.openclaw.test.repository;

import com.openclaw.test.entity.Task;
import com.openclaw.test.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByCreatorStartingWith(String identityType, Pageable pageable);

    Page<Task> findByStatusAndCreatorStartingWith(TaskStatus status, String identityType, Pageable pageable);
}
