package com.testframework.regression.repository;

import com.testframework.regression.domain.TestResult;
import com.testframework.regression.domain.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByTestCase(TestCase testCase);
    List<TestResult> findByExecutionId(String executionId);

    @Query("select r from TestResult r join fetch r.testCase tc where r.executionId = :executionId")
    List<TestResult> findByExecutionIdWithTestCase(@Param("executionId") String executionId);

    List<TestResult> findByExecutedAtBetweenOrderByExecutedAtDesc(OffsetDateTime start, OffsetDateTime end);

    @Query("select r from TestResult r join fetch r.testCase tc where r.executedAt >= :start and r.executedAt <= :end order by r.executedAt desc")
    List<TestResult> findByExecutedAtBetweenWithTestCase(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);
}



