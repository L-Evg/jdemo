package com.gd.simple_load;

import com.gd.util.JaggerPropertiesProvider;
import com.griddynamics.jagger.invoker.v2.JHttpQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * A simple query provider. Provide queries for {@link SimpleJLoadScenarioProvider}.
 * An example of a query: /sleep/100
 *
 *  Provide two types of queries:
 *  fixed e.g.
 *   /sleep/100
 *   where "100" is a fixed value and does not change (in fact that is a runtime property).
 *   and random:
 *   /sleep/{random_int}
 *   where MAX(random) is equal to the fixed value mentioned above.
 *    *   //TODO: refactor, this one and the TextQueriesProvider should have one super.
 */
// begin: following section is used for docu generation - Query provider
public class SimpleSleepQueriesProvider  implements Iterable {
    private String propValue;

    public SimpleSleepQueriesProvider(JaggerPropertiesProvider provider) {
        this.propValue = provider.getTestPropertyValue("p1.aut.sleep_time");
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

/*    @Bean
    public String getPropValue(){
        String PROP_NAME = "p1.aut.sleep_time";

        return getTestPropertyValue(PROP_NAME);
    }*/
}
// end: following section is used for docu generation - Query provider

