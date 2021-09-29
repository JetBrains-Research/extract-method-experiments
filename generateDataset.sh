#!/usr/bin/env bash

# https://stackoverflow.com/a/246128
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
if uname -s | grep -iq cygwin ; then
    DIR=$(cygpath -w "$DIR")
    PWD=$(cygpath -w "$PWD")
fi

"$DIR/gradlew" --stop
"$DIR/gradlew" clean
"$DIR/gradlew" -p "$DIR" runRefactoringsExperiments -Prunner=RefactoringsExperiments -PprojectsDirPath="$1" -PdatasetsDirPath="$2" -PgeneratePositiveSamples