#!/bin/bash
export JAVA_HOME="/c/Program Files/Java/jdk-25.0.2"
export PATH="$JAVA_HOME/bin:/c/maven/bin:$PATH"
cd /c/app/chronos-pulse
mvn compile -q 2>&1
