package com.jstart.qypicture.template.sendCodeTemplate;

/**
 * 发送验证码模板方法
 */
public abstract class SendCodeTemplate {

    public void sendCode(String account) {
        sendCode(account, null);
    }


    public void sendCode(String account, String context) {

        // 发送验证码前的格式校验
        checkFormat(account);
        // 发送验证码
        send(account, context);

    }


    protected abstract void checkFormat(String account);

    protected abstract void send(String account, String context);
}
