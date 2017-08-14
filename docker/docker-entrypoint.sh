#!/bin/sh

# check if config files exist. If not, copy from default
echo "Checking if $CC_CONFIG_DIR has contents..."
if [ ! "$(ls -A $CC_CONFIG_DIR)" ]; then
        echo "Copying default config..."
        cp -r "$CC_DEFAULT_DIR/." "$CC_CONFIG_DIR/"
fi

echo "Invoking java -jar clustercode.jar $JAVA_ARGS"
exec java -jar clustercode.jar $JAVA_ARGS