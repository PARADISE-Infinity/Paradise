/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.util;

/**
 * Lock that is blocking or free based on a condition that can be set.
 */
public class ConditionLock {

    private volatile boolean condition;

    /**
     * Create a Lock that is blocking or free based on a condition that can be set.
     * 
     * @param initialState whether the lock is initially free (i.e. the condition is true) or not
     * @see {@link #await()}
     * @see {@link #setCondition(boolean)}
     */
    public ConditionLock(boolean initialState) {
        condition = initialState;
    }

    /**
     * Blocks if the condition of the Lock is false, returns immediately otherwise.
     * 
     * @see {@link #setCondition(boolean)}
     */
    public void await() {
        synchronized (this) {
            try {
                while (!condition) {
                    wait();
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }

    }

    /**
     * Set the condition of this Lock. If it is set to true any potential waiting Threads are resumed, if it is set to false, future calls
     * of {@link #await()} will block.
     * 
     * @param state whether the lock is free or not
     * @see {@link #await()}
     */
    public void setCondition(boolean state) {
        synchronized (this) {
            condition = state;
            if (condition) {
                notifyAll();
            }
        }
    }

}
