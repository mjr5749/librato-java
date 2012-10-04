package com.librato.metrics;

import java.util.HashMap;
import java.util.Map;

import static com.librato.metrics.AssertionHelper.notNull;

/**
 * User: mihasya
 * Date: 6/17/12
 * Time: 10:25 PM
 * Represents a reading from a counter
 */
public class CounterMeasurement extends BaseMeasurement implements Measurement {

    private final Long count;

    public CounterMeasurement(String name, Long count) {
        this(name, null, count);
    }
    
    public CounterMeasurement(String name, String source, Long count) {
        super(name, source);
        this.count = notNull(count);
    }

    @Override
    public Map<String, Number> toMap() {
        Map<String, Number> value = new HashMap<String, Number>(1);
        value.put("value", count);
        return value;
    }
}
