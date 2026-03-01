package com.jstart.qypicture.template.sendCodeTemplate;


import com.jstart.qypicture.constant.UserConstant;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 发送邮箱验证码
 */
@Slf4j
@Component
public class SendEmail extends SendCodeTemplate {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.mail.username}")
    private String from;


    // 类初始化时预编译（只需编译一次）
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+" +
                    "(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
                    "@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+" +
                    "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?"
    );


    @Override
    protected void checkFormat(String account) {
        if (!EMAIL_PATTERN.matcher(account).matches()) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "邮箱格式错误");

        }
    }

    @Override
    protected void send(String account, String context) {
        // 1. 先生成验证码并存入Redis（确保用户立即可用）
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        redisTemplate.opsForValue().set(UserConstant.USER_ACCOUNT_CODE_KEY + account, code, 300, TimeUnit.SECONDS);

        // 2. 构建邮件内容
        String finalContext = (context == null) ? "您的验证码是: " + code + "，请勿泄露给他人。" : context;

        // 3. 异步发送邮件（不阻塞接口返回）
        CompletableFuture.runAsync(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(account);
                message.setSubject("登录验证码");
                message.setText(finalContext);
                mailSender.send(message);
                log.info("发送邮件到: {} 成功，验证码为：{}", account, code);
            } catch (MailException e) {
                log.error("邮件发送失败: {}, 错误: {}", account, e.getMessage());
            }
        });
        try {

            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new BusinessException(ResultEnum.SYSTEM_ERROR);
        }
    }
}

