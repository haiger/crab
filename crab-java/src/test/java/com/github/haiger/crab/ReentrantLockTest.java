package com.github.haiger.crab;

import org.junit.Test;

/**
 * @author Haiger
 * @version $Id: ReentrantLockTest.java, v 0.1 2018-01-12 14:13:17 Haiger Exp $
 */
public class ReentrantLockTest extends BaseTest {

    @Test
    public void testLock() {
        String bizName = "bizName_Test";
        String bizValue = "bizValue_Test";
        try {
            lock.lock(bizName, bizValue);

            sleep(1);
        } finally {
            lock.unlock(bizName);
        }
    }
}
