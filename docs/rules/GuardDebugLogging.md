# GuardDebugLogging
**Category:** `pmd`<br/>
**Rule Key:** `pmd:GuardDebugLogging`<br/>


-----

When log messages are composed by concatenating strings, the whole section should be guarded by a isDebugEnabled() check to avoid performance and memory issues.
