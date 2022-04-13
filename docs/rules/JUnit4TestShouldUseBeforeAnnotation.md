# JUnit4TestShouldUseBeforeAnnotation
**Category:** `pmd7-unit-tests`<br/>
**Rule Key:** `pmd7-unit-tests:JUnit4TestShouldUseBeforeAnnotation`<br/>


-----

In JUnit 3, the setUp method was used to set up all data entities required in running tests.
JUnit 4 skips the setUp method and executes all methods annotated with @Before before all tests Example:
<pre>
public class MyTest {
    public void setUp() {
        bad();
    }
}
public class MyTest2 {
    @Before public void setUp() {
        good();
    }
}
</pre>
