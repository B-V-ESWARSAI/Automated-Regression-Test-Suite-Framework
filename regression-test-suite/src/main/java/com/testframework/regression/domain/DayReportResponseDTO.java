package com.testframework.regression.domain;

import java.util.ArrayList;
import java.util.List;

public class DayReportResponseDTO {
    private String date; // YYYY-MM-DD
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private double passRate;
    private int executionCount;
    private List<ExecutionRecord> executions = new ArrayList<>();
    private List<TestResultDetailDTO> results = new ArrayList<>();

    public DayReportResponseDTO() {}

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getFailedTests() { return failedTests; }
    public void setFailedTests(int failedTests) { this.failedTests = failedTests; }

    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }

    public int getExecutionCount() { return executionCount; }
    public void setExecutionCount(int executionCount) { this.executionCount = executionCount; }

    public List<ExecutionRecord> getExecutions() { return executions; }
    public void setExecutions(List<ExecutionRecord> executions) { this.executions = executions; }

    public List<TestResultDetailDTO> getResults() { return results; }
    public void setResults(List<TestResultDetailDTO> results) { this.results = results; }
}
