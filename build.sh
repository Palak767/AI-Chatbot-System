#!/bin/bash

# build.sh - Simple script to compile and run the AIChatbotSystem.java file.

JAVA_FILE="AIChatbotSystem.java"
CLASS_NAME="AIChatbotSystem"

if [ "$1" == "compile" ]; then
    echo "--- Compiling $JAVA_FILE ---"
    # Ensure compilation works for Java 11+ which is needed for HttpClient
    javac $JAVA_FILE
    if [ $? -eq 0 ]; then
        echo "Compilation successful."
    else
        echo "Compilation failed."
        exit 1
    fi

elif [ "$1" == "run" ]; then
    if [ ! -f "$CLASS_NAME.class" ]; then
        echo "Error: Class file not found. Please compile first using './build.sh compile'."
        exit 1
    fi
    echo "--- Running $CLASS_NAME ---"
    java $CLASS_NAME

else
    echo "Usage: ./build.sh [compile|run]"
    echo ""
    echo "  compile: Compiles AIChatbotSystem.java to AIChatbotSystem.class."
    echo "  run:     Executes the compiled AIChatbotSystem program."
fi