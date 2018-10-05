#!/bin/bash

# check if config files exist. If not, copy from default
echo "Checking if $CC_CONFIG_DIR has contents..."
if [ ! "$(ls -A $CC_CONFIG_DIR)" ]; then
        echo "Copying default config..."
        cp -r "$CC_DEFAULT_DIR/config/." "$CC_CONFIG_DIR/"
fi

profiledir="/profiles"

# check if profiles exist. If not, copy from default
echo "Checking if $profiledir has contents..."
if [ ! "$(ls -A $profiledir)" ]; then
        echo "Copying default profiles..."
        cp -r "$CC_DEFAULT_DIR/profiles/." "$profiledir/"
fi

echo "Invoking java $JAVA_ARGS -jar clustercode.jar"
exec java ${JAVA_ARGS} -jar clustercode.jar
