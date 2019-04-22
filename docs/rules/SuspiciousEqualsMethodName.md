# SuspiciousEqualsMethodName
**Category:** `pmd`<br/>
**Rule Key:** `pmd:SuspiciousEqualsMethodName`<br/>
> :warning: This rule is **deprecated** in favour of [S1201](https://rules.sonarsource.com/java/RSPEC-1201).

-----

The method name and parameter number are suspiciously close to equals(Object), which may mean you are intending to override the equals(Object) method. Example :
<pre>
public class Foo {
  public int equals(Object o) {
  // oops, this probably was supposed to be boolean equals
  }
  public boolean equals(String s) {
  // oops, this probably was supposed to be equals(Object)
  }
}
</pre>
