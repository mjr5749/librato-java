package com.librato.metrics;

import java.util.Map;

/**
 * User: mihasya
 * Date: 6/17/12
 * Time: 10:15 PM
 * Represents a Librato measurement
 */
public interface Measurement {
    public String getName();
    public String getSource();
    public Map<String, Number> toMap();
}
