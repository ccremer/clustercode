@echo off

set java=java

%java% -version
IF %ERRORLEVEL% EQU 0 (
	%java% -jar clustercode.jar
) else (
	echo Java is not installed or in PATH. Modify your environment variables.
	pause
)
