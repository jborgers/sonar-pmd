# UnnecessaryBooleanAssertion
**Category:** `pmd7-unit-tests`<br/>
**Rule Key:** `pmd7-unit-tests:UnnecessaryBooleanAssertion`<br/>


-----

A JUnit test assertion with a boolean literal is unnecessary since it always will eval to the same thing. Consider using flow control (in case of assertTrue(false) or similar) or simply removing statements like assertTrue(true) and assertFalse(false). If you just want a test to halt, use the fail method.
<pre>
public class SimpleTest extends TestCase {
  public void testX() {
    assertTrue(true); // violation
  }
}</pre>
