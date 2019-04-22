# SimplifyBooleanReturns
**Category:** `pmd`<br/>
**Rule Key:** `pmd:SimplifyBooleanReturns`<br/>
> :warning: This rule is **deprecated** in favour of [S1126](https://rules.sonarsource.com/java/RSPEC-1126).

-----

Avoid unnecessary if..then..else statements when returning a boolean. Example :
<pre>
public class Foo {
  private int bar =2;
  public boolean isBarEqualsTo(int x) {
    // this bit of code
    if (bar == x) {
     return true;
    } else {
     return false;
    }
    // can be replaced with a simple
    // return bar == x;
  }
}
</pre>
