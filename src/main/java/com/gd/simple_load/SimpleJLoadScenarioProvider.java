package com.gd.simple_load;

import com.gd.aux.ExampleCustomHttpInvokerProvider;
import com.gd.util.JaggerPropertiesProvider;
import com.griddynamics.jagger.engine.e1.collector.CollectThreadsTestListener;
import com.griddynamics.jagger.engine.e1.collector.DefaultResponseValidatorProvider;
import com.griddynamics.jagger.engine.e1.collector.ExampleResponseValidatorProvider;
import com.griddynamics.jagger.engine.e1.collector.NotNullResponseValidator;
import com.griddynamics.jagger.engine.e1.collector.invocation.NotNullInvocationListener;
import com.griddynamics.jagger.engine.e1.collector.loadscenario.ExampleLoadScenarioListener;
import com.griddynamics.jagger.engine.e1.collector.testgroup.ExampleTestGroupListener;
import com.griddynamics.jagger.invoker.RoundRobinLoadBalancer;
import com.griddynamics.jagger.user.test.configurations.JLoadScenario;
import com.griddynamics.jagger.user.test.configurations.JLoadTest;
import com.griddynamics.jagger.user.test.configurations.JParallelTestsGroup;
import com.griddynamics.jagger.user.test.configurations.JTestDefinition;
import com.griddynamics.jagger.user.test.configurations.auxiliary.Id;
import com.griddynamics.jagger.user.test.configurations.limits.JLimit;
import com.griddynamics.jagger.user.test.configurations.limits.JLimitVsBaseline;
import com.griddynamics.jagger.user.test.configurations.limits.JLimitVsRefValue;
import com.griddynamics.jagger.user.test.configurations.limits.auxiliary.*;
import com.griddynamics.jagger.user.test.configurations.load.JLoadProfile;
import com.griddynamics.jagger.user.test.configurations.load.JLoadProfileRps;
import com.griddynamics.jagger.user.test.configurations.load.auxiliary.RequestsPerSecond;
import com.griddynamics.jagger.user.test.configurations.termination.JTerminationCriteria;
import com.griddynamics.jagger.user.test.configurations.termination.JTerminationCriteriaIterations;
import com.griddynamics.jagger.user.test.configurations.termination.auxiliary.IterationsNumber;
import com.griddynamics.jagger.user.test.configurations.termination.auxiliary.MaxDurationInSeconds;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * By extending {@link JaggerPropertiesProvider} you get access to all Jagger properties and test properties. You can use them for configuration of JLoadScenario.<p>
 * Benefit of this approach is that you can change JLoadScenario configuration by changing properties file and no recompilation is needed.<p>
 * Properties in test.properties do not override properties from environment.properties.
 */
@Configuration
public class SimpleJLoadScenarioProvider extends JaggerPropertiesProvider {

        @Bean
        public JLoadScenario simpleJaggerLoadScenario() {
            // Example of using JaggerPropertiesProvider
            Long iterationsNumber = Long.valueOf(getTestPropertyValue("p1.load.scenario.termination.iterations"));
            Long maxDurationInSeconds = Long.valueOf(getTestPropertyValue("p1.load.scenario.termination.max.duration.seconds"));

            JTerminationCriteria jTerminationCriteria = JTerminationCriteriaIterations
                    .of(IterationsNumber.of(iterationsNumber), MaxDurationInSeconds.of(maxDurationInSeconds));

            JLoadProfile jLoadProfileRps = JLoadProfileRps
                    .builder(RequestsPerSecond.of(Long.valueOf(getTestPropertyValue("p1.load.scenario.profile.rps"))))
                    .withMaxLoadThreads(Long.valueOf(getTestPropertyValue("p1.load.scenario.profile.max_threads")))
                    .withWarmUpTimeInMilliseconds(Long.valueOf(getTestPropertyValue("p1.load.scenario.profile.warmUpTime")))
                    .build();

            // For standard metrics use JMetricName.
            // JLimitVsRefValue is used to compare the results with the referenced value.
            JLimit successRateLimit = JLimitVsRefValue.builder(JMetricName.PERF_SUCCESS_RATE_OK, RefValue.of(1D))
                    // the threshold is relative.
                    .withOnlyWarnings(LowWarnThresh.of(Double.valueOf(getTestPropertyValue("p1.load.scenario.limit.suc_rate.low_warn"))),
                                       UpWarnThresh.of(Double.valueOf(getTestPropertyValue("p1.load.scenario.limit.suc_rate.up_warn"))))
                    .build();

            // For standard metrics use JMetricName.
            // JLimitVsBaseline is used to compare the results with the baseline.
            // Use 'chassis.engine.e1.reporting.session.comparison.baseline.session.id' to set baseline.
            JLimit throughputLimit = JLimitVsBaseline.builder(JMetricName.PERF_THROUGHPUT)
                    // the threshold is relative.
                    .withOnlyErrors(LowErrThresh.of(0.99), UpErrThresh.of(1.00001))
                    .build();

            // For standard metrics use JMetricName.
            // JMetricName.PERF_LATENCY_PERCENTILE is used to set limits for latency percentile metrics.
            JLimit latencyPercentileLimit = JLimitVsRefValue.builder(JMetricName.PERF_LATENCY_PERCENTILE(95D), RefValue.of(0.1D))
                    // the threshold is relative.
                    .withOnlyWarnings(LowWarnThresh.of(0.50), UpWarnThresh.of(1.5))
                    .build();

            // Example of using JaggerPropertiesProvider
            String testDefinitionComment = getTestPropertyValue("p1.test.definition.comment");

            JTestDefinition jTestDefinition_Sleep = JTestDefinition
                    .builder(Id.of("testDefinition_for_sleep_resource"), new SimpleEndpointsProvider(this))
                    // optional
                    .withComment(testDefinitionComment)
                    .withInvoker(ExampleCustomHttpInvokerProvider.nonVerbose())
                    .withQueryProvider(new SimpleSleepQueriesProvider(this))
                    .withLoadBalancer(new RoundRobinLoadBalancer())
                    .addValidator(new ExampleResponseValidatorProvider("we are always good"))
                    .addValidator(DefaultResponseValidatorProvider.of(NotNullResponseValidator.class))
                    .addListener(new NotNullInvocationListener())
                    .build();

            JLoadTest jLoadTest_Sleep = JLoadTest
                    .builder(Id.of("loadTest_for_sleep_resource"), jTestDefinition_Sleep, jLoadProfileRps, jTerminationCriteria)
                    .addListener(new CollectThreadsTestListener())
                    .withLimits(successRateLimit, throughputLimit, latencyPercentileLimit)
                    .build();

            JTestDefinition jTestDefinition_Text = JTestDefinition
                    .builder(Id.of("testDefinition_for_text_resource"), new SimpleEndpointsProvider(this))
                    //.withInvoker(ExampleCustomHttpInvokerProvider.nonVerbose())
                    .withQueryProvider(new SimpleTextQueriesProvider(this))
                    //.addValidator(new ExampleResponseValidatorProvider("are we always good?"))
                    .addValidator(DefaultResponseValidatorProvider.of(NotNullResponseValidator.class))
                    //.addListener(new NotNullInvocationListener())
                    .build();

            JLoadTest jLoadTest_Text = JLoadTest
                    .builder(Id.of("loadTest_for_text_resource"), jTestDefinition_Text, jLoadProfileRps, jTerminationCriteria)
                    .addListener(new CollectThreadsTestListener())
                    .withLimits(successRateLimit, throughputLimit, latencyPercentileLimit)
                    .build();

            JParallelTestsGroup jParallelTestsGroup = JParallelTestsGroup
                    .builder(Id.of("parallelTestsGroup"), jLoadTest_Sleep, jLoadTest_Text)
                    .addListener(new ExampleTestGroupListener())
                    .build();

            // For JLoadScenario which is supposed to be executed by Jagger its ID must be set to 'jagger.load.scenario.id.to.execute' property's value
            return JLoadScenario.builder(Id.of("simpleJaggerLoadScenario"), jParallelTestsGroup)
                    .addListener(new ExampleLoadScenarioListener())
                    .withLatencyPercentiles(Arrays.asList(10D, 25.5D, 42D, 95D))
                    .build();
        }
        // end: following section is used for docu generation - Detailed load test scenario configuration
}
