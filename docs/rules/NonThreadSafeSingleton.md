# NonThreadSafeSingleton
**Category:** `pmd`<br/>
**Rule Key:** `pmd:NonThreadSafeSingleton`<br/>
> :warning: This rule is **deprecated** in favour of [S2444](https://rules.sonarsource.com/java/RSPEC-2444).

-----

Non-thread safe singletons can result in bad state changes. Eliminate static singletons if possible by instantiating the object directly. Static singletons are usually not needed as only a single instance exists anyway. Other possible fixes are to synchronize the entire method or to use an initialize-on-demand holder class (do not use the double-check idiom). See Effective Java, item 48.
