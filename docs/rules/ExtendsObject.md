# ExtendsObject
**Category:** `pmd`<br/>
**Rule Key:** `pmd:ExtendsObject`<br/>
> :warning: This rule is **deprecated** in favour of [S1939](https://rules.sonarsource.com/java/RSPEC-1939).

-----

No need to explicitly extend Object. Example:
<pre>
public class Foo extends Object { // not required
}
</pre>
