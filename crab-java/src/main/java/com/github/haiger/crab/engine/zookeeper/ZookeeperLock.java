package com.github.haiger.crab.engine.zookeeper;

import com.github.haiger.crab.engine.Lock;

/**
 * @author Haiger
 * @version $Id: ZookeeperLock.java, v 0.1 2018-01-11 15:44:59 Haiger Exp $
 */
public class ZookeeperLock implements Lock {
    @Override
    public void lock(String lockName, String lockValue, long leaseTime) {

    }

    @Override
    public boolean tryLock(String lockName, String lockValue, long leaseTime) {
        return false;
    }

    @Override
    public void unlock(String lockName, String lockValue) {

    }

    @Override
    public boolean renewLock(String lockName, String lockValue, long leaseTime) {
        return false;
    }

    @Override
    public void shutDown() {

    }
}
