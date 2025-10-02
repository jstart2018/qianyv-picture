package com.jstart.qypicture.manager.sendCodeTemplate;


import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 发送短信
 */
@Component
@Slf4j
public class SendSMS extends SendCodeTemplate {
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(?:(?:\\+|00)86)?1(?:" +
                    "3\\d{3}|" +          // 13x
                    "4[5-9]\\d{2}|" +     // 145-149
                    "5[0-35-9]\\d{2}|" +  // 15x (除154)
                    "6[2567]\\d{2}|" +    // 162,165-167
                    "7[0-8]\\d{2}|" +     // 170-178
                    "8\\d{3}|" +          // 18x
                    "9[0-35-9]\\d{2}" +   // 19x (除194)
                    ")\\d{6}$"
    );

    @Override
    protected void checkFormat(String account) {
        // 检查手机号格式
        if (!PHONE_PATTERN.matcher(account).matches()) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "手机号格式错误");
        }
    }

    //todo: 短信登录待完成
    @Override
    protected void send(String account, String context) {

    }
}
