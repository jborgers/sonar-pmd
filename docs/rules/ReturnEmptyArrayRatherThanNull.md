# ReturnEmptyArrayRatherThanNull
**Category:** `pmd`<br/>
**Rule Key:** `pmd:ReturnEmptyArrayRatherThanNull`<br/>
> :warning: This rule is **deprecated** in favour of [S1168](https://rules.sonarsource.com/java/RSPEC-1168).

-----

For any method that returns an array, it's a better behavior to return an empty array rather than a null reference. Example :
<pre>
public class Example
{
  // Not a good idea...
  public int []badBehavior()
  {
    // ...
    return null;
  }

  // Good behavior
  public String[] bonnePratique()
  {
    //...
    return new String[0];
  }
}
</pre>
