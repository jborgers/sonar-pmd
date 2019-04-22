
# MisleadingVariableName
**Category:** `pmd`<br/>
**Rule Key:** `pmd:MisleadingVariableName`<br/>
> :warning: This rule is **deprecated** in favour of [S00117](https://rules.sonarsource.com/java/RSPEC-00117).

-----

Detects when a non-field has a name starting with 'm_'. This usually indicates a field and thus is confusing. Example :
<pre>
public class Foo {
  private int m_foo; // OK
  public void bar(String m_baz) {  // Bad
    int m_boz = 42; // Bad
  }
}
</pre>

<p>
  This rule is deprecated, use {rule:squid:S00117} instead.
</p>

