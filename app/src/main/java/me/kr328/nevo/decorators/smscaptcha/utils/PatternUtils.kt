package me.kr328.nevo.decorators.smscaptcha.utils

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

object PatternUtils {
    fun checkPatternValid(pattern: String): Boolean {
        try {
            Pattern.compile(pattern)
        } catch (e: PatternSyntaxException) {
            return false
        }
        return true
    }

    fun compilePattern(pattern: String, def: String, options: Set<RegexOption>): Regex {
        try {
            return Regex(pattern, options)
        } catch (ignored: PatternSyntaxException) {
        }
        return Regex(def, options)
    }
}