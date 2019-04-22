# DoNotThrowExceptionInFinally
**Category:** `pmd`<br/>
**Rule Key:** `pmd:DoNotThrowExceptionInFinally`<br/>
> :warning: This rule is **deprecated** in favour of [S1163](https://rules.sonarsource.com/java/RSPEC-1163).

-----

Throwing exception in a finally block is confusing. It may mask exception or a defect of the code, it also render code cleanup uninstable. Example :
<pre>
public class Foo
{
  public void bar()
  {
    try {
    // Here do some stuff
    }
    catch( Exception e) {
    // Handling the issue
    }
    finally
    {
      // is this really a good idea ?
      throw new Exception();
    }
  }
}
</pre>
