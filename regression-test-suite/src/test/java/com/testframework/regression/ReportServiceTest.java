package com.testframework.regression;

import com.testframework.regression.domain.TestCase;
import com.testframework.regression.domain.TestResult;
import com.testframework.regression.domain.TestStatus;
import com.testframework.regression.domain.TestType;
import com.testframework.regression.repository.TestResultRepository;
import com.testframework.regression.service.ReportService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ReportServiceTest {

    @Mock
    private TestResultRepository testResultRepository;

    private ReportService reportService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reportService = new ReportService(testResultRepository);
    }

    @Test
    public void testGenerateHTMLReportForDate() throws Exception {
        TestCase tc = new TestCase();
        tc.setId(1L);
        tc.setName("BlazeDemo_HomePage_Test");
        tc.setType(TestType.UI);

        TestResult result = new TestResult();
        result.setId(10L);
        result.setTestCase(tc);
        result.setStatus(TestStatus.PASSED);
        result.setExecutedAt(OffsetDateTime.now());
        result.setExecutionId("exec_123");

        when(testResultRepository.findByExecutedAtBetweenWithTestCase(any(), any()))
                .thenReturn(Collections.singletonList(result));

        String path = reportService.generateHTMLReportForDate(LocalDate.now());

        assertThat(path).isNotNull();
        File file = new File(path);
        assertThat(file.exists()).isTrue();
        String content = Files.readString(Paths.get(path));
        assertThat(content).contains("Daily Test Execution Report");
        assertThat(content).contains("BlazeDemo_HomePage_Test");
    }

    @Test
    public void testGenerateCSVReportForDate() throws Exception {
        TestCase tc = new TestCase();
        tc.setId(2L);
        tc.setName("ReqRes_GetUsers_Page2");
        tc.setType(TestType.API);

        TestResult result = new TestResult();
        result.setId(20L);
        result.setTestCase(tc);
        result.setStatus(TestStatus.PASSED);
        result.setExecutedAt(OffsetDateTime.now());
        result.setExecutionId("exec_456");

        when(testResultRepository.findByExecutedAtBetweenWithTestCase(any(), any()))
                .thenReturn(Collections.singletonList(result));

        String path = reportService.generateCSVReportForDate(LocalDate.now());

        assertThat(path).isNotNull();
        File file = new File(path);
        assertThat(file.exists()).isTrue();
        String content = Files.readString(Paths.get(path));
        assertThat(content).contains("Execution ID,Test Case ID,Test Case Name,Type,Status,Executed At,Artifact(s),Message");
        assertThat(content).contains("ReqRes_GetUsers_Page2");
    }

    @Test
    public void testGenerateJSONReportForDate() throws Exception {
        TestCase tc = new TestCase();
        tc.setId(3L);
        tc.setName("ReqRes_CreateUser");
        tc.setType(TestType.API);

        TestResult result = new TestResult();
        result.setId(30L);
        result.setTestCase(tc);
        result.setStatus(TestStatus.PASSED);
        result.setExecutedAt(OffsetDateTime.now());
        result.setExecutionId("exec_789");

        when(testResultRepository.findByExecutedAtBetweenWithTestCase(any(), any()))
                .thenReturn(Collections.singletonList(result));

        String path = reportService.generateJSONReportForDate(LocalDate.now());

        assertThat(path).isNotNull();
        File file = new File(path);
        assertThat(file.exists()).isTrue();
        String content = Files.readString(Paths.get(path));
        assertThat(content).contains("\"reportType\" : \"DAILY\"");
        assertThat(content).contains("ReqRes_CreateUser");
    }
}
