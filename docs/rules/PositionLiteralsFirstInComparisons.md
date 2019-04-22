
# PositionLiteralsFirstInComparisons
**Category:** `pmd`<br/>
**Rule Key:** `pmd:PositionLiteralsFirstInComparisons`<br/>
> :warning: This rule is **deprecated** in favour of [S1132](https://rules.sonarsource.com/java/RSPEC-1132).

-----

Position literals first in String comparisons - that way if the String is null you won't get a NullPointerException, it'll just return false.

<p>
  This rule is deprecated, use {rule:squid:S1132} instead.
</p>

