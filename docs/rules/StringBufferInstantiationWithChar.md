# StringBufferInstantiationWithChar
**Category:** `pmd`<br/>
**Rule Key:** `pmd:StringBufferInstantiationWithChar`<br/>
> :warning: This rule is **deprecated** in favour of [S1317](https://rules.sonarsource.com/java/RSPEC-1317).

-----

StringBuffer sb = new StringBuffer('c'); The char will be converted into int to intialize StringBuffer size.
