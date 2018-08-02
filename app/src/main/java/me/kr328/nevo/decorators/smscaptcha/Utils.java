package me.kr328.nevo.decorators.smscaptcha;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static Pattern PATTERN_EXIST_MSM_CODE = Pattern.compile("验证|认证|驗證|認證");
    private static Pattern PATTERN_CAPTCHAS       = Pattern.compile("(\\d{4,10})");

    public static String[] findSmsCaptchas(CharSequence sms) {
        if ( sms == null ) return new String[0];

        if ( !PATTERN_EXIST_MSM_CODE.matcher(sms).find() )
            return new String[0];

        ArrayList<String> captchas = new ArrayList<>();
        Matcher matcher = PATTERN_CAPTCHAS.matcher(sms);

        while ( matcher.find() )
            captchas.add(matcher.group(1));

        return captchas.toArray(new String[0]);
    }

    public static String repeat(char ch ,int count) {
        StringBuilder builder = new StringBuilder();

        for ( int i = 0 ; i < count ; i++ )
            builder.append(ch);

        return builder.toString();
    }
}
