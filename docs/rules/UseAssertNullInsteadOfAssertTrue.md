# UseAssertNullInsteadOfAssertTrue
**Category:** `pmd-unit-tests`<br/>
**Rule Key:** `pmd-unit-tests:UseAssertNullInsteadOfAssertTrue`<br/>


-----

This rule detects JUnit assertions in object references equality. These assertions should be made by more specific methods, like assertNull, assertNotNull.
<pre>
public class FooTest extends TestCase {
  void testCode() {
    Object a = doSomething();

    assertTrue(a==null); // violation
    assertNull(a);  // good usage
    assertTrue(a != null); // violation
    assertNotNull(a);  // good usage
  }
}
</pre>
