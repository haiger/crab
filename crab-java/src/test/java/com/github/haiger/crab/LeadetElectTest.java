package com.github.haiger.crab;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Haiger
 * @version $Id: LeadetElectTest.java, v 0.1 2018-01-12 15:20:42 Haiger Exp $
 */
public class LeadetElectTest extends BaseTest {

    @Test
    public void testLeader() {
        leader.start();

        leader.setListener((isLeader, changeTime) -> {
            System.out.println("isLeader:" + isLeader + "---changeTime:" + changeTime);
        });

        sleep(1);

        Assert.assertEquals(true, leader.isLeader());

        leader.suspendLeaderElect(4000);
        sleep(1);
        Assert.assertEquals(false, leader.isLeader());

        sleep(5);
        Assert.assertEquals(true, leader.isLeader());

        sleep(1);
    }
}
