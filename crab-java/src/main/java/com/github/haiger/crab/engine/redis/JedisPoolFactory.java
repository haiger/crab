package com.github.haiger.crab.engine.redis;

import com.github.haiger.crab.conf.RedisConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Haiger
 * @version $Id: JedisPoolFactory.java, v 0.1 2018-01-12 13:47:26 Haiger Exp $
 */
public class JedisPoolFactory {

    public static List<JedisPoolInfo> createJedisPoolList(RedisConfig redisConfig) {
        List<JedisPoolInfo> jedisPoolInfos = new ArrayList<JedisPoolInfo>();
        JedisPoolConfig poolConfig = createPoolConfig(redisConfig);
        String nodes = redisConfig.getNodes();
        String[] ipAndPorts = nodes.split(",");
        for (String ipAndPort : ipAndPorts) {
            JedisPoolInfo jedisPoolInfo = new JedisPoolInfo();
            String[] split = ipAndPort.split(":");
            jedisPoolInfo.setJedisPool(new JedisPool(poolConfig, split[0], Integer.parseInt(split[1]), redisConfig.getConnectionTimeout(),
                    redisConfig.getReadTimeout(), redisConfig.getPassword(), redisConfig.getDatabase(), null, false, null, null, null));
            jedisPoolInfo.setIpAndPort(ipAndPort);
            jedisPoolInfos.add(jedisPoolInfo);
        }
        return jedisPoolInfos;
    }

    private static JedisPoolConfig createPoolConfig(RedisConfig redisConfig) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redisConfig.getPoolMaxActive());
        config.setMaxIdle(redisConfig.getPoolMaxIdle());
        config.setMinIdle(redisConfig.getPoolMinIdle());
        config.setMaxWaitMillis(redisConfig.getPoolMaxWait());
        config.setTestOnBorrow(redisConfig.isPoolTestOnBorrow());
        return config;
    }
}
