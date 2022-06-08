# EmptyInitializer
**Category:** `pmd`<br/>
**Rule Key:** `pmd:EmptyInitializer`<br/>
> :warning: This rule is **deprecated** in favour of [S108](https://rules.sonarsource.com/java/RSPEC-108).

-----

An empty initializer was found. Example :
<pre>
public class Foo {

   static {} // Why ?

   {} // Again, why ?

}
</pre>
