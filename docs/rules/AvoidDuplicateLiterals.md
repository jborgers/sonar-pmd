
# AvoidDuplicateLiterals
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidDuplicateLiterals`<br/>
> :warning: This rule is **deprecated** in favour of [S1192](https://rules.sonarsource.com/java/RSPEC-1192).

-----

Code containing duplicate String literals can usually be improved by declaring the String as a constant field. Example :
<pre>
public class Foo {
 private void bar() {
    buz("Howdy");
    buz("Howdy");
    buz("Howdy");
    buz("Howdy");
 }
 private void buz(String x) {}
}
</pre>

<p>
  This rule is deprecated, use {rule:squid:S1192} instead.
</p>

