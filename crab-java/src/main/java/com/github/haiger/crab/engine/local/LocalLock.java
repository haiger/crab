package com.github.haiger.crab.engine.local;

import com.github.haiger.crab.engine.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Haiger
 * @version $Id: LocalLock.java, v 0.1 2018-01-11 17:08:18 Haiger Exp $
 */
public class LocalLock implements Lock {
    private static final Logger LOG = LoggerFactory.getLogger(LocalLock.class);

    private long spinIntervalTime;
    private ConcurrentHashMap<String/*lockName*/, String/*lockValue*/> lockMap = new ConcurrentHashMap<>();

    public LocalLock(long spinIntervalTime) {
        this.spinIntervalTime = spinIntervalTime;
    }

    @Override
    public void lock(String lockName, String lockValue, long leaseTime) {
        while (true) {
            if (tryLock(lockName, lockValue, leaseTime)) {
                break;
            }

            try {
                Thread.sleep(spinIntervalTime + new Random().nextInt(20));
            } catch (InterruptedException e) {// TODO, throw interruptedException to service.
                LOG.error("lock(" + lockName + ") has be interrupted.");
            }
        }
    }

    @Override
    public boolean tryLock(String lockName, String lockValue, long leaseTime) {
        boolean isLocked = false;
        String oldLockVaule = lockMap.putIfAbsent(lockName, lockValue);
        if (oldLockVaule == null) {
            isLocked = true;
            LOG.info("locked---lockName:" + lockName + "--lockValue:" + lockValue);
        }

        return isLocked;
    }

    @Override
    public void unlock(String lockName, String lockValue) {
        String currentLockValue = lockMap.remove(lockName);
        LOG.info("releaseLock---lockName:" + lockName + "--lockValue:" + currentLockValue);
    }

    @Override
    public boolean renewLock(String lockName, String lockValue, long leaseTime) {
        return true;
    }

    @Override
    public void shutDown() {
        // do nothing
        LOG.info("localLock shutDown.");
    }

}
