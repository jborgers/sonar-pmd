# JUnitStaticSuite
**Category:** `pmd7-unit-tests`<br/>
**Rule Key:** `pmd7-unit-tests:JUnitStaticSuite`<br/>


-----

The suite() method in a JUnit test needs to be both public and static.
<pre>
import junit.framework.*;

public class Foo extends TestCase {
  public void suite() {} // violation, should be static
  private static void suite() {} // violation, should be public
}
</pre>
