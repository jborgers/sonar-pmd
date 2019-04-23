# AssignmentInOperand
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AssignmentInOperand`<br/>
> :warning: This rule is **deprecated** in favour of `squid:AssignmentInSubExpressionCheck`.

-----

<!-- (c) 2019 PMD -->
Avoid assignments in operands; this can make code more complicated and harder to read.

<h2>Example:</h2>
<pre>
public void bar() {
  int x = 2;
  if ((x = getX()) == 3) {
    System.out.println("3!");
  }
}
</pre>
