package com.gd.user_scenarios;

import com.gd.util.JaggerPropertiesProvider;
import com.griddynamics.jagger.invoker.scenario.JHttpUserScenarioInvocationListener;
import com.griddynamics.jagger.invoker.scenario.JHttpUserScenarioInvokerProvider;
import com.griddynamics.jagger.user.test.configurations.JLoadScenario;
import com.griddynamics.jagger.user.test.configurations.JLoadTest;
import com.griddynamics.jagger.user.test.configurations.JParallelTestsGroup;
import com.griddynamics.jagger.user.test.configurations.JTestDefinition;
import com.griddynamics.jagger.user.test.configurations.auxiliary.Id;
import com.griddynamics.jagger.user.test.configurations.limits.JLimit;
import com.griddynamics.jagger.user.test.configurations.limits.JLimitVsRefValue;
import com.griddynamics.jagger.user.test.configurations.limits.auxiliary.LowErrThresh;
import com.griddynamics.jagger.user.test.configurations.limits.auxiliary.RefValue;
import com.griddynamics.jagger.user.test.configurations.limits.auxiliary.UpErrThresh;
import com.griddynamics.jagger.user.test.configurations.load.JLoadProfile;
import com.griddynamics.jagger.user.test.configurations.load.JLoadProfileInvocation;
import com.griddynamics.jagger.user.test.configurations.load.auxiliary.InvocationCount;
import com.griddynamics.jagger.user.test.configurations.load.auxiliary.ThreadCount;
import com.griddynamics.jagger.user.test.configurations.termination.JTerminationCriteria;
import com.griddynamics.jagger.user.test.configurations.termination.JTerminationCriteriaIterations;
import com.griddynamics.jagger.user.test.configurations.termination.auxiliary.IterationsNumber;
import com.griddynamics.jagger.user.test.configurations.termination.auxiliary.MaxDurationInSeconds;
import com.griddynamics.jagger.util.StandardMetricsNamesUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.griddynamics.jagger.util.StandardMetricsNamesUtil.generateMetricId;
import static com.griddynamics.jagger.util.StandardMetricsNamesUtil.generateScenarioStepId;

/**
 * Example of user scenario load scenario
 */
@Configuration
public class JLoadUserScenarioProvider extends JaggerPropertiesProvider {

    @Bean
    public JLoadScenario userScenario() {

        JTestDefinition jTestDefinition = JTestDefinition.builder(Id.of("td_usr_scn"), new UserScenarioProvider(this))
                .withInvoker(new JHttpUserScenarioInvokerProvider())
                .addListener(new JHttpUserScenarioInvocationListener())
                .build();

        JLoadProfile jLoadProfileInvocations = JLoadProfileInvocation.builder(
                 InvocationCount.of(Integer.valueOf(getTestPropertyValue("p2.load.scenario.profile.iterations"))),
                 ThreadCount.of(Integer.valueOf(getTestPropertyValue("p2.load.scenario.profile.max_threads"))))
                .build();

        JTerminationCriteria jTerminationCriteria = JTerminationCriteriaIterations.of(
                IterationsNumber.of(Integer.valueOf(getTestPropertyValue("p2.load.scenario.termination.iterations"))),
                MaxDurationInSeconds.of(Integer.valueOf(getTestPropertyValue("p2.load.scenario.termination.max.duration.seconds"))));

        String stepId = generateScenarioStepId(UserScenarioProvider.SCENARIO_ID, UserScenarioProvider.STEP_1_ID, 1);
        String metricId = generateMetricId(stepId, StandardMetricsNamesUtil.LATENCY_ID);
        JLimit firstStepLimit = JLimitVsRefValue.builder(metricId + "-avg", RefValue.of(1.5))
                .withOnlyErrors(LowErrThresh.of(0.5), UpErrThresh.of(1.5))
                .build();

        JLoadTest jLoadTest = JLoadTest.builder(Id.of("lt_usr_scn"), jTestDefinition, jLoadProfileInvocations, jTerminationCriteria)
                .withLimits(firstStepLimit)
                .build();

        JParallelTestsGroup jParallelTestsGroup = JParallelTestsGroup.builder(Id.of("ptg_usr_scenarios"), jLoadTest).build();

        return JLoadScenario.builder(Id.of("userScenario"), jParallelTestsGroup).build();
    }
}

