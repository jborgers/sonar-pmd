# Release Process

This document describes the process for creating a new release of the SonarQube PMD plugin.

## Prerequisites

Before starting the release process:

1. Ensure all commits have been pushed
2. Verify the build passes with the `build.yml` GitHub Actions workflow
3. In Github, close all issues and pull requests related to the new release x.y.z.

## Preparation

### Update PMD Rules and Generate Release Notes

For updating PMD rules and generating release notes, refer to the [scripts documentation](scripts/README.md).

In Github create a Draft release, or a pre-release.

## Release Steps

1. Update documentation:
   - Create release notes in `CHANGELOG.md` (update `..master` to `..x.y.z`)
   - Copy the commented out SNAPSHOT section to new SNAPSHOT release x.y.z+1-SNAPSHOT
   - Uncomment the current SNAPSHOT release to non-SNAPSHOT upcoming release x.y.z
   - Fill in the "Implemented highlights"
   - Update `README.md` if needed
   - Commit an push changes

2. Publish the release in Github:
   - Fill the to-be-created version
   - Generate the release notes, sync with "Implemented highlights" from `CHANGELOG.md`
   - Press Publish button

   This will trigger the release workflow, which injects the git tag via maven `-Drevision=<tag>`.

3. Post-release tasks:
   - Manually release the staging repository in [Sonatype](https://oss.sonatype.org/#welcome) for Maven Central
   - Make release available in Sonar marketplace and post a message for the shiny new release (see below)

## Prepare for Next Development Cycle

1. Update version information:
   - Change the `revision` property to `x.y.z+1-SNAPSHOT` in `.mvn/maven.config`
   - Commit and push with the message "Prepare release x.y.z+1-SNAPSHOT"

2. Update GitHub:
   - Close milestone `x.y.z`
   - Create new milestone `x.y.z+1`

## Troubleshooting

If the release fails and needs to be "restarted":
1. Drop the staging repository
2. Delete the tag locally: `git tag -d x.y.z` (or delete in IntelliJ)
3. Delete the tag remotely: `git push origin :refs/tags/x.y.z` (or use context menu in Intellij)
4. Might need to also delete the tag in Github
5. Fix the issue, commit, push, and restart the release process

## Publishing to Marketplace

- Follow the [Deploying to the Marketplace](https://community.sonarsource.com/t/deploying-to-the-marketplace/35236) guide
- Reference our [first forum post](https://community.sonarsource.com/t/new-release-sonar-pmd-plugin-3-4-0/63091) as an example
