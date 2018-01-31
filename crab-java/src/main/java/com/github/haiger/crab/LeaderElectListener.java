package com.github.haiger.crab;

/**
 * @author Haiger
 * @version $Id: LeaderElectListener.java, v 0.1 2018-01-22 17:30:41 Haiger Exp $
 */
public interface LeaderElectListener {

    void onLeaderChanged(boolean isLeader, long changeTime);
}
