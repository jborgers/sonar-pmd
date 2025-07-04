# Create release

To create a new release, set git tag with new version number and push the tag.
The Github Actions `release.yml` will build and release to Github actions and Maven Central.

Make sure that all commits have been pushed and build 
with `build.yml` workflow before setting and pushing the tag.

## Preparation

### Update PMD rules (if needed)
If the PMD version has been updated or rules have changed, regenerate the rules-java.xml file:
```
./mvnw generate-resources -Pgenerate-pmd-rules -pl sonar-pmd-plugin
```
This will run the Groovy script that extracts rules from PMD and generates the rules-java.xml file in the correct location.

### Generate Release Notes for PMD Rules
To generate release notes comparing the old and new PMD rules:

1. Download the rules file from the previous release tag directly:

   ```commandline
   wget https://raw.githubusercontent.com/jborgers/sonar-pmd/<previous-release-tag>/sonar-pmd-plugin/src/main/resources/org/sonar/plugins/pmd/rules-java.xml -O scripts/old-rules.xml
   ```

2. Run the release notes generator script:
   ```commandline
   # Make the script executable (if needed)
   chmod +x scripts/generate_release_notes.groovy

   # Run with default options
   ./scripts/generate_release_notes.groovy

   # Or specify custom paths
   ./scripts/generate_release_notes.groovy --old <path-to-old-rules> --new <path-to-new-rules> --report <output-file>
   ```

3. The script will generate a Markdown report (default: docs/pmd_release_notes.md) containing:
   - Summary of rule changes
   - Rules that have been removed
   - Rules that have been added
   - Rules that remain unchanged (with status changes and alternatives for deprecated rules)

## Release Steps
- create release notes in `CHANGELOG.md`, update `..master` to `..x.y.z`; and update `README.md`
- commit both
- `git tag x.y.z`
- `git push --tags`

The release workflow will be triggered, using the git tag for `-Drevision=<tag>`. 

- manually release staging repo in [Sonatype](https://oss.sonatype.org/#welcome) for Maven Central
- manually change Github actions release from draft to final and limit the changelog here: [releases](https://github.com/jborgers/sonar-pmd/releases) 

Next prepare for next SNAPSHOT:

- change `revision` property in `x.y.z+1-SNAPSHOT` in parent pom
- prepare `CHANGELOG.md` for `x.y.z+1-SNAPSHOT`
- commit and push with comment "Prepare release x.y.z+1-SNAPSHOT"

When release fails before "release staging in Sonatype"
- drop staging repo
- `git tag -d x.y.z` or delete tag in IntelliJ
- `git push origin :refs/tags/x.y.z` or delete tag in context menu, delete remotes
- fix-commit-push and start release again with tagging steps above

In GitHub:

- close milestone `x.y.z`
- create new milestone `x.y.z+1`

To marketplace:

- See [deploying-to-the-marketplace](https://community.sonarsource.com/t/deploying-to-the-marketplace/35236)
- See our first forum post: [new-release-sonar-pmd-plugin-3-4-0](https://community.sonarsource.com/t/new-release-sonar-pmd-plugin-3-4-0/63091)
