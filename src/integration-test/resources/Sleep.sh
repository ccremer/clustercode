#!/bin/bash

echo $(date)
echo "Sleeping for $1 milliseconds"
sleep $(($1 / 1000))
echo $(date)

exit 0
