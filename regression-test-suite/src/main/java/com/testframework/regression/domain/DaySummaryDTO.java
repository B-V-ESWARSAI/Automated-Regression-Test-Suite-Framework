package com.testframework.regression.domain;

public class DaySummaryDTO {
    private String date; // YYYY-MM-DD
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private int executionCount;
    private double passRate;

    public DaySummaryDTO() {}

    public DaySummaryDTO(String date, int totalTests, int passedTests, int failedTests, int executionCount, double passRate) {
        this.date = date;
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.executionCount = executionCount;
        this.passRate = passRate;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getFailedTests() { return failedTests; }
    public void setFailedTests(int failedTests) { this.failedTests = failedTests; }

    public int getExecutionCount() { return executionCount; }
    public void setExecutionCount(int executionCount) { this.executionCount = executionCount; }

    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }
}
