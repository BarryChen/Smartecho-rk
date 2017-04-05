adb install -r ./app/build/outputs/apk/app-debug.apk
adb push ./app/src/main/assets/smartecho.properties /sdcard/
adb shell am start -n com.rockchip.smartecho/com.rockchip.echo.SmartEchoActivity
