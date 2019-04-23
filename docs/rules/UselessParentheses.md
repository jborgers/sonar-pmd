# UselessParentheses
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UselessParentheses`<br/>
> :warning: This rule is **deprecated** in favour of `squid:UselessParenthesesCheck`.

-----

Useless parentheses should be removed. Example:
<pre>
public class Foo {

   private int _bar1;
   private Integer _bar2;

   public void setBar(int n) {
      _bar1 = Integer.valueOf((n)); // here
      _bar2 = (n); // and here
   }

}
</pre>
