package com.testframework.regression.domain;

import java.time.OffsetDateTime;

public class TestResultDetailDTO {
    private Long id;
    private Long testCaseId;
    private String testCaseName;
    private String testCaseType;
    private String testCaseDescription;
    private String status;
    private OffsetDateTime executedAt;
    private String executionId;
    private String message;
    private String screenshotPath;
    private String apiRequestPath;
    private String apiResponsePath;

    public TestResultDetailDTO() {}

    public TestResultDetailDTO(TestResult result) {
        if (result != null) {
            this.id = result.getId();
            if (result.getTestCase() != null) {
                this.testCaseId = result.getTestCase().getId();
                this.testCaseName = result.getTestCase().getName();
                this.testCaseType = result.getTestCase().getType() != null ? result.getTestCase().getType().name() : null;
                this.testCaseDescription = result.getTestCase().getDescription();
            }
            this.status = result.getStatus() != null ? result.getStatus().name() : "PENDING";
            this.executedAt = result.getExecutedAt();
            this.executionId = result.getExecutionId();
            this.message = result.getMessage();
            this.screenshotPath = result.getScreenshotPath();
            this.apiRequestPath = result.getApiRequestPath();
            this.apiResponsePath = result.getApiResponsePath();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTestCaseId() { return testCaseId; }
    public void setTestCaseId(Long testCaseId) { this.testCaseId = testCaseId; }

    public String getTestCaseName() { return testCaseName; }
    public void setTestCaseName(String testCaseName) { this.testCaseName = testCaseName; }

    public String getTestCaseType() { return testCaseType; }
    public void setTestCaseType(String testCaseType) { this.testCaseType = testCaseType; }

    public String getTestCaseDescription() { return testCaseDescription; }
    public void setTestCaseDescription(String testCaseDescription) { this.testCaseDescription = testCaseDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(OffsetDateTime executedAt) { this.executedAt = executedAt; }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }

    public String getApiRequestPath() { return apiRequestPath; }
    public void setApiRequestPath(String apiRequestPath) { this.apiRequestPath = apiRequestPath; }

    public String getApiResponsePath() { return apiResponsePath; }
    public void setApiResponsePath(String apiResponsePath) { this.apiResponsePath = apiResponsePath; }
}
