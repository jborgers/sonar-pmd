To release a build

- change all `x.y.z-SNAPSHOT` to `x.y.z` in all poms
- change tag `HEAD` to `x.y.z` in scm tag in parent pom
- finish and update `CHANGELOG.md`, update `..master` to `..x.y.z`
- commit and push with comment "Release x.y.z"
- `git tag x.y.z`
- `git push --tags`
- use github action `release` to deploy to maven central
- release staging repo in Sonatype ui
- change all `x.y.z` in all poms to `x.y.z+1-SNAPSHOT`
- change tag `x.y.z` in scm tag in parent pom to `HEAD`
- prepare `CHANGELOG.md` for `x.y.z+1-SNAPSHOT`
- commit and push with comment "Prepare for release x.y.z+1-SNAPSHOT"

When release fails before "release staging in Sonatype ui"
- drop staging repo
- `git tag -d x.y.z`
- `git push origin :refs/tags/x.y.z`
- fix-commit-push and start release again with tagging steps above

In GitHub:

- create release from tag via Actions
- update release notes with `CHANGELOG.md` contents
- download `sonar-pmd-plugin-x.y.z.jar` and upload in release notes
- close milestone `x.y.z`
- create new milestone `x.y.z+1`

To marketplace:

- See [deploying-to-the-marketplace](https://community.sonarsource.com/t/deploying-to-the-marketplace/35236)
- See our first forum post: [new-release-sonar-pmd-plugin-3-4-0](https://community.sonarsource.com/t/new-release-sonar-pmd-plugin-3-4-0/63091)
