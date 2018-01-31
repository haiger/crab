package com.github.haiger.crab;

import com.github.haiger.crab.conf.LockEngine;
import com.github.haiger.crab.engine.Lock;
import com.github.haiger.crab.util.CrabException;
import com.github.haiger.crab.util.IdGenerator;
import com.github.haiger.crab.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Haiger
 * @version $Id: ReentrantLock.java, v 0.1 2018-01-11 15:46:18 Haiger Exp $
 */
public class ReentrantLock implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(ReentrantLock.class);

    private Lock lock;

    private String lockNamePrefix;
    private String lockNameSuffix;
    private long   lockLeaseTime;
    private long   lockRenewIntervalTime;

    private ThreadLocal<Map<String, String[]>> localLockKeyAndValue;
    private ConcurrentHashMap<String, String>  currentLock; //记录当前除leader_lock外的所有锁信息

    private ScheduledExecutorService lockRenewScheduler;
    private ThreadPoolExecutor       lockRenewExecutor;

    public ReentrantLock(LockContext context) throws CrabException {
        lock = context.getCurrentLockEngine();
        localLockKeyAndValue = new ThreadLocal<>();
        currentLock = new ConcurrentHashMap<>();
        this.lockNamePrefix = context.getLockNamePrefix();
        this.lockNameSuffix = context.getLockNameSuffix();
        this.lockLeaseTime = context.getLockLeaseTime();
        this.lockRenewIntervalTime = context.getLockRenewIntervalTime();

        if (context.getLockEngine() == LockEngine.REDIS) {
            this.lockRenewScheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory("Lock-Renew-Scheduler-"));

            this.lockRenewExecutor = new ThreadPoolExecutor(context.getLockRenewExecutorCoreSize(), context.getLockRenewExecutorMaxSize(),
                    0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(context.getLockRenewExecutorQueueSize()),
                    new NamedThreadFactory("Lock-Renew-Executor-"));

            lockRenewScheduler.scheduleAtFixedRate(() -> renewCurrentLock(), 0, lockRenewIntervalTime, TimeUnit.MILLISECONDS);
        }
    }

    public void lock(String bizName, String bizValue) {
        String lockName = getLockName(bizName);
        String lockValue = getLockValue(bizValue);

        if (!isReenterable(lockName)) {
            lock.lock(lockName, lockValue, lockLeaseTime);
            storeLocalLock(lockName, lockValue);
            currentLock.put(lockName, lockValue);
        }
    }

    public boolean tryLock(String bizName, String bizValue) {
        String lockName = getLockName(bizName);
        String lockValue = getLockValue(bizValue);

        if (isReenterable(lockName)) {
            return true;
        }

        boolean locked = lock.tryLock(lockName, lockValue, lockLeaseTime);
        if (locked) {
            storeLocalLock(lockName, lockValue);
            currentLock.put(lockName, lockValue);
        }
        return locked;
    }

    public void unlock(String bizName) {
        Map<String, String[]> localLock = localLockKeyAndValue.get();
        if (localLock != null) {
            String lockName = getLockName(bizName);
            String[] value = localLock.get(lockName);
            if (value != null) {
                String lockValue = value[0];
                int reentrantTimes = Integer.valueOf(value[1]);
                LOG.info("reentrantTimes: " + reentrantTimes);
                reentrantTimes = reentrantTimes - 1;
                value[1] = reentrantTimes + "";
                if (reentrantTimes == 0) {
                    currentLock.remove(lockName);
                    removeLocalLock(lockName);
                    lock.unlock(lockName, lockValue);
                    LOG.info("releaseLock---lockName:" + lockName + "--lockValue:" + lockValue);
                }
            }
        }
    }

    public void close() {
        if (lockRenewScheduler != null) {
            lockRenewScheduler.shutdown();
        }
        if (lockRenewExecutor != null) {
            lockRenewExecutor.shutdown();
        }

        while (!lockRenewExecutor.isTerminated()) {
        }

        if (lock != null) {
            lock.shutDown();
        }
    }

    private String getLockName(String bizName) {
        return lockNamePrefix + "_" + bizName + "_" + lockNameSuffix;
    }

    private String getLockValue(String bizValue) {
        if (bizValue == null) {
            return IdGenerator.id();
        }
        return bizValue + "_" + IdGenerator.id();
    }

    private void storeLocalLock(String lockName, String lockValue) {
        Map<String, String[]> localLock = localLockKeyAndValue.get();
        if (localLock == null) {
            localLock = new HashMap<>();
            String[] value = {lockValue, "1"};
            localLock.put(lockName, value);
            localLockKeyAndValue.set(localLock);
        } else {
            String[] value = localLock.get(lockName);
            if (value == null) {
                value = new String[2];
                value[0] = lockValue;
                value[1] = "1";
                localLock.put(lockName, value);
            }
        }
    }

    private boolean isReenterable(String lockName) {
        boolean isReenterable = getLocalLockValue(lockName) != null ? true : false;
        if (isReenterable) {
            incReentrantTimes(lockName);
        }
        return isReenterable;
    }

    private void incReentrantTimes(String lockName) {
        Map<String, String[]> localLock = localLockKeyAndValue.get();
        String[] value = localLock.get(lockName);
        int reentrantTimes = Integer.valueOf(value[1]);
        reentrantTimes = reentrantTimes + 1;
        value[1] = reentrantTimes + "";
    }

    private String[] getLocalLockValue(String lockName) {
        Map<String, String[]> localLock = localLockKeyAndValue.get();
        return localLock == null ? null : localLock.get(lockName);
    }

    private void removeLocalLock(String lockName) {
        Map<String, String[]> localLock = localLockKeyAndValue.get();
        if (localLock != null) {
            localLock.remove(lockName);
        }
    }

    private void renewCurrentLock() {
        for (Map.Entry<String, String> m : currentLock.entrySet()) {
            lockRenewExecutor.execute(() -> lock.renewLock(m.getKey(), m.getValue(), lockLeaseTime));
        }
    }
}
