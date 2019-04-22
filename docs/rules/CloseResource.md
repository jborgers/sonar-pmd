# CloseResource
**Category:** `pmd`<br/>
**Rule Key:** `pmd:CloseResource`<br/>
> :warning: This rule is **deprecated** in favour of [S2095](https://rules.sonarsource.com/java/RSPEC-2095).

-----

Ensure that resources (like Connection, Statement, and ResultSet objects) are always closed after use. It does this by looking for code patterned like :
<pre>
Connection c = openConnection();
try {
  // do stuff, and maybe catch something
} finally {
  c.close();
}
</pre>
