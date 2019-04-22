# ArrayIsStoredDirectly
**Category:** `pmd`<br/>
**Rule Key:** `pmd:ArrayIsStoredDirectly`<br/>
> :warning: This rule is **deprecated** in favour of [S2384](https://rules.sonarsource.com/java/RSPEC-2384).

-----

Constructors and methods receiving arrays should clone objects and store the copy. This prevents that future changes from the user affect the internal functionality.
