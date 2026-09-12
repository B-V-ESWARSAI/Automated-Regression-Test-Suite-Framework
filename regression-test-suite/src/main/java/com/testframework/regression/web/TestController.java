package com.testframework.regression.web;

import com.testframework.regression.domain.*;
import com.testframework.regression.engine.TestIntegrationEngine;
import com.testframework.regression.service.TestCaseService;
import com.testframework.regression.service.TestResultService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/tests")
public class TestController {

    private final TestCaseService testCaseService;
    private final TestIntegrationEngine testIntegrationEngine;
    private final TestResultService testResultService;

    public TestController(TestCaseService testCaseService, 
                          TestIntegrationEngine testIntegrationEngine,
                          TestResultService testResultService) {
        this.testCaseService = testCaseService;
        this.testIntegrationEngine = testIntegrationEngine;
        this.testResultService = testResultService;
    }

    // Day-wise test results endpoint (defaults to current date if not provided)
    @GetMapping("/by-date")
    public ResponseEntity<DayReportResponseDTO> getResultsByDate(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(testResultService.getResultsForDate(queryDate));
    }

    // 30-Day day-by-day historical summary
    @GetMapping("/history/30days")
    public ResponseEntity<List<DaySummaryDTO>> getLast30DaysSummary(
            @RequestParam(name = "days", defaultValue = "30") int days) {
        return ResponseEntity.ok(testResultService.getLastNDaysSummary(days));
    }

    // Weekly summary (Last 7 Days)
    @GetMapping("/summary/weekly")
    public ResponseEntity<PeriodSummaryDTO> getWeeklySummary() {
        return ResponseEntity.ok(testResultService.getPeriodSummary("WEEK", 7));
    }

    // Monthly summary (Last 30 Days)
    @GetMapping("/summary/monthly")
    public ResponseEntity<PeriodSummaryDTO> getMonthlySummary() {
        return ResponseEntity.ok(testResultService.getPeriodSummary("MONTH", 30));
    }

    // Test Integration Engine APIs
    @PostMapping("/integrate")
    public ResponseEntity<?> createTestCase(@RequestBody TestCaseRequest request) {
        try {
            TestCase testCase = testIntegrationEngine.createTestCase(
                request.getName(), 
                request.getType(), 
                request.getDescription()
            );
            return ResponseEntity.ok(testCase);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error creating test case: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCase> getTestCase(@PathVariable Long id) {
        TestCase testCase = testIntegrationEngine.getTestCase(id);
        if (testCase != null) {
            return ResponseEntity.ok(testCase);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<TestCase>> getAllTestCases() {
        return ResponseEntity.ok(testIntegrationEngine.getAllTestCases());
    }

    // Legacy endpoints for backward compatibility
    @PostMapping
    public ResponseEntity<TestCase> create(@RequestBody TestCase testCase) {
        return ResponseEntity.ok(testCaseService.save(testCase));
    }

    @GetMapping("/list")
    public ResponseEntity<List<TestCase>> list() {
        return ResponseEntity.ok(testCaseService.findAll());
    }

    // Request DTOs
    public static class TestCaseRequest {
        private String name;
        private TestType type;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public TestType getType() { return type; }
        public void setType(TestType type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}