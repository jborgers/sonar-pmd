
# AvoidCatchingNPE
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidCatchingNPE`<br/>
> :warning: This rule is **deprecated** in favour of [S1696](https://rules.sonarsource.com/java/RSPEC-1696).

-----

Code should never throw NPE under normal circumstances. A catch block may hide the original error, causing other more subtle errors in its wake.

<p>
  This rule is deprecated, use {rule:squid:S1696} instead.
</p>

