package me.kr328.nevo.decorators.smscaptcha.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptchaUtils {
    private static final Pattern defaultIdentifyPattern = Pattern.compile(".*((验证|確認|驗證|校验|动态|确认|随机|激活|兑换|认证|交易|授权|操作|提取|安全|登陆|登录|verification |confirmation )(码|碼|代码|代碼|号码|密码|code|コード)|口令|Steam|快递|快件|单号|订单).*");
    private static final Pattern defaultParsePattern    = Pattern.compile("((?<!\\d|(联通|尾号|金额|支付|末四位)(为)?)(G-)?\\d{4,6}(?!\\d|年|账|动画))|((?<=(code is|码|碼|コードは)[是为為]?[『「【〖（(：: ]?)(?<![a-zA-Z0-9])[a-zA-Z0-9]{4,6}(?![a-zA-Z0-9]))|((?<!\\w)\\w{4,6}(?!\\w)(?= is your))|((?<=(取件码|密码|货码|暗号)[『「【〖（(：: ]?)(?<![a-zA-Z0-9])[a-zA-Z0-9]{4,8}(?![a-zA-Z0-9]))");

    private boolean useDefault;

    private Pattern customIdentifyPattern;
    private Pattern customParsePattern;

    public CaptchaUtils(boolean useDefault ,Pattern customIdentifyPattern, Pattern customParsePattern) {
        this.useDefault            = useDefault;
        this.customIdentifyPattern = customIdentifyPattern;
        this.customParsePattern    = customParsePattern.pattern().isEmpty() ? Pattern.compile("^$") : customParsePattern;
    }

    public static String replaceCaptchaWithChar(CharSequence message, String[] captchas, char ch) {
        String messageString = message.toString();

        for (String captcha : captchas)
            messageString = messageString.replace(captcha, StringUtils.repeat(ch, captcha.length()));
        return messageString;
    }

    public String[] findSmsCaptchas(String[] messages) {
        if (messages == null) return new String[0];

        ArrayList<String> captchas = new ArrayList<>();

        for ( String message : messages ) {
            if (!(customIdentifyPattern.matcher(message).matches() || ( useDefault && defaultIdentifyPattern.matcher(message).matches() )))
                continue;

            Matcher customMatcher  = customParsePattern.matcher(message);
            Matcher defaultMatcher = defaultParsePattern.matcher(message);

            while (customMatcher.find())
                captchas.add(customMatcher.group(0));

            while (useDefault && defaultMatcher.find())
                captchas.add(defaultMatcher.group(0));
        }

        return captchas.toArray(new String[0]);
    }
}
