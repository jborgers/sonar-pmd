# UnnecessaryParentheses
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UnnecessaryParentheses`<br/>
> :warning: This rule is **deprecated** in favour of [UselessParenthesesCheck](https://rules.sonarsource.com/java/RSPEC-selessParenthesesCheck).

-----

Sometimes expressions are wrapped in unnecessary parentheses, making them look like a function call. Example :
<pre>
public class Foo {
  boolean bar() {
    return (true);
  }
}
</pre>

<p>
  This rule is deprecated, use {rule:squid:UselessParenthesesCheck} instead.
</p>
