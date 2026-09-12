package com.testframework.regression.scheduler;

import com.testframework.regression.engine.SuiteRegistry;
import com.testframework.regression.engine.TestIntegrationEngine;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@EnableScheduling
public class TestScheduler {

    private final TestIntegrationEngine testIntegrationEngine;
    private final SuiteRegistry suiteRegistry;

    public TestScheduler(TestIntegrationEngine testIntegrationEngine, SuiteRegistry suiteRegistry) {
        this.testIntegrationEngine = testIntegrationEngine;
        this.suiteRegistry = suiteRegistry;
    }

    // Daily schedule at 11:26 AM local time - run combined (UI + API)
    @Scheduled(cron = "0 26 11 * * *")
    public void runDailyCombinedSuite() {
        List<Long> uiIds = suiteRegistry.resolveSuiteToTestCaseIds("BLAZE_SMOKE").orElse(List.of());
        List<Long> apiIds = suiteRegistry.resolveSuiteToTestCaseIds("REQRES_SMOKE").orElse(List.of());
        List<Long> combined = new ArrayList<>(uiIds);
        combined.addAll(apiIds);
        testIntegrationEngine.executeParallel(combined);
    }
}


