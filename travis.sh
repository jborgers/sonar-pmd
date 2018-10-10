#!/bin/bash

set -euo pipefail

case "$TEST" in

ci)
  mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar verify -B -e -V
  ;;

plugin)
  # Build plugin
  mvn clean package -Dsource.skip=true -Denforcer.skip=true -Danimal.sniffer.skip=true -Dmaven.test.skip=true

  cd its/plugin

  # Unset environment settings defined by Travis that will collide with our integration tests
  unset SONARQUBE_SCANNER_PARAMS SONAR_TOKEN SONAR_SCANNER_HOME

  # Run integration tests
  mvn clean package -Dtest.sonar.version=${SQ_VERSION} -Dmaven.test.redirectTestOutputToFile=false
  ;;

*)
  echo "Unexpected TEST mode: $TEST"
  exit 1
  ;;

esac
