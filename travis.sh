#!/bin/bash

set -eo pipefail

case "$TEST" in

ci)
  mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar verify -B -e -V -Dskip.failsafe.tests
  ;;

plugin)

  # Unset environment settings defined by Travis that will collide with our integration tests
  unset SONARQUBE_SCANNER_PARAMS SONAR_TOKEN SONAR_SCANNER_HOME

  # Run integration tests
  if [ -n "$SJ_VERSION" ]; then
    mvn verify -Dtest.sonar.version=${SQ_VERSION} -Dtest.sonar.plugin.version.java=${SJ_VERSION} -Dskip.surefire.tests
  else
    mvn verify -Dtest.sonar.version=${SQ_VERSION} -Dskip.surefire.tests
  fi
  ;;

javadoc)
    # Create JavaDocs to check for problems with JavaDoc generation
    mvn javadoc:javadoc
    ;;

*)
  echo "Unexpected TEST mode: $TEST"
  exit 1
  ;;

esac
