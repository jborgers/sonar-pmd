# NoPackage
**Category:** `pmd`<br/>
**Rule Key:** `pmd:NoPackage`<br/>
> :warning: This rule is **deprecated** in favour of [S1220](https://rules.sonarsource.com/java/RSPEC-1220).

-----

Detects when a class or interface does not have a package definition. Example :
<pre>
// no package declaration
public class ClassInDefaultPackage {
}
  </pre>
