# SonarQube PMD Plugin 

[![Sonatype Central](https://maven-badges.sml.io/sonatype-central/org.sonarsource.pmd/sonar-pmd-plugin/badge.svg)](https://maven-badges.sml.io/sonatype-central/org.sonarsource.pmd/sonar-pmd-plugin)
[![Maven Central](https://maven-badges.sml.io/maven-central/org.sonarsource.pmd/sonar-pmd-plugin/badge.svg)](https://maven-badges.sml.io/maven-central/org.sonarsource.pmd/sonar-pmd-plugin)
![Build Status](https://github.com/jborgers/sonar-pmd/actions/workflows/build.yml/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jborgers_sonar-pmd&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jborgers_sonar-pmd)

Sonar-PMD is a plugin that provides coding rules from [PMD](https://pmd.github.io/) for use in SonarQube.

Starting April 2022, the project has found a new home. We, [jborgers](https://github.com/jborgers) and [stokpop](https://github.com/stokpop), 
aim to provide an active project and well-maintained sonar-pmd plugin. It is now sponsored by [Rabobank](https://www.rabobank.com/).

## Installation
The plugin should be available in the SonarQube marketplace and is preferably installed from within SonarQube (Administration → Marketplace → Search _pmd_).

Alternatively, download the [latest JAR file](https://github.com/jborgers/sonar-pmd/releases/latest), put it into the plugin directory (`./extensions/downloads`) and restart SonarQube.

## Usage
Usage should be straight forward:
1. Activate some PMD rules in your quality profile.
2. Run an analysis.

### PMD version
Sonar PMD plugin version 4.0+ supports PMD 7 which is incompatible with PMD 6: the reason for a major release. 
Use version 4.0+ for child plugins with custom rules written in PMD 7, such as [sonar-pmd-jpinpoint 2.0.0](https://github.com/jborgers/sonar-pmd-jpinpoint/releases/tag/2.0.0).

### Java version
Sonar-PMD analyzes the given source code with the Java source version defined in your Gradle or Maven project.
In case you are not using one of these build tools, or if that does not match the version you are using, set the `sonar.java.source` property to tell PMD which version of Java your source code complies to. 

Possible values: 8 to 25 and 25-preview

## Table of supported versions
| Sonar-PMD Plugin       | 3.5.1           | 4.0.0   | 4.0.3      | 4.1.0       | 4.2.0         | 4.2.1           |  
|------------------------|-----------------|---------|------------|-------------|---------------------------------| 
| PMD                    | 6.55.0          | 7.10.0  | 7.14.0     | 7.15.0      | [7.17.0](https://github.com/pmd/pmd/releases/tag/pmd_releases%2F7.17.0) | [7.17.0](https://github.com/pmd/pmd/releases/tag/pmd_releases%2F7.17.0)   |
| Max. Java Version      | 20-preview (*1) | 20 (*2) | 24-preview | 24-preview  | 25-preview    | 25-preview      |  
| Min. SonarQube Version | 9.9.4           | 9.9.4   | 9.9.4      | 9.9.6       | 9.9.6         | 9.9.9           | 
| Max. SonarQube Version | 10.5+           | 10.8+   | 25.6+      | 25.6+       | 25.9+         | 25.10+          | 

(*1) Note: Supports all tested Java 21 features; on parsing errors, warns instead of breaks.   
(*2) Note: Does not support Java 20-preview nor Java 21.

## Limited Java PMD rule support before 4.1.0
PMD rules created since PMD 5.5.0 in 2016 were missing in release 4.0.3 and before.

Additionally, the sonar-pmd plugin marked the PMD rules which have a known adopted alternative in Sonar as `Deprecated`. 
Furthermore, PMD rules which were deprecated by PMD itself had the `Deprecated` mark as well, which was confusing. 

## Full Java PMD rule support starting with 4.1.0
With version 4.1.0 we introduce easy incorporation of new PMD rules into this plugin and thereby support the full up-to-date set of PMD rules in Sonar.

From now on, only rules that are deprecated in PMD are also marked `Deprecated` in Sonar. Rules that have alternative rules in Sonar are tagged with 
`has-sonar-alternative`, so they can be easily selected in SonarQube. The documentation will include the link to known alternative Sonar rule.

Limitations:
1. Referred alternative Java Sonar rules are limited to rules from before 2016, newer Java Sonar rules are not referred to yet. 
If you find missing alternative rules please create a Github issue.
2. The estimated amount of time to fix issues is only available for rules from before 2016.
3. Properties of the rules cannot be changed via SonarQube, currently only defaults can be used.

## Java PMD rules summary

- Total rules in old version (4.1.0): 282
- Total rules in new version (4.2.0): 292
- Rules added: 10
- Rules removed: 0
- Rules unchanged: 233
- Rules updated: 49
- Rules renamed: 10

See details: [pmd_release_notes_4.2.0.md](docs/pmd_release_notes_4.2.0.md)

## Support for other languages
Support for Apex PMD rules is work in progress. 

## Main vs Test sources and analysis scope
SonarQube distinguishes between main source files and test source files.
Sonar-PMD assigns a scope to each PMD rule so it runs only where it makes sense.

How scope is set automatically:
- Default: ALL (rule runs on both main and test sources).
- If a rule name contains "Test" or "JUnit" (case-insensitive), it is treated as a test rule and scoped to TEST automatically. 
- Test rules get the `tests` Sonar tag conform existing Sonar test scoped rules.

How to configure or override the scope in the PMD rules XML:
1. Force a rule to run only on tests: add the tag `tests`.
1. Force a rule to run only on main sources: add the tag `main-sources`.
1. Force a rule run on ALL sources: add both tags `tests` and `main-sources`. 

Note that the latter two options will override the rule name matching the test pattern. Also, the `tests` tag will not be shown.

Notes:
- The PMD tag `main-sources` is used for scope control and is not shown in the rule tags list in SonarQube.

## License
Sonar-PMD is licensed under the [GNU Lesser General Public License, Version 3.0](https://github.com/jborgers/sonar-pmd/blob/master/LICENSE.md).

Parts of the rule descriptions displayed in SonarQube have been extracted from [PMD](https://pmd.github.io/) and are licensed under a [BSD-style license](https://github.com/pmd/pmd/blob/master/LICENSE).  

## Build and test the plugin
To build the plugin and run the integration tests (use java 17 to build the plugin):

    ./mvnw clean verify

## Generate PMD rules XML (Java and Kotlin)
To regenerate the `rules-java.xml` and `rules-kotlin.xml` from PMD 7 using the provided Groovy script, run from the project root:

    ./mvnw clean generate-resources -Pgenerate-pmd-rules -pl sonar-pmd-plugin -am

Notes:
- The `-am` (also-make) flag ensures dependent modules (e.g., `sonar-pmd-lib`) are built in the reactor even when building only `sonar-pmd-plugin` with `-pl`.
- If `sonar-pmd-lib` fails to build so new changes are not reflected in the rules, try running `mvn clean package` in the `sonar-pmd-lib` module.
   
