
# CloneMethodMustImplementCloneableWithTypeResolution
**Category:** `pmd`<br/>
**Rule Key:** `pmd:CloneMethodMustImplementCloneableWithTypeResolution`<br/>
> :warning: This rule is **deprecated** in favour of [S1182](https://rules.sonarsource.com/java/RSPEC-1182).

-----

The method clone() should only be implemented if the class implements the Cloneable interface with the exception
of a final method that only throws CloneNotSupportedException. This version uses PMD's type resolution facilities,
and can detect if the class implements or extends a Cloneable class. Example:
<pre>
public class MyClass {
  public Object clone() throws CloneNotSupportedException {
    return foo;
  }
}
</pre>

<p>
  This rule is deprecated, use {rule:squid:S1182} instead.
</p>

