# AvoidCallingFinalize
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AvoidCallingFinalize`<br/>
> :warning: This rule is **deprecated** in favour of `java:ObjectFinalizeCheck`.

-----

<!-- (c) 2019 PMD -->
<p>
  The method <code>Object.finalize()</code> is called by the garbage collector on an object when garbage collection determines that there are no more references to the object.
  It should not be invoked by application logic.
</p>
<p>
  Note that Oracle has declared <code>Object.finalize()</code> as deprecated since JDK 9.
</p>
