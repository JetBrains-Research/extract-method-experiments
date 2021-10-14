python gitcloner/main.py $1 $2

cd $2
find . -type f ! -name '*.java' -delete
