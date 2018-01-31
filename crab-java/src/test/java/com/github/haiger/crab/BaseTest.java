package com.github.haiger.crab;

import com.github.haiger.crab.conf.Config;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.TimeUnit;

/**
 * @author Haiger
 * @version $Id: BaseTest.java, v 0.1 2018-01-12 15:17:50 Haiger Exp $
 */
public class BaseTest {
    protected ReentrantLock lock;
    protected LeaderElect   leader;

    @Before
    public void init() {
        try {
            LockContext context = LockContext.getInstance().build(Config.getInstance().load("/crab.properties"));
            lock = new ReentrantLock(context);
            leader = new LeaderElect(context);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @After
    public void close() {
        lock.close();
        leader.close();
    }

    public void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
