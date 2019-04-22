# FinalizeOverloaded
**Category:** `pmd`<br/>
**Rule Key:** `pmd:FinalizeOverloaded`<br/>
> :warning: This rule is **deprecated** in favour of [S1175](https://rules.sonarsource.com/java/RSPEC-1175).

-----

Methods named finalize() should not have parameters. It is confusing and probably a bug to overload finalize(). It will not be called by the VM.
