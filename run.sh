#!/bin/zsh


if test $1 = '-c'
then
  gradle fatjar --no-daemon
fi
#echo "input file is"
#read input
#echo "output file is"
#read output
java -ea -jar ./build/libs/c0.jar input.txt -o output.txt