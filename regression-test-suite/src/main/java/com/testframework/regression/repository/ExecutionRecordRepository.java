package com.testframework.regression.repository;

import com.testframework.regression.domain.ExecutionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ExecutionRecordRepository extends JpaRepository<ExecutionRecord, Long> {
    Optional<ExecutionRecord> findByExecutionId(String executionId);
    List<ExecutionRecord> findByStartTimeBetweenOrderByStartTimeDesc(OffsetDateTime start, OffsetDateTime end);
    List<ExecutionRecord> findAllByOrderByStartTimeDesc();
}








