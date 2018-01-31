package com.github.haiger.crab.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Haiger
 * @version $Id: NamedThreadFactory.java, v 0.1 2018-01-11 16:09:07 Haiger Exp $
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String baseName;
    private final LongAdder threadNum = new LongAdder();

    public NamedThreadFactory(String baseName) {
        this.baseName = baseName;
    }

    public Thread newThread(Runnable r) {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        threadNum.increment();
        t.setName(baseName + "-" + threadNum.intValue());
        return t;
    }
}