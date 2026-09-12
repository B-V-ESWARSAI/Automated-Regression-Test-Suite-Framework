package com.testframework.regression.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.testframework.regression.domain.TestResult;
import com.testframework.regression.domain.TestResultDetailDTO;
import com.testframework.regression.repository.TestResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    private static final String REPORT_DIR = "test-output/reports";
    private final TestResultRepository testResultRepository;
    private final ObjectMapper objectMapper;
    
    public ReportService(TestResultRepository testResultRepository) {
        this.testResultRepository = testResultRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Files.createDirectories(Paths.get(REPORT_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Transactional(readOnly = true)
    public String generateHTMLReport(String executionId) {
        try {
            List<TestResult> results = testResultRepository.findByExecutionIdWithTestCase(executionId);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "test_report_" + executionId + "_" + timestamp + ".html";
            Path filePath = Paths.get(REPORT_DIR, fileName);
            
            String html = buildHtmlReport("Execution Report - " + executionId, "Execution ID: " + executionId, results);
            Files.write(filePath, html.getBytes());
            return filePath.toString();
        } catch (IOException e) {
            return "Report generation failed: " + e.getMessage();
        }
    }
    
    @Transactional(readOnly = true)
    public String generateCSVReport(String executionId) {
        try {
            List<TestResult> results = testResultRepository.findByExecutionIdWithTestCase(executionId);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "test_report_" + executionId + "_" + timestamp + ".csv";
            Path filePath = Paths.get(REPORT_DIR, fileName);
            
            writeCsvFile(filePath, results);
            return filePath.toString();
        } catch (IOException e) {
            return "CSV report generation failed: " + e.getMessage();
        }
    }
    
    @Transactional(readOnly = true)
    public String collectLogs(String executionId) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "test_logs_" + executionId + "_" + timestamp + ".txt";
            Path filePath = Paths.get(REPORT_DIR, fileName);
            
            StringBuilder logs = new StringBuilder();
            logs.append("Test Execution Logs\n");
            logs.append("==================\n");
            logs.append("Execution ID: ").append(executionId).append("\n");
            logs.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            
            List<TestResult> results = testResultRepository.findByExecutionIdWithTestCase(executionId);
            for (TestResult result : results) {
                logs.append("Test Case: ").append(result.getTestCase() != null ? result.getTestCase().getName() : "Unknown").append("\n");
                logs.append("Type: ").append(result.getTestCase() != null ? result.getTestCase().getType() : "Unknown").append("\n");
                logs.append("Status: ").append(result.getStatus()).append("\n");
                logs.append("Executed At: ").append(result.getExecutedAt()).append("\n");
                if (result.getMessage() != null) {
                    logs.append("Message: ").append(result.getMessage()).append("\n");
                }
                logs.append("---\n");
            }
            
            Files.write(filePath, logs.toString().getBytes());
            return filePath.toString();
        } catch (IOException e) {
            return "Log collection failed: " + e.getMessage();
        }
    }

    @Transactional(readOnly = true)
    public String generateJUnitReport(String executionId) {
        try {
            List<TestResult> results = testResultRepository.findByExecutionIdWithTestCase(executionId);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "junit_report_" + executionId + "_" + timestamp + ".xml";
            Path filePath = Paths.get(REPORT_DIR, fileName);

            long tests = results.size();
            long failures = results.stream().filter(r -> !"PASSED".equals(r.getStatus().name())).count();
            OffsetDateTime min = results.stream().map(TestResult::getExecutedAt).min(Comparator.naturalOrder()).orElse(null);
            OffsetDateTime max = results.stream().map(TestResult::getExecutedAt).max(Comparator.naturalOrder()).orElse(null);
            long durationSec = (min != null && max != null) ? java.time.Duration.between(min, max).toSeconds() : 0;

            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<testsuite name=\"RegressionSuite\" tests=\"").append(tests)
               .append("\" failures=\"").append(failures)
               .append("\" time=\"").append(durationSec)
               .append("\">\n");

            for (TestResult r : results) {
                String className = r.getTestCase() != null && r.getTestCase().getType() != null ? r.getTestCase().getType().name() : "Test";
                String testName = r.getTestCase() != null ? r.getTestCase().getName() : "Unknown";
                xml.append("  <testcase classname=\"").append(className)
                   .append("\" name=\"").append(testName).append("\">\n");
                if (!"PASSED".equals(r.getStatus().name())) {
                    xml.append("    <failure message=\"")
                       .append(escapeXml(r.getMessage()))
                       .append("\"/>\n");
                }
                xml.append("  </testcase>\n");
            }

            xml.append("</testsuite>\n");
            Files.write(filePath, xml.toString().getBytes());
            return filePath.toString();
        } catch (IOException e) {
            return "JUnit report generation failed: " + e.getMessage();
        }
    }

    // --- Date-specific Report Generation ---

    @Transactional(readOnly = true)
    public String generateHTMLReportForDate(LocalDate date) {
        try {
            ZoneOffset offset = OffsetDateTime.now().getOffset();
            OffsetDateTime start = date.atStartOfDay().atOffset(offset);
            OffsetDateTime end = date.atTime(LocalTime.MAX).atOffset(offset);

            List<TestResult> results = testResultRepository.findByExecutedAtBetweenWithTestCase(start, end);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "test_report_date_" + date + "_" + timestamp + ".html";
            Path filePath = Paths.get(REPORT_DIR, fileName);

            String html = buildHtmlReport("Daily Test Execution Report - " + date, "Date: " + date, results);
            Files.write(filePath, html.getBytes());
            return filePath.toString();
        } catch (IOException e) {
            return "Report generation failed: " + e.getMessage();
        }
    }

    @Transactional(readOnly = true)
    public String generateCSVReportForDate(LocalDate date) {
        try {
            ZoneOffset offset = OffsetDateTime.now().getOffset();
            OffsetDateTime start = date.atStartOfDay().atOffset(offset);
            OffsetDateTime end = date.atTime(LocalTime.MAX).atOffset(offset);

            List<TestResult> results = testResultRepository.findByExecutedAtBetweenWithTestCase(start, end);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "test_report_date_" + date + "_" + timestamp + ".csv";
            Path filePath = Paths.get(REPORT_DIR, fileName);

            writeCsvFile(filePath, results);
            return filePath.toString();
        } catch (IOException e) {
            return "CSV report generation failed: " + e.getMessage();
        }
    }

    @Transactional(readOnly = true)
    public String generateJSONReportForDate(LocalDate date) {
        try {
            ZoneOffset offset = OffsetDateTime.now().getOffset();
            OffsetDateTime start = date.atStartOfDay().atOffset(offset);
            OffsetDateTime end = date.atTime(LocalTime.MAX).atOffset(offset);

            List<TestResult> results = testResultRepository.findByExecutedAtBetweenWithTestCase(start, end);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "test_report_date_" + date + "_" + timestamp + ".json";
            Path filePath = Paths.get(REPORT_DIR, fileName);

            Map<String, Object> data = buildReportDataMap("DAILY", date.toString(), date.toString(), results);
            objectMapper.writeValue(filePath.toFile(), data);
            return filePath.toString();
        } catch (IOException e) {
            return "JSON report generation failed: " + e.getMessage();
        }
    }

    // --- Period-specific (Weekly/Monthly) Report Generation ---

    @Transactional(readOnly = true)
    public String generateHTMLReportForPeriod(String periodType, int days) {
        try {
            ZoneOffset offset = OffsetDateTime.now().getOffset();
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);

            OffsetDateTime start = startDate.atStartOfDay().atOffset(offset);
            OffsetDateTime end = endDate.atTime(LocalTime.MAX).atOffset(offset);

            List<TestResult> results = testResultRepository.findByExecutedAtBetweenWithTestCase(start, end);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String title = (days <= 7 ? "Weekly" : "Monthly") + " Summary Report (" + startDate + " to " + endDate + ")";
            String fileName = "test_report_" + periodType.toLowerCase() + "_" + timestamp + ".html";
            Path filePath = Paths.get(REPORT_DIR, fileName);

            String html = buildHtmlReport(title, "Period: " + startDate + " to " + endDate, results);
            Files.write(filePath, html.getBytes());
            return filePath.toString();
        } catch (IOException e) {
            return "Report generation failed: " + e.getMessage();
        }
    }

    @Transactional(readOnly = true)
    public String generateCSVReportForPeriod(String periodType, int days) {
        try {
            ZoneOffset offset = OffsetDateTime.now().getOffset();
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);

            OffsetDateTime start = startDate.atStartOfDay().atOffset(offset);
            OffsetDateTime end = endDate.atTime(LocalTime.MAX).atOffset(offset);

            List<TestResult> results = testResultRepository.findByExecutedAtBetweenWithTestCase(start, end);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "test_report_" + periodType.toLowerCase() + "_" + timestamp + ".csv";
            Path filePath = Paths.get(REPORT_DIR, fileName);

            writeCsvFile(filePath, results);
            return filePath.toString();
        } catch (IOException e) {
            return "CSV report generation failed: " + e.getMessage();
        }
    }

    @Transactional(readOnly = true)
    public String generateJSONReportForPeriod(String periodType, int days) {
        try {
            ZoneOffset offset = OffsetDateTime.now().getOffset();
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);

            OffsetDateTime start = startDate.atStartOfDay().atOffset(offset);
            OffsetDateTime end = endDate.atTime(LocalTime.MAX).atOffset(offset);

            List<TestResult> results = testResultRepository.findByExecutedAtBetweenWithTestCase(start, end);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "test_report_" + periodType.toLowerCase() + "_" + timestamp + ".json";
            Path filePath = Paths.get(REPORT_DIR, fileName);

            Map<String, Object> data = buildReportDataMap(periodType.toUpperCase(), startDate.toString(), endDate.toString(), results);
            objectMapper.writeValue(filePath.toFile(), data);
            return filePath.toString();
        } catch (IOException e) {
            return "JSON report generation failed: " + e.getMessage();
        }
    }

    // --- Helper Methods ---

    private void writeCsvFile(Path filePath, List<TestResult> results) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.append("Execution ID,Test Case ID,Test Case Name,Type,Status,Executed At,Artifact(s),Message\n");
            for (TestResult result : results) {
                String artifacts = "";
                if (result.getScreenshotPath() != null) artifacts += Paths.get(result.getScreenshotPath()).getFileName().toString();
                if (result.getApiRequestPath() != null) artifacts += (artifacts.isEmpty() ? "" : "; ") + Paths.get(result.getApiRequestPath()).getFileName().toString();
                if (result.getApiResponsePath() != null) artifacts += (artifacts.isEmpty() ? "" : "; ") + Paths.get(result.getApiResponsePath()).getFileName().toString();

                String testCaseId = result.getTestCase() != null ? String.valueOf(result.getTestCase().getId()) : "";
                String testCaseName = result.getTestCase() != null ? result.getTestCase().getName() : "";
                String type = result.getTestCase() != null && result.getTestCase().getType() != null ? result.getTestCase().getType().name() : "";
                String status = result.getStatus() != null ? result.getStatus().name() : "";
                String executedAt = result.getExecutedAt() != null ? result.getExecutedAt().toString() : "";
                String execId = result.getExecutionId() != null ? result.getExecutionId() : "";
                String msg = result.getMessage() != null ? result.getMessage() : "";

                writer.append(escapeCsv(execId)).append(",");
                writer.append(testCaseId).append(",");
                writer.append(escapeCsv(testCaseName)).append(",");
                writer.append(type).append(",");
                writer.append(status).append(",");
                writer.append(executedAt).append(",");
                writer.append(escapeCsv(artifacts)).append(",");
                writer.append(escapeCsv(msg)).append("\n");
            }
        }
    }

    private Map<String, Object> buildReportDataMap(String type, String startDate, String endDate, List<TestResult> results) {
        long totalTests = results.size();
        long passedTests = results.stream().filter(r -> r.getStatus() != null && "PASSED".equalsIgnoreCase(r.getStatus().name())).count();
        long failedTests = totalTests - passedTests;
        double passRate = totalTests > 0 ? Math.round(((double) passedTests / totalTests) * 1000.0) / 10.0 : 0.0;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("reportType", type);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        map.put("totalTests", totalTests);
        map.put("passedTests", passedTests);
        map.put("failedTests", failedTests);
        map.put("passRate", passRate);

        List<TestResultDetailDTO> dtoList = results.stream().map(TestResultDetailDTO::new).collect(Collectors.toList());
        map.put("results", dtoList);
        return map;
    }

    private String buildHtmlReport(String title, String subtitle, List<TestResult> results) {
        long totalTests = results.size();
        long passedTests = results.stream().filter(r -> r.getStatus() != null && "PASSED".equalsIgnoreCase(r.getStatus().name())).count();
        long failedTests = totalTests - passedTests;
        double passRate = totalTests > 0 ? Math.round(((double) passedTests / totalTests) * 1000.0) / 10.0 : 0.0;

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='en'>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>").append(title).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f172a; color: #f8fafc; margin: 0; padding: 24px; }\n");
        html.append(".container { max-width: 1200px; margin: 0 auto; }\n");
        html.append(".header { border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 16px; margin-bottom: 24px; }\n");
        html.append(".title { font-size: 1.6rem; font-weight: 700; color: #38bdf8; margin: 0 0 6px 0; }\n");
        html.append(".subtitle { color: #94a3b8; font-size: 0.9rem; margin: 0; }\n");
        html.append(".metrics { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 14px; margin-bottom: 24px; }\n");
        html.append(".card { background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(255,255,255,0.08); border-radius: 12px; padding: 16px; }\n");
        html.append(".card-label { font-size: 0.8rem; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 6px; }\n");
        html.append(".card-val { font-size: 1.8rem; font-weight: 700; }\n");
        html.append(".val-pass { color: #34d399; }\n");
        html.append(".val-fail { color: #f87171; }\n");
        html.append(".val-rate { color: #38bdf8; }\n");
        html.append("table { width: 100%; border-collapse: collapse; background: rgba(30, 41, 59, 0.7); border-radius: 12px; overflow: hidden; }\n");
        html.append("th { background: #1e293b; color: #94a3b8; font-size: 0.8rem; text-transform: uppercase; padding: 12px 14px; text-align: left; border-bottom: 1px solid rgba(255,255,255,0.08); }\n");
        html.append("td { padding: 12px 14px; border-bottom: 1px solid rgba(255,255,255,0.04); font-size: 0.88rem; }\n");
        html.append(".badge { display: inline-block; padding: 3px 8px; border-radius: 6px; font-size: 0.75rem; font-weight: 700; }\n");
        html.append(".badge-pass { background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.4); }\n");
        html.append(".badge-fail { background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.4); }\n");
        html.append(".badge-ui { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }\n");
        html.append(".badge-api { background: rgba(6, 182, 212, 0.2); color: #22d3ee; }\n");
        html.append(".mono { font-family: monospace; font-size: 0.82rem; color: #cbd5e1; }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<div class='container'>\n");
        html.append("  <div class='header'>\n");
        html.append("    <h1 class='title'>⚡ ").append(escapeHtml(title)).append("</h1>\n");
        html.append("    <p class='subtitle'>").append(escapeHtml(subtitle)).append(" | Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n");
        html.append("  </div>\n");

        // Metrics Cards
        html.append("  <div class='metrics'>\n");
        html.append("    <div class='card'><div class='card-label'>Total Tests</div><div class='card-val'>").append(totalTests).append("</div></div>\n");
        html.append("    <div class='card'><div class='card-label'>Passed Tests</div><div class='card-val val-pass'>").append(passedTests).append("</div></div>\n");
        html.append("    <div class='card'><div class='card-label'>Failed Tests</div><div class='card-val val-fail'>").append(failedTests).append("</div></div>\n");
        html.append("    <div class='card'><div class='card-label'>Pass Rate</div><div class='card-val val-rate'>").append(passRate).append("%</div></div>\n");
        html.append("  </div>\n");

        // Table
        html.append("  <table>\n");
        html.append("    <thead>\n");
        html.append("      <tr><th>ID</th><th>Test Case</th><th>Type</th><th>Status</th><th>Execution ID</th><th>Executed At</th><th>Message</th></tr>\n");
        html.append("    </thead>\n");
        html.append("    <tbody>\n");

        if (results.isEmpty()) {
            html.append("      <tr><td colspan='7' style='text-align:center; padding: 2rem; color: #94a3b8;'>No test executions found in this period.</td></tr>\n");
        } else {
            for (TestResult r : results) {
                String testId = r.getTestCase() != null ? "#" + r.getTestCase().getId() : "--";
                String testName = r.getTestCase() != null ? r.getTestCase().getName() : "Unknown";
                String type = r.getTestCase() != null && r.getTestCase().getType() != null ? r.getTestCase().getType().name() : "UI";
                String typeBadge = "UI".equalsIgnoreCase(type) ? "<span class='badge badge-ui'>UI</span>" : "<span class='badge badge-api'>API</span>";
                boolean isPassed = r.getStatus() != null && "PASSED".equalsIgnoreCase(r.getStatus().name());
                String statusBadge = isPassed ? "<span class='badge badge-pass'>PASSED</span>" : "<span class='badge badge-fail'>FAILED</span>";
                String execId = r.getExecutionId() != null ? r.getExecutionId() : "--";
                String time = r.getExecutedAt() != null ? r.getExecutedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "--";
                String msg = r.getMessage() != null ? r.getMessage() : "";

                html.append("      <tr>\n");
                html.append("        <td class='mono'>").append(testId).append("</td>\n");
                html.append("        <td><strong>").append(escapeHtml(testName)).append("</strong></td>\n");
                html.append("        <td>").append(typeBadge).append("</td>\n");
                html.append("        <td>").append(statusBadge).append("</td>\n");
                html.append("        <td class='mono'>").append(escapeHtml(execId)).append("</td>\n");
                html.append("        <td class='mono'>").append(time).append("</td>\n");
                html.append("        <td class='mono' style='max-width:300px; overflow:hidden; text-overflow:ellipsis;'>").append(escapeHtml(msg)).append("</td>\n");
                html.append("      </tr>\n");
            }
        }

        html.append("    </tbody>\n");
        html.append("  </table>\n");
        html.append("</div>\n");
        html.append("</body>\n</html>");
        return html.toString();
    }

    private static String escapeCsv(String val) {
        if (val == null) return "\"\"";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }

    private static String escapeHtml(String val) {
        if (val == null) return "";
        return val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String escapeXml(String in) {
        if (in == null) return "";
        return in.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                 .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
