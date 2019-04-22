# NcssConstructorCount
**Category:** `pmd`<br/>
**Rule Key:** `pmd:NcssConstructorCount`<br/>
> :warning: This rule is **deprecated** in favour of [S138](https://rules.sonarsource.com/java/RSPEC-138).

-----

This rule uses the NCSS (Non Commenting Source Statements) algorithm to determine the number of lines of code for a given constructor. NCSS ignores comments, and counts actual statements. Using this algorithm, lines of code that are split are counted as one.
