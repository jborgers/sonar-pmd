# JUnitAssertionsShouldIncludeMessage
**Category:** `pmd7-unit-tests`<br/>
**Rule Key:** `pmd7-unit-tests:JUnitAssertionsShouldIncludeMessage`<br/>
> :warning: This rule is **deprecated** in favour of [S2698](https://rules.sonarsource.com/java/RSPEC-2698).

-----

JUnit assertions should include a message - i.e., use the three argument version of assertEquals(), not the two argument version.
<pre>
public class Foo extends TestCase {
  public void testSomething() {
    assertEquals("foo", "bar"); // violation, should be assertEquals("Foo does not equals bar", "foo", "bar");
  }
}
</pre>
