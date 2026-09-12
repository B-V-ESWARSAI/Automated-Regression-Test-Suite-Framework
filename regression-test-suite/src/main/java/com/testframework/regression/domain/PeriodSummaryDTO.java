package com.testframework.regression.domain;

import java.util.ArrayList;
import java.util.List;

public class PeriodSummaryDTO {
    private String periodType; // WEEK or MONTH
    private String startDate; // YYYY-MM-DD
    private String endDate; // YYYY-MM-DD
    private int totalExecutions;
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private double passRate;
    private int uiTestsCount;
    private int apiTestsCount;
    private List<DaySummaryDTO> dailyBreakdown = new ArrayList<>();

    public PeriodSummaryDTO() {}

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getTotalExecutions() { return totalExecutions; }
    public void setTotalExecutions(int totalExecutions) { this.totalExecutions = totalExecutions; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getFailedTests() { return failedTests; }
    public void setFailedTests(int failedTests) { this.failedTests = failedTests; }

    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }

    public int getUiTestsCount() { return uiTestsCount; }
    public void setUiTestsCount(int uiTestsCount) { this.uiTestsCount = uiTestsCount; }

    public int getApiTestsCount() { return apiTestsCount; }
    public void setApiTestsCount(int apiTestsCount) { this.apiTestsCount = apiTestsCount; }

    public List<DaySummaryDTO> getDailyBreakdown() { return dailyBreakdown; }
    public void setDailyBreakdown(List<DaySummaryDTO> dailyBreakdown) { this.dailyBreakdown = dailyBreakdown; }
}
