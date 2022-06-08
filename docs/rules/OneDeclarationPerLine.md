# OneDeclarationPerLine
**Category:** `pmd`<br/>
**Rule Key:** `pmd:OneDeclarationPerLine`<br/>
> :warning: This rule is **deprecated** in favour of [S122](https://rules.sonarsource.com/java/RSPEC-122).

-----

Java allows the use of several variables declaration of the same type on one line. However, it
can lead to quite messy code. This rule looks for several declarations on the same line. Example:
<pre>
String name; // separate declarations
String lastname;

String name, lastname; // combined declaration, a violation
</pre>
