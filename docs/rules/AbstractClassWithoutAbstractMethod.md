# AbstractClassWithoutAbstractMethod
**Category:** `pmd`<br/>
**Rule Key:** `pmd:AbstractClassWithoutAbstractMethod`<br/>
> :warning: This rule is **deprecated** in favour of [S1694](https://rules.sonarsource.com/java/RSPEC-1694).

-----

<!-- (c) 2019 PMD -->
The abstract class does not contain any abstract methods. An abstract class suggests an incomplete implementation, which is to be completed by subclasses implementing the abstract methods. If the class is intended to be used as a base class only (not to be instantiated directly) a protected constructor can be provided prevent direct instantiation.
