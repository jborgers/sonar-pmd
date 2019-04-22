# CloneMethodMustImplementCloneable
**Category:** `pmd`<br/>
**Rule Key:** `pmd:CloneMethodMustImplementCloneable`<br/>
> :warning: This rule is **deprecated** in favour of [S1182](https://rules.sonarsource.com/java/RSPEC-1182).

-----

The method clone() should only be implemented if the class implements the Cloneable interface with the exception of a final method that only throws CloneNotSupportedException.
