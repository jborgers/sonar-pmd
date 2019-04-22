
# AvoidStringBufferField
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidStringBufferField`<br/>
> :warning: This rule is **deprecated** in favour of [S1149](https://rules.sonarsource.com/java/RSPEC-1149).

-----

StringBuffers can grow quite a lot, and so may become a source of memory leak (if the owning class has a long life time). Example :
<pre>
class Foo {
  private StringBuffer memoryLeak;
}
</pre>

<p>
  This rule is deprecated, use {rule:squid:S1149} instead.
</p>

