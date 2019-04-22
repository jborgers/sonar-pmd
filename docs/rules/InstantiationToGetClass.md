
# InstantiationToGetClass
**Category:** `pmd`<br/>
**Rule Key:** `pmd:InstantiationToGetClass`<br/>
> :warning: This rule is **deprecated** in favour of [S2133](https://rules.sonarsource.com/java/RSPEC-2133).

-----

Avoid instantiating an object just to call getClass() on it; use the .class public member instead. Example : replace
<code>Class c = new String().getClass();</code> with <code>Class c = String.class;</code>

<p>
  This rule is deprecated, use {rule:squid:S2133} instead.
</p>

