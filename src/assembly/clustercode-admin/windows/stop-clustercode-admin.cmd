@echo off

echo Stopping clustercode-admin...
cd nginx
start nginx.exe -s stop
echo done. This window autocloses in 3 seconds.
ping 1.1.1.1 -n 1 -w 3000 >NUL
