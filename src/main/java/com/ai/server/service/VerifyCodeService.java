package com.ai.server.service;

public interface VerifyCodeService {
    
    /**
     * 保存验证码到Redis
     * @param ip 客户端IP地址
     * @param code 验证码
     */
    void saveVerifyCode(String ip, String code);
    
    /**
     * 从Redis获取验证码
     * @param ip 客户端IP地址
     * @return 验证码
     */
    String getVerifyCode(String ip);
    
    /**
     * 验证验证码
     * @param ip 客户端IP地址
     * @param inputCode 用户输入的验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String ip, String inputCode);
    
    /**
     * 删除验证码
     * @param ip 客户端IP地址
     */
    void removeVerifyCode(String ip);
}
