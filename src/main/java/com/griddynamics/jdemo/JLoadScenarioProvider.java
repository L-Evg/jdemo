package com.griddynamics.jdemo;

import com.griddynamics.jagger.user.test.configurations.termination.JTerminationCriteriaBackground;
import com.griddynamics.util.JaggerPropertiesProvider;
import com.griddynamics.jagger.engine.e1.collector.loadscenario.ExampleLoadScenarioListener;
import com.griddynamics.jagger.engine.e1.collector.testgroup.ExampleTestGroupListener;
import com.griddynamics.jagger.invoker.scenario.JHttpUserScenarioInvocationListener;
import com.griddynamics.jagger.invoker.scenario.JHttpUserScenarioInvokerProvider;
import com.griddynamics.jagger.user.test.configurations.JLoadScenario;
import com.griddynamics.jagger.user.test.configurations.JLoadTest;
import com.griddynamics.jagger.user.test.configurations.JParallelTestsGroup;
import com.griddynamics.jagger.user.test.configurations.JTestDefinition;
import com.griddynamics.jagger.user.test.configurations.auxiliary.Id;
import com.griddynamics.jagger.user.test.configurations.limits.JLimit;
import com.griddynamics.jagger.user.test.configurations.limits.JLimitVsRefValue;
import com.griddynamics.jagger.user.test.configurations.limits.auxiliary.JMetricName;
import com.griddynamics.jagger.user.test.configurations.limits.auxiliary.LowErrThresh;
import com.griddynamics.jagger.user.test.configurations.limits.auxiliary.RefValue;
import com.griddynamics.jagger.user.test.configurations.limits.auxiliary.UpErrThresh;
import com.griddynamics.jagger.user.test.configurations.load.JLoadProfile;
import com.griddynamics.jagger.user.test.configurations.load.JLoadProfileInvocation;
import com.griddynamics.jagger.user.test.configurations.load.JLoadProfileRps;
import com.griddynamics.jagger.user.test.configurations.load.auxiliary.InvocationCount;
import com.griddynamics.jagger.user.test.configurations.load.auxiliary.RequestsPerSecond;
import com.griddynamics.jagger.user.test.configurations.load.auxiliary.ThreadCount;
import com.griddynamics.jagger.user.test.configurations.loadbalancer.JLoadBalancer;
import com.griddynamics.jagger.user.test.configurations.termination.JTerminationCriteria;
import com.griddynamics.jagger.user.test.configurations.termination.JTerminationCriteriaIterations;
import com.griddynamics.jagger.user.test.configurations.termination.auxiliary.IterationsNumber;
import com.griddynamics.jagger.user.test.configurations.termination.auxiliary.MaxDurationInSeconds;
import com.griddynamics.jagger.util.StandardMetricsNamesUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.griddynamics.jdemo.UserScenarioProvider.*;
import static com.griddynamics.jagger.user.test.configurations.loadbalancer.JLoadBalancer.DefaultLoadBalancer.ROUND_ROBIN;

/**
 * By extending {@link JaggerPropertiesProvider} you get access to all Jagger properties and test properties. You can use them for configuration of JLoadScenario.<p>
 * Benefit of this approach is that you can change JLoadScenario configuration by changing properties file and no recompilation is needed.<p>
 * Properties in test.properties do not override properties from environment.properties.
 */
@Configuration
public class JLoadScenarioProvider extends JaggerPropertiesProvider {

        @Bean
        public JLoadScenario demoJaggerLoadScenario() {
            JLoadTest jLoadTest_Simple = buildSimpleJaggerLoadTest();

            JLoadTest jLoadTest_UserScenario = buildUserScenarioLoadTest();

            JParallelTestsGroup jParallelTestsGroup = JParallelTestsGroup
                    .builder(Id.of("parallelTestsGroup"), jLoadTest_Simple, jLoadTest_UserScenario)
                    .addListener(new ExampleTestGroupListener())
                    .build();

            return JLoadScenario.builder(Id.of("demoJaggerLoadScenario"), jParallelTestsGroup)
                    .addListener(new ExampleLoadScenarioListener())
                    .withLatencyPercentiles(Arrays.asList(42D, 95D))
                    .build();
        }


    private JLoadTest buildSimpleJaggerLoadTest(){
        JTestDefinition jTestDefinition = JTestDefinition.builder(Id.of("td_simple"), new SimpleEndpointsProvider(this))
                                            .withQueryProvider(new SimpleQueriesProvider(this))
                                            .build();

        JLoadProfile jLoadProfileRps = JLoadProfileRps.builder(RequestsPerSecond.of(10)).
                withMaxLoadThreads(10).
                withWarmUpTimeInMilliseconds(10000).
                build();

        JTerminationCriteria jTerminationCriteria = JTerminationCriteriaBackground.getInstance();

        return  JLoadTest.builder(Id.of("lt_simple"), jTestDefinition, jLoadProfileRps, jTerminationCriteria).build();
    }

    private JLoadTest buildUserScenarioLoadTest() {

        JTestDefinition jTestDefinition =
                JTestDefinition.builder(Id.of("td_user_scenario"), new UserScenarioProvider(this))
                        .withInvoker(new JHttpUserScenarioInvokerProvider())
                        // Exclusive access - single scenario is executed only by one virtual user at a time. No parallel execution
                        // Random seed - different virtual users execute scenarios in different order
                        .withLoadBalancer(JLoadBalancer.builder(ROUND_ROBIN)
                                .withExclusiveAccess()
                                .withRandomSeed(1234)
                                .build())
                        .addListener(JHttpUserScenarioInvocationListener.builder()
                                .withLatencyAvgStddevAggregators()
                                .withLatencyMinMaxAggregators()
                                .withLatencyPercentileAggregators(50D, 95D, 99D)
                                .build())
                        .build();

        JLoadProfile jLoadProfileInvocations =
                JLoadProfileInvocation.builder(InvocationCount.of(10), ThreadCount.of(1))
                        .build();

        JTerminationCriteria jTerminationCriteria =
                JTerminationCriteriaIterations.of(IterationsNumber.of(10), MaxDurationInSeconds.of(120));

        // We are setting acceptance criteria for particular metric of the selected step in the scenario
        JLimit avgLatencyLimit =
                JLimitVsRefValue.builder(SCENARIO_ID, STEP_1_ID, StandardMetricsNamesUtil.LATENCY_AVG_AGG_ID, RefValue.of(1.2))
                        .withOnlyErrors(LowErrThresh.of(0.25), UpErrThresh.of(2.0))
                        .build();
        JLimit stdDevLatencyLimit =
                JLimitVsRefValue.builder(SCENARIO_ID, STEP_1_ID, StandardMetricsNamesUtil.LATENCY_STD_DEV_AGG_ID, RefValue.of(0.5))
                        .withOnlyErrors(LowErrThresh.of(0.5), UpErrThresh.of(1.5))
                        .build();
        JLimit maxLatencyLimit =
                JLimitVsRefValue.builder(SCENARIO_ID, STEP_2_ID, StandardMetricsNamesUtil.LATENCY_MAX_AGG_ID, RefValue.of(2.0))
                        .withOnlyErrors(LowErrThresh.of(0.5), UpErrThresh.of(1.5))
                        .build();
        JLimit minDevLatencyLimit =
                JLimitVsRefValue.builder(SCENARIO_ID, STEP_2_ID, StandardMetricsNamesUtil.LATENCY_MIN_AGG_ID, RefValue.of(0.2))
                        .withOnlyErrors(LowErrThresh.of(0.5), UpErrThresh.of(1.5))
                        .build();
        JLimit percentile99LatencyLimit =
                JLimitVsRefValue.builder(SCENARIO_ID, STEP_2_ID, JMetricName.PERF_LATENCY_PERCENTILE(99D), RefValue.of(2.0))
                        .withOnlyErrors(LowErrThresh.of(0.5), UpErrThresh.of(1.5))
                        .build();
        JLimit successRateLimit =
                JLimitVsRefValue.builder(SCENARIO_ID, STEP_1_ID, JMetricName.PERF_SUCCESS_RATE_OK, RefValue.of(1.0))
                        .withOnlyErrors(LowErrThresh.of(0.99), UpErrThresh.of(1.01))
                        .build();
        JLimit errorsLimit =
                JLimitVsRefValue.builder(SCENARIO_ID, STEP_2_ID, JMetricName.PERF_SUCCESS_RATE_FAILS, RefValue.of(0.0))
                        .withOnlyErrors(LowErrThresh.of(0.99), UpErrThresh.of(1.01))
                        .build();

        return JLoadTest.builder(Id.of("lt_user_scenario"), jTestDefinition, jLoadProfileInvocations, jTerminationCriteria)
                        .withLimits(avgLatencyLimit, stdDevLatencyLimit, minDevLatencyLimit, maxLatencyLimit, percentile99LatencyLimit, successRateLimit, errorsLimit)
                        .build();
    }

}