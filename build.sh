#!/bin/bash

javac -cp javaparser-1.0.8.jar:commons-lang3-3.1.jar:. *.java
./makejar.sh
ghc Test.hs

rm *.class
rm *.hi
rm *.o