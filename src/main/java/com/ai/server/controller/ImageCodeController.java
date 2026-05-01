package com.ai.server.controller;


import com.ai.server.common.VerifyCode;
import com.ai.server.service.VerifyCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@Slf4j
public class ImageCodeController {

    @Autowired
    private VerifyCodeService verifyCodeService;

    /**
     * 生成验证码
     * @param request HTTP请求对象，用于获取客户端IP
     * @param response HTTP响应对象，用于输出验证码图片
     * @throws IOException IO异常
     */
    @RequestMapping("/createVerify")
    public void createVerify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        VerifyCode vc = new VerifyCode();
        vc.drawImage(response.getOutputStream());
        //response.getOutputStream()：这获取了 HTTP 响应的输出字节流。Controller 没有返回字符串或对象（没有 @ResponseBody），这意味着 Spring MVC 会把方法的返回值直接写进 HTTP 的 Body 里。
        //drawImage(...)：这个方法内部（通常是你引入的 VerifyCode 工具类封装好的）做了以下事情：
        //在内存中创建一张空白图片（BufferedImage）。
        //使用 Java 的 Graphics2D 类在图片上随机画上字符（也就是 vc.getCode() 生成的文本）。
        //画上干扰线、噪点。
        //设置响应头 Content-Type 为 image/jpeg 或 image/png（这个通常在 drawImage 内部或者通过拦截器设置了）。
        //最后，把这张内存中的图片编码成 JPG/PNG 的二进制字节流，塞进 response.getOutputStream() 里返回给前端浏览器。
        //设置浏览器不缓存
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        
        String ip = getClientIp(request);
        String code = vc.getCode();
        verifyCodeService.saveVerifyCode(ip, code);
        
        log.info("生成验证码，IP: {}, 验证码: {}", ip, code);
    }

    /**
     * 验证验证码是否正确
     * @param request HTTP请求对象，用于获取客户端IP
     * @param verify 用户输入的验证码
     * @return 验证结果
     */
    @RequestMapping("/checkVerify")
    @ResponseBody
    public boolean checkVerify(HttpServletRequest request, @RequestParam("verify") String verify) {
        String ip = getClientIp(request);
        boolean isValid = verifyCodeService.verifyCode(ip, verify);
        log.info("验证验证码，IP: {}, 结果: {}", ip, isValid ? "成功" : "失败");
        return isValid;
    }

    /**
     * 获取客户端真实IP地址
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
