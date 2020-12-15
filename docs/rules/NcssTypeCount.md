# NcssTypeCount
**Category:** `pmd`<br/>
**Rule Key:** `pmd:NcssTypeCount`<br/>
> :warning: This rule is **deprecated** in favour of [S104](https://jira.sonarsource.com/browse/RSPEC-104).
-----

This rule uses the NCSS (Non Commenting Source Statements) algorithm to determine the number of lines of code for a given type. NCSS ignores comments, and counts actual statements. Using this algorithm, lines of code that are split are counted as one.

<pre>
public class Foo extends Bar {
    public Foo() {
        //this class only has 6 NCSS lines
        super();





        super.foo();
    }
}
</pre>