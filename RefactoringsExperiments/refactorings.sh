#!/usr/bin/env bash

if [ $# -ne "3" ]; then
    echo "usage: ./refactorings.sh "
    exit 1
fi

# https://stackoverflow.com/a/246128
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
if uname -s | grep -iq cygwin ; then
    DIR=$(cygpath -w "$DIR")
    PWD=$(cygpath -w "$PWD")
fi

"$DIR/gradlew" --stop
"$DIR/gradlew" clean
"$DIR/gradlew" -p "$DIR" runRefactoringsExperiments -PrunPositives="$PWD/$1" -PrunNegatives="$PWD/$2" -PrepoPathsFile="$PWD/$3"