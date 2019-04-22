# LogicInversion
**Category:** `pmd`<br/>
**Rule Key:** `pmd:LogicInversion`<br/>
> :warning: This rule is **deprecated** in favour of [S1940](https://rules.sonarsource.com/java/RSPEC-1940).

-----

Use opposite operator instead of negating the whole expression with a logic complement operator. Example:
<pre>
public boolean bar(int a, int b) {

  if (!(a == b)) // use !=
    return false;

  if (!(a < b)) // use >=
    return false;

  return true;
}
</pre>
