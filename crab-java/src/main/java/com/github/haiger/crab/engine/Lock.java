package com.github.haiger.crab.engine;

/**
 * @author Haiger
 * @version $Id: Lock.java, v 0.1 2018-01-11 14:28:49 Haiger Exp $
 */
public interface Lock {

    void lock(String lockName, String lockValue, long leaseTime);

    boolean tryLock(String lockName, String lockValue, long leaseTime);

    void unlock(String lockName, String lockValue);

    boolean renewLock(String lockName, String lockValue, long leaseTime);

    void shutDown();
}
