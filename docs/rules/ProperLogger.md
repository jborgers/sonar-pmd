# ProperLogger
**Category:** `pmd`<br/>
**Rule Key:** `pmd:ProperLogger`<br/>
> :warning: This rule is **deprecated** in favour of [S1312](https://rules.sonarsource.com/java/RSPEC-1312).

-----

Logger should normally be defined private static final and have the correct class. Private final Log log; is also allowed for rare cases when loggers need to be passed around, but the logger needs to be passed into the constructor.
