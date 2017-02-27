package com.gd.user_scenarios;

import com.gd.util.JaggerPropertiesProvider;
import com.griddynamics.jagger.invoker.scenario.JHttpScenarioGlobalContext;
import com.griddynamics.jagger.invoker.scenario.JHttpUserScenario;
import com.griddynamics.jagger.invoker.scenario.JHttpUserScenarioStep;
import com.griddynamics.jagger.invoker.v2.JHttpEndpoint;
import com.griddynamics.jagger.invoker.v2.JHttpQuery;
import com.griddynamics.jagger.jaas.storage.model.TestExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Example of user scenario provider
 */
public class UserScenarioProvider implements Iterable {
    public static final String SCENARIO_ID = "jaas-user-scenario";
    public static final String SCENARIO_DISPLAY_NAME = "JaaS user Scenario";
    public static final String STEP_1_ID = "step1";
    public static final String STEP_2_ID = "step2";
    public static final String STEP_3_ID = "step3";

    private static Logger log = LoggerFactory.getLogger(UserScenarioProvider.class);

    private List<JHttpUserScenario> userScenarios = new ArrayList<>();

    public UserScenarioProvider(JaggerPropertiesProvider provider) {
        JHttpUserScenario userScenario = new JHttpUserScenario(SCENARIO_ID, SCENARIO_DISPLAY_NAME);

        String endpoint = provider.getTestPropertyValue("p2.aut.url");
        userScenario
                .withScenarioGlobalContext(new JHttpScenarioGlobalContext()
                        .withGlobalEndpoint(new JHttpEndpoint(endpoint)))
                .addStep(JHttpUserScenarioStep.builder(STEP_1_ID)
                        .withDisplayName("JaaS-1. GET environments.")
                        .withQuery(new JHttpQuery().get().path("/jaas/envs"))
                        .withWaitAfterExecutionInSeconds(1)
                        .build())
                .addStep(JHttpUserScenarioStep.builder(STEP_2_ID)
                        .withDisplayName("JaaS-1. GET executions.")
                        .withQuery(new JHttpQuery<TestExecutionEntity>().get().
                                path("/jaas/executions").
                                body(new TestExecutionEntity() {{setEnvId("1"); setLoadScenarioId("1"); setTestProjectURL("http://usr-scenario-odin");}}))
                        .withPostProcessFunction(response -> {
                            if (response.getStatus().is2xxSuccessful())
                                log.info("Successfully got list of executions.");

                            if (null != response.getBody()) {
                                log.info("------------------------ \n GET /executions response : {} \n ------------------------", new String((byte[]) response.getBody()));
                            }
                            return true;
                        })
                        .build())
                .addStep(JHttpUserScenarioStep.builder(STEP_3_ID)
                        .withDisplayName("JaaS-1. POST an execution.")
                        .withQuery(new JHttpQuery<TestExecutionEntity>().post().
                                path("/jaas/executions").
                                body(new TestExecutionEntity() {{setEnvId("2"); setLoadScenarioId("2"); setTestProjectURL("http://usr-scenario-odin");}}))
                        .build());

        JHttpUserScenario userScenario2 = new JHttpUserScenario(SCENARIO_ID, SCENARIO_DISPLAY_NAME);

        userScenario2
                .withScenarioGlobalContext(new JHttpScenarioGlobalContext()
                        .withGlobalEndpoint(new JHttpEndpoint(endpoint)))
                .addStep(JHttpUserScenarioStep.builder(STEP_1_ID)
                        .withDisplayName("JaaS-2. GET executions.")
                        .withQuery(new JHttpQuery<TestExecutionEntity>().get().
                                path("/jaas/executions").
                                body(new TestExecutionEntity() {{setEnvId("1"); setLoadScenarioId("1"); setTestProjectURL("http://usr-scenario-dva");}}))
                        .build());

        userScenarios.add(userScenario);
        userScenarios.add(userScenario2);
    }

    @Override
    public Iterator<JHttpUserScenario> iterator() {
        return userScenarios.iterator();
    }
}

