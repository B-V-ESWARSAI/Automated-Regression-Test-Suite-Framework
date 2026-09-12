package com.testframework.regression.service;

import com.testframework.regression.domain.*;
import com.testframework.regression.repository.ExecutionRecordRepository;
import com.testframework.regression.repository.TestResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final ExecutionRecordRepository executionRecordRepository;

    public TestResultService(TestResultRepository testResultRepository, ExecutionRecordRepository executionRecordRepository) {
        this.testResultRepository = testResultRepository;
        this.executionRecordRepository = executionRecordRepository;
    }

    public TestResult save(TestResult testResult) {
        return testResultRepository.save(testResult);
    }

    public List<TestResult> findByTestCase(TestCase testCase) {
        return testResultRepository.findByTestCase(testCase);
    }

    @Transactional(readOnly = true)
    public DayReportResponseDTO getResultsForDate(LocalDate date) {
        ZoneOffset offset = OffsetDateTime.now().getOffset();
        OffsetDateTime start = date.atStartOfDay().atOffset(offset);
        OffsetDateTime end = date.atTime(LocalTime.MAX).atOffset(offset);

        List<TestResult> results = testResultRepository.findByExecutedAtBetweenWithTestCase(start, end);
        List<ExecutionRecord> executions = executionRecordRepository.findByStartTimeBetweenOrderByStartTimeDesc(start, end);

        DayReportResponseDTO dto = new DayReportResponseDTO();
        dto.setDate(date.toString());
        dto.setTotalTests(results.size());

        int passed = (int) results.stream().filter(r -> r.getStatus() != null && "PASSED".equalsIgnoreCase(r.getStatus().name())).count();
        int failed = (int) results.stream().filter(r -> r.getStatus() != null && "FAILED".equalsIgnoreCase(r.getStatus().name())).count();

        dto.setPassedTests(passed);
        dto.setFailedTests(failed);
        dto.setPassRate(results.size() > 0 ? Math.round(((double) passed / results.size()) * 1000.0) / 10.0 : 0.0);
        dto.setExecutionCount(executions.size());
        dto.setExecutions(executions);

        List<TestResultDetailDTO> resultDTOs = results.stream()
                .map(TestResultDetailDTO::new)
                .collect(Collectors.toList());
        dto.setResults(resultDTOs);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<DaySummaryDTO> getLastNDaysSummary(int days) {
        ZoneOffset offset = OffsetDateTime.now().getOffset();
        LocalDate today = LocalDate.now();
        List<DaySummaryDTO> summaryList = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            OffsetDateTime start = d.atStartOfDay().atOffset(offset);
            OffsetDateTime end = d.atTime(LocalTime.MAX).atOffset(offset);

            List<TestResult> results = testResultRepository.findByExecutedAtBetweenOrderByExecutedAtDesc(start, end);
            List<ExecutionRecord> executions = executionRecordRepository.findByStartTimeBetweenOrderByStartTimeDesc(start, end);

            int total = results.size();
            int passed = (int) results.stream().filter(r -> r.getStatus() != null && "PASSED".equalsIgnoreCase(r.getStatus().name())).count();
            int failed = (int) results.stream().filter(r -> r.getStatus() != null && "FAILED".equalsIgnoreCase(r.getStatus().name())).count();
            double passRate = total > 0 ? Math.round(((double) passed / total) * 1000.0) / 10.0 : 0.0;

            summaryList.add(new DaySummaryDTO(d.toString(), total, passed, failed, executions.size(), passRate));
        }
        return summaryList;
    }

    @Transactional(readOnly = true)
    public PeriodSummaryDTO getPeriodSummary(String periodType, int days) {
        ZoneOffset offset = OffsetDateTime.now().getOffset();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        OffsetDateTime start = startDate.atStartOfDay().atOffset(offset);
        OffsetDateTime end = endDate.atTime(LocalTime.MAX).atOffset(offset);

        List<TestResult> results = testResultRepository.findByExecutedAtBetweenWithTestCase(start, end);
        List<ExecutionRecord> executions = executionRecordRepository.findByStartTimeBetweenOrderByStartTimeDesc(start, end);

        PeriodSummaryDTO periodDTO = new PeriodSummaryDTO();
        periodDTO.setPeriodType(periodType.toUpperCase());
        periodDTO.setStartDate(startDate.toString());
        periodDTO.setEndDate(endDate.toString());
        periodDTO.setTotalExecutions(executions.size());
        periodDTO.setTotalTests(results.size());

        int passed = (int) results.stream().filter(r -> r.getStatus() != null && "PASSED".equalsIgnoreCase(r.getStatus().name())).count();
        int failed = (int) results.stream().filter(r -> r.getStatus() != null && "FAILED".equalsIgnoreCase(r.getStatus().name())).count();
        periodDTO.setPassedTests(passed);
        periodDTO.setFailedTests(failed);
        periodDTO.setPassRate(results.size() > 0 ? Math.round(((double) passed / results.size()) * 1000.0) / 10.0 : 0.0);

        int uiCount = (int) results.stream().filter(r -> r.getTestCase() != null && TestType.UI.equals(r.getTestCase().getType())).count();
        int apiCount = (int) results.stream().filter(r -> r.getTestCase() != null && TestType.API.equals(r.getTestCase().getType())).count();
        periodDTO.setUiTestsCount(uiCount);
        periodDTO.setApiTestsCount(apiCount);

        periodDTO.setDailyBreakdown(getLastNDaysSummary(days));

        return periodDTO;
    }
}
