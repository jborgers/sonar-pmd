# UnnecessaryParentheses
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UnnecessaryParentheses`<br/>
> :warning: This rule is **deprecated** in favour of `java:UselessParenthesesCheck`.

-----

Sometimes expressions are wrapped in unnecessary parentheses, making them look like a function call. Example :
<pre>
public class Foo {
  boolean bar() {
    return (true);
  }
}
</pre>
