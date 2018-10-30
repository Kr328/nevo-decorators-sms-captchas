短信权限帮助
----------

* **权限说明**
  Android 4.4 (Katkit) 时 Android 限制了 **非默认短信应用** 修改短信的权限  
  开启该权限除了 **运行时权限** 授权外,还需要修改系统的 **AppOps** 的配置

* **隐私政策**
  参见 [隐私政策](https://kr328.github.io/nevo-decorators-sms-captchas/privacy_policy)

* **授予短信读取权限**
  请在设置中开启本应用的短信权限

* **授予短信修改权限**
  以下操作 **二选一**
  - 使用 ADB
    1. 准备计算机并配置ADB环境
    2. 打开手机的USB调试并连接至计算机
    3. 运行以下命令 (仅一行)
       ```shell
       adb shell appops set me.kr328.nevo.decorators.smscaptcha WRITE_SMS allow
       ```
  - 使用 AppOps 类应用 (AppOps,AppOpsX)  
    在列表中找到本应用并调整 **短信写入权限** 为 **允许**  
