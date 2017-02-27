package com.griddynamics.jdemo;

import com.griddynamics.util.JaggerPropertiesProvider;
import com.griddynamics.jagger.invoker.v2.JHttpQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple query provider. Provide queries for {@link JLoadScenarioProvider}.
 * An example of a query: /sleep/pulse/10000/3000 ("/sleep/pulse/{period}/{delayMax}")
 *
 */
// begin: following section is used for docu generation - Query provider
public class SimpleQueriesProvider implements Iterable {
    private boolean isExtraLoadMode = false;

    public SimpleQueriesProvider(JaggerPropertiesProvider provider) {
        this.isExtraLoadMode = provider.getExtraLoadFlag();
    }

    @Override
    public Iterator iterator() {
        List<JHttpQuery> queries = new ArrayList<>();

        final String subPath = "/sleep/pulse";
        if (isExtraLoadMode) {
            queries.add(new JHttpQuery()
                    .get()
                    .path(subPath + "/10000/3000"));
        } else {
            queries.add(new JHttpQuery()
                    .get()
                    .path(subPath + "/10000/1000"));
        }

        return queries.iterator();
    }
}
// end: following section is used for docu generation - Query provider

