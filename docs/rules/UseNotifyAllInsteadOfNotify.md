# UseNotifyAllInsteadOfNotify
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UseNotifyAllInsteadOfNotify`<br/>
> :warning: This rule is **deprecated** in favour of [S2446](https://rules.sonarsource.com/java/RSPEC-2446).

-----

Thread.notify() awakens a thread monitoring the object. If more than one thread is monitoring, then only one is chosen. The thread chosen is arbitrary; thus it's usually safer to call notifyAll() instead.
