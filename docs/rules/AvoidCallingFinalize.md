# AvoidCallingFinalize
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidCallingFinalize`<br/>
> :warning: This rule is **deprecated** in favour of [ObjectFinalizeCheck](https://rules.sonarsource.com/java/RSPEC-bjectFinalizeCheck).

-----

Object.finalize() is called by the garbage collector on an object when garbage collection determines that there are no more references to the object.

<p>
  This rule is deprecated, use {rule:squid:ObjectFinalizeCheck} instead.
</p>
