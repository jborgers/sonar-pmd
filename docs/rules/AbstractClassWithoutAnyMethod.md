# AbstractClassWithoutAnyMethod
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AbstractClassWithoutAnyMethod`<br/>
> :warning: This rule is **deprecated** in favour of [S1694](https://rules.sonarsource.com/java/RSPEC-1694).

-----

<!-- (c) 2019 PMD -->
If an abstract class does not provide any method, it may be acting as a simple data container that is not meant to be instantiated. In this case, it is probably better to use a private or protected constructor in order to prevent instantiation than make the class misleadingly abstract.
<h2>Example:</h2>
<pre>
public class abstract Example {
  String field;
  int otherField;
}
</pre>
