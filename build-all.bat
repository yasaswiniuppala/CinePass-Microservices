@echo off
cd /d %~dp0
call mvn clean package -DskipTests
if errorlevel 1 (echo BUILD FAILED & exit /b 1)
echo Build successful.
