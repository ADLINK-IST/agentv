package com.prismtech.agentv;

import java.util.concurrent.TimeUnit;

/**
 * Represent a relative time measurement
 */
public class Duration {
    private long duration;
    private TimeUnit timeUnit;

    public Duration(long d, TimeUnit tu) {
        this.duration = d;
        this.timeUnit = tu;
    }

    public long getDuration() { return this.duration; }
    public TimeUnit getTimeUnit() { return this.timeUnit; }
}
