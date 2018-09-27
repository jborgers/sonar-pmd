#!/bin/bash

set -euo pipefail

case "$TEST" in

ci)
  mvn verify -B -e -V
  ;;

plugin)
  mvn package -T2 -Dsource.skip=true -Denforcer.skip=true -Danimal.sniffer.skip=true -Dmaven.test.skip=true
  cd its/plugin
  mvn package -Dsonar.runtimeVersion="$SQ_VERSION" -DjavaVersion="LATEST_RELEASE" -Dmaven.test.redirectTestOutputToFile=false
  ;;

*)
  echo "Unexpected TEST mode: $TEST"
  exit 1
  ;;

esac
