package me.kr328.nevo.decorators.smscaptcha.utils;

public class StringUtils {
    public static String repeat(char ch ,int count) {
        StringBuilder builder = new StringBuilder();

        for ( int i = 0 ; i < count ; i++ )
            builder.append(ch);

        return builder.toString();
    }
}
