# MethodWithSameNameAsEnclosingClass
**Category:** `pmd`<br/>
**Rule Key:** `pmd:MethodWithSameNameAsEnclosingClass`<br/>
> :warning: This rule is **deprecated** in favour of [S1223](https://rules.sonarsource.com/java/RSPEC-1223).

-----

Non-constructor methods should not have the same name as the enclosing class. Example :
<pre>
public class MyClass {
  // this is bad because it is a method
  public void MyClass() {}
  // this is OK because it is a constructor
  public MyClass() {}
}
</pre>
