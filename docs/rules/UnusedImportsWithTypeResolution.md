
# UnusedImportsWithTypeResolution
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UnusedImportsWithTypeResolution`<br/>
> :warning: This rule is **deprecated** in favour of [UselessImportCheck](https://rules.sonarsource.com/java/RSPEC-selessImportCheck).

-----

Avoid unused import statements. This rule will find unused on demand imports, i.e. import com.foo.*. Example:
<pre>
import java.io.*; // not referenced or required

public class Foo {}
</pre>

<p>
  This rule is deprecated, use {rule:squid:UselessImportCheck} instead.
</p>

