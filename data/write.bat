@echo off

cd /d %~dp0
adb push sample.geojson storage/self/primary/Android/data/com.example.sample/files/
pause
