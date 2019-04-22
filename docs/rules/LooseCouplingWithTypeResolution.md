
# LooseCouplingWithTypeResolution
**Category:** `pmd`<br/>
**Rule Key:** `pmd:LooseCouplingWithTypeResolution`<br/>
> :warning: This rule is **deprecated** in favour of [S1319](https://rules.sonarsource.com/java/RSPEC-1319).

-----

Avoid using implementation types (i.e., HashSet); use the interface (i.e, Set) instead Example:
<pre>
import java.util.ArrayList;
import java.util.HashSet;

public class Bar {

  // Use List instead
  private ArrayList list = new ArrayList();

  // Use Set instead
  public HashSet getFoo() {
    return new HashSet();
  }
}
</pre>

<p>
  This rule is deprecated, use {rule:squid:S1319} instead.
</p>


