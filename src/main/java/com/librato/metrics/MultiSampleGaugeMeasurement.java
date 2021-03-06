package com.librato.metrics;

import static com.librato.metrics.AssertionHelper.numeric;

import java.util.HashMap;
import java.util.Map;


/**
 * User: mihasya
 * Date: 6/17/12
 * Time: 10:06 PM
 * a class for representing a gauge reading that might come from multiple samples
 * <p/>
 * See http://dev.librato.com/v1/post/metrics for why some fields are optional
 */
public class MultiSampleGaugeMeasurement extends BaseMeasurement implements Measurement {

    private final Long count;
    private final Number sum;
    private final Number max;
    private final Number min;
    private final Number sum_squares;

    public MultiSampleGaugeMeasurement(String name, Long count, Number sum, Number max, Number min,
                                       Number sum_squares) {
        this(name, null, count, sum, max, min, sum_squares);
    }
    
    public MultiSampleGaugeMeasurement(String name, String source, Long count, Number sum, Number max, Number min,
            Number sum_squares) {
        super(name, source);
        
        if (count == null || count == 0) {
            throw new IllegalArgumentException("The Librato API requires the count to be > 0 for complex metrics. See http://dev.librato.com/v1/post/metrics");
        }

        this.count = count;
        this.sum = numeric(sum);
        this.max = numeric(max);
        this.min = numeric(min);
        this.sum_squares = numeric(sum_squares);
    }


    @Override
    public Map<String, Number> toMap() {
        Map<String, Number> result = new HashMap<String, Number>(5);
        result.put("count", count);
        result.put("sum", sum);
        if (max != null) {
            result.put("max", max);
        }
        if (min != null) {
            result.put("min", min);
        }
        if (sum_squares != null) {
            result.put("sum_squares", sum_squares);
        }
        return result;
    }
}
