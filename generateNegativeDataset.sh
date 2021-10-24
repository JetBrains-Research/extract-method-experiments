DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
if uname -s | grep -iq cygwin ; then
    DIR=$(cygpath -w "$DIR")
    PWD=$(cygpath -w "$PWD")
fi

for d in $1/*/ ; do # iterating through all directories in $1
	"$DIR/gradlew" runNegativeRefactorings -PinputProjectPath="$PWD/$d" -PoutputFilePath="$PWD/out/$(basename $1)/$(basename $d).csv" & sleep 40m;	"$DIR/gradlew" --stop & sleep 10
done

"$DIR/gradlew" --stop
