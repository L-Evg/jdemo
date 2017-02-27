package com.gd.simple_load;

import com.gd.util.JaggerPropertiesProvider;
import com.griddynamics.jagger.invoker.v2.JHttpQuery;

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
    private Integer propValue;
    private boolean isExtraLoadMode = false; //TODO: Re-factor, avoid duplicates.
    private int extraLoadCoef;

    public SimpleSleepQueriesProvider(JaggerPropertiesProvider provider) {
        this.propValue = Integer.valueOf(provider.getTestPropertyValue("p1.aut.sleep_time"));
        this.isExtraLoadMode = Boolean.valueOf(provider.getTestPropertyValue("load.extra"));
        this.extraLoadCoef = Integer.valueOf(provider.getTestPropertyValue("load.extra.coef"));
    }

    @Override
    public Iterator iterator() {
        List<JHttpQuery> queries = new ArrayList<>();
        queries.add(new JHttpQuery()
                .get()
                .path(getValue().toString()));

        queries.add(new JHttpQuery()
                .get()
                .responseBodyType(String.class)
                .path(Integer.toString(50 + new Random().nextInt(getValue()))));

        return queries.iterator();
    }

    private Integer getValue(){
        if (isExtraLoadMode) {
            return propValue * extraLoadCoef;
        }

        return propValue;
    }
}
// end: following section is used for docu generation - Query provider

