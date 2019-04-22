
# SimplifyBooleanExpressions
**Category:** `pmd`<br/>
**Rule Key:** `pmd:SimplifyBooleanExpressions`<br/>
> :warning: This rule is **deprecated** in favour of [S1125](https://rules.sonarsource.com/java/RSPEC-1125).

-----

Avoid unnecessary comparisons in boolean expressions - this complicates simple code. Example :
<pre>
public class Bar {
 // can be simplified to
 // bar = isFoo();
 private boolean bar = (isFoo() == true);

 public isFoo() { return false;}
}
</pre>

<p>
  This rule is deprecated, use {rule:squid:S1125} instead.
</p>

