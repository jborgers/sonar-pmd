
# EmptyInitializer
**Category:** `pmd`<br/>
**Rule Key:** `pmd:EmptyInitializer`<br/>
> :warning: This rule is **deprecated** in favour of [S00108](https://rules.sonarsource.com/java/RSPEC-00108).

-----

An empty initializer was found. Example :
<pre>
public class Foo {

   static {} // Why ?

   {} // Again, why ?

}
</pre>

<p>
  This rule is deprecated, use {rule:squid:S00108} instead.
</p>

