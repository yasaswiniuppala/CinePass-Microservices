@echo off
cd /d %~dp0
call mvn -pl show-service -am spring-boot:run
