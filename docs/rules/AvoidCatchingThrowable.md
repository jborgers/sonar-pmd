# AvoidCatchingThrowable
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidCatchingThrowable`<br/>
> :warning: This rule is **deprecated** in favour of [S1181](https://rules.sonarsource.com/java/RSPEC-1181).

-----

<!-- (c) 2019 PMD -->
Catching <code>Throwable</code> errors is not recommended since its scope is very broad. It includes runtime issues such as
<code>OutOfMemoryError</code> that should be exposed and managed separately.
