DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
if uname -s | grep -iq cygwin ; then
    DIR=$(cygpath -w "$DIR")
    PWD=$(cygpath -w "$PWD")
fi

for i in $(seq 0 1 $3) ; do # iterating through 20 rows of
  "$DIR/gradlew" runNegativeRefactorings -PinputMappingPath="$PWD/$1" -PoutputFilePath="$PWD/$2/$i.csv" -Pindex=$i

"$DIR/gradlew" --stop