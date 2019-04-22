# AvoidPrefixingMethodParameters
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidPrefixingMethodParameters`<br/>
> :warning: This rule is **deprecated** in favour of [S00117](https://rules.sonarsource.com/java/RSPEC-00117).

-----

Prefixing parameters by 'in' or 'out' pollutes the name of the parameters and reduces code readability.
To indicate whether or not a parameter will be modify in a method, its better to document method
behavior with Javadoc. Example:
<pre>
// Not really clear
public class Foo {
  public void bar(
      int inLeftOperand,
      Result outRightOperand) {
      outRightOperand.setValue(inLeftOperand * outRightOperand.getValue());
  }
}
</pre>
