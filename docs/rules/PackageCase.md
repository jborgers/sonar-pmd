# PackageCase
**Category:** `pmd`<br/>
**Rule Key:** `pmd:PackageCase`<br/>
> :warning: This rule is **deprecated** in favour of [S00120](https://rules.sonarsource.com/java/RSPEC-00120).

-----

Detects when a package definition contains upper case characters. Example :
<pre>
package com.MyCompany;  // <- should be lower case name
public class SomeClass {
}
</pre>
