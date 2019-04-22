# UnnecessaryConstructor
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UnnecessaryConstructor`<br/>
> :warning: This rule is **deprecated** in favour of [S1186](https://rules.sonarsource.com/java/RSPEC-1186).

-----

This rule detects when a constructor is not necessary; i.e., when there's only one constructor, it's public, has an empty body, and takes no arguments.
