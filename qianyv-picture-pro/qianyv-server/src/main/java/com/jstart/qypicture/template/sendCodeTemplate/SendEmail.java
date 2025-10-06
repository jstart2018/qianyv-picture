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
        //发送邮件的逻辑
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(account);
            message.setSubject("登录验证码"); //邮件主题
            String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
            if (context == null) {
                context = "您的验证码是: " + code + "，请勿泄露给他人。";
            }
            message.setText(context);
            mailSender.send(message);
            //验证码存入redis，设置过期时间为5分钟
            redisTemplate.opsForValue().set(UserConstant.USER_LOGIN_CODE_KEY + account, code, 300, TimeUnit.SECONDS);
        } catch (MailException e) {
            throw new BusinessException(ResultEnum.SYSTEM_ERROR, "邮件发送失败");
        }
        log.info("发送邮件到: {} 成功，验证码为：{}", account, context);
    }
}

