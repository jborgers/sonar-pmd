# DefaultLabelNotLastInSwitchStmt
**Category:** `pmd`<br/>
**Rule Key:** `pmd:DefaultLabelNotLastInSwitchStmt`<br/>
> :warning: This rule is **deprecated** in favour of `java:SwitchLastCaseIsDefaultCheck`.

-----

Switch statements should have a default label. Example :
<pre>
public class Foo {
 void bar(int a) {
  switch (a) {
   case 1:  // do something
      break;
   default:  // the default case should be last, by convention
      break;
   case 2:
      break;
  }
 }
}
  </pre>
