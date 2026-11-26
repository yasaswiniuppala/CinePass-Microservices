@echo off
cd /d %~dp0
call mvn -pl api-gateway -am spring-boot:run
