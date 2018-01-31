package com.github.haiger.crab;

import com.github.haiger.crab.engine.Lock;
import com.github.haiger.crab.util.CrabException;
import com.github.haiger.crab.util.IdGenerator;
import com.github.haiger.crab.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Haiger
 * @version $Id: LeaderElect.java, v 0.1 2018-01-11 16:02:00 Haiger Exp $
 */
public class LeaderElect implements Closeable, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(LeaderElect.class);

    private Lock        lock;
    private LockContext config;

    private LeaderElectListener listener;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, new NamedThreadFactory("LeaderElect-"));

    private volatile boolean       isLeader  = false;
    private          AtomicBoolean isStarted = new AtomicBoolean(false);
    private String currentLockValue;
    private String leaderLockName;

    private boolean isSuspend   = false; // 暂停Leader竞争
    private long    suspendTime = 10 * 1000;// 默认暂停时间

    public LeaderElect(LockContext config) throws CrabException {
        this.lock = config.getCurrentLockEngine();
        this.config = config;
        this.leaderLockName = config.getLockNamePrefix() + "_" + config.getLeaderLockName() + "_" + config.getLockNameSuffix();

        start();
    }

    public void start() {
        if (!isStarted.getAndSet(true)) {
            LOG.info("LeaderElect is start.");
            scheduler.scheduleAtFixedRate((Runnable) () -> {
                if (isLeader) {
                    return;
                }
                if (tryElect()) {
                    changeLeader(true);
                    LOG.info("this core is leader. leaderLock = {}:{}", leaderLockName, currentLockValue);
                }
            }, 0, config.getSpinIntervalTime() + new Random().nextInt(20), TimeUnit.MILLISECONDS);

            scheduler.scheduleAtFixedRate(this, 0, config.getLockRenewIntervalTime(), TimeUnit.MILLISECONDS);
        }
    }

    public void close() {
        LOG.info("LeaderElect shutDown.");
        scheduler.shutdown();
        while (!scheduler.isTerminated()) {
        }
        if (lock != null) {
            lock.shutDown();
        }
    }

    public boolean isLeader() {
        return this.isLeader;
    }

    public void suspendLeaderElect(long suspendTime) {
        this.isSuspend = true;
        this.suspendTime = suspendTime;
    }

    public void setListener(LeaderElectListener listener) {
        this.listener = listener;
    }

    private boolean tryElect() {
        if (!isSuspend) {
            String lockValue = getLeaderLockValue();
            boolean locked = lock.tryLock(leaderLockName, lockValue, config.getLockLeaseTime());
            if (locked) {
                currentLockValue = lockValue;
            }
            return locked;
        } else {
            return false;
        }
    }

    private boolean renewLeader() {
        boolean res = lock.renewLock(leaderLockName, currentLockValue, config.getLockLeaseTime());
        if (!res) {
            sleep(10L);
            res = lock.renewLock(leaderLockName, currentLockValue, config.getLockLeaseTime());
        }
        if (!res) {
            lock.unlock(leaderLockName, currentLockValue);
        }
        return res;
    }

    private String getLeaderLockValue() {
        return IdGenerator.id();
    }

    @Override
    public void run() {
        if (isSuspend) {
            changeLeader(false);
            sleep(suspendTime);
            isSuspend = false;
        }
        if (isLeader()) {
            if (!renewLeader()) {
                changeLeader(false);
                LOG.info("this core abandon the leader");
            }
        }
    }

    private void changeLeader(boolean isLeader) {
        this.isLeader = isLeader;
        if (listener != null) {
            listener.onLeaderChanged(isLeader, System.currentTimeMillis());
        }
    }

    private void sleep(long milliSeconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliSeconds);
        } catch (InterruptedException e) {
            LOG.error("TimeUnit sleep has interrupted.");
        }
    }
}
