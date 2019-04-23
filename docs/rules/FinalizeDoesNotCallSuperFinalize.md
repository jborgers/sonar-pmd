# FinalizeDoesNotCallSuperFinalize
**Category:** `pmd`<br/>
**Rule Key:** `pmd:FinalizeDoesNotCallSuperFinalize`<br/>
> :warning: This rule is **deprecated** in favour of `squid:ObjectFinalizeOverridenCallsSuperFinalizeCheck`.

-----

If the finalize() is implemented, its last action should be to call super.finalize.
