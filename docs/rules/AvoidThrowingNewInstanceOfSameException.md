# AvoidThrowingNewInstanceOfSameException
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidThrowingNewInstanceOfSameException`<br/>
> :warning: This rule is **deprecated** in favour of [S1166](https://rules.sonarsource.com/java/RSPEC-1166).

-----

Catch blocks that merely rethrow a caught exception wrapped inside a new instance of the same type only add to code size and runtime complexity. Example :
<pre>
public class Foo {
  void bar() {
    try {
      // do something
    }  catch (SomeException se) {
      // harmless comment
      throw new SomeException(se);
    }
  }
}
</pre>
