#!/bin/bash
MODULE="press"
VERSION=`grep self conf/dependencies.yml | sed "s/.*$MODULE //"`
TARGET=/var/www/repo/play-$MODULE/$MODULE-$VERSION.zip

rm -fr dist
play dependencies --sync || exit $?
play build-module || exit $?
zip --delete dist/*.zip "lib/mockito*"

if [ -e $TARGET ]; then
    echo "Not publishing, $MODULE-$VERSION already exists"
else
    cp dist/*.zip $TARGET
fi
