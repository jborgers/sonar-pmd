# AvoidThrowingRawExceptionTypes
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidThrowingRawExceptionTypes`<br/>
> :warning: This rule is **deprecated** in favour of [S00112](https://rules.sonarsource.com/java/RSPEC-00112).

-----

<p>
  Avoid throwing certain exception types. Rather than throw a raw RuntimeException, Throwable, Exception, or Error, use
  a subclassed exception or error instead.
</p>
