# FinalizeShouldBeProtected
**Category:** `pmd`<br/>
**Rule Key:** `pmd:FinalizeShouldBeProtected`<br/>
> :warning: This rule is **deprecated** in favour of [S1174](https://rules.sonarsource.com/java/RSPEC-1174).

-----

If you override finalize(), make it protected. If you make it public, other classes may call it.
