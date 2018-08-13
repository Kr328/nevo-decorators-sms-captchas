package me.kr328.nevo.decorators.smscaptcha.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptchaUtils {
    private Pattern mCheckPattern;
    private Pattern mParsePattern;

    public CaptchaUtils(Pattern checkPattern, Pattern parsePattern) {
        mCheckPattern = checkPattern;
        mParsePattern = parsePattern;
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
            if (!mCheckPattern.matcher(message).matches())
                continue;

            Matcher matcher = mParsePattern.matcher(message);

            while (matcher.find())
                captchas.add(matcher.group(0));
        }

        return captchas.toArray(new String[0]);
    }
}
