package com.prismtech.agentv;

import java.util.concurrent.TimeUnit;

/**
 * This interface defines the protocol that has to be supported by the
 *  applications that want to be deployed as agentv's capsules.
 *
 *
 * @author <a href="mailto:angelo.corsaro@prismtech.com"> Angelo Corsaro</a>
 * @version 0.1.0
 */
public interface PeriodicMicrosvc extends Microsvc {

    Duration getPeriod();

    /**
     * This method is called by the microservice framework with the
     * period provided by getPeriod.
     *
     * Invoking the schedule operation on a stopped microservice should
     * raise an java.lang.IllegalStateException
     *
     * Notice that only a microsvc in the started state can be scheduled
     */
    void schedule();
}
