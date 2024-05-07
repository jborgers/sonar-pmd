# SonarQube PMD Plugin 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.sonarsource.pmd/sonar-pmd-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.sonarsource.pmd/sonar-pmd-plugin)
![Build Status](https://github.com/jborgers/sonar-pmd/actions/workflows/build.yml/badge.svg)

Sonar-PMD is a plugin that provides coding rules from [PMD](https://pmd.github.io/) for use in SonarQube.

Starting April 2022, the project has found a new home. We, [jborgers](https://github.com/jborgers) and [stokpop](https://github.com/stokpop), 
aim to provide an active project and well-maintained sonar-pmd plugin. It is now sponsored by [Rabobank](https://www.rabobank.com/).

For a list of all rules and their status, see: [RULES.md](https://github.com/jborgers/sonar-pmd/blob/master/docs/RULES.md)

## Installation
The plugin should be available in the SonarQube marketplace and is preferably installed from within SonarQube (Administration -->  Marketplace --> Search _pmd_).

This plugin is available again from the Marketplace with the release of version 3.4.0.
Alternatively, download the [latest JAR file](https://github.com/jborgers/sonar-pmd/releases/latest), put it into the plugin directory (`./extensions/plugins`) and restart SonarQube.

## Usage
Usage should be straight forward:
1. Activate some PMD rules in your quality profile.
2. Run an analysis.

### Java version
Sonar-PMD analyzes the given source code with the Java source version defined in your Gradle or Maven project.
In case you are not using one of these build tools, or if that does not match the version you are using, set the `sonar.java.source` property to tell PMD which version of Java your source code complies to. 

Possible values : 1.6 to 1.8/8 to 20-preview

## Table of supported versions
| Sonar-PMD Plugin       | 3.1.x | 3.3.x  | 3.4.0           | 3.5.0         | 3.5.1         | 4.0.0 (planned) |
|------------------------|-------|--------|-----------------|---------------|---------------|-----------------|
| PMD                    | 6.9.0 | 6.30.0 | 6.45.0          | 6.55.0        | 6.55.0        | 7.1.0           |
| Max. Java Version      | 11    | 15     | 18              | 20-preview *2 | 20-preview *2 | 22              |
| Min. SonarQube Version | 6.6   | 6.7    | _8.9(*1)_ / 9.3 | 9.8           | 9.9.4         | 10.0            |
| Max. SonarQube Version |       |        | 9.9             | 10.4          | 10.5+         | 10.5+           |

(*1) Note: Plugin version 3.4.x runs in SonarQube 8.9, however, Java 17+ is only fully supported in SonarQube 9.3+.   
(*2) Note: Supports all tested Java 21 features; on parsing errors, warns instead of breaks 

A majority of the PMD rules have been rewritten in the Sonar Java plugin. Rewritten rules are marked "Deprecated" in the PMD plugin, but a [concise summary of replaced rules](http://dist.sonarsource.com/reports/coverage/pmd.html) is available.

## Rules on test
PMD tool provides some rules that can check the code of JUnit tests. Please note that these rules (and only these rules) will be applied only on the test files of your project.

## License
Sonar-PMD is licensed under the [GNU Lesser General Public License, Version 3.0](https://github.com/jborgers/sonar-pmd/blob/master/LICENSE.md).

Parts of the rule descriptions displayed in SonarQube have been extracted from [PMD](https://pmd.github.io/) and are licensed under a [BSD-style license](https://github.com/pmd/pmd/blob/master/LICENSE).  

## Build and test the plugin
To build the plugin and run the integration tests:

    ./mvnw clean verify
   
