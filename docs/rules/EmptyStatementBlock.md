# EmptyStatementBlock
**Category:** `pmd`<br/>
**Rule Key:** `pmd:EmptyStatementBlock`<br/>
> :warning: This rule is **deprecated** in favour of [S108](https://rules.sonarsource.com/java/RSPEC-108).

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
