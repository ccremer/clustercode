@echo off

echo Starting clustercode-admin...
cd nginx
start /b nginx.exe
echo done. Stop the server with stop-clustercode-admin.cmd
echo This window autocloses in 5 seconds.
ping 1.1.1.1 -n 1 -w 5000 >NUL
