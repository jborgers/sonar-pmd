# UnusedAssignment
**Category:** `pmd`<br/>
**Rule Key:** `pmd:UnusedAssignment`<br/>


-----

The value assigned to this variable is never used or always overwritten.

Problem: Assignments to variables for which the assigned value is not used because a new value is assigned before actual use, is unnecessary work and may indicate a bug.
Solution: remove the first assignment and make sure that is as intended.

Variables whose name starts with `ignored` or `unused` are filtered out, as
is standard practice for exceptions.

Limitations:
* The rule currently cannot know which method calls throw exceptions, or which exceptions they throw.
  In the body of a try block, every method or constructor call is assumed to throw.  This may cause false-negatives.
  The only other language construct that is assumed to throw is the `throw` statement, in particular,
  things like `assert` statements, or NullPointerExceptions on dereference are ignored.
* The rule cannot resolve assignments across constructors, when they're called with the special
  `this(...)` syntax. This may cause false-negatives.

Examples:
<pre>
class A {
    // this field initializer is redundant,
    // it is always overwritten in the constructor
    int f = 1;

    A(int f) {
        this.f = f;
    }
}
</pre>

<pre>
void bar() {
    String doodle = "init"; // bad, assigned value overwritten, not used
    doodle = "k";
    String str = "first"; // good
    String other = str.toString();
    str = "empty"; // bad
    doSomething();
    str = "other"; // good
    doSomethingWith(str);
    str = ""; // bad
    str = "second";
}
</pre>

