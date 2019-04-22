
# FinalizeDoesNotCallSuperFinalize
**Category:** `pmd`<br/>
**Rule Key:** `pmd:FinalizeDoesNotCallSuperFinalize`<br/>
> :warning: This rule is **deprecated** in favour of [ObjectFinalizeOverridenCallsSuperFinalizeCheck](https://rules.sonarsource.com/java/RSPEC-bjectFinalizeOverridenCallsSuperFinalizeCheck).

-----

If the finalize() is implemented, its last action should be to call super.finalize.

<p>
  This rule is deprecated, use {rule:squid:ObjectFinalizeOverridenCallsSuperFinalizeCheck} instead.
</p>

