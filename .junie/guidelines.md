# Sonar-PMD Plugin Guidelines

## Project Overview
Sonar-PMD is a SonarQube plugin that integrates PMD (a static code analyzer) into SonarQube. It provides coding rules from PMD for use in SonarQube, allowing users to detect code quality issues in their Java, Apex, and Kotlin code.

The project is currently maintained by Jeroen Borgers and Peter Paul Bakker, and is sponsored by Rabobank. It was previously maintained by SonarSource and later by Jens Gerdes before being transferred to the current maintainers in 2022.

## Project Structure
The project is organized as a multi-module Maven project with the following modules:

1. **sonar-pmd-lib**: Core library containing the PMD rule definitions and integration logic
2. **sonar-pmd-plugin**: The actual SonarQube plugin that gets packaged and deployed
3. **integration-test**: Integration tests for the plugin

Key directories:
- `/sonar-pmd-lib/src/main/java`: Core implementation classes
- `/sonar-pmd-plugin/src/main/java`: Plugin-specific implementation
- `/integration-test/src/test/java`: Integration tests

## Build Requirements
- Java 17 is required to build the plugin
- Maven 3.8+ is required

## Testing Guidelines
When making changes to the plugin, Junie should:

1. **Run unit tests** to verify that the changes don't break existing functionality:
   ```
   ./mvnw clean test
   ```

2. **Run integration tests** for more comprehensive testing:
   ```
   ./mvnw clean verify
   ```

3. **Test with different SonarQube versions** if making changes that might affect compatibility. The plugin currently supports SonarQube 9.9.4 and above.

## Code Style
The project follows standard Java code style conventions. When making changes:

1. Keep code clean and readable
2. Add appropriate JavaDoc comments for public classes and methods
3. Follow existing patterns in the codebase
4. Ensure backward compatibility when possible

## Version Compatibility
The plugin has specific version compatibility requirements:
- PMD version: 7.17.0
- Java source compatibility: 8 to 25 (including 25-preview)
- SonarQube compatibility: 9.9.4 and above

## Release Process
The project uses semantic versioning:
- Major version changes indicate breaking changes (e.g., PMD 6 to PMD 7)
- Minor version changes indicate new features
- Patch version changes indicate bug fixes

## Important Notes
1. A number of the PMD rules have been rewritten in the default Sonar Java plugin. For known alternatives, the `has-sonar-alternative` tag is added with references to these alternative(s).
2. The plugin is licensed under the GNU Lesser General Public License, Version 3.0.
3. Parts of the rule descriptions displayed in SonarQube have been extracted from PMD and are licensed under a BSD-style license.
