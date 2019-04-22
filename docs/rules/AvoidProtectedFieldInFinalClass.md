# AvoidProtectedFieldInFinalClass
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidProtectedFieldInFinalClass`<br/>
> :warning: This rule is **deprecated** in favour of [S2156](https://rules.sonarsource.com/java/RSPEC-2156).

-----

Do not use protected fields in final classes since they cannot be subclassed. Clarify your intent by using private or package access modifiers instead.
