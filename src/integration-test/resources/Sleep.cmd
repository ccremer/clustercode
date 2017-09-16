@echo off
:: This script sleeps for the given time in millis
echo %time%
echo Sleeping for %1 milliseconds
ping 1.1.1.1 -n 1 -w %1 >NUL
echo %time%

exit 0