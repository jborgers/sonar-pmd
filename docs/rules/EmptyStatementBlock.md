
# EmptyStatementBlock
**Category:** `pmd`<br/>
**Rule Key:** `pmd:EmptyStatementBlock`<br/>
> :warning: This rule is **deprecated** in favour of [S00108](https://rules.sonarsource.com/java/RSPEC-00108).

-----

Empty block statements serve no purpose and should be removed. Example:
<pre>
public class Foo {

   private int _bar;

   public void setBar(int bar) {
      { _bar = bar; } // Why not?
      {} // But remove this.
   }

}
</pre>

<p>
  This rule is deprecated, use {rule:squid:S00108} instead.
</p>

