package com.testframework.regression;

import com.testframework.regression.domain.*;
import com.testframework.regression.repository.ExecutionRecordRepository;
import com.testframework.regression.repository.TestResultRepository;
import com.testframework.regression.service.TestResultService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestResultServiceTest {

    @Mock
    private TestResultRepository testResultRepository;

    @Mock
    private ExecutionRecordRepository executionRecordRepository;

    private TestResultService testResultService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testResultService = new TestResultService(testResultRepository, executionRecordRepository);
    }

    @Test
    public void testGetResultsForDate() {
        LocalDate today = LocalDate.now();

        TestCase tc1 = new TestCase();
        tc1.setId(1L);
        tc1.setName("Test_UI_1");
        tc1.setType(TestType.UI);

        TestCase tc2 = new TestCase();
        tc2.setId(2L);
        tc2.setName("Test_API_1");
        tc2.setType(TestType.API);

        TestResult r1 = new TestResult();
        r1.setId(101L);
        r1.setTestCase(tc1);
        r1.setStatus(TestStatus.PASSED);
        r1.setExecutedAt(OffsetDateTime.now());
        r1.setExecutionId("exec_1");

        TestResult r2 = new TestResult();
        r2.setId(102L);
        r2.setTestCase(tc2);
        r2.setStatus(TestStatus.FAILED);
        r2.setExecutedAt(OffsetDateTime.now());
        r2.setExecutionId("exec_1");
        r2.setMessage("API timeout");

        ExecutionRecord rec = new ExecutionRecord();
        rec.setExecutionId("exec_1");
        rec.setStatus("COMPLETED");

        when(testResultRepository.findByExecutedAtBetweenWithTestCase(any(), any())).thenReturn(Arrays.asList(r1, r2));
        when(executionRecordRepository.findByStartTimeBetweenOrderByStartTimeDesc(any(), any())).thenReturn(Collections.singletonList(rec));

        DayReportResponseDTO response = testResultService.getResultsForDate(today);

        assertThat(response).isNotNull();
        assertThat(response.getDate()).isEqualTo(today.toString());
        assertThat(response.getTotalTests()).isEqualTo(2);
        assertThat(response.getPassedTests()).isEqualTo(1);
        assertThat(response.getFailedTests()).isEqualTo(1);
        assertThat(response.getPassRate()).isEqualTo(50.0);
        assertThat(response.getExecutionCount()).isEqualTo(1);
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).getTestCaseName()).isEqualTo("Test_UI_1");
        assertThat(response.getResults().get(1).getMessage()).isEqualTo("API timeout");
    }

    @Test
    public void testGetLastNDaysSummary() {
        when(testResultRepository.findByExecutedAtBetweenOrderByExecutedAtDesc(any(), any())).thenReturn(Collections.emptyList());
        when(executionRecordRepository.findByStartTimeBetweenOrderByStartTimeDesc(any(), any())).thenReturn(Collections.emptyList());

        List<DaySummaryDTO> summary = testResultService.getLastNDaysSummary(30);

        assertThat(summary).hasSize(30);
        assertThat(summary.get(summary.size() - 1).getDate()).isEqualTo(LocalDate.now().toString());
    }

    @Test
    public void testGetPeriodSummaryWeekly() {
        TestCase tc1 = new TestCase();
        tc1.setId(1L);
        tc1.setName("Test_UI");
        tc1.setType(TestType.UI);

        TestResult r1 = new TestResult();
        r1.setId(101L);
        r1.setTestCase(tc1);
        r1.setStatus(TestStatus.PASSED);
        r1.setExecutedAt(OffsetDateTime.now());

        when(testResultRepository.findByExecutedAtBetweenWithTestCase(any(), any())).thenReturn(Collections.singletonList(r1));
        when(executionRecordRepository.findByStartTimeBetweenOrderByStartTimeDesc(any(), any())).thenReturn(Collections.emptyList());
        when(testResultRepository.findByExecutedAtBetweenOrderByExecutedAtDesc(any(), any())).thenReturn(Collections.emptyList());

        PeriodSummaryDTO period = testResultService.getPeriodSummary("WEEK", 7);

        assertThat(period).isNotNull();
        assertThat(period.getPeriodType()).isEqualTo("WEEK");
        assertThat(period.getTotalTests()).isEqualTo(1);
        assertThat(period.getPassedTests()).isEqualTo(1);
        assertThat(period.getPassRate()).isEqualTo(100.0);
        assertThat(period.getUiTestsCount()).isEqualTo(1);
        assertThat(period.getApiTestsCount()).isEqualTo(0);
        assertThat(period.getDailyBreakdown()).hasSize(7);
    }
}
