@echo off
cd /d %~dp0
call mvn -pl user-service -am spring-boot:run
