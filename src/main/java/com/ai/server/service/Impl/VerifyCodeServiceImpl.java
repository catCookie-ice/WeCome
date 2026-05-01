package com.ai.server.service.Impl;

import com.ai.server.service.VerifyCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 * <p>
 * 基于Redis实现验证码的存储、验证和管理功能。
 * 每个验证码与客户端IP地址绑定，确保安全性。
 * 验证码具有自动过期机制（默认5分钟），验证成功后自动删除，实现一次性使用。
 * </p>
 *
 * @author WeCome Team
 * @version 1.0
 * @see VerifyCodeService
 * @since 2026-05-01
 */
@Service
@Slf4j
public class VerifyCodeServiceImpl implements VerifyCodeService {

    /**
     * Redis操作模板
     * <p>
     * Spring Data Redis提供的操作Redis的核心类。
     * 键被规定为String类型，值为Object类型（可以存字符串、数字或序列化对象）。
     * 本实现中实际存储的是验证码字符串。
     * </p>
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis键前缀
     * <p>
     * 用于标识验证码在Redis中的存储位置。
     * 使用冒号分隔是Redis的命名规范，方便在Redis客户端中按业务分类查看数据。
     * 完整的键格式为：verify:code:{ip}
     * </p>
     */
    private static final String VERIFY_CODE_PREFIX = "verify:code:";

    /**
     * 验证码过期时间（单位：分钟）
     * <p>
     * 验证码在此时间后会自动从Redis中删除，防止长期占用内存。
     * 默认设置为5分钟，平衡安全性和用户体验。
     * </p>
     */
    private static final long EXPIRE_TIME = 5;

    /**
     * 保存验证码到Redis
     * <p>
     * 将生成的验证码与客户端IP地址绑定后存储到Redis中，
     * 并设置过期时间，确保验证码不会永久占用存储空间。
     * </p>
     *
     * @param ip   客户端IP地址，用于唯一标识验证码的来源
     * @param code 验证码字符串，通常为4位随机字符
     */
    @Override
    public void saveVerifyCode(String ip, String code) {
        String key = VERIFY_CODE_PREFIX + ip;// 拼接完整的 key，例如 "verify:code:192.168.1.1"
        redisTemplate.opsForValue().set(key, code, EXPIRE_TIME, TimeUnit.MINUTES);
        log.info("保存验证码到Redis，IP: {}, 验证码: {}", ip, code);
    }
    /**
     * 从Redis获取验证码
     * <p>
     * 根据客户端IP地址构建Redis键，查询对应的验证码。
     * 如果验证码已过期或不存在，则返回null。
     * </p>
     *
     * @param ip 客户端IP地址，用于定位验证码
     * @return 验证码字符串，如果不存在或已过期则返回null
     */
    @Override
    public String getVerifyCode(String ip) {//根据 IP 构建 Key，去 Redis 中查询。如果查不到（已过期或从未生成），返回 null。这里做了非空判断和类型转换（toString()）。
        String key = VERIFY_CODE_PREFIX + ip;
        Object code = redisTemplate.opsForValue().get(key);
        return code != null ? code.toString() : null;
    }
    /**
     * 验证用户输入的验证码是否正确
     * <p>
     * 从Redis中获取存储的验证码，与用户输入的验证码进行忽略大小写的比对。
     * 验证成功后立即删除验证码，实现"一次一码"的安全机制。
     * 如果验证码不存在或已过期，验证失败。
     * </p>
     *
     * @param ip        客户端IP地址，用于获取对应的验证码
     * @param inputCode 用户输入的验证码字符串
     * @return true 表示验证成功，false 表示验证失败（验证码错误、过期或不存在）
     */
    @Override
    public boolean verifyCode(String ip, String inputCode) {
        String storedCode = getVerifyCode(ip);
        if (storedCode == null) {
            log.warn("验证码已过期或不存在，IP: {}", ip);
            return false;
        }
        
        boolean isValid = storedCode.equalsIgnoreCase(inputCode);//equalsIgnoreCase 进行忽略大小写的比对
        if (isValid) {
            removeVerifyCode(ip);// 【重要】：一次一码，验证成功后立即销毁
            log.info("验证码验证成功，IP: {}", ip);
        } else {
            log.warn("验证码错误，IP: {}, 输入: {}, 存储: {}", ip, inputCode, storedCode);
        }
        
        return isValid;
    }
    /**
     * 删除指定IP的验证码
     * <p>
     * 从Redis中删除指定客户端IP对应的验证码。
     * 通常在验证成功后调用，实现"一次一码"机制，防止验证码被重复使用。
     * 也可用于主动清除过期或无效的验证码。
     * </p>
     *
     * @param ip 客户端IP地址，用于定位要删除的验证码
     */
    @Override
    public void removeVerifyCode(String ip) {
        String key = VERIFY_CODE_PREFIX + ip;
        redisTemplate.delete(key);
        log.info("删除验证码，IP: {}", ip);
    }
}
