#!/bin/bash

javac -cp javaparser-1.0.8.jar:. *.java
./makejar.sh
ghc Main.hs

rm *.class
rm *.hi
rm *.o