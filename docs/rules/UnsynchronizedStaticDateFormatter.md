
# UnsynchronizedStaticDateFormatter
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UnsynchronizedStaticDateFormatter`<br/>
> :warning: This rule is **deprecated** in favour of [S2156](https://rules.sonarsource.com/java/RSPEC-2156).

-----

SimpleDateFormat is not synchronized. Sun recomends separate format instances for each thread. If multiple threads must access a static formatter, the formatter must be synchronized either on method or block level.

<p>
  This rule is deprecated, use {rule:squid:S2156} instead.
</p>

