SMS Permission help
----------

* **Why**

  Android limit **Non-default SMS Application** edit SMS permission since **Andorid 4.4 (Kitkat)**   
  Except grant **Runtime Permission** ,We need edit **appops configure**

* **Privacy Plicy**

  See [Privacy Policy](https://kr328.github.io/nevo-decorators-sms-captchas/privacy_policy)

* **Grant Read Permission**

  Grant **SMS Permission** in **Settings**

* **Grant Edit Permission**
  Choose **one of the following operations**
  - With ADB
     Run the following command

       ```shell
       adb shell appops set me.kr328.nevo.decorators.smscaptcha WRITE_SMS allow
       ```
  - With appops Edit Application (e.g AppOps,AppOpsX)
    Find this app in the list and adjust the **All SMS Permission** to **Allow**

