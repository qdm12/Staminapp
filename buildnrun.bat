@echo off
echo Connecting watch...
adb -d forward tcp:4444 localabstract:/adb-hub
adb connect 127.0.0.1:4444
adb devices
echo Building project...
cd C:\Android\Staminapp && call gradlew.bat assembleDebug --stacktrace
echo Installing handheld...
cd C:\Android\Staminapp\mobile\build\outputs\apk\ && adb -d install -rd mobile-debug.apk

REM cd C:\Android\Staminapp\mobile\build\outputs\apk\ && adb -s 127.0.0.1:4444 install -rd wear-debug.apk
echo Launching...
adb -d shell am start -n com.stamina.staminapp/.LoginActivity
echo DONE !
echo.