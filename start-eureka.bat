@echo off
cd /d %~dp0
call mvn -pl eureka-server -am spring-boot:run
