#!/bin/bash

SCOMMITHASH=`cd $WORKSPACE && git rev-parse --short HEAD`
sed "s/\(static String COMMIT_HASH\).*/\1 = \"$SCOMMITHASH\";/" $WORKSPACE/eHour-common/src/main/java/net/rrm/ehour/util/CommitHash.java > $WORKSPACE/eHour-common/src/main/java/net/rrm/ehour/util/CommitHash.java.${SCOMMITHASH}
mv $WORKSPACE/eHour-common/src/main/java/net/rrm/ehour/util/CommitHash.java.${SCOMMITHASH} $WORKSPACE/eHour-common/src/main/java/net/rrm/ehour/util/CommitHash.java
git commit $WORKSPACE/eHour-common/src/main/java/net/rrm/ehour/util/CommitHash.java -m "CI - Update recent commit hash of the build - $SCOMMITHASH"
