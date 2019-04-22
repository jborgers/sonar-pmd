# BrokenNullCheck
**Category:** `pmd`<br/>
**Rule Key:** `pmd:BrokenNullCheck`<br/>
> :warning: This rule is **deprecated** in favour of [S1697](https://rules.sonarsource.com/java/RSPEC-1697).

-----

The null check is broken since it will throw a Nullpointer itself. The reason is that a method is called on the object when it is null. It is likely that you used || instead of && or vice versa.
