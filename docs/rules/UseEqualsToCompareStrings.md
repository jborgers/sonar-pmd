# UseEqualsToCompareStrings
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UseEqualsToCompareStrings`<br/>
> :warning: This rule is **deprecated** in favour of `squid:StringEqualityComparisonCheck`, [S1698](https://rules.sonarsource.com/java/RSPEC-1698).

-----

Using "==" or "!=" to compare strings only works if intern version is used on both sides.
