# Release Process

This document describes the process for creating a new release of the SonarQube PMD plugin.

## Prerequisites

Before starting the release process:

1. Ensure all commits have been pushed
2. Verify the build passes with the `build.yml` GitHub Actions workflow

## Preparation

### Update PMD Rules and Generate Release Notes

For updating PMD rules and generating release notes, refer to the [scripts documentation](scripts/README.md).

## Release Steps

1. Update documentation:
   - Create release notes in `CHANGELOG.md` (update `..master` to `..x.y.z`)
   - Update `README.md` if needed
   - Commit changes

2. Create and push the release tag:
   ```bash
   git tag x.y.z
   git push --tags
   ```

   This will trigger the release workflow, which uses the git tag for `-Drevision=<tag>`.

3. Post-release tasks:
   - Manually release the staging repository in [Sonatype](https://oss.sonatype.org/#welcome) for Maven Central
   - Update the GitHub Actions release from draft to final and edit the changelog at [GitHub Releases](https://github.com/jborgers/sonar-pmd/releases)

## Prepare for Next Development Cycle

1. Update version information:
   - Change the `revision` property to `x.y.z+1-SNAPSHOT` in the parent pom
   - Prepare `CHANGELOG.md` for the next version
   - Commit and push with the message "Prepare release x.y.z+1-SNAPSHOT"

2. Update GitHub:
   - Close milestone `x.y.z`
   - Create new milestone `x.y.z+1`

## Troubleshooting

If the release fails before "release staging in Sonatype":
1. Drop the staging repository
2. Delete the tag locally: `git tag -d x.y.z` (or delete in IntelliJ)
3. Delete the tag remotely: `git push origin :refs/tags/x.y.z` (or use context menu)
4. Fix the issue, commit, push, and restart the release process

## Publishing to Marketplace

- Follow the [Deploying to the Marketplace](https://community.sonarsource.com/t/deploying-to-the-marketplace/35236) guide
- Reference our [first forum post](https://community.sonarsource.com/t/new-release-sonar-pmd-plugin-3-4-0/63091) as an example
