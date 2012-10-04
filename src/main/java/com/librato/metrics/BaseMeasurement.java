package com.librato.metrics;

import static com.librato.metrics.AssertionHelper.notNull;


/*
 * Abstract base class for measurements that provides the name
 * and source field implementations.
 */
public abstract class BaseMeasurement implements Measurement {
    
    private String name;
    private String source;
    
    public BaseMeasurement(String name) {
        this(name, null);
    }
    
    public BaseMeasurement(String name, String source) {
        this.name = notNull(name);
        this.source = source;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSource() {
        return source;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(this.getName());
        
        if(this.getSource() != null)
            s.append("(").append(this.getSource()).append(")");
        
        s.append(" data=" + this.toMap());
        
        return s.toString();
    }

}
