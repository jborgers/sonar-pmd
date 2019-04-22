
# DontCallThreadRun
**Category:** `pmd`<br/>
**Rule Key:** `pmd:DontCallThreadRun`<br/>
> :warning: This rule is **deprecated** in favour of [S1217](https://rules.sonarsource.com/java/RSPEC-1217).

-----

Explicitly calling Thread.run() method will execute in the caller's thread of control.  Instead, call Thread.start() for the intended behavior.

<p>
  This rule is deprecated, use {rule:squid:S1217} instead.
</p>

