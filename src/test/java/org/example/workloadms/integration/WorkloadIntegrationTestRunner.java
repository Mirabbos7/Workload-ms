package org.example.workloadms.integration;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/integration")
@ConfigurationParameters({
        @ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME,
                value = "org.example.workloadms.component,org.example.workloadms.integration"),
        @ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
                value = "pretty, html:target/cucumber-reports/integration/report.html, json:target/cucumber-reports/integration/report.json"),
        @ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
})
public class WorkloadIntegrationTestRunner {
}