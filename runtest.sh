#!/bin/bash

#This file is intended for use with the Berkeley Delta Debugging tool

CLASSPATH=/Users/jkoppel/tarski/tools/javaparser-to-hs/javaparser-1.0.8.jar:/Users/jkoppel/tarski/tools/javaparser-to-hs/commons-lang3-3.1.jar:/Users/jkoppel/tarski/tools/javaparser-to-hs/javaparser-to-hs.jar:/Users/jkoppel/tarski/tools/javaparser-to-hs
java -cp ../..:../../javaparser-1.0.8.jar CheckParses $1 && ! /Users/jkoppel/tarski/tools/javaparser-to-hs/Test $1
