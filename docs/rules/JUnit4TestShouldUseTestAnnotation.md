# JUnit4TestShouldUseTestAnnotation
**Category:** `pmd-unit-tests`<br/>
**Rule Key:** `pmd-unit-tests:JUnit4TestShouldUseTestAnnotation`<br/>


-----

In JUnit 3, the framework executed all methods which started with the word test as a unit test.
In JUnit 4, only methods annotated with the @Test annotation are executed. Example:
<pre>
public class MyTest {
    public void testBad() {
        doSomething();
    }

	@Test
    public void testGood() {
        doSomething();
    }
}
</pre>
