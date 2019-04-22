# PreserveStackTrace
**Category:** `pmd`<br/>
**Rule Key:** `pmd:PreserveStackTrace`<br/>
> :warning: This rule is **deprecated** in favour of [S1166](https://rules.sonarsource.com/java/RSPEC-1166).

-----

Throwing a new exception from a catch block without passing the original exception into the new Exception will cause the true stack trace to be lost, and can make it difficult to debug effectively.
