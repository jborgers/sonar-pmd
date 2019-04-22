# DoNotCallSystemExit
**Category:** `pmd`<br/>
**Rule Key:** `pmd:DoNotCallSystemExit`<br/>
> :warning: This rule is **deprecated** in favour of [S1147](https://rules.sonarsource.com/java/RSPEC-1147).

-----

Web applications should not call System.exit(), since only the web container or the application server should stop the JVM.
