@echo off
cd /d %~dp0
call mvn -pl booking-service -am spring-boot:run
