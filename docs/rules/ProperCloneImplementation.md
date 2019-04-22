# ProperCloneImplementation
**Category:** `pmd`<br/>
**Rule Key:** `pmd:ProperCloneImplementation`<br/>
> :warning: This rule is **deprecated** in favour of [S1182](https://rules.sonarsource.com/java/RSPEC-1182).

-----

Object clone() should be implemented with super.clone(). Example :
<pre>
class Foo{
    public Object clone(){
        return new Foo(); // This is bad
    }
}
  </pre>
