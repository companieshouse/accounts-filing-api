package uk.gov.companieshouse.accounts.filing.utils.mapping;

import java.util.HashMap;
import java.util.Map;

public class ImmutableConverter {

    private ImmutableConverter() {}

    /**
     * Makes immutable map; a mutable map.
     * 
     * @param immutableMap
     * @return
     */
    public static Map<String, Object> toMutableMap(final Map<String, Object> immutableMap) {
        return new HashMap<>(immutableMap);
    }
}
