# EmptyCatchBlock
**Category:** `pmd`<br/>
**Rule Key:** `pmd:EmptyCatchBlock`<br/>
> :warning: This rule is **deprecated** in favour of [S108](https://rules.sonarsource.com/java/RSPEC-108).

-----

<p>
  Empty Catch Block finds instances where an exception is caught, but nothing is done. In most circumstances, this
  swallows an exception which should either be acted on or reported.
</p>
