package com.github.haiger.crab.engine.redis;

import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

/**
 * @author Haiger
 * @version $Id: JedisPoolInfo.java, v 0.1 2018-01-11 15:30:38 Haiger Exp $
 */
public class JedisPoolInfo {
    private String      ipAndPort;
    private Pool<Jedis> jedisPool;

    public String getIpAndPort() {
        return ipAndPort;
    }

    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }

    public Pool<Jedis> getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ipAndPort == null) ? 0 : ipAndPort.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JedisPoolInfo other = (JedisPoolInfo) obj;
        if (ipAndPort == null) {
            if (other.ipAndPort != null) {
                return false;
            }
        } else if (!ipAndPort.equals(other.ipAndPort)) {
            return false;
        }
        return true;
    }
}
