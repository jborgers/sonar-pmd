# PMD Rules Release Notes for version 4.2.0
_Do not edit this generated file._

## Summary
- Total rules in old version (4.1.0): 282
- Total rules in new version (4.2.0): 292
- Rules added: 10
- Rules removed: 0
- Rules unchanged: 278
- Rules updated: 4
- Rules renamed: 0

## Added Rules
The following rules have been added in the new version:

| Rule Key | Name | Severity | Category |
|----------|------|----------|----------|
| CollectionTypeMismatch | Collection type mismatch | Medium | errorprone |
| DanglingJavadoc | Dangling javadoc | Medium | documentation |
| ModifierOrder | Modifier order | Blocker | codestyle |
| OverrideBothEqualsAndHashCodeOnComparable | Override both equals and hash code on comparable | Medium | errorprone |
| RelianceOnDefaultCharset | Reliance on default charset | Medium | bestpractices |
| ReplaceJavaUtilCalendar | Replace java util calendar | Medium | errorprone |
| ReplaceJavaUtilDate | Replace java util date | Medium | errorprone |
| TypeParameterNamingConventions | Type parameter naming conventions | Low | codestyle |
| UselessPureMethodCall | Useless pure method call | Medium | errorprone |
| VariableCanBeInlined | Variable can be inlined | Medium | codestyle |

## Updated Rules
The following rules have been updated in the new version:

| Rule Key | Name | Old Severity | New Severity | Old Status | New Status | Alternatives | Category |
|----------|------|--------------|--------------|------------|------------|--------------|----------|
| AvoidLosingExceptionInformation | Avoid losing exception information |  |  | Active | Deprecated |  | errorprone |
| GenericsNaming | Generics naming |  |  | Active | Deprecated |  | codestyle |
| UnnecessaryLocalBeforeReturn | Unnecessary local before return |  |  | Active | Deprecated |  | codestyle |
| UselessOperationOnImmutable | Useless operation on immutable |  |  | Active | Deprecated |  | errorprone |

## Unchanged Rules
The following rules exist in both versions with no changes:

| Rule Key | Name | Severity | Status | Alternatives | Category |
|----------|------|----------|--------|--------------|----------|
| AbstractClassWithoutAbstractMethod | Abstract class without abstract method | Medium | Active |  | bestpractices |
| AbstractClassWithoutAnyMethod | Abstract class without any method | Blocker | Active |  | design |
| AccessorClassGeneration | Accessor class generation | Medium | Active |  | bestpractices |
| AccessorMethodGeneration | Accessor method generation | Medium | Active |  | bestpractices |
| AddEmptyString | Add empty string | Medium | Active |  | performance |
| AppendCharacterWithChar | Append character with char | Medium | Active |  | performance |
| ArrayIsStoredDirectly | Array is stored directly | Medium | Active |  | bestpractices |
| AssignmentInOperand | Assignment in operand | Medium | Active |  | errorprone |
| AssignmentToNonFinalStatic | Assignment to non final static | Medium | Active |  | errorprone |
| AtLeastOneConstructor | At least one constructor | Medium | Active |  | codestyle |
| AvoidAccessibilityAlteration | Avoid accessibility alteration | Medium | Active |  | errorprone |
| AvoidArrayLoops | Avoid array loops | Medium | Active |  | performance |
| AvoidAssertAsIdentifier | Avoid assert as identifier | High | Active |  | errorprone |
| AvoidBranchingStatementAsLastInLoop | Avoid branching statement as last in loop | High | Active |  | errorprone |
| AvoidCalendarDateCreation | Avoid calendar date creation | Medium | Active |  | performance |
| AvoidCallingFinalize | Avoid calling finalize | Medium | Active |  | errorprone |
| AvoidCatchingGenericException | Avoid catching generic exception | Medium | Active |  | design |
| AvoidCatchingNPE | Avoid catching NPE | Medium | Active |  | errorprone |
| AvoidCatchingThrowable | Avoid catching Throwable | Medium | Active |  | errorprone |
| AvoidDecimalLiteralsInBigDecimalConstructor | Avoid decimal literals in BigDecimal constructor | Medium | Active |  | errorprone |
| AvoidDeeplyNestedIfStmts | Avoid deeply nested if stmts | Medium | Active |  | design |
| AvoidDollarSigns | Avoid dollar signs | Medium | Active |  | codestyle |
| AvoidDuplicateLiterals | Avoid duplicate literals | Medium | Active |  | errorprone |
| AvoidEnumAsIdentifier | Avoid enum as identifier | High | Active |  | errorprone |
| AvoidFieldNameMatchingMethodName | Avoid field name matching method name | Medium | Active |  | errorprone |
| AvoidFieldNameMatchingTypeName | Avoid field name matching type name | Medium | Active |  | errorprone |
| AvoidFileStream | Avoid file stream | Blocker | Active |  | performance |
| AvoidInstanceofChecksInCatchClause | Avoid instanceof checks in catch clause | Medium | Active |  | errorprone |
| AvoidInstantiatingObjectsInLoops | Avoid instantiating objects in loops | Medium | Active |  | performance |
| AvoidLiteralsInIfCondition | Avoid literals in if condition | Medium | Active |  | errorprone |
| AvoidMessageDigestField | Avoid message digest field | Medium | Active |  | bestpractices |
| AvoidMultipleUnaryOperators | Avoid multiple unary operators | High | Active |  | errorprone |
| AvoidPrintStackTrace | Avoid print stack trace | Medium | Active |  | bestpractices |
| AvoidProtectedFieldInFinalClass | Avoid protected field in final class | Medium | Active |  | codestyle |
| AvoidProtectedMethodInFinalClassNotExtending | Avoid protected method in final class not extending | Medium | Active |  | codestyle |
| AvoidReassigningCatchVariables | Avoid reassigning catch variables | Medium | Active |  | bestpractices |
| AvoidReassigningLoopVariables | Avoid reassigning loop variables | Medium | Active |  | bestpractices |
| AvoidReassigningParameters | Avoid reassigning parameters | High | Active |  | bestpractices |
| AvoidRethrowingException | Avoid rethrowing exception | Medium | Active |  | design |
| AvoidStringBufferField | Avoid StringBuffer field | Medium | Active |  | bestpractices |
| AvoidSynchronizedAtMethodLevel | Avoid synchronized at method level | Medium | Active |  | multithreading |
| AvoidSynchronizedStatement | Avoid synchronized statement | Medium | Active |  | multithreading |
| AvoidThreadGroup | Avoid ThreadGroup | Medium | Active |  | multithreading |
| AvoidThrowingNewInstanceOfSameException | Avoid throwing new instance of same exception | Medium | Active |  | design |
| AvoidThrowingNullPointerException | Avoid throwing NullPointerException | Blocker | Active |  | design |
| AvoidThrowingRawExceptionTypes | Avoid throwing raw exception types | Blocker | Active |  | design |
| AvoidUncheckedExceptionsInSignatures | Avoid unchecked exceptions in signatures | Medium | Active |  | design |
| AvoidUsingHardCodedIP | Avoid using hard coded IP | Medium | Active |  | bestpractices |
| AvoidUsingNativeCode | Avoid using native code | High | Active |  | codestyle |
| AvoidUsingOctalValues | Avoid using octal values | Medium | Active |  | errorprone |
| AvoidUsingVolatile | Avoid using volatile | High | Active |  | multithreading |
| BigIntegerInstantiation | BigInteger instantiation | Medium | Active |  | performance |
| BooleanGetMethodName | Boolean get method name | Low | Active |  | codestyle |
| BrokenNullCheck | Broken null check | High | Active |  | errorprone |
| CallSuperFirst | Call super first | Medium | Active |  | errorprone |
| CallSuperInConstructor | Call super in constructor | Medium | Active |  | codestyle |
| CallSuperLast | Call super last | Medium | Active |  | errorprone |
| CheckResultSet | Check result set | Medium | Active |  | bestpractices |
| CheckSkipResult | Check skip result | Medium | Active |  | errorprone |
| ClassCastExceptionWithToArray | ClassCastException with toArray | Medium | Active |  | errorprone |
| ClassNamingConventions | Class naming conventions | Blocker | Active |  | codestyle |
| ClassWithOnlyPrivateConstructorsShouldBeFinal | Class with only private constructors should be final | Blocker | Active |  | design |
| CloneMethodMustBePublic | Clone method must be public | Medium | Active |  | errorprone |
| CloneMethodMustImplementCloneable | Clone method must implement Cloneable | Medium | Active |  | errorprone |
| CloneMethodReturnTypeMustMatchClassName | Clone method return type must match class name | Medium | Active |  | errorprone |
| CloseResource | Close resource | Medium | Active |  | errorprone |
| CognitiveComplexity | Cognitive complexity | Medium | Active |  | design |
| CollapsibleIfStatements | Collapsible if statements | Medium | Active |  | design |
| CommentContent | Comment content | Medium | Active |  | documentation |
| CommentDefaultAccessModifier | Comment default access modifier | Medium | Active |  | codestyle |
| CommentRequired | Comment required | Medium | Active |  | documentation |
| CommentSize | Comment size | Medium | Active |  | documentation |
| CompareObjectsWithEquals | Compare objects with equals | Medium | Active |  | errorprone |
| ComparisonWithNaN | Comparison with NaN | Medium | Active |  | errorprone |
| ConfusingArgumentToVarargsMethod | Confusing argument to varargs method | Medium | Active |  | errorprone |
| ConfusingTernary | Confusing ternary | Medium | Active |  | codestyle |
| ConsecutiveAppendsShouldReuse | Consecutive appends should reuse | Medium | Active |  | performance |
| ConsecutiveLiteralAppends | Consecutive literal appends | Medium | Active |  | performance |
| ConstantsInInterface | Constants in interface | Medium | Active |  | bestpractices |
| ConstructorCallsOverridableMethod | Constructor calls overridable method | Blocker | Active |  | errorprone |
| ControlStatementBraces | Control statement braces | Medium | Active |  | codestyle |
| CouplingBetweenObjects | Coupling between objects | Medium | Active |  | design |
| CyclomaticComplexity | Cyclomatic complexity | Medium | Active |  | design |
| DataClass | Data class | Medium | Active |  | design |
| DefaultLabelNotLastInSwitch | Default label not last in switch | Medium | Active |  | bestpractices |
| DetachedTestCase | Detached test case | Medium | Active |  | errorprone |
| DoNotCallGarbageCollectionExplicitly | Do not call garbage collection explicitly | High | Active |  | errorprone |
| DoNotExtendJavaLangError | Do not extend java.lang.Error | Medium | Active |  | design |
| DoNotExtendJavaLangThrowable | Do not extend java.lang.Throwable | Medium | Active |  | errorprone |
| DoNotHardCodeSDCard | Do not hard code SDCard | Medium | Active |  | errorprone |
| DoNotTerminateVM | Do not terminate VM | Medium | Active |  | errorprone |
| DoNotThrowExceptionInFinally | Do not throw exception in finally | Low | Active |  | errorprone |
| DoNotUseThreads | Do not use threads | Medium | Active |  | multithreading |
| DontCallThreadRun | Dont call thread run | Low | Active |  | multithreading |
| DontImportSun | Dont import sun | Low | Active |  | errorprone |
| DontUseFloatTypeForLoopIndices | Dont use float type for loop indices | Medium | Active |  | errorprone |
| DoubleBraceInitialization | Double brace initialization | Medium | Active |  | bestpractices |
| DoubleCheckedLocking | Double checked locking | Blocker | Active |  | multithreading |
| EmptyCatchBlock | Empty catch block | Medium | Active |  | errorprone |
| EmptyControlStatement | Empty control statement | Medium | Active |  | codestyle |
| EmptyFinalizer | Empty finalizer | Medium | Active |  | errorprone |
| EmptyMethodInAbstractClassShouldBeAbstract | Empty method in abstract class should be abstract | Blocker | Active |  | codestyle |
| EqualsNull | Equals null | Blocker | Active |  | errorprone |
| ExceptionAsFlowControl | Exception as flow control | Medium | Active |  | design |
| ExcessiveImports | Excessive imports | Medium | Active |  | design |
| ExcessiveParameterList | Excessive parameter List | Medium | Active |  | design |
| ExcessivePublicCount | Excessive public count | Medium | Active |  | design |
| ExhaustiveSwitchHasDefault | Exhaustive switch has default | Medium | Active |  | bestpractices |
| ExtendsObject | Extends object | Low | Active |  | codestyle |
| FieldDeclarationsShouldBeAtStartOfClass | Field declarations should be at start of class | Medium | Active |  | codestyle |
| FieldNamingConventions | Field naming conventions | Blocker | Active |  | codestyle |
| FinalFieldCouldBeStatic | Final field could be static | Medium | Active |  | design |
| FinalParameterInAbstractMethod | Final parameter in abstract method | Blocker | Active |  | codestyle |
| FinalizeDoesNotCallSuperFinalize | Finalize does not call super finalize | Medium | Active |  | errorprone |
| FinalizeOnlyCallsSuperFinalize | Finalize only calls super finalize | Medium | Active |  | errorprone |
| FinalizeOverloaded | Finalize overloaded | Medium | Active |  | errorprone |
| FinalizeShouldBeProtected | Finalize should be protected | Medium | Active |  | errorprone |
| ForLoopCanBeForeach | For loop can be foreach | Medium | Active |  | bestpractices |
| ForLoopShouldBeWhileLoop | For loop should be while loop | Medium | Active |  | codestyle |
| ForLoopVariableCount | For loop variable count | Medium | Active |  | bestpractices |
| FormalParameterNamingConventions | Formal parameter naming conventions | Blocker | Active |  | codestyle |
| GodClass | God class | Medium | Active |  | design |
| GuardLogStatement | Guard log statement | High | Active |  | bestpractices |
| HardCodedCryptoKey | Hard coded crypto key | Medium | Active |  | security |
| IdempotentOperations | Idempotent operations | Medium | Active |  | errorprone |
| IdenticalCatchBranches | Identical catch branches | Medium | Active |  | codestyle |
| ImmutableField | Immutable field | Medium | Active |  | design |
| ImplicitFunctionalInterface | Implicit functional interface | High | Active |  | bestpractices |
| ImplicitSwitchFallThrough | Implicit switch fall through | Medium | Active |  | errorprone |
| InefficientEmptyStringCheck | Inefficient empty string check | Medium | Active |  | performance |
| InefficientStringBuffering | Inefficient string buffering | Medium | Active |  | performance |
| InsecureCryptoIv | Insecure crypto IV | Medium | Active |  | security |
| InstantiationToGetClass | Instantiation to get class | Low | Active |  | errorprone |
| InsufficientStringBufferDeclaration | Insufficient StringBuffer declaration | Medium | Active |  | performance |
| InvalidJavaBean | Invalid Java bean | Medium | Active |  | design |
| InvalidLogMessageFormat | Invalid log message format | Info | Active |  | errorprone |
| JUnit4SuitesShouldUseSuiteAnnotation | JUnit4 suites should use suite annotation | Medium | Active |  | bestpractices |
| JUnit5TestShouldBePackagePrivate | JUnit5 test should be package private | Medium | Active |  | bestpractices |
| JUnitSpelling | JUnit spelling | Medium | Active |  | errorprone |
| JUnitStaticSuite | JUnit static suite | Medium | Active |  | errorprone |
| JUnitUseExpected | JUnit use expected | Medium | Active |  | bestpractices |
| JumbledIncrementer | Jumbled incrementer | Medium | Active |  | errorprone |
| LambdaCanBeMethodReference | Lambda can be method reference | Medium | Active |  | codestyle |
| LawOfDemeter | Law of demeter | Medium | Active |  | design |
| LinguisticNaming | Linguistic naming | Medium | Active |  | codestyle |
| LiteralsFirstInComparisons | Literals first in comparisons | Medium | Active |  | bestpractices |
| LocalHomeNamingConvention | Local home naming convention | Low | Active |  | codestyle |
| LocalInterfaceSessionNamingConvention | Local interface session naming convention | Low | Active |  | codestyle |
| LocalVariableCouldBeFinal | Local variable could be final | Medium | Active |  | codestyle |
| LocalVariableNamingConventions | Local variable naming conventions | Blocker | Active |  | codestyle |
| LogicInversion | Logic inversion | Medium | Active |  | design |
| LongVariable | Long variable | Medium | Active |  | codestyle |
| LooseCoupling | Loose coupling | Medium | Active |  | bestpractices |
| LoosePackageCoupling | Loose package coupling | Medium | Active |  | design |
| MDBAndSessionBeanNamingConvention | MDB and session bean naming convention | Low | Active |  | codestyle |
| MethodArgumentCouldBeFinal | Method argument could be final | Medium | Active |  | codestyle |
| MethodNamingConventions | Method naming conventions | Blocker | Active |  | codestyle |
| MethodReturnsInternalArray | Method returns internal array | Medium | Active |  | bestpractices |
| MethodWithSameNameAsEnclosingClass | Method with same name as enclosing class | Medium | Active |  | errorprone |
| MisplacedNullCheck | Misplaced null check | Medium | Active |  | errorprone |
| MissingOverride | Missing override | Medium | Active |  | bestpractices |
| MissingSerialVersionUID | Missing serialVersionUID | Medium | Active |  | errorprone |
| MissingStaticMethodInNonInstantiatableClass | Missing static method in non instantiatable class | Medium | Active |  | errorprone |
| MoreThanOneLogger | More than one logger | High | Active |  | errorprone |
| MutableStaticState | Mutable static state | Medium | Active |  | design |
| NPathComplexity | NPath complexity | Medium | Active |  | design |
| NcssCount | NCSS count | Medium | Active |  | design |
| NoPackage | No package | Medium | Active |  | codestyle |
| NonCaseLabelInSwitch | Non case label in switch | Medium | Active |  | errorprone |
| NonExhaustiveSwitch | Non exhaustive switch | Medium | Active |  | bestpractices |
| NonSerializableClass | Non serializable class | Medium | Active |  | errorprone |
| NonStaticInitializer | Non static initializer | Medium | Active |  | errorprone |
| NonThreadSafeSingleton | Non thread safe singleton | Medium | Active |  | multithreading |
| NullAssignment | Null assignment | Medium | Active |  | errorprone |
| OneDeclarationPerLine | One declaration per line | Low | Active |  | bestpractices |
| OnlyOneReturn | Only one return | Medium | Active |  | codestyle |
| OptimizableToArrayCall | Optimizable toArray call | Medium | Active |  | performance |
| OverrideBothEqualsAndHashcode | Override both equals and hashcode | Medium | Active |  | errorprone |
| PackageCase | Package case | Medium | Active |  | codestyle |
| PrematureDeclaration | Premature declaration | Medium | Active |  | codestyle |
| PreserveStackTrace | Preserve stack trace | Medium | Active |  | bestpractices |
| PrimitiveWrapperInstantiation | Primitive wrapper instantiation | Medium | Active |  | bestpractices |
| ProperCloneImplementation | Proper clone implementation | High | Active |  | errorprone |
| ProperLogger | Proper logger | Medium | Active |  | errorprone |
| RedundantFieldInitializer | Redundant field initializer | Medium | Active |  | performance |
| RemoteInterfaceNamingConvention | Remote interface naming convention | Low | Active |  | codestyle |
| RemoteSessionInterfaceNamingConvention | Remote session interface naming convention | Low | Active |  | codestyle |
| ReplaceEnumerationWithIterator | Replace Enumeration with Iterator | Medium | Active |  | bestpractices |
| ReplaceHashtableWithMap | Replace Hashtable with Map | Medium | Active |  | bestpractices |
| ReplaceVectorWithList | Replace Vector with List | Medium | Active |  | bestpractices |
| ReturnEmptyCollectionRatherThanNull | Return empty collection rather than null | Blocker | Active |  | errorprone |
| ReturnFromFinallyBlock | Return from finally block | Medium | Active |  | errorprone |
| ShortClassName | Short class name | Low | Active |  | codestyle |
| ShortMethodName | Short method name | Medium | Active |  | codestyle |
| ShortVariable | Short variable | Medium | Active |  | codestyle |
| SignatureDeclareThrowsException | Signature declare throws Exception | Medium | Active |  | design |
| SimpleDateFormatNeedsLocale | SimpleDateFormat needs Locale | Medium | Active |  | errorprone |
| SimplifiableTestAssertion | Simplifiable test assertion | Medium | Active |  | bestpractices |
| SimplifiedTernary | Simplified ternary | Medium | Active |  | design |
| SimplifyBooleanExpressions | Simplify boolean expressions | Medium | Active |  | design |
| SimplifyBooleanReturns | Simplify boolean returns | Medium | Active |  | design |
| SimplifyConditional | Simplify conditional | Medium | Active |  | design |
| SingleMethodSingleton | Single method singleton | High | Active |  | errorprone |
| SingletonClassReturningNewInstance | Singleton class returning new instance | High | Active |  | errorprone |
| SingularField | Singular field | Medium | Active |  | design |
| StaticEJBFieldShouldBeFinal | Static EJBField should be final | Medium | Active |  | errorprone |
| StringBufferInstantiationWithChar | StringBuffer instantiation with char | Low | Active |  | errorprone |
| StringInstantiation | String instantiation | High | Active |  | performance |
| StringToString | String to string | Medium | Active |  | performance |
| SuspiciousEqualsMethodName | Suspicious equals method name | High | Active |  | errorprone |
| SuspiciousHashcodeMethodName | Suspicious hashcode method name | Medium | Active |  | errorprone |
| SuspiciousOctalEscape | Suspicious octal escape | Medium | Active |  | errorprone |
| SwitchDensity | Switch density | Medium | Active |  | design |
| SystemPrintln | System println | High | Active |  | bestpractices |
| TestClassWithoutTestCases | Test class without test cases | Medium | Active |  | errorprone |
| TooFewBranchesForSwitch | Too few branches for switch | Medium | Active |  | performance |
| TooManyFields | Too many fields | Medium | Active |  | design |
| TooManyMethods | Too many methods | Medium | Active |  | design |
| TooManyStaticImports | Too many static imports | Medium | Active |  | codestyle |
| UncommentedEmptyConstructor | Uncommented empty constructor | Medium | Active |  | documentation |
| UncommentedEmptyMethodBody | Uncommented empty method body | Medium | Active |  | documentation |
| UnconditionalIfStatement | Unconditional if statement | Medium | Active |  | errorprone |
| UnitTestAssertionsShouldIncludeMessage | Unit test assertions should include message | Medium | Active |  | bestpractices |
| UnitTestContainsTooManyAsserts | Unit test contains too many asserts | Medium | Active |  | bestpractices |
| UnitTestShouldIncludeAssert | Unit test should include assert | Medium | Active |  | bestpractices |
| UnitTestShouldUseAfterAnnotation | Unit test should use after annotation | Medium | Active |  | bestpractices |
| UnitTestShouldUseBeforeAnnotation | Unit test should use before annotation | Medium | Active |  | bestpractices |
| UnitTestShouldUseTestAnnotation | Unit test should use test annotation | Medium | Active |  | bestpractices |
| UnnecessaryAnnotationValueElement | Unnecessary annotation value element | Medium | Active |  | codestyle |
| UnnecessaryBooleanAssertion | Unnecessary boolean assertion | Medium | Active |  | errorprone |
| UnnecessaryBoxing | Unnecessary boxing | Medium | Active |  | codestyle |
| UnnecessaryCaseChange | Unnecessary case change | Medium | Active |  | errorprone |
| UnnecessaryCast | Unnecessary cast | Medium | Active |  | codestyle |
| UnnecessaryConstructor | Unnecessary constructor | Medium | Active |  | codestyle |
| UnnecessaryConversionTemporary | Unnecessary conversion temporary | Medium | Active |  | errorprone |
| UnnecessaryFullyQualifiedName | Unnecessary fully qualified name | Low | Active |  | codestyle |
| UnnecessaryImport | Unnecessary import | Low | Active |  | codestyle |
| UnnecessaryModifier | Unnecessary modifier | Medium | Active |  | codestyle |
| UnnecessaryReturn | Unnecessary return | Medium | Active |  | codestyle |
| UnnecessarySemicolon | Unnecessary semicolon | Medium | Active |  | codestyle |
| UnnecessaryVarargsArrayCreation | Unnecessary varargs array creation | Medium | Active |  | bestpractices |
| UnnecessaryWarningSuppression | Unnecessary warning suppression | Medium | Active |  | bestpractices |
| UnsynchronizedStaticFormatter | Unsynchronized static formatter | Medium | Active |  | multithreading |
| UnusedAssignment | Unused assignment | Medium | Active |  | bestpractices |
| UnusedFormalParameter | Unused formal parameter | Medium | Active |  | bestpractices |
| UnusedLocalVariable | Unused local variable | Medium | Active |  | bestpractices |
| UnusedNullCheckInEquals | Unused null check in equals | Medium | Active |  | errorprone |
| UnusedPrivateField | Unused private field | Medium | Active |  | bestpractices |
| UnusedPrivateMethod | Unused private method | Medium | Active |  | bestpractices |
| UseArrayListInsteadOfVector | Use Arrays.asList | Medium | Active |  | performance |
| UseArraysAsList | Use arrays as List | Medium | Active |  | performance |
| UseCollectionIsEmpty | Use Collection.isEmpty | Medium | Active |  | bestpractices |
| UseConcurrentHashMap | Use ConcurrentHashMap | Medium | Active |  | multithreading |
| UseCorrectExceptionLogging | Use correct exception logging | Medium | Active |  | errorprone |
| UseDiamondOperator | Use diamond operator | Medium | Active |  | codestyle |
| UseEnumCollections | Use enum collections | Medium | Active |  | bestpractices |
| UseEqualsToCompareStrings | Use equals to compare strings | Medium | Active |  | errorprone |
| UseExplicitTypes | Use explicit types | Medium | Active |  | codestyle |
| UseIOStreamsWithApacheCommonsFileItem | Use IOStreams with apache commons FileItem | Medium | Active |  | performance |
| UseIndexOfChar | Use index of char | Medium | Active |  | performance |
| UseLocaleWithCaseConversions | Use Locale with case conversions | Medium | Active |  | errorprone |
| UseNotifyAllInsteadOfNotify | Use notifyAll instead of notify | Medium | Active |  | multithreading |
| UseObjectForClearerAPI | Use object for clearer API | Medium | Active |  | design |
| UseProperClassLoader | Use proper ClassLoader | Medium | Active |  | errorprone |
| UseShortArrayInitializer | Use short Array initializer | Medium | Active |  | codestyle |
| UseStandardCharsets | Use standard Charsets | Medium | Active |  | bestpractices |
| UseStringBufferForStringAppends | Use StringBuffer for string appends | Medium | Active |  | performance |
| UseStringBufferLength | Use StringBuffer length | Medium | Active |  | performance |
| UseTryWithResources | Use try with resources | Medium | Active |  | bestpractices |
| UseUnderscoresInNumericLiterals | Use underscores in numeric literals | Medium | Active |  | codestyle |
| UseUtilityClass | Use utility class | Medium | Active |  | design |
| UseVarargs | Use varargs | Low | Active |  | bestpractices |
| UselessOverridingMethod | Useless overriding method | Medium | Active |  | design |
| UselessParentheses | Useless parentheses | Low | Active |  | codestyle |
| UselessQualifiedThis | Useless qualified this | Medium | Active |  | codestyle |
| UselessStringValueOf | Useless String.valueOf | Medium | Active |  | performance |
| WhileLoopWithLiteralBoolean | While loop with literal boolean | Medium | Active |  | bestpractices |
| XPathRule | PMD XPath Template Rule |  | Active |  |  |

## Removed Rules
No rules were removed.

Report generated on Fri Sep 26 18:01:25 CEST 2025
