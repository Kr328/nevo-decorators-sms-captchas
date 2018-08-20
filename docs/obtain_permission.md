短信权限帮助
----------

* ##### 权限说明
  Android 4.4 (Katkit) 时 Android 限制了 **非默认短信应用** 修改短信的权限    
  开启该权限除了**运行时权限**授权外,还需要修改系统的 **AppOps** 的配置

* ##### 授予短信修改权限
    1. 准备计算机并配置ADB环境
    2. 打开手机的USB调试并连接至计算机
    3. 运行以下命令 (仅一行)
       ```shell
       adb shell appops set me.kr328.nevo.decorators.smscaptcha WRITE_SMS allow
       ```
