
# MethodReturnsInternalArray
**Category:** `pmd`<br/>
**Rule Key:** `pmd:MethodReturnsInternalArray`<br/>
> :warning: This rule is **deprecated** in favour of [S2384](https://rules.sonarsource.com/java/RSPEC-2384).

-----

Exposing internal arrays directly allows the user to modify some code that could be critical. It is safer to return a copy of the array.

<p>
  This rule is deprecated, use {rule:squid:S2384} instead.
</p>

