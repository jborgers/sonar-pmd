
# UseCollectionIsEmpty
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UseCollectionIsEmpty`<br/>
> :warning: This rule is **deprecated** in favour of [S1155](https://rules.sonarsource.com/java/RSPEC-1155).

-----

The isEmpty() method on java.util.Collection is provided to see if a collection has any elements. Comparing the value of size() to 0 merely duplicates existing behavior.

<p>
  This rule is deprecated, use {rule:squid:S1155} instead.
</p>

