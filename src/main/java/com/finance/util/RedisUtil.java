package com.finance.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具
 * 封装常用的 Redis 操作方法：set、get、delete、expire、hasKey 等，简化 Redis 操作代码
 */
@Component
public class RedisUtil {

    //StringRedisTemplate 是 Spring 提供的专门操作 Redis 的类
    private final StringRedisTemplate redisTemplate;

    public RedisUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 存数据（永久有效）
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 存数据（带过期时间）
     * @param key
     * @param value
     * @param timeout
     * @param unit
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        //最后一个参数是时间单位
        //TimeUnit.SECONDS  → 秒
        //TimeUnit.MINUTES → 分钟
        //TimeUnit.HOURS → 小时
        //TimeUnit.DAYS → 天
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }


    /**
     * 取数据
     * @param key
     * @return
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除数据
     * @param key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }


    /**
     * 判断 key 是否存在
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    /**
     * 设置过期时间
     * @param key
     * @param timeout
     * @param unit
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }
}
