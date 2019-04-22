
# JUnitSpelling
**Category:** `pmd-unit-tests`<br/>
**Rule Key:** `pmd-unit-tests:JUnitSpelling`<br/>


-----

Some JUnit framework methods are easy to misspell.
<pre>
import junit.framework.*;

public class Foo extends TestCase {
  public void setup() {} // violation, should be setUp()
  public void TearDown() {} // violation, should be tearDown()
}
</pre>

