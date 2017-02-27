package com.gd.simple_load;

import com.gd.util.JaggerPropertiesProvider;
import com.griddynamics.jagger.invoker.v2.JHttpQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * A simple query provider. Provide queries for {@link SimpleJLoadScenarioProvider}.
 * An example of a query: /net/text/100
 *
 *  Provide two types of queries:
 *  fixed e.g.
 *   /net/text/100
 *   where "100" is a fixed value and does not change (in fact that is a runtime property).
 *   and random:
 *   /net/text/{random_int}
 *   where MAX(random) is equal to the fixed value mentioned above.
 *   //TODO: refactor, this one and the SleepQueriesProvider should have one super.
 */
// begin: following section is used for docu generation - Query provider
public class SimpleTextQueriesProvider implements Iterable {
    private String propValue;

    public SimpleTextQueriesProvider(JaggerPropertiesProvider provider) {
        this.propValue = provider.getTestPropertyValue("p1.aut.text_amount");
    }

    @Override
    public Iterator iterator() {
        List<JHttpQuery> queries = new ArrayList<>();
        queries.add(new JHttpQuery()
                .get()
                .path(propValue));

        queries.add(new JHttpQuery()
                .get()
                .responseBodyType(String.class)
                .path(Integer.toString(50 + new Random().nextInt(Integer.valueOf(propValue)))));

        return queries.iterator();
    }
}
// end: following section is used for docu generation - Query provider

