@echo off
cd /d %~dp0
call mvn -pl movie-service -am spring-boot:run
