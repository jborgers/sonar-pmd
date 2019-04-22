# EmptyStatementNotInLoop
**Category:** `pmd`<br/>
**Rule Key:** `pmd:EmptyStatementNotInLoop`<br/>
> :warning: This rule is **deprecated** in favour of [EmptyStatementUsageCheck](https://rules.sonarsource.com/java/RSPEC-mptyStatementUsageCheck).

-----

An empty statement (aka a semicolon by itself) that is not used as the sole body of a for loop or while loop is probably a bug. It could also be a double semicolon, which is useless and should be removed.

<p>
  This rule is deprecated, use {rule:squid:EmptyStatementUsageCheck} instead.
</p>
