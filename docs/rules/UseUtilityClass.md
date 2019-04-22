# UseUtilityClass
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UseUtilityClass`<br/>
> :warning: This rule is **deprecated** in favour of [S1118](https://rules.sonarsource.com/java/RSPEC-1118).

-----

For classes that only have static methods, consider making them utility classes.
Note that this doesn't apply to abstract classes, since their subclasses may well include non-static methods.
Also, if you want this class to be a utility class, remember to add a private constructor to prevent instantiation.
(Note, that this use was known before PMD 5.1.0 as UseSingleton).
