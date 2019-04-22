
# IdempotentOperations
**Category:** `pmd`<br/>
**Rule Key:** `pmd:IdempotentOperations`<br/>
> :warning: This rule is **deprecated** in favour of [S1656](https://rules.sonarsource.com/java/RSPEC-1656).

-----

Avoid idempotent operations - they are have no effect. Example : <br/><code>int x = 2;<br/> x = x;</code>

<p>
  This rule is deprecated, use {rule:squid:S1656} instead.
</p>

