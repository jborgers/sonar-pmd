# AvoidCatchingThrowable
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidCatchingThrowable`<br/>
> :warning: This rule is **deprecated** in favour of [S1181](https://rules.sonarsource.com/java/RSPEC-1181).

-----

This is dangerous because it casts too wide a net; it can catch things like OutOfMemoryError.
