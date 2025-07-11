# PMD Rules Release Notes for version 4.1.0
_Do not edit this generated file._

## Summary
- Total rules in old version (4.0.3): 206
- Total rules in new version (4.1.0): 281
- Rules added: 81
- Rules removed: 6
- Rules unchanged: 200
- Rules renamed: 10

## Removed Rules
The following rules have been removed in the new version:

| Rule Key | Priority | Status |
|----------|----------|--------|
| AvoidConstantsInterface | MAJOR | DEPRECATED |
| CloneMethodMustImplementCloneableWithTypeResolution | MAJOR | DEPRECATED |
| GuardLogStatementJavaUtil | MAJOR | Active |
| LooseCouplingWithTypeResolution | MAJOR | DEPRECATED |
| UnnecessaryParentheses | MINOR | DEPRECATED |
| XPathRule | MAJOR | DEPRECATED |

## Added Rules
The following rules have been added in the new version:

| Rule Key | Name | Severity | Status |
|----------|------|----------|--------|
| AccessorMethodGeneration | Accessor method generation | MAJOR | Active |
| AvoidCalendarDateCreation | Avoid calendar date creation | MAJOR | Active |
| AvoidFileStream | Avoid file stream | BLOCKER | Active |
| AvoidMessageDigestField | Avoid message digest field | MAJOR | Active |
| AvoidReassigningCatchVariables | Avoid reassigning catch variables | MAJOR | Active |
| AvoidReassigningLoopVariables | Avoid reassigning loop variables | MAJOR | Active |
| AvoidSynchronizedStatement | Avoid synchronized statement | MAJOR | Active |
| AvoidUncheckedExceptionsInSignatures | Avoid unchecked exceptions in signatures | MAJOR | Active |
| CloneMethodMustImplementCloneable | Clone method must implement cloneable | MAJOR | Active |
| CognitiveComplexity | Cognitive complexity | MAJOR | Active |
| ComparisonWithNaN | Comparison with na n | MAJOR | Active |
| ConfusingArgumentToVarargsMethod | Confusing argument to varargs method | MAJOR | Active |
| ConstantsInInterface | Constants in interface | MAJOR | Active |
| ControlStatementBraces | Control statement braces | MAJOR | Active |
| DataClass | Data class | MAJOR | Active |
| DefaultLabelNotLastInSwitch | Default label not last in switch | MAJOR | Active |
| DetachedTestCase | Detached test case | MAJOR | Active |
| DoNotExtendJavaLangThrowable | Do not extend java lang throwable | MAJOR | Active |
| DoNotTerminateVM | Do not terminate VM | MAJOR | Active |
| DoubleBraceInitialization | Double brace initialization | MAJOR | Active |
| EmptyControlStatement | Empty control statement | MAJOR | Active |
| ExhaustiveSwitchHasDefault | Exhaustive switch has default | MAJOR | Active |
| FieldNamingConventions | Field naming conventions | BLOCKER | Active |
| FinalParameterInAbstractMethod | Final parameter in abstract method | BLOCKER | Active |
| ForLoopCanBeForeach | For loop can be foreach | MAJOR | Active |
| ForLoopVariableCount | For loop variable count | MAJOR | Active |
| FormalParameterNamingConventions | Formal parameter naming conventions | BLOCKER | Active |
| GuardLogStatement | Guard log statement | CRITICAL | Active |
| HardCodedCryptoKey | Hard coded crypto key | MAJOR | Active |
| IdenticalCatchBranches | Identical catch branches | MAJOR | Active |
| ImplicitFunctionalInterface | Implicit functional interface | CRITICAL | Active |
| ImplicitSwitchFallThrough | Implicit switch fall through | MAJOR | Active |
| InsecureCryptoIv | Insecure crypto iv | MAJOR | Active |
| InvalidJavaBean | Invalid java bean | MAJOR | Active |
| InvalidLogMessageFormat | Invalid log message format | INFO | Active |
| JUnit4SuitesShouldUseSuiteAnnotation | JUnit4 suites should use suite annotation | MAJOR | Active |
| JUnit5TestShouldBePackagePrivate | JUnit5 test should be package private | MAJOR | Active |
| JUnitSpelling | JUnit spelling | MAJOR | Active |
| JUnitStaticSuite | JUnit static suite | MAJOR | Active |
| JUnitUseExpected | JUnit use expected | MAJOR | Active |
| LambdaCanBeMethodReference | Lambda can be method reference | MAJOR | Active |
| LinguisticNaming | Linguistic naming | MAJOR | Active |
| LiteralsFirstInComparisons | Literals first in comparisons | MAJOR | Active |
| LocalVariableNamingConventions | Local variable naming conventions | BLOCKER | Active |
| LooseCoupling | Loose coupling | MAJOR | Active |
| MissingOverride | Missing override | MAJOR | Active |
| MutableStaticState | Mutable static state | MAJOR | Active |
| NcssCount | Ncss count | MAJOR | Active |
| NonCaseLabelInSwitch | Non case label in switch | MAJOR | Active |
| NonExhaustiveSwitch | Non exhaustive switch | MAJOR | Active |
| NonSerializableClass | Non serializable class | MAJOR | Active |
| PrimitiveWrapperInstantiation | Primitive wrapper instantiation | MAJOR | Active |
| ReturnEmptyCollectionRatherThanNull | Return empty collection rather than null | BLOCKER | Active |
| SimplifiableTestAssertion | Simplifiable test assertion | MAJOR | Active |
| TestClassWithoutTestCases | Test class without test cases | MAJOR | Active |
| TooFewBranchesForSwitch | Too few branches for switch | MAJOR | Active |
| UnitTestAssertionsShouldIncludeMessage | Unit test assertions should include message | MAJOR | Active |
| UnitTestContainsTooManyAsserts | Unit test contains too many asserts | MAJOR | Active |
| UnitTestShouldIncludeAssert | Unit test should include assert | MAJOR | Active |
| UnitTestShouldUseAfterAnnotation | Unit test should use after annotation | MAJOR | Active |
| UnitTestShouldUseBeforeAnnotation | Unit test should use before annotation | MAJOR | Active |
| UnitTestShouldUseTestAnnotation | Unit test should use test annotation | MAJOR | Active |
| UnnecessaryAnnotationValueElement | Unnecessary annotation value element | MAJOR | Active |
| UnnecessaryBooleanAssertion | Unnecessary boolean assertion | MAJOR | Active |
| UnnecessaryBoxing | Unnecessary boxing | MAJOR | Active |
| UnnecessaryCast | Unnecessary cast | MAJOR | Active |
| UnnecessaryImport | Unnecessary import | MINOR | Active |
| UnnecessaryModifier | Unnecessary modifier | MAJOR | Active |
| UnnecessarySemicolon | Unnecessary semicolon | MAJOR | Active |
| UnnecessaryVarargsArrayCreation | Unnecessary varargs array creation | MAJOR | Active |
| UnnecessaryWarningSuppression | Unnecessary warning suppression | MAJOR | Active |
| UnsynchronizedStaticFormatter | Unsynchronized static formatter | MAJOR | Active |
| UseDiamondOperator | Use diamond operator | MAJOR | Active |
| UseEnumCollections | Use enum collections | MAJOR | Active |
| UseExplicitTypes | Use explicit types | MAJOR | Active |
| UseIOStreamsWithApacheCommonsFileItem | Use IOStreams with apache commons file item | MAJOR | Active |
| UseShortArrayInitializer | Use short array initializer | MAJOR | Active |
| UseStandardCharsets | Use standard charsets | MAJOR | Active |
| UseTryWithResources | Use try with resources | MAJOR | Active |
| UseUnderscoresInNumericLiterals | Use underscores in numeric literals | MAJOR | Active |
| WhileLoopWithLiteralBoolean | While loop with literal boolean | MAJOR | Active |

## Unchanged Rules
The following rules exist in both versions:

| Rule Key | Name | Old Priority | New Severity | Old Status | New Status | Alternatives |
|----------|------|--------------|--------------|------------|------------|--------------|
| AbstractClassWithoutAbstractMethod | Abstract class without abstract method | MAJOR | MAJOR | DEPRECATED | Active |  |
| AbstractClassWithoutAnyMethod | Abstract class without any method | MAJOR | BLOCKER | DEPRECATED | Active |  |
| AccessorClassGeneration | Accessor class generation | MAJOR | MAJOR | Active | Active |  |
| AddEmptyString | Add empty string | MAJOR | MAJOR | Active | Active |  |
| AppendCharacterWithChar | Append character with char | MINOR | MAJOR | Active | Active |  |
| ArrayIsStoredDirectly | Array is stored directly | CRITICAL | MAJOR | DEPRECATED | Active |  |
| AssignmentInOperand | Assignment in operand | MAJOR | MAJOR | DEPRECATED | Active |  |
| AssignmentToNonFinalStatic | Assignment to non final static | MAJOR | MAJOR | Active | Active |  |
| AtLeastOneConstructor | At least one constructor | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidAccessibilityAlteration | Avoid accessibility alteration | MAJOR | MAJOR | Active | Active |  |
| AvoidArrayLoops | Avoid array loops | MAJOR | MAJOR | Active | Active |  |
| AvoidAssertAsIdentifier | Avoid assert as identifier | MAJOR | CRITICAL | DEPRECATED | Active |  |
| AvoidBranchingStatementAsLastInLoop | Avoid branching statement as last in loop | MAJOR | CRITICAL | Active | Active |  |
| AvoidCallingFinalize | Avoid calling finalize | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidCatchingGenericException | Avoid catching generic exception | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidCatchingNPE | Avoid catching NPE | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidCatchingThrowable | Avoid catching throwable | CRITICAL | MAJOR | DEPRECATED | Active |  |
| AvoidDecimalLiteralsInBigDecimalConstructor | Avoid decimal literals in big decimal constructor | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidDeeplyNestedIfStmts | Avoid deeply nested if stmts | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidDollarSigns | Avoid dollar signs | MINOR | MAJOR | DEPRECATED | Active |  |
| AvoidDuplicateLiterals | Avoid duplicate literals | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidEnumAsIdentifier | Avoid enum as identifier | MAJOR | CRITICAL | DEPRECATED | Active |  |
| AvoidFieldNameMatchingMethodName | Avoid field name matching method name | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidFieldNameMatchingTypeName | Avoid field name matching type name | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidInstanceofChecksInCatchClause | Avoid instanceof checks in catch clause | MINOR | MAJOR | DEPRECATED | Active |  |
| AvoidInstantiatingObjectsInLoops | Avoid instantiating objects in loops | MINOR | MAJOR | Active | Active |  |
| AvoidLiteralsInIfCondition | Avoid literals in if condition | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidLosingExceptionInformation | Avoid losing exception information | MAJOR | CRITICAL | DEPRECATED | Active |  |
| AvoidMultipleUnaryOperators | Avoid multiple unary operators | MAJOR | CRITICAL | DEPRECATED | Active |  |
| AvoidPrintStackTrace | Avoid print stack trace | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidProtectedFieldInFinalClass | Avoid protected field in final class | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidProtectedMethodInFinalClassNotExtending | Avoid protected method in final class not extending | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidReassigningParameters | Avoid reassigning parameters | MAJOR | CRITICAL | DEPRECATED | Active |  |
| AvoidRethrowingException | Avoid rethrowing exception | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidStringBufferField | Avoid string buffer field | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidSynchronizedAtMethodLevel | Avoid synchronized at method level | MAJOR | MAJOR | Active | Active |  |
| AvoidThreadGroup | Avoid thread group | CRITICAL | MAJOR | Active | Active |  |
| AvoidThrowingNewInstanceOfSameException | Avoid throwing new instance of same exception | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidThrowingNullPointerException | Avoid throwing null pointer exception | MAJOR | BLOCKER | DEPRECATED | Active |  |
| AvoidThrowingRawExceptionTypes | Avoid throwing raw exception types | MAJOR | BLOCKER | DEPRECATED | Active |  |
| AvoidUsingHardCodedIP | Avoid using hard coded IP | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidUsingNativeCode | Avoid using native code | MAJOR | CRITICAL | Active | Active |  |
| AvoidUsingOctalValues | Avoid using octal values | MAJOR | MAJOR | DEPRECATED | Active |  |
| AvoidUsingVolatile | Avoid using volatile | MAJOR | CRITICAL | Active | Active |  |
| BigIntegerInstantiation | Big integer instantiation | MAJOR | MAJOR | Active | Active |  |
| BooleanGetMethodName | Boolean get method name | MAJOR | MINOR | Active | Active |  |
| BrokenNullCheck | Broken null check | CRITICAL | CRITICAL | DEPRECATED | Active |  |
| CallSuperFirst | Call super first | MAJOR | MAJOR | Active | Active |  |
| CallSuperInConstructor | Call super in constructor | MINOR | MAJOR | Active | Active |  |
| CallSuperLast | Call super last | MAJOR | MAJOR | Active | Active |  |
| CheckResultSet | Check result set | MAJOR | MAJOR | Active | Active |  |
| CheckSkipResult | Check skip result | MINOR | MAJOR | DEPRECATED | Active |  |
| ClassCastExceptionWithToArray | Class cast exception with to array | MAJOR | MAJOR | Active | Active |  |
| ClassNamingConventions | Class naming conventions | MAJOR | BLOCKER | DEPRECATED | Active |  |
| ClassWithOnlyPrivateConstructorsShouldBeFinal | Class with only private constructors should be final | MAJOR | BLOCKER | DEPRECATED | Active |  |
| CloneMethodMustBePublic | Clone method must be public | MAJOR | MAJOR | Active | Active |  |
| CloneMethodReturnTypeMustMatchClassName | Clone method return type must match class name | MAJOR | MAJOR | Active | Active |  |
| CloseResource | Close resource | CRITICAL | MAJOR | DEPRECATED | Active |  |
| CollapsibleIfStatements | Collapsible if statements | MINOR | MAJOR | DEPRECATED | Active |  |
| CommentContent | Comment content | MINOR | MAJOR | Active | Active |  |
| CommentDefaultAccessModifier | Comment default access modifier | MAJOR | MAJOR | Active | Active |  |
| CommentRequired | Comment required | MINOR | MAJOR | Active | Active |  |
| CommentSize | Comment size | MINOR | MAJOR | Active | Active |  |
| CompareObjectsWithEquals | Compare objects with equals | MAJOR | MAJOR | DEPRECATED | Active |  |
| ConfusingTernary | Confusing ternary | MAJOR | MAJOR | Active | Active |  |
| ConsecutiveAppendsShouldReuse | Consecutive appends should reuse | MAJOR | MAJOR | Active | Active |  |
| ConsecutiveLiteralAppends | Consecutive literal appends | MINOR | MAJOR | Active | Active |  |
| ConstructorCallsOverridableMethod | Constructor calls overridable method | MAJOR | BLOCKER | DEPRECATED | Active |  |
| CouplingBetweenObjects | Coupling between objects | MAJOR | MAJOR | DEPRECATED | Active |  |
| CyclomaticComplexity | Cyclomatic complexity | MAJOR | MAJOR | DEPRECATED | Active |  |
| DoNotCallGarbageCollectionExplicitly | Do not call garbage collection explicitly | CRITICAL | CRITICAL | DEPRECATED | Active |  |
| DoNotExtendJavaLangError | Do not extend java lang error | MAJOR | MAJOR | DEPRECATED | Active |  |
| DoNotHardCodeSDCard | Do not hard code SDCard | MAJOR | MAJOR | Active | Active |  |
| DoNotThrowExceptionInFinally | Do not throw exception in finally | MAJOR | MINOR | DEPRECATED | Active |  |
| DoNotUseThreads | Do not use threads | MAJOR | MAJOR | Active | Active |  |
| DontCallThreadRun | Dont call thread run | MAJOR | MINOR | DEPRECATED | Active |  |
| DontImportSun | Dont import sun | MINOR | MINOR | DEPRECATED | Active |  |
| DontUseFloatTypeForLoopIndices | Dont use float type for loop indices | MAJOR | MAJOR | Active | Active |  |
| DoubleCheckedLocking | Double checked locking | MAJOR | BLOCKER | Active | Active |  |
| EmptyCatchBlock | Empty catch block | CRITICAL | MAJOR | DEPRECATED | Active |  |
| EmptyFinalizer | Empty finalizer | MAJOR | MAJOR | DEPRECATED | Active |  |
| EmptyMethodInAbstractClassShouldBeAbstract | Empty method in abstract class should be abstract | MAJOR | BLOCKER | Active | Active |  |
| EqualsNull | Equals null | CRITICAL | BLOCKER | DEPRECATED | Active |  |
| ExceptionAsFlowControl | Exception as flow control | MAJOR | MAJOR | DEPRECATED | Active |  |
| ExcessiveImports | Excessive imports | MAJOR | MAJOR | DEPRECATED | Active |  |
| ExcessiveParameterList | Excessive parameter list | MAJOR | MAJOR | DEPRECATED | Active |  |
| ExcessivePublicCount | Excessive public count | MAJOR | MAJOR | DEPRECATED | Active |  |
| ExtendsObject | Extends object | MINOR | MINOR | DEPRECATED | Active |  |
| FieldDeclarationsShouldBeAtStartOfClass | Field declarations should be at start of class | MINOR | MAJOR | DEPRECATED | Active |  |
| FinalFieldCouldBeStatic | Final field could be static | MINOR | MAJOR | DEPRECATED | Active |  |
| FinalizeDoesNotCallSuperFinalize | Finalize does not call super finalize | MAJOR | MAJOR | DEPRECATED | Active |  |
| FinalizeOnlyCallsSuperFinalize | Finalize only calls super finalize | MAJOR | MAJOR | DEPRECATED | Active |  |
| FinalizeOverloaded | Finalize overloaded | MAJOR | MAJOR | DEPRECATED | Active |  |
| FinalizeShouldBeProtected | Finalize should be protected | MAJOR | MAJOR | DEPRECATED | Active |  |
| ForLoopShouldBeWhileLoop | For loop should be while loop | MINOR | MAJOR | DEPRECATED | Active |  |
| GenericsNaming | Generics naming | MAJOR | MINOR | DEPRECATED | Active |  |
| GodClass | God class | MAJOR | MAJOR | Active | Active |  |
| IdempotentOperations | Idempotent operations | MAJOR | MAJOR | DEPRECATED | Active |  |
| ImmutableField | Immutable field | MAJOR | MAJOR | Active | Active |  |
| InefficientEmptyStringCheck | Inefficient empty string check | MAJOR | MAJOR | Active | Active |  |
| InefficientStringBuffering | Inefficient string buffering | MAJOR | MAJOR | Active | Active |  |
| InstantiationToGetClass | Instantiation to get class | MAJOR | MINOR | DEPRECATED | Active |  |
| InsufficientStringBufferDeclaration | Insufficient string buffer declaration | MAJOR | MAJOR | Active | Active |  |
| JumbledIncrementer | Jumbled incrementer | MAJOR | MAJOR | DEPRECATED | Active |  |
| LawOfDemeter | Law of demeter | MAJOR | MAJOR | Active | Active |  |
| LocalHomeNamingConvention | Local home naming convention | MAJOR | MINOR | Active | Active |  |
| LocalInterfaceSessionNamingConvention | Local interface session naming convention | MAJOR | MINOR | Active | Active |  |
| LocalVariableCouldBeFinal | Local variable could be final | MINOR | MAJOR | Active | Active |  |
| LogicInversion | Logic inversion | MINOR | MAJOR | DEPRECATED | Active |  |
| LongVariable | Long variable | MAJOR | MAJOR | DEPRECATED | Active |  |
| LoosePackageCoupling | Loose package coupling | MAJOR | MAJOR | DEPRECATED | Active |  |
| MDBAndSessionBeanNamingConvention | MDBAnd session bean naming convention | MAJOR | MINOR | Active | Active |  |
| MethodArgumentCouldBeFinal | Method argument could be final | MINOR | MAJOR | DEPRECATED | Active |  |
| MethodNamingConventions | Method naming conventions | MAJOR | BLOCKER | DEPRECATED | Active |  |
| MethodReturnsInternalArray | Method returns internal array | CRITICAL | MAJOR | DEPRECATED | Active |  |
| MethodWithSameNameAsEnclosingClass | Method with same name as enclosing class | MAJOR | MAJOR | DEPRECATED | Active |  |
| MisplacedNullCheck | Misplaced null check | CRITICAL | MAJOR | DEPRECATED | Active |  |
| MissingSerialVersionUID | Missing serial version UID | MAJOR | MAJOR | DEPRECATED | Active |  |
| MissingStaticMethodInNonInstantiatableClass | Missing static method in non instantiatable class | MAJOR | MAJOR | Active | Active |  |
| MoreThanOneLogger | More than one logger | MAJOR | CRITICAL | DEPRECATED | Active |  |
| NPathComplexity | NPath complexity | MAJOR | MAJOR | Active | Active |  |
| NoPackage | No package | MAJOR | MAJOR | DEPRECATED | Active |  |
| NonStaticInitializer | Non static initializer | MAJOR | MAJOR | DEPRECATED | Active |  |
| NonThreadSafeSingleton | Non thread safe singleton | MAJOR | MAJOR | DEPRECATED | Active |  |
| NullAssignment | Null assignment | MAJOR | MAJOR | Active | Active |  |
| OneDeclarationPerLine | One declaration per line | MAJOR | MINOR | DEPRECATED | Active |  |
| OnlyOneReturn | Only one return | MINOR | MAJOR | DEPRECATED | Active |  |
| OptimizableToArrayCall | Optimizable to array call | MAJOR | MAJOR | Active | Active |  |
| OverrideBothEqualsAndHashcode | Override both equals and hashcode | BLOCKER | MAJOR | DEPRECATED | Active |  |
| PackageCase | Package case | MAJOR | MAJOR | DEPRECATED | Active |  |
| PrematureDeclaration | Premature declaration | MAJOR | MAJOR | DEPRECATED | Active |  |
| PreserveStackTrace | Preserve stack trace | MAJOR | MAJOR | DEPRECATED | Active |  |
| ProperCloneImplementation | Proper clone implementation | CRITICAL | CRITICAL | DEPRECATED | Active |  |
| ProperLogger | Proper logger | MAJOR | MAJOR | DEPRECATED | Active |  |
| RedundantFieldInitializer | Redundant field initializer | MAJOR | MAJOR | Active | Active |  |
| RemoteInterfaceNamingConvention | Remote interface naming convention | MAJOR | MINOR | Active | Active |  |
| RemoteSessionInterfaceNamingConvention | Remote session interface naming convention | MAJOR | MINOR | Active | Active |  |
| ReplaceEnumerationWithIterator | Replace enumeration with iterator | MAJOR | MAJOR | DEPRECATED | Active |  |
| ReplaceHashtableWithMap | Replace hashtable with map | MAJOR | MAJOR | DEPRECATED | Active |  |
| ReplaceVectorWithList | Replace vector with list | MAJOR | MAJOR | DEPRECATED | Active |  |
| ReturnFromFinallyBlock | Return from finally block | MAJOR | MAJOR | DEPRECATED | Active |  |
| ShortClassName | Short class name | MINOR | MINOR | DEPRECATED | Active |  |
| ShortMethodName | Short method name | MAJOR | MAJOR | DEPRECATED | Active |  |
| ShortVariable | Short variable | MAJOR | MAJOR | DEPRECATED | Active |  |
| SignatureDeclareThrowsException | Signature declare throws exception | MAJOR | MAJOR | DEPRECATED | Active |  |
| SimpleDateFormatNeedsLocale | Simple date format needs locale | MAJOR | MAJOR | Active | Active |  |
| SimplifiedTernary | Simplified ternary | MAJOR | MAJOR | Active | Active |  |
| SimplifyBooleanExpressions | Simplify boolean expressions | MAJOR | MAJOR | DEPRECATED | Active |  |
| SimplifyBooleanReturns | Simplify boolean returns | MINOR | MAJOR | DEPRECATED | Active |  |
| SimplifyConditional | Simplify conditional | MAJOR | MAJOR | Active | Active |  |
| SingleMethodSingleton | Single method singleton | CRITICAL | CRITICAL | Active | Active |  |
| SingletonClassReturningNewInstance | Singleton class returning new instance | MAJOR | CRITICAL | Active | Active |  |
| SingularField | Singular field | MINOR | MAJOR | Active | Active |  |
| StaticEJBFieldShouldBeFinal | Static EJBField should be final | MAJOR | MAJOR | Active | Active |  |
| StringBufferInstantiationWithChar | String buffer instantiation with char | MAJOR | MINOR | DEPRECATED | Active |  |
| StringInstantiation | String instantiation | MAJOR | CRITICAL | Active | Active |  |
| StringToString | String to string | MAJOR | MAJOR | DEPRECATED | Active |  |
| SuspiciousEqualsMethodName | Suspicious equals method name | CRITICAL | CRITICAL | DEPRECATED | Active |  |
| SuspiciousHashcodeMethodName | Suspicious hashcode method name | MAJOR | MAJOR | DEPRECATED | Active |  |
| SuspiciousOctalEscape | Suspicious octal escape | MAJOR | MAJOR | Active | Active |  |
| SwitchDensity | Switch density | MAJOR | MAJOR | DEPRECATED | Active |  |
| SystemPrintln | System println | MAJOR | CRITICAL | DEPRECATED | Active |  |
| TooManyFields | Too many fields | MAJOR | MAJOR | Active | Active |  |
| TooManyMethods | Too many methods | MAJOR | MAJOR | DEPRECATED | Active |  |
| TooManyStaticImports | Too many static imports | MAJOR | MAJOR | Active | Active |  |
| UncommentedEmptyConstructor | Uncommented empty constructor | MAJOR | MAJOR | DEPRECATED | Active |  |
| UncommentedEmptyMethodBody | Uncommented empty method body | MAJOR | MAJOR | DEPRECATED | Active |  |
| UnconditionalIfStatement | Unconditional if statement | CRITICAL | MAJOR | DEPRECATED | Active |  |
| UnnecessaryCaseChange | Unnecessary case change | MINOR | MAJOR | DEPRECATED | Active |  |
| UnnecessaryConstructor | Unnecessary constructor | MAJOR | MAJOR | DEPRECATED | Active |  |
| UnnecessaryConversionTemporary | Unnecessary conversion temporary | MAJOR | MAJOR | DEPRECATED | Active |  |
| UnnecessaryFullyQualifiedName | Unnecessary fully qualified name | MAJOR | MINOR | Active | Active |  |
| UnnecessaryLocalBeforeReturn | Unnecessary local before return | MAJOR | MAJOR | DEPRECATED | Active |  |
| UnnecessaryReturn | Unnecessary return | MINOR | MAJOR | Active | Active |  |
| UnusedAssignment | Unused assignment | MAJOR | MAJOR | Active | Active |  |
| UnusedFormalParameter | Unused formal parameter | MAJOR | MAJOR | DEPRECATED | Active |  |
| UnusedLocalVariable | Unused local variable | MAJOR | MAJOR | DEPRECATED | Active |  |
| UnusedNullCheckInEquals | Unused null check in equals | MAJOR | MAJOR | Active | Active |  |
| UnusedPrivateField | Unused private field | MAJOR | MAJOR | DEPRECATED | Active |  |
| UnusedPrivateMethod | Unused private method | MAJOR | MAJOR | DEPRECATED | Active |  |
| UseArrayListInsteadOfVector | Use array list instead of vector | MAJOR | MAJOR | DEPRECATED | Active |  |
| UseArraysAsList | Use arrays as list | MAJOR | MAJOR | Active | Active |  |
| UseCollectionIsEmpty | Use collection is empty | MINOR | MAJOR | DEPRECATED | Active |  |
| UseConcurrentHashMap | Use concurrent hash map | MAJOR | MAJOR | Active | Active |  |
| UseCorrectExceptionLogging | Use correct exception logging | MAJOR | MAJOR | DEPRECATED | Active |  |
| UseEqualsToCompareStrings | Use equals to compare strings | MAJOR | MAJOR | DEPRECATED | Active |  |
| UseIndexOfChar | Use index of char | MAJOR | MAJOR | Active | Active |  |
| UseLocaleWithCaseConversions | Use locale with case conversions | MAJOR | MAJOR | Active | Active |  |
| UseNotifyAllInsteadOfNotify | Use notify all instead of notify | MAJOR | MAJOR | DEPRECATED | Active |  |
| UseObjectForClearerAPI | Use object for clearer API | MINOR | MAJOR | DEPRECATED | Active |  |
| UseProperClassLoader | Use proper class loader | CRITICAL | MAJOR | Active | Active |  |
| UseStringBufferForStringAppends | Use string buffer for string appends | MAJOR | MAJOR | Active | Active |  |
| UseStringBufferLength | Use string buffer length | MINOR | MAJOR | Active | Active |  |
| UseUtilityClass | Use utility class | MAJOR | MAJOR | DEPRECATED | Active |  |
| UseVarargs | Use varargs | MAJOR | MINOR | Active | Active |  |
| UselessOperationOnImmutable | Useless operation on immutable | CRITICAL | MAJOR | Active | Active |  |
| UselessOverridingMethod | Useless overriding method | MAJOR | MAJOR | DEPRECATED | Active |  |
| UselessParentheses | Useless parentheses | INFO | MINOR | DEPRECATED | Active |  |
| UselessQualifiedThis | Useless qualified this | MAJOR | MAJOR | Active | Active |  |
| UselessStringValueOf | Useless string value of | MINOR | MAJOR | DEPRECATED | Active |  |

## Renamed Rules
The following rules have new names:

| Rule name | New rule name | Category |
|-----------|---------------|----------|
| DefaultLabelNotLastInSwitchStmt | DefaultLabelNotLastInSwitch | bestpractices |
| JUnit4TestShouldUseAfterAnnotation | UnitTestShouldUseAfterAnnotation | bestpractices |
| JUnit4TestShouldUseBeforeAnnotation | UnitTestShouldUseBeforeAnnotation | bestpractices |
| JUnit4TestShouldUseTestAnnotation | UnitTestShouldUseTestAnnotation | bestpractices |
| JUnitAssertionsShouldIncludeMessage | UnitTestAssertionsShouldIncludeMessage | bestpractices |
| JUnitTestContainsTooManyAsserts | UnitTestContainsTooManyAsserts | bestpractices |
| JUnitTestsShouldIncludeAssert | UnitTestShouldIncludeAssert | bestpractices |
| NonCaseLabelInSwitchStatement | NonCaseLabelInSwitch | errorprone |
| SwitchStmtsShouldHaveDefault | NonExhaustiveSwitch | bestpractices |
| TooFewBranchesForASwitchStatement | TooFewBranchesForSwitch | performance |

Report generated on Fri Jul 11 13:04:40 CEST 2025
