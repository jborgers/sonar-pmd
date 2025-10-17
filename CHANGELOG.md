# Changelog

[//]: # (## [4.3.0-SNAPSHOT]&#40;https://github.com/jborgers/sonar-pmd/tree/4.3.0-SNAPSHOT&#41; &#40;2025-xx-xx&#41;)

[//]: # ()
[//]: # (**Implemented highlights**)

[//]: # ()
[//]: # ([Full Changelog]&#40;https://github.com/jborgers/sonar-pmd/compare/4.2.0..master&#41;)



## [4.2.1](https://github.com/jborgers/sonar-pmd/tree/4.2.1) (2025-10-17)

**Implemented highlights**
* Remove required plugins pom.xml reference for Sonar market place
* Improved regex warning message

[Full Changelog](https://github.com/jborgers/sonar-pmd/compare/4.2.0..4.2.1)

## [4.2.0](https://github.com/jborgers/sonar-pmd/tree/4.2.0) (2025-10-13)

**Implemented highlights**
* Java 25 support
* Now PMD Java and Kotlin rules are available from 7.17.0 (292, up from 282), see details [pmd_release_notes_4.2.0.md](docs/pmd_release_notes_4.2.0.md)
* Activate Kotlin sensor 
* Add params in sonar rules xml based on Java Rule properties
* Fix Analysis scope for main and test sources
* Adjust severity level for code-style category 

[Full Changelog](https://github.com/jborgers/sonar-pmd/compare/4.1.0..4.2.0)

## [4.1.0](https://github.com/jborgers/sonar-pmd/tree/4.1.0) (2025-07-18)

**Implemented highlights**
* Now all current PMD Java rules are available (282, up from 206), see details [pmd_release_notes_4.1.0.md](docs/pmd_release_notes_4.1.0.md) 
* Generate Sonar rules xml for the plugin directly from the PMD 7.15.0 rules xml: makes all Java rules available and up-to-date automatically
* Updated and non-deprecated the "PMD XPath Template Rule" (pmd:XPathRule) to create custom Java rules with powerful PMD7 XPath expressions
* Generate nicely formatted html descriptions from the PMD rule description markup
* Added `pmd` tag and category tag for each rule
* Added `has-sonar-alternative` tag for rules with known Sonar alternative (instead of making rules with alternatives `Deprecated`)
* Simplified release process by automation
* Maven release via Sonatype Central Portal

[Full Changelog](https://github.com/jborgers/sonar-pmd/compare/4.0.3..4.1.0)

## [4.0.3](https://github.com/jborgers/sonar-pmd/tree/4.0.3) (2025-06-06)

Versions update release.

**Implemented highlights**
* Include PMD 7.14.0
* Updated minor dependencies
* Fix pom.xml revision tags in release

[Full Changelog](https://github.com/jborgers/sonar-pmd/compare/4.0.2..4.0.3)

**Implemented highlights**
* Remove the custom profile importer/exporter to support SonarQube Server 25.4 [#504](https://github.com/jborgers/sonar-pmd/issues/504)
* Add unused assignment rule [#505](https://github.com/jborgers/sonar-pmd/pull/505)
* Include PMD 7.13.0

## [4.0.2](https://github.com/jborgers/sonar-pmd/tree/4.0.2) (2025-06-06)

Bugfix release to work with latest SonarQube releases. See [#508](https://github.com/jborgers/sonar-pmd/issues/508) and [#509](https://github.com/jborgers/sonar-pmd/issues/509).

[Full Changelog](https://github.com/jborgers/sonar-pmd/compare/4.0.1..4.0.2)

**Implemented highlights**
* Remove the custom profile importer/exporter to support SonarQube Server 25.4 [#504](https://github.com/jborgers/sonar-pmd/issues/504)
* Add unused assignment rule [#505](https://github.com/jborgers/sonar-pmd/pull/505)
* Include PMD 7.13.0

## [4.0.1](https://github.com/jborgers/sonar-pmd/tree/4.0.1) (2025-03-03)
[Full Changelog](https://github.com/jborgers/sonar-pmd/compare/4.0.0..4.0.1)

**Implemented highlights**
* Fix supported java versions from 21 up to 24-preview [#499](https://github.com/jborgers/sonar-pmd/pull/499)
* Removed all junit tests, they have been moved or removed in PMD7 [#502](https://github.com/jborgers/sonar-pmd/pull/502)

**Limitations**
* Not all PMD 7 rules are made available in Sonar, yet, see [#495](https://github.com/jborgers/sonar-pmd/issues/495), [#498](https://github.com/jborgers/sonar-pmd/issues/498)

**Contributors**
* [Markus](https://github.com/meisenla)

## [4.0.0](https://github.com/jborgers/sonar-pmd/tree/4.0.0) (2025-02-24)
[Full Changelog](https://github.com/jborgers/sonar-pmd/compare/3.5.1..4.0.0)

**Implemented highlights:**
- Supports PMD 7 which is incompatible with PMD 6: the reason for a major release
- Supports latest SonarQube [9.9.4 - 10.8+]
- Supports running on Java 11 on analysis side for SQ 9.9.4 - 10.2.x 
- Supports running on Java 17 for all supported versions
- Needed for child plugins with custom rules written in PMD 7, such as [sonar-pmd-jpinpoint 2.0.0](https://github.com/jborgers/sonar-pmd-jpinpoint/releases/tag/2.0.0)

## [3.5.1](https://github.com/jborgers/sonar-pmd/tree/3.5.1) (2024-05-07)
[Full Changelog](https://github.com/jborgers/sonar-pmd/compare/3.5.0..3.5.1)

**Implemented highlights:**
- Supports latest SonarQube [9.9.4 - 10.5+]
- Supports running on Java 11 on analysis side for SQ 9.9.4 - 10.2.x
- Supports running on Java 17 for all supported versions
- Updated Sonar Plugin API+impl for SonarQube 9.9.4+
- Upgraded various dependencies

- ## [3.5.0](https://github.com/jborgers/sonar-pmd/tree/3.5.0) (2024-04-23)
[Full Changelog](https://github.com/jborgers/sonar-pmd/compare/3.4.0...3.5.0)

**Contributors:**
- [jborgers](https://github.com/jborgers) 
- [renewolfert](https://github.com/renewolfert)

**Implemented highlights:**
- Updated PMD (6.55.0) (last PMD-6) #422
- Support analyzing up to Java 20-preview (close to 21) #422
- Java 21+ falls back to 20-preview with warning (no error) #422
- Updated Sonar Plugin API+impl (9.8.0.63668) (SonarQube 9.8+) 
- Upgraded various dependencies
- Needs Java 17, the class file version is 61 

## [3.4.0](https://github.com/jborgers/sonar-pmd/tree/3.4.0) (2022-05-11)
[Full Changelog](https://github.com/jborgers/sonar-pmd/compare/3.3.1...3.4.0)

**Contributors:**
- [jborgers](https://github.com/jborgers)
- [stokpop](https://github.com/stokpop)
- [jensgerdes](https://github.com/jensgerdes) (Many thanks for his great maintenance and decision to transfer)

**Implemented highlights:**
- Updated PMD (6.45.0) #319
- Support for Java 18 (including 17) #319
- Updated Sonar Plugin API (9.4.0.54424) #309
- Removed explicit dependency on Java plugin for new SonarQube Marketplace setup #303
- Upgraded various dependencies
- Transferred maintenance to [jborgers](https://github.com/jborgers) and [stokpop](https://github.com/stokpop)

## [3.3.1](https://github.com/jensgerdes/sonar-pmd/tree/3.3.1) (2021-01-29)
[Full Changelog](https://github.com/jensgerdes/sonar-pmd/compare/3.3.0...3.3.1)

**Contributors:**
- [jborgers](https://github.com/jborgers)

**Closed issues:**
- Fixed Windows incompatibility introduced in 3.3.0 [\#244](https://github.com/jensgerdes/sonar-pmd/issues/244)

## [3.3.0](https://github.com/jensgerdes/sonar-pmd/tree/3.3.0) (2021-01-11)
[Full Changelog](https://github.com/jensgerdes/sonar-pmd/compare/3.2.1...3.3.0)

**Contributors:**
- [jborgers](https://github.com/jborgers)
- [robinverduijn](https://github.com/robinverduijn)

**Implemented enhancements:**
- Updated PMD (6.30.0)
- Support for Java 15
- Updated Sonar-Java API (6.0.1)

**Closed issues:**
- Fixed deprecated PMD API Usage [\#239](https://github.com/jensgerdes/sonar-pmd/issues/239)
- Fixed CVE-2018-10237 [\#230](https://github.com/jensgerdes/sonar-pmd/issues/230)
- Fixed incorrect rule description [\#78](https://github.com/jensgerdes/sonar-pmd/issues/78)

**Merged pull requests:**
- Move to pmd-6.29 and solve api incompatibility [\#228](https://github.com/jensgerdes/sonar-pmd/pull/228) ([jborgers](https://github.com/jborgers))
- Update pmd-java dependency to 6.22.0 [\#167](https://github.com/jensgerdes/sonar-pmd/pull/167) ([robinverduijn](https://github.com/robinverduijn))
- Use correct parent classloader to fix Java 9 style modules [\#168](https://github.com/jensgerdes/sonar-pmd/pull/168) ([robinverduijn](https://github.com/robinverduijn))

## [3.2.1](https://github.com/jensgerdes/sonar-pmd/tree/3.2.1) (2019-04-15)
[Full Changelog](https://github.com/jensgerdes/sonar-pmd/compare/3.2.0...3.2.1)

**Closed issues:**
- OptimizableToArrayCall message doesn't reflect PMD 6.x changes on this rule [\#75](https://github.com/jensgerdes/sonar-pmd/issues/75) 
- Code samples in rule documentation appeared in one single line

## [3.2.0](https://github.com/jensgerdes/sonar-pmd/tree/3.2.0) (2019-03-29)
[Full Changelog](https://github.com/jensgerdes/sonar-pmd/compare/3.1.3...3.2.0)

**Implemented enhancements:**
- Updated PMD (6.10.0)

**Closed issues:**
- SonarQube 7.6+ Support [\#81](https://github.com/jensgerdes/sonar-pmd/issues/81) 


## [3.1.3](https://github.com/jensgerdes/sonar-pmd/tree/3.1.3) (2018-11-29)
[Full Changelog](https://github.com/jensgerdes/sonar-pmd/compare/3.1.2...3.1.3)

**Closed issues:**
- Sonar-PMD may break analysis due to illegally formatted violation reports [\#70](https://github.com/jensgerdes/sonar-pmd/issues/70) 


## [3.1.2](https://github.com/jensgerdes/sonar-pmd/tree/3.1.2) (2018-11-26)
[Full Changelog](https://github.com/jensgerdes/sonar-pmd/compare/3.1.0...3.1.2)

**Closed issues:**
- Sonar PMD can not be used with Java 9+ using the sonar-scanner-gradle plugin [\#69](https://github.com/jensgerdes/sonar-pmd/issues/69) 


## [3.1.0](https://github.com/jensgerdes/sonar-pmd/tree/3.1.0) (2018-11-19)
[Full Changelog](https://github.com/jensgerdes/sonar-pmd/compare/3.0.1...3.1.0)

**Implemented enhancements:**
- Java 11 support
- Updated PMD (6.9.0)

**Breaking changes:**
- Config parameters of Rule [CyclomaticComplexity](https://pmd.github.io/pmd-6.9.0/pmd_rules_java_design.html#cyclomaticcomplexity) changed.
  - Removed: 
    - `reportLevel`
    - `showClassesComplexity`
    - `showMethodsComplexity`
  - Added
    - `classReportLevel`
    - `methodReportLevel`
- Config parameters of Rule [AvoidUsingHardCodedIP](https://pmd.github.io/pmd-6.9.0/pmd_rules_java_bestpractices.html#avoidusinghardcodedip) changed.
  - Removed: `pattern`
  - Added: `checkAddressTypes`


**Closed issues:**
- Upgrade pmd version to the latest one for parsing Java 8 code successfully [\#34](https://github.com/jensgerdes/sonar-pmd/issues/34) 
- Please update PMD to at least 5.5.2 [\#38](https://github.com/jensgerdes/sonar-pmd/issues/38)
- Java 10 compatibility [\#44](https://github.com/jensgerdes/sonar-pmd/issues/44)
- Upgrade to a recent PMD version? [\#48](https://github.com/jensgerdes/sonar-pmd/issues/48)
- Convert project into Multi module Maven Project [\#59](https://github.com/jensgerdes/sonar-pmd/issues/59)
- Sonar-PMD can not be used with all rules [\#64](https://github.com/jensgerdes/sonar-pmd/issues/64)

**Merged pull requests:**
- Upgrade to latest PMD [\#51](https://github.com/jensgerdes/sonar-pmd/pull/51) ([spasam](https://github.com/spasam))

## [3.0.1](https://github.com/jensgerdes/sonar-pmd/tree/3.0.1) (2018-10-29)
[Full Changelog](https://github.com/jensgerdes/sonar-pmd/compare/3.0.0...3.0.1)

**Fixed bugs:**
- Plugin does not work with SonarQube 7.4 [\#55](https://github.com/jensgerdes/sonar-pmd/issues/55)

## [3.0.0](https://github.com/jensgerdes/sonar-pmd/tree/3.0.0) (2018-10-29)
[Full Changelog](https://github.com/jensgerdes/sonar-pmd/compare/2.6...3.0.0)

**Implemented enhancements:**
- CodeStyle defined by _.editorconfig_
- Tests migrated to JUnit 5 & AssertJ
- Changed versioning scheme (_MAJOR.MINOR.PATCH_) 

**Fixed bugs:**
- Plugin doesn't work with SonarQube 7.3 [\#49](https://github.com/jensgerdes/sonar-pmd/issues/49)
- Fixed travis build and integration tests [\#52](https://github.com/jensgerdes/sonar-pmd/issues/52)

**Closed issues:**
- Usage of deprecated SonarQube API < 5.6 [\#42](https://github.com/jensgerdes/sonar-pmd/issues/42)
- Compatibility matrix not up to date [\#47](https://github.com/jensgerdes/sonar-pmd/issues/47)
- New maintainer? [\#50](https://github.com/jensgerdes/sonar-pmd/issues/50)

**Merged pull requests:**
- Fix grammar in AvoidUsingShortType.html [\#32](https://github.com/jensgerdes/sonar-pmd/pull/32) ([simon04](https://github.com/simon04))
- Add version 2.6 to PMD Plugin -> PMD mapping [\#41](https://github.com/jensgerdes/sonar-pmd/pull/41) ([char16t](https://github.com/char16t))

## [2.6](https://github.com/SonarSource/sonar-pmd/tree/2.6) (2016-06-30)
[Full Changelog](https://github.com/SonarSource/sonar-pmd/compare/2.5...2.6)

**Implemented enhancements:**
- Upgrade to PMD 5.4.2

## [2.5](https://github.com/SonarSource/sonar-pmd/tree/2.5) (2015-11-04)
[Full Changelog](https://github.com/SonarSource/sonar-pmd/compare/2.4.1...2.5)

**Implemented enhancements:**
- Upgrade to PMD 5.4.0

**Fixed bugs:**
- Fixed Integration tests

## [2.4.1](https://github.com/SonarSource/sonar-pmd/tree/2.4.1) (2015-05-05)
[Full Changelog](https://github.com/SonarSource/sonar-pmd/compare/2.4...2.4.1)

**Implemented enhancements:**
- Upgrade to PMD 5.3.1

## [2.4](https://github.com/SonarSource/sonar-pmd/tree/2.4) (2015-04-17)
[Full Changelog](https://github.com/SonarSource/sonar-pmd/compare/2.3...2.4)

**Implemented enhancements:**
- Upgrade to PMD 5.3.0

## [2.3](https://github.com/SonarSource/sonar-pmd/tree/2.3) (2014-11-28)
[Full Changelog](https://github.com/SonarSource/sonar-pmd/compare/2.2...2.3)

**Implemented enhancements:**
- Upgrade to PMD 5.2.1

## [2.2](https://github.com/SonarSource/sonar-pmd/tree/2.2) (2014-05-14)
[Full Changelog](https://github.com/SonarSource/sonar-pmd/compare/2.1...2.2)

**Implemented enhancements:**
- Upgrade to PMD 5.1.1
- Upgrade dependency on Sonar API to 4.2

## [2.1](https://github.com/SonarSource/sonar-pmd/tree/2.1) (2014-03-28)
[Full Changelog](https://github.com/SonarSource/sonar-pmd/compare/2.0...2.1)

## [2.0](https://github.com/SonarSource/sonar-pmd/tree/2.0) (2014-01-07)
[Full Changelog](https://github.com/SonarSource/sonar-pmd/compare/ed919df...2.0)

Initial setup of GIT repository.