package com.librato.metrics;

import static com.librato.metrics.AssertionHelper.numeric;

import java.util.HashMap;
import java.util.Map;

/**
 * User: mihasya
 * Date: 6/17/12
 * Time: 10:01 PM
 * A class representing a single gauge reading
 * <p/>
 * See http://dev.librato.com/v1/post/metrics for an explanation of basic vs multi-sample gauge
 */
public class SingleValueGaugeMeasurement extends BaseMeasurement implements Measurement {

    private final Number reading;

    public SingleValueGaugeMeasurement(String name, Number reading) {
        this(name, null, reading);
    }
    
    public SingleValueGaugeMeasurement(String name, String source, Number reading) {
        super(name, source);
        this.reading = numeric(numeric(reading));
    }

    @Override
    public Map<String, Number> toMap() {
        Map<String, Number> value = new HashMap<String, Number>();
        value.put("value", reading);
        return value;
    }
}
