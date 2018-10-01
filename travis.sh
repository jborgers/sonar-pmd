#!/bin/bash

set -euo pipefail

case "$TEST" in

ci)
  mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar verify -B -e -V
  ;;

plugin)
  mvn clean package -T2 -Dsource.skip=true -Denforcer.skip=true -Danimal.sniffer.skip=true -Dmaven.test.skip=true
  cd its/plugin
  echo "####"
  printenv | cut -d= -f1
  echo "####"
  mvn clean package -Dtest.sonar.version=${SQ_VERSION} -Dmaven.test.redirectTestOutputToFile=false
  ;;

*)
  echo "Unexpected TEST mode: $TEST"
  exit 1
  ;;

esac
