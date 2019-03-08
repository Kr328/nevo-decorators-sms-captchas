package me.kr328.nevo.decorators.smscaptcha;

public class Global {
    public final static String PREFIX = BuildConfig.APPLICATION_ID;
    public final static String PREFIX_INTENT = PREFIX + ".intent";
    public final static String PREFIX_INTENT_ACTION = PREFIX_INTENT + ".action";
    public final static String PREFIX_INTENT_EXTRA = PREFIX_INTENT + ".extra";
    public final static String PREFIX_NOTIFICATION = PREFIX + ".notification";
    public final static String PREFIX_NOTIFICATION_EXTRA = PREFIX_NOTIFICATION + ".extra";

    public final static String INTENT_CAPTCHA_NOTIFICATION_SHOW      = PREFIX_INTENT_ACTION + ".notification.show";
    public final static String INTENT_CAPTCHA_NOTIFICATION_CANCEL    = PREFIX_INTENT_ACTION + ".notification.cancel";
    public final static String INTENT_CAPTCHA_NOTIFICATION_DO_CANCEL = PREFIX_INTENT_ACTION + ".notification.cancel.do";

    public final static String INTENT_NOTIFICATION_KEY     = "key";
    public final static String INTENT_NOTIFICATION_CAPTCHA = "captcha";

    public final static String NEVOLUTION_PACKAGE_NAME = "com.oasisfeng.nevo";
}
