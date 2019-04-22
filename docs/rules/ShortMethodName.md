
# ShortMethodName
**Category:** `pmd`<br/>
**Rule Key:** `pmd:ShortMethodName`<br/>
> :warning: This rule is **deprecated** in favour of [S00100](https://rules.sonarsource.com/java/RSPEC-00100).

-----

Detects when very short method names are used. Example :
<pre>
public class ShortMethod {
  public void a( int i ) { // Violation
  }
}
</pre>

<p>
  This rule is deprecated, use {rule:squid:S00100} instead.
</p>

