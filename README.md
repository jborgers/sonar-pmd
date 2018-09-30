# SonarQube PMD Plugin [![Build Status](https://api.travis-ci.org/jensgerdes/sonar-pmd.svg?branch=fix_travis_builds)](https://travis-ci.org/jensgerdes/sonar-pmd) [![LGPL 3 License](https://img.shields.io/badge/license-LGPLv3-green.svg)](https://github.com/jensgerdes/sonar-pmd/blob/master/LICENSE.md)
Sonar-PMD is a plugin that provides coding rules from [PMD](https://pmd.github.io/).

**Build status (all branches)**

[![Build Status](https://api.travis-ci.org/jensgerdes/sonar-pmd.svg)](https://travis-ci.org/SonarSource/sonar-java)

## Description / Features
PMD Plugin|2.0|2.1|2.2|2.3|2.4.1|2.5|2.6
-------|---|---|---|---|---|---|---
PMD|4.3|4.3|5.1.1|5.2.1|5.3.1|5.4.0|5.4.2

A majority of the PMD rules have been rewritten in the Java plugin. Rewritten rules are marked "Deprecated" in the PMD plugin, but a [concise summary of replaced rules](http://dist.sonarsource.com/reports/coverage/pmd.html) is available.

## Usage
In the quality profile, activate some rules from PMD and run an analysis on your project.
Set the sonar.java.source property to tell PMD which version of Java your source code complies to. The default value is 1.5. Possible values: 1.4, 1.5 or 5, 1.6 or 6, 1.7 or 7. Since version 2.2 of the plugin, this property can also be set to 1.8 or 8.

## Rules on test
PMD tool provides some rules that can check the code of JUnit tests.Please note that these rules (and only these rules) will be applied only on the test files of your project.

## License
Licensed under the [GNU Lesser General Public License, Version 3.0](https://github.com/jensgerdes/sonar-pmd/blob/master/LICENSE.md)
