# AvoidCatchingNPE
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidCatchingNPE`<br/>
> :warning: This rule is **deprecated** in favour of [S1696](https://rules.sonarsource.com/java/RSPEC-1696).

-----

<!-- (c) 2019 PMD -->
Code should never throw <code>NullPointerException</code>s under normal circumstances.
A catch block may hide the original error, causing other, more subtle problems later on.
