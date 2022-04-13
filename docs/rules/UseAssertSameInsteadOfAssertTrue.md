# UseAssertSameInsteadOfAssertTrue
**Category:** `pmd7-unit-tests`<br/>
**Rule Key:** `pmd7-unit-tests:UseAssertSameInsteadOfAssertTrue`<br/>


-----

This rule detects JUnit assertions in object references equality. These assertions should be made by more specific methods, like assertSame, assertNotSame.
<pre>
public class FooTest extends TestCase {
  void testCode() {
    Object a, b;

    assertTrue(a==b); // violation
    assertSame(a, b); // good usage
  }
}
</pre>
