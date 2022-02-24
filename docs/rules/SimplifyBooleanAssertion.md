# SimplifyBooleanAssertion
**Category:** `pmd7-unit-tests`<br/>
**Rule Key:** `pmd7-unit-tests:SimplifyBooleanAssertion`<br/>


-----

Avoid negation in an assertTrue or assertFalse test. For example, rephrase: assertTrue(!expr); as: assertFalse(expr);
<pre>
public class SimpleTest extends TestCase {
  public void testX() {
    assertTrue("not empty", !r.isEmpty()); // violation, replace with assertFalse("not empty", r.isEmpty())
    assertFalse(!r.isEmpty()); // violation, replace with assertTrue("empty", r.isEmpty())
  }
}
</pre>
