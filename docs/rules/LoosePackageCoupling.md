# LoosePackageCoupling
**Category:** `pmd`<br/>
**Rule Key:** `pmd:LoosePackageCoupling`<br/>
> :warning: This rule is **deprecated** in favour of `squid:ArchitecturalConstraint`.

-----

Avoid using classes from the configured package hierarchy outside of the package hierarchy,
except when using one of the configured allowed classes. Example:
<pre>
package some.package;

import some.other.package.subpackage.subsubpackage.DontUseThisClass;

public class Bar {
   DontUseThisClass boo = new DontUseThisClass();
}
</pre>
