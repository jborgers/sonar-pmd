# SonarQube PMD Plugin [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.sonarsource.pmd/sonar-pmd-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.sonarsource.pmd/sonar-pmd-plugin) [![Build Status](https://api.travis-ci.org/jensgerdes/sonar-pmd.svg?branch=4.x)](https://travis-ci.org/jensgerdes/sonar-pmd) [![SonarStatus](https://sonarcloud.io/api/project_badges/measure?project=org.sonarsource.pmd%3Asonar-pmd&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.sonarsource.pmd%3Asonar-pmd) [![SonarStatus](https://sonarcloud.io/api/project_badges/measure?project=org.sonarsource.pmd%3Asonar-pmd&metric=coverage)](https://sonarcloud.io/dashboard?id=org.sonarsource.pmd%3Asonar-pmd)
Sonar-PMD is a plugin that provides coding rules from [PMD](https://pmd.github.io/).

For a list of all rules and their status, see: [RULES.md](https://github.com/jensgerdes/sonar-pmd/blob/master/docs/RULES.md)

## Recent changes
For recent changes, check the [CHANGELOG.md](https://github.com/jensgerdes/sonar-pmd/blob/4.x/CHANGELOG.md)

## Installation
The plugin is available in the SonarQube marketplace and should preferably be installed from within SonarQube (Administration -->  Marketplace --> Search _pmd_).

Alternatively, download the [latest JAR file](https://github.com/jensgerdes/sonar-pmd/releases/latest), put it into the plugin directory (`./extensions/plugins`) and restart SonarQube.

## Usage
Usage should be straight forward:
1. Activate some PMD rules in your quality profile.
2. Run an analysis.

### Troubleshooting
Sonar-PMD analyzes the given source code with the Java source version defined in your Gradle or Maven project.
In case you are not using one of these build tools, PMD uses the default Java version - which is **1.6**.  

If that does not match the version you are using, set the `sonar.java.source` property to tell PMD which version of Java your source code complies to. 

<details>
  <summary>Possible sonar.java.source values</summary>
  * 1.4
  * 1.5 or 5 
  * 1.6 or 6 
  * 1.7 or 7 
  * 1.8 or 8
  * 9
  * 10
  * 11
  * 12
  * 13
  * 14
  * 15
</details>

## Feature Compatibility Matrix
| Sonar-PMD Plugin | PMD | Min. SonarQube Version | Max. supported Java Version |
|----------|---------|-------------|------|
|4.0.0-SNAPSHOT|6.31.0|7.6|15|
|**3.3.x (STABLE)**|**6.30.0**|**6.7**|**15**|
|3.2.x|6.10.0|6.6|11|
|3.1.x|6.9.0|6.6|11|
|3.0.0|5.4.2|6.6|1.8|
|2.6|5.4.2|4.5.4|1.8|
|2.5|5.4.0|4.5.4|1.7|
|2.4.1|5.3.1|???|???|
|2.3|5.2.1|???|???|
|2.2|5.1.1|???|???|
|2.1|4.3|???|???|
|2.0|4.3|???|???|

A majority of the PMD rules have been rewritten in the Java plugin. Rewritten rules are marked "Deprecated" in the PMD plugin, but a [concise summary of replaced rules](http://dist.sonarsource.com/reports/coverage/pmd.html) is available.

## Rules on test
PMD tool provides some rules that can check the code of JUnit tests. Please note that these rules (and only these rules) will be applied only on the test files of your project.

## License
Sonar-PMD is licensed under the [GNU Lesser General Public License, Version 3.0](https://github.com/jensgerdes/sonar-pmd/blob/master/LICENSE.md).

Parts of the rule descriptions displayed in SonarQube have been extracted from [PMD](https://pmd.github.io/) and are licensed under a [BSD-style license](https://github.com/pmd/pmd/blob/master/LICENSE).  

