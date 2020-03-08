package me.kr328.nevo.decorators.smscaptcha.utils

import android.content.Context
import me.kr328.nevo.decorators.smscaptcha.R

class CaptchaUtils(context: Context,
                   private val customIdentifyRegex: Regex,
                   private val customExtractRegex: Regex) {
    private val defaultIdentifyRegex = Regex(context.getString(R.string.default_identify_regex))
    private val defaultExtractRegex = Regex(context.getString(R.string.default_extract_regex))

    fun extractSmsCaptchas(messages: List<String>, useDefault: Boolean): List<String> {
        return messages.flatMap { extractSmsCaptcha(it, useDefault) }
    }

    fun replaceCaptchaWithChar(message: String, captchas: List<String>, ch: String = "*"): String {
        var result = message

        captchas.forEach {
            result = result.replace(it, ch.repeat(it.length))
        }

        return result
    }

    private fun extractSmsCaptcha(message: String, useDefault: Boolean): List<String> {
        if (!(useDefault && defaultIdentifyRegex.matches(message) || customIdentifyRegex.matches(message)))
            return emptyList()

        val defaultMatched = if (useDefault)
            defaultExtractRegex.findAll(message).map { it.value }.toList()
        else
            emptyList()

        return customExtractRegex.findAll(message).map { it.value }.toList() + defaultMatched
    }
}