package me.kr328.nevo.decorators.smscaptcha.utils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternUtils {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean checkPatternValid(String pattern) {
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return false;
        }
        return true;
    }

    public static Pattern compilePattern(String pattern, String def, int flags) {
        try {
            return Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException ignored) {
        }
        return Pattern.compile(def, flags);
    }
}
