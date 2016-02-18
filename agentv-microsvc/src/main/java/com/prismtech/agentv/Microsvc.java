package com.prismtech.agentv;


import org.omg.dds.sub.Subscriber;
import org.omg.dds.pub.Publisher;

/**
 * This interface defines the protocol that has to be supported by the
 * applications that want to be deployed as agentv's capsules.
 *
 *
 * @author <a href="mailto:angelo.corsaro@prismtech.com"> Angelo Corsaro</a>
 * @version 0.1.0
 */
public interface Microsvc {

    /**
     * This is the first operation called on a microservice by the
     * agentv runtime.  As such this operation should initialise the
     * application and ensure that it is in a state that would allow
     * the application to be started.
     *
     * @param pub the publisher to be used 
     *        to create DataWriters
     *
     * @param sub the subscriber  to be used 
     *        to create DataReaders.
     *
     * @param args the command line arguments
     */
    void init(Publisher pub, Subscriber sub, String[] args);


    /**
     * Starts the execution of the microsvc
     *
     * @return true if the execution has completed, false if the exeution is continuion on
     * other threads.
     */
    boolean start();

    void stop();

    /**
     * Should release resources any I/O resources.
     */
    void close();
}
