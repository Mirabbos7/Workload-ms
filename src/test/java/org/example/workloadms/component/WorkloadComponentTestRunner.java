package org.example.workloadms.component;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/component")
@ConfigurationParameters({
        @ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME,
                value = "org.example.workloadms.component"),
        @ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
                value = "pretty, html:target/cucumber-reports/component/report.html, json:target/cucumber-reports/component/report.json"),
        @ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
})
public class WorkloadComponentTestRunner {
}
