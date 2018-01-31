package com.github.haiger.crab.engine.redis;

import com.github.haiger.crab.engine.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Haiger
 * @version $Id: RedisLock.java, v 0.1 2018-01-11 14:44:29 Haiger Exp $
 */
public class RedisLock implements Lock {
    private static final Logger LOG = LoggerFactory.getLogger(RedisLock.class);

    private List<JedisPoolInfo> jedisPools;
    private int                 nodeSize;
    private long                spinIntervalTime;
    private static final String UNLOCK_SCRIPT     = "if (redis.call('get',KEYS[1]) == ARGV[1]) then "
            + " return redis.call('del',KEYS[1]) else return 0 end;";
    private static final String RENEW_LOCK_SCRIPT = "if (redis.call('get',KEYS[1]) == ARGV[1]) then "
            + " return redis.call('pexpire',KEYS[1],ARGV[2]) else return 0 end;";

    /**
     *
     * @param jedisPools 必须是 奇数 个
     * @param spinIntervalTime
     * @throws IllegalArgumentException 当 jedisPools 不是奇数个时抛异常
     */
    public RedisLock(List<JedisPoolInfo> jedisPools, long spinIntervalTime) throws IllegalArgumentException {

        // 只允许奇数台 redis 实现分布式锁
        if (jedisPools.size() % 2 == 0) {
            throw new IllegalArgumentException("jedisPools size must be odd !!!");
        }

        this.jedisPools = jedisPools;
        this.nodeSize = jedisPools.size();
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
        long start = System.currentTimeMillis();
        int lockedCount = lockOps(lockName, lockValue, leaseTime);

        // nodeSize 必为奇数
        if (nodeSize == 1) {
            if (lockedCount == 1) {
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed >= leaseTime / 2) {// 避免获取锁期间的耗时，已经超过或者接近lease time。
                    expireOps(lockName, leaseTime);
                }
                LOG.info("locked---lockName:" + lockName + "--lockValue:" + lockValue);
                return true;
            }
        } else {
            if (lockedCount >= ((nodeSize / 2) + 1)) {
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed >= leaseTime / 2) {// 避免获取锁期间的耗时，已经超过或者接近lease time。
                    expireOps(lockName, leaseTime);
                }
                LOG.info("locked---lockName:" + lockName + "--lockValue:" + lockValue);
                return true;
            }

            if (lockedCount > 0) {
                unlockOps(lockName, lockValue);// 获取锁失败时，有可能部分redis实例获取成功，需要将其清除。
            }
        }

        return false;
    }

    @Override
    public void unlock(String lockName, String lockValue) {
        unlockOps(lockName, lockValue);// 失败了是否重试--TODO
    }

    @Override
    public boolean renewLock(String lockName, String lockValue, long leaseTime) {
        int renewCount = renewLockOps(lockName, lockValue, leaseTime);
        if (nodeSize % 2 > 0 && nodeSize > 1) {
            if (renewCount >= nodeSize / 2 + 1) {
                return true;
            }
            //
            //                if (renewCount > 0) {
            //                    unlockOps(lockName, lockValue);// renew 失败，释放资源。
            //                }
        } else {
            if (renewCount == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void shutDown() {
        for (JedisPoolInfo jedisPoolInfo : jedisPools) {
            LOG.info("redisLock shutDown: {}", jedisPoolInfo.getIpAndPort());
            jedisPoolInfo.getJedisPool().close();
        }
    }

    private int lockOps(String lockName, String lockValue, long leaseTime) {
        int lockedCount = 0;
        List<JedisPoolInfo> uncontrolableRedis = new ArrayList<>();
        for (JedisPoolInfo jedisPool : jedisPools) {
            try (Jedis jedis = jedisPool.getJedisPool().getResource()) {
                String res = jedis.set(lockName, lockValue, "NX", "PX", leaseTime);
                if ("OK".equalsIgnoreCase(res)) {
                    lockedCount++;
                } else {
                    uncontrolableRedis.add(jedisPool);
                }
            } catch (Throwable t) {
                LOG.error("lockOps({}) set command exception. redis host:{},t={}", lockName, jedisPool.getIpAndPort(), t.getMessage());
            }
        }
        if (lockedCount >= ((nodeSize / 2) + 1) && lockedCount < nodeSize) {
            for (JedisPoolInfo jedisPool : uncontrolableRedis) {
                try (Jedis jedis = jedisPool.getJedisPool().getResource()) {
                    String res = jedis.psetex(lockName, (long) leaseTime, lockValue);
                    if ("OK".equalsIgnoreCase(res)) {
                        lockedCount++;
                    } else {
                        LOG.warn("lockOps this core get lock={}, cannot set key to redis={}", lockName, jedisPool.getIpAndPort());
                    }
                } catch (Throwable t) {
                    LOG.error("lockOps({}) set command exception. redis host:{},t={}", lockName, jedisPool.getIpAndPort(), t.getMessage());
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.info("lockOps this core get the lock,lock key = {},lock size = {}", lockName, lockedCount);
            }
        }
        return lockedCount;
    }

    private void unlockOps(String lockName, String lockValue) {
        List<String> keys = new ArrayList<>(1);
        List<String> args = new ArrayList<>(1);
        keys.add(lockName);
        args.add(lockValue);

        for (JedisPoolInfo jedisPool : jedisPools) {
            try (Jedis jedis = jedisPool.getJedisPool().getResource()) {
                jedis.eval(UNLOCK_SCRIPT, keys, args);
            } catch (Throwable t) {// 执行失败暂时跳过，让其自动过期。
                LOG.error("unlockOps({}) eval command exception. redis host:{},t={}", lockName, jedisPool.getIpAndPort(), t.getMessage());
            }
        }
    }

    private int renewLockOps(String lockName, String lockValue, long leaseTime) {
        List<String> keys = new ArrayList<>(1);
        List<String> args = new ArrayList<>(2);
        keys.add(lockName);
        args.add(lockValue);
        args.add(leaseTime + "");

        int renewCount = 0;
        List<JedisPoolInfo> uncontrolableRedis = new ArrayList<>();
        for (JedisPoolInfo jedisPool : jedisPools) {
            try (Jedis jedis = jedisPool.getJedisPool().getResource()) {
                Object res = jedis.eval(RENEW_LOCK_SCRIPT, keys, args);
                //String res = jedis.psetex(lockName, leaseTime, lockValue);
                if ((long) res > 0) {
                    renewCount++;
                } else {
                    uncontrolableRedis.add(jedisPool);
                }
            } catch (Throwable t) {
                LOG.error("renewLockOps({}) eval command exception. redis host:{},t={}", lockName, jedisPool.getIpAndPort(),
                        t.getMessage());
            }
        }
        if (renewCount >= ((nodeSize / 2) + 1) && renewCount < nodeSize) {
            for (JedisPoolInfo jedisPool : uncontrolableRedis) {
                try (Jedis jedis = jedisPool.getJedisPool().getResource()) {
                    String res = jedis.psetex(lockName, leaseTime, lockValue);
                    if ("OK".equalsIgnoreCase(res)) {
                        renewCount++;
                    } else {
                        LOG.warn("renewLockOps this core get lock={}, cannot renew key to redis={}", lockName, jedisPool.getIpAndPort());
                    }
                } catch (Throwable t) {
                    LOG.error("renewLockOps({}) set command exception. redis host:{},t={}", lockName, jedisPool.getIpAndPort(),
                            t.getMessage());
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.info("[renewLock] this core get the lock,lock key = {},lock size = {}", lockName, renewCount);
            }
        }
        return renewCount;
    }

    private void expireOps(String lockName, long leaseTime) {
        for (JedisPoolInfo jedisPool : jedisPools) {
            try (Jedis jedis = jedisPool.getJedisPool().getResource()) {
                jedis.pexpire(lockName, leaseTime);
            } catch (Exception e) {
                LOG.error("expireOps({}) expire command exception. redis host:{},e={}", lockName, jedisPool.getIpAndPort(), e);
            }
        }
    }
}
