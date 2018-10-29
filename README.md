# SonarQube PMD Plugin [![Build Status](https://api.travis-ci.org/jensgerdes/sonar-pmd.svg?branch=master)](https://travis-ci.org/jensgerdes/sonar-pmd) [![SonarStatus](https://sonarcloud.io/api/project_badges/measure?project=org.sonarsource.pmd%3Asonar-pmd-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.sonarsource.pmd%3Asonar-pmd-plugin) [![SonarStatus](https://sonarcloud.io/api/project_badges/measure?project=org.sonarsource.pmd%3Asonar-pmd-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=org.sonarsource.pmd%3Asonar-pmd-plugin)
Sonar-PMD is a plugin that provides coding rules from [PMD](https://pmd.github.io/).

## Description / Features
PMD Plugin|2.0|2.1|2.2|2.3|2.4.1|2.5|2.6|3.0.0
-------|---|---|---|---|---|---|---|---
PMD|4.3|4.3|5.1.1|5.2.1|5.3.1|5.4.0|5.4.2|5.4.2
Supported Java Version | |  |  |  |  | 1.7 | 1.8 | 1.8
Min. SonarQube Version |  |  |  |  |  | 4.5.4 | 4.5.4 | 6.6

A majority of the PMD rules have been rewritten in the Java plugin. Rewritten rules are marked "Deprecated" in the PMD plugin, but a [concise summary of replaced rules](http://dist.sonarsource.com/reports/coverage/pmd.html) is available.

## Usage
In the quality profile, activate some rules from PMD and run an analysis on your project.
Set the sonar.java.source property to tell PMD which version of Java your source code complies to. The default value is 1.5. 

Possible values: 
- 1.4
- 1.5 or 5 
- 1.6 or 6 
- 1.7 or 7 
- 1.8 or 8

## Rules on test
PMD tool provides some rules that can check the code of JUnit tests. Please note that these rules (and only these rules) will be applied only on the test files of your project.

## License
Licensed under the [GNU Lesser General Public License, Version 3.0](https://github.com/jensgerdes/sonar-pmd/blob/master/LICENSE.md)

