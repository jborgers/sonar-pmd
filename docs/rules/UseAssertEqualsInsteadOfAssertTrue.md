# UseAssertEqualsInsteadOfAssertTrue
**Category:** `pmd-unit-tests`<br/>
**Rule Key:** `pmd-unit-tests:UseAssertEqualsInsteadOfAssertTrue`<br/>


-----

This rule detects JUnit assertions in object equality. These assertions should be made by more specific methods, like assertEquals.
<pre>
public class FooTest extends TestCase {
  void testCode() {
    Object a, b;

    assertTrue(a.equals(b)); // violation
    assertEquals("a should equals b", a, b); // good usage
  }
}</pre>
