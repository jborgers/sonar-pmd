# ArrayIsStoredDirectly
**Category:** `pmd`<br/>
**Rule Key:** `pmd:ArrayIsStoredDirectly`<br/>
> :warning: This rule is **deprecated** in favour of [S2384](https://rules.sonarsource.com/java/RSPEC-2384).

-----

<!-- (c) 2019 PMD -->
Constructors and methods receiving arrays should clone objects and store the copy. This prevents future changes from the user from affecting the original array.
