# MisplacedNullCheck
**Category:** `pmd`<br/>
**Rule Key:** `pmd:MisplacedNullCheck`<br/>
> :warning: This rule is **deprecated** in favour of [S1697](https://rules.sonarsource.com/java/RSPEC-1697), [S2259](https://rules.sonarsource.com/java/RSPEC-2259).

-----

The null check here is misplaced. if the variable is null you'll get a NullPointerException.
Either the check is useless (the variable will never be null) or it's incorrect.
<br>Example :
<pre>
if (object1!=null && object2.equals(object1)) { 
  ...
}      
</pre>
