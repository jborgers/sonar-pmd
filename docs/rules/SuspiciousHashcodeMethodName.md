# SuspiciousHashcodeMethodName
**Category:** `pmd`<br/>
**Rule Key:** `pmd:SuspiciousHashcodeMethodName`<br/>
> :warning: This rule is **deprecated** in favour of [S1221](https://rules.sonarsource.com/java/RSPEC-1221).

-----

The method name and return type are suspiciously close to hashCode(), which may mean you are intending to override the hashCode() method. Example :
<pre>
public class Foo {
  public int hashcode() {
  // oops, this probably was supposed to be hashCode
  }
}</pre>
