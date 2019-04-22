# AbstractClassWithoutAnyMethod
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AbstractClassWithoutAnyMethod`<br/>
> :warning: This rule is **deprecated** in favour of [S1694](https://rules.sonarsource.com/java/RSPEC-1694).

-----

If the abstract class does not provides any methods, it may be just a data container that is not to be instantiated. In this case, it's probably better to use a private or a protected constructor in order to prevent instantiation than make the class misleadingly abstract. Example :
<pre>
public class abstract Example {
  String field;
  int otherField;
}
</pre>
