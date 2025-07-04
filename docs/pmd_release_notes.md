# PMD Rules Release Notes for version 4.1.0
_Do not edit this generated file._

## Summary
- Total rules in old version: 206
- Total rules in new version: 291
- Rules removed: 6
- Rules added: 91
- Rules unchanged: 200

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
| DefaultLabelNotLastInSwitchStmt | Default label not last in switch stmt | MAJOR | DEPRECATED |
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
| JUnit4SuitesShouldUseSuiteAnnotation | JUnit4suites should use suite annotation | MAJOR | Active |
| JUnit4TestShouldUseAfterAnnotation | JUnit4test should use after annotation | MAJOR | DEPRECATED |
| JUnit4TestShouldUseBeforeAnnotation | JUnit4test should use before annotation | MAJOR | DEPRECATED |
| JUnit4TestShouldUseTestAnnotation | JUnit4test should use test annotation | MAJOR | DEPRECATED |
| JUnit5TestShouldBePackagePrivate | JUnit5test should be package private | MAJOR | Active |
| JUnitAssertionsShouldIncludeMessage | JUnit assertions should include message | MAJOR | DEPRECATED |
| JUnitSpelling | JUnit spelling | MAJOR | Active |
| JUnitStaticSuite | JUnit static suite | MAJOR | Active |
| JUnitTestContainsTooManyAsserts | JUnit test contains too many asserts | MAJOR | DEPRECATED |
| JUnitTestsShouldIncludeAssert | JUnit tests should include assert | MAJOR | DEPRECATED |
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
| NonCaseLabelInSwitchStatement | Non case label in switch statement | MAJOR | DEPRECATED |
| NonExhaustiveSwitch | Non exhaustive switch | MAJOR | Active |
| NonSerializableClass | Non serializable class | MAJOR | Active |
| PrimitiveWrapperInstantiation | Primitive wrapper instantiation | MAJOR | Active |
| ReturnEmptyCollectionRatherThanNull | Return empty collection rather than null | BLOCKER | Active |
| SimplifiableTestAssertion | Simplifiable test assertion | MAJOR | Active |
| SwitchStmtsShouldHaveDefault | Switch stmts should have default | MAJOR | DEPRECATED |
| TestClassWithoutTestCases | Test class without test cases | MAJOR | Active |
| TooFewBranchesForASwitchStatement | Too few branches for ASwitch statement | MAJOR | DEPRECATED |
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
| AbstractClassWithoutAbstractMethod | Abstract class without abstract method | MAJOR | MAJOR | DEPRECATED | Active | [S1694](https://rules.sonarsource.com/java/RSPEC-1694) |
| AbstractClassWithoutAnyMethod | Abstract class without any method | MAJOR | BLOCKER | DEPRECATED | Active | [S1694](https://rules.sonarsource.com/java/RSPEC-1694) |
| AccessorClassGeneration | Accessor class generation | MAJOR | MAJOR | Active | Active |  |
| AddEmptyString | Add empty string | MAJOR | MAJOR | Active | Active |  |
| AppendCharacterWithChar | Append character with char | MINOR | MAJOR | Active | Active |  |
| ArrayIsStoredDirectly | Array is stored directly | CRITICAL | MAJOR | DEPRECATED | Active | [S2384](https://rules.sonarsource.com/java/RSPEC-2384) |
| AssignmentInOperand | Assignment in operand | MAJOR | MAJOR | DEPRECATED | Active | `java:AssignmentInSubExpressionCheck` |
| AssignmentToNonFinalStatic | Assignment to non final static | MAJOR | MAJOR | Active | Active |  |
| AtLeastOneConstructor | At least one constructor | MAJOR | MAJOR | DEPRECATED | Active | [S1118](https://rules.sonarsource.com/java/RSPEC-1118), [S1258](https://rules.sonarsource.com/java/RSPEC-1258) |
| AvoidAccessibilityAlteration | Avoid accessibility alteration | MAJOR | MAJOR | Active | Active |  |
| AvoidArrayLoops | Avoid array loops | MAJOR | MAJOR | Active | Active |  |
| AvoidAssertAsIdentifier | Avoid assert as identifier | MAJOR | CRITICAL | DEPRECATED | Active | [S1190](https://rules.sonarsource.com/java/RSPEC-1190) |
| AvoidBranchingStatementAsLastInLoop | Avoid branching statement as last in loop | MAJOR | CRITICAL | Active | Active |  |
| AvoidCallingFinalize | Avoid calling finalize | MAJOR | MAJOR | DEPRECATED | Active | `java:ObjectFinalizeCheck` |
| AvoidCatchingGenericException | Avoid catching generic exception | MAJOR | MAJOR | DEPRECATED | Active | [S2221](https://rules.sonarsource.com/java/RSPEC-2221) |
| AvoidCatchingNPE | Avoid catching NPE | MAJOR | MAJOR | DEPRECATED | Active | [S1696](https://rules.sonarsource.com/java/RSPEC-1696) |
| AvoidCatchingThrowable | Avoid catching throwable | CRITICAL | MAJOR | DEPRECATED | Active | [S1181](https://rules.sonarsource.com/java/RSPEC-1181) |
| AvoidDecimalLiteralsInBigDecimalConstructor | Avoid decimal literals in big decimal constructor | MAJOR | MAJOR | DEPRECATED | Active | [S2111](https://rules.sonarsource.com/java/RSPEC-2111) |
| AvoidDeeplyNestedIfStmts | Avoid deeply nested if stmts | MAJOR | MAJOR | DEPRECATED | Active | [S134](https://rules.sonarsource.com/java/RSPEC-134) |
| AvoidDollarSigns | Avoid dollar signs | MINOR | MAJOR | DEPRECATED | Active | [S114](https://rules.sonarsource.com/java/RSPEC-114), [S115](https://rules.sonarsource.com/java/RSPEC-115), [S116](https://rules.sonarsource.com/java/RSPEC-116), [S117](https://rules.sonarsource.com/java/RSPEC-117) |
| AvoidDuplicateLiterals | Avoid duplicate literals | MAJOR | MAJOR | DEPRECATED | Active | [S1192](https://rules.sonarsource.com/java/RSPEC-1192) |
| AvoidEnumAsIdentifier | Avoid enum as identifier | MAJOR | CRITICAL | DEPRECATED | Active | [S1190](https://rules.sonarsource.com/java/RSPEC-1190) |
| AvoidFieldNameMatchingMethodName | Avoid field name matching method name | MAJOR | MAJOR | DEPRECATED | Active | [S1845](https://rules.sonarsource.com/java/RSPEC-1845) |
| AvoidFieldNameMatchingTypeName | Avoid field name matching type name | MAJOR | MAJOR | DEPRECATED | Active | [S1700](https://rules.sonarsource.com/java/RSPEC-1700) |
| AvoidInstanceofChecksInCatchClause | Avoid instanceof checks in catch clause | MINOR | MAJOR | DEPRECATED | Active | [S1193](https://rules.sonarsource.com/java/RSPEC-1193) |
| AvoidInstantiatingObjectsInLoops | Avoid instantiating objects in loops | MINOR | MAJOR | Active | Active |  |
| AvoidLiteralsInIfCondition | Avoid literals in if condition | MAJOR | MAJOR | DEPRECATED | Active | [S109](https://rules.sonarsource.com/java/RSPEC-109) |
| AvoidLosingExceptionInformation | Avoid losing exception information | MAJOR | CRITICAL | DEPRECATED | Active | [S1166](https://rules.sonarsource.com/java/RSPEC-1166) |
| AvoidMultipleUnaryOperators | Avoid multiple unary operators | MAJOR | CRITICAL | DEPRECATED | Active | [S881](https://rules.sonarsource.com/java/RSPEC-881) |
| AvoidPrintStackTrace | Avoid print stack trace | MAJOR | MAJOR | DEPRECATED | Active | [S1148](https://rules.sonarsource.com/java/RSPEC-1148) |
| AvoidProtectedFieldInFinalClass | Avoid protected field in final class | MAJOR | MAJOR | DEPRECATED | Active | [S2156](https://rules.sonarsource.com/java/RSPEC-2156) |
| AvoidProtectedMethodInFinalClassNotExtending | Avoid protected method in final class not extending | MAJOR | MAJOR | DEPRECATED | Active | [S2156](https://rules.sonarsource.com/java/RSPEC-2156) |
| AvoidReassigningParameters | Avoid reassigning parameters | MAJOR | CRITICAL | DEPRECATED | Active | [S1226](https://rules.sonarsource.com/java/RSPEC-1226) |
| AvoidRethrowingException | Avoid rethrowing exception | MAJOR | MAJOR | DEPRECATED | Active | [S1166](https://rules.sonarsource.com/java/RSPEC-1166) |
| AvoidStringBufferField | Avoid string buffer field | MAJOR | MAJOR | DEPRECATED | Active | [S1149](https://rules.sonarsource.com/java/RSPEC-1149) |
| AvoidSynchronizedAtMethodLevel | Avoid synchronized at method level | MAJOR | MAJOR | Active | Active |  |
| AvoidThreadGroup | Avoid thread group | CRITICAL | MAJOR | Active | Active |  |
| AvoidThrowingNewInstanceOfSameException | Avoid throwing new instance of same exception | MAJOR | MAJOR | DEPRECATED | Active | [S1166](https://rules.sonarsource.com/java/RSPEC-1166) |
| AvoidThrowingNullPointerException | Avoid throwing null pointer exception | MAJOR | BLOCKER | DEPRECATED | Active | [S1695](https://rules.sonarsource.com/java/RSPEC-1695) |
| AvoidThrowingRawExceptionTypes | Avoid throwing raw exception types | MAJOR | BLOCKER | DEPRECATED | Active | [S112](https://rules.sonarsource.com/java/RSPEC-112) |
| AvoidUsingHardCodedIP | Avoid using hard coded IP | MAJOR | MAJOR | DEPRECATED | Active | [S1313](https://rules.sonarsource.com/java/RSPEC-1313) |
| AvoidUsingNativeCode | Avoid using native code | MAJOR | CRITICAL | Active | Active |  |
| AvoidUsingOctalValues | Avoid using octal values | MAJOR | MAJOR | DEPRECATED | Active | [S1314](https://rules.sonarsource.com/java/RSPEC-1314) |
| AvoidUsingVolatile | Avoid using volatile | MAJOR | CRITICAL | Active | Active |  |
| BigIntegerInstantiation | Big integer instantiation | MAJOR | MAJOR | Active | Active |  |
| BooleanGetMethodName | Boolean get method name | MAJOR | MINOR | Active | Active |  |
| BrokenNullCheck | Broken null check | CRITICAL | CRITICAL | DEPRECATED | Active | [S1697](https://rules.sonarsource.com/java/RSPEC-1697) |
| CallSuperFirst | Call super first | MAJOR | MAJOR | Active | Active |  |
| CallSuperInConstructor | Call super in constructor | MINOR | MAJOR | Active | Active |  |
| CallSuperLast | Call super last | MAJOR | MAJOR | Active | Active |  |
| CheckResultSet | Check result set | MAJOR | MAJOR | Active | Active |  |
| CheckSkipResult | Check skip result | MINOR | MAJOR | DEPRECATED | Active | [S2674](https://rules.sonarsource.com/java/RSPEC-2674) |
| ClassCastExceptionWithToArray | Class cast exception with to array | MAJOR | MAJOR | Active | Active |  |
| ClassNamingConventions | Class naming conventions | MAJOR | BLOCKER | DEPRECATED | Active | [S101](https://rules.sonarsource.com/java/RSPEC-101), [S114](https://rules.sonarsource.com/java/RSPEC-114) |
| ClassWithOnlyPrivateConstructorsShouldBeFinal | Class with only private constructors should be final | MAJOR | BLOCKER | DEPRECATED | Active | [S2974](https://rules.sonarsource.com/java/RSPEC-2974) |
| CloneMethodMustBePublic | Clone method must be public | MAJOR | MAJOR | Active | Active |  |
| CloneMethodReturnTypeMustMatchClassName | Clone method return type must match class name | MAJOR | MAJOR | Active | Active |  |
| CloseResource | Close resource | CRITICAL | MAJOR | DEPRECATED | Active | [S2095](https://rules.sonarsource.com/java/RSPEC-2095) |
| CollapsibleIfStatements | Collapsible if statements | MINOR | MAJOR | DEPRECATED | Active | [S1066](https://rules.sonarsource.com/java/RSPEC-1066) |
| CommentContent | Comment content | MINOR | MAJOR | Active | Active |  |
| CommentDefaultAccessModifier | Comment default access modifier | MAJOR | MAJOR | Active | Active |  |
| CommentRequired | Comment required | MINOR | MAJOR | Active | Active |  |
| CommentSize | Comment size | MINOR | MAJOR | Active | Active |  |
| CompareObjectsWithEquals | Compare objects with equals | MAJOR | MAJOR | DEPRECATED | Active | [S1698](https://rules.sonarsource.com/java/RSPEC-1698) |
| ConfusingTernary | Confusing ternary | MAJOR | MAJOR | Active | Active |  |
| ConsecutiveAppendsShouldReuse | Consecutive appends should reuse | MAJOR | MAJOR | Active | Active |  |
| ConsecutiveLiteralAppends | Consecutive literal appends | MINOR | MAJOR | Active | Active |  |
| ConstructorCallsOverridableMethod | Constructor calls overridable method | MAJOR | BLOCKER | DEPRECATED | Active | [S1699](https://rules.sonarsource.com/java/RSPEC-1699) |
| CouplingBetweenObjects | Coupling between objects | MAJOR | MAJOR | DEPRECATED | Active | [S1200](https://rules.sonarsource.com/java/RSPEC-1200) |
| CyclomaticComplexity | Cyclomatic complexity | MAJOR | MAJOR | DEPRECATED | Active | `java:MethodCyclomaticComplexity`, `java:ClassCyclomaticComplexity` |
| DoNotCallGarbageCollectionExplicitly | Do not call garbage collection explicitly | CRITICAL | CRITICAL | DEPRECATED | Active | [S1215](https://rules.sonarsource.com/java/RSPEC-1215) |
| DoNotExtendJavaLangError | Do not extend java lang error | MAJOR | MAJOR | DEPRECATED | Active | [S1194](https://rules.sonarsource.com/java/RSPEC-1194) |
| DoNotHardCodeSDCard | Do not hard code SDCard | MAJOR | MAJOR | Active | Active |  |
| DoNotThrowExceptionInFinally | Do not throw exception in finally | MAJOR | MINOR | DEPRECATED | Active | [S1163](https://rules.sonarsource.com/java/RSPEC-1163) |
| DoNotUseThreads | Do not use threads | MAJOR | MAJOR | Active | Active |  |
| DontCallThreadRun | Dont call thread run | MAJOR | MINOR | DEPRECATED | Active | [S1217](https://rules.sonarsource.com/java/RSPEC-1217) |
| DontImportSun | Dont import sun | MINOR | MINOR | DEPRECATED | Active | [S1191](https://rules.sonarsource.com/java/RSPEC-1191) |
| DontUseFloatTypeForLoopIndices | Dont use float type for loop indices | MAJOR | MAJOR | Active | Active |  |
| DoubleCheckedLocking | Double checked locking | MAJOR | BLOCKER | Active | Active |  |
| EmptyCatchBlock | Empty catch block | CRITICAL | MAJOR | DEPRECATED | Active | [S108](https://rules.sonarsource.com/java/RSPEC-108) |
| EmptyFinalizer | Empty finalizer | MAJOR | MAJOR | DEPRECATED | Active | [S1186](https://rules.sonarsource.com/java/RSPEC-1186) |
| EmptyMethodInAbstractClassShouldBeAbstract | Empty method in abstract class should be abstract | MAJOR | BLOCKER | Active | Active |  |
| EqualsNull | Equals null | CRITICAL | BLOCKER | DEPRECATED | Active | [S2159](https://rules.sonarsource.com/java/RSPEC-2159) |
| ExceptionAsFlowControl | Exception as flow control | MAJOR | MAJOR | DEPRECATED | Active | [S1141](https://rules.sonarsource.com/java/RSPEC-1141) |
| ExcessiveImports | Excessive imports | MAJOR | MAJOR | DEPRECATED | Active | [S1200](https://rules.sonarsource.com/java/RSPEC-1200) |
| ExcessiveParameterList | Excessive parameter list | MAJOR | MAJOR | DEPRECATED | Active | [S107](https://rules.sonarsource.com/java/RSPEC-107) |
| ExcessivePublicCount | Excessive public count | MAJOR | MAJOR | DEPRECATED | Active | [S1448](https://rules.sonarsource.com/java/RSPEC-1448) |
| ExtendsObject | Extends object | MINOR | MINOR | DEPRECATED | Active | [S1939](https://rules.sonarsource.com/java/RSPEC-1939) |
| FieldDeclarationsShouldBeAtStartOfClass | Field declarations should be at start of class | MINOR | MAJOR | DEPRECATED | Active | [S1213](https://rules.sonarsource.com/java/RSPEC-1213) |
| FinalFieldCouldBeStatic | Final field could be static | MINOR | MAJOR | DEPRECATED | Active | [S1170](https://rules.sonarsource.com/java/RSPEC-1170) |
| FinalizeDoesNotCallSuperFinalize | Finalize does not call super finalize | MAJOR | MAJOR | DEPRECATED | Active | `java:ObjectFinalizeOverridenCallsSuperFinalizeCheck` |
| FinalizeOnlyCallsSuperFinalize | Finalize only calls super finalize | MAJOR | MAJOR | DEPRECATED | Active | [S1185](https://rules.sonarsource.com/java/RSPEC-1185) |
| FinalizeOverloaded | Finalize overloaded | MAJOR | MAJOR | DEPRECATED | Active | [S1175](https://rules.sonarsource.com/java/RSPEC-1175) |
| FinalizeShouldBeProtected | Finalize should be protected | MAJOR | MAJOR | DEPRECATED | Active | [S1174](https://rules.sonarsource.com/java/RSPEC-1174) |
| ForLoopShouldBeWhileLoop | For loop should be while loop | MINOR | MAJOR | DEPRECATED | Active | [S1264](https://rules.sonarsource.com/java/RSPEC-1264) |
| GenericsNaming | Generics naming | MAJOR | MINOR | DEPRECATED | Active | [S119](https://rules.sonarsource.com/java/RSPEC-119) |
| GodClass | God class | MAJOR | MAJOR | Active | Active |  |
| IdempotentOperations | Idempotent operations | MAJOR | MAJOR | DEPRECATED | Active | [S1656](https://rules.sonarsource.com/java/RSPEC-1656) |
| ImmutableField | Immutable field | MAJOR | MAJOR | Active | Active |  |
| InefficientEmptyStringCheck | Inefficient empty string check | MAJOR | MAJOR | Active | Active |  |
| InefficientStringBuffering | Inefficient string buffering | MAJOR | MAJOR | Active | Active |  |
| InstantiationToGetClass | Instantiation to get class | MAJOR | MINOR | DEPRECATED | Active | [S2133](https://rules.sonarsource.com/java/RSPEC-2133) |
| InsufficientStringBufferDeclaration | Insufficient string buffer declaration | MAJOR | MAJOR | Active | Active |  |
| JumbledIncrementer | Jumbled incrementer | MAJOR | MAJOR | DEPRECATED | Active | `java:ForLoopCounterChangedCheck` |
| LawOfDemeter | Law of demeter | MAJOR | MAJOR | Active | Active |  |
| LocalHomeNamingConvention | Local home naming convention | MAJOR | MINOR | Active | Active |  |
| LocalInterfaceSessionNamingConvention | Local interface session naming convention | MAJOR | MINOR | Active | Active |  |
| LocalVariableCouldBeFinal | Local variable could be final | MINOR | MAJOR | Active | Active |  |
| LogicInversion | Logic inversion | MINOR | MAJOR | DEPRECATED | Active | [S1940](https://rules.sonarsource.com/java/RSPEC-1940) |
| LongVariable | Long variable | MAJOR | MAJOR | DEPRECATED | Active | [S117](https://rules.sonarsource.com/java/RSPEC-117) |
| LoosePackageCoupling | Loose package coupling | MAJOR | MAJOR | DEPRECATED | Active | `java:ArchitecturalConstraint` |
| MDBAndSessionBeanNamingConvention | MDBAnd session bean naming convention | MAJOR | MINOR | Active | Active |  |
| MethodArgumentCouldBeFinal | Method argument could be final | MINOR | MAJOR | DEPRECATED | Active | [S1226](https://rules.sonarsource.com/java/RSPEC-1226) |
| MethodNamingConventions | Method naming conventions | MAJOR | BLOCKER | DEPRECATED | Active | [S100](https://rules.sonarsource.com/java/RSPEC-100) |
| MethodReturnsInternalArray | Method returns internal array | CRITICAL | MAJOR | DEPRECATED | Active | [S2384](https://rules.sonarsource.com/java/RSPEC-2384) |
| MethodWithSameNameAsEnclosingClass | Method with same name as enclosing class | MAJOR | MAJOR | DEPRECATED | Active | [S1223](https://rules.sonarsource.com/java/RSPEC-1223) |
| MisplacedNullCheck | Misplaced null check | CRITICAL | MAJOR | DEPRECATED | Active | [S1697](https://rules.sonarsource.com/java/RSPEC-1697), [S2259](https://rules.sonarsource.com/java/RSPEC-2259) |
| MissingSerialVersionUID | Missing serial version UID | MAJOR | MAJOR | DEPRECATED | Active | [S2057](https://rules.sonarsource.com/java/RSPEC-2057) |
| MissingStaticMethodInNonInstantiatableClass | Missing static method in non instantiatable class | MAJOR | MAJOR | Active | Active |  |
| MoreThanOneLogger | More than one logger | MAJOR | CRITICAL | DEPRECATED | Active | [S1312](https://rules.sonarsource.com/java/RSPEC-1312) |
| NPathComplexity | NPath complexity | MAJOR | MAJOR | Active | Active |  |
| NoPackage | No package | MAJOR | MAJOR | DEPRECATED | Active | [S1220](https://rules.sonarsource.com/java/RSPEC-1220) |
| NonStaticInitializer | Non static initializer | MAJOR | MAJOR | DEPRECATED | Active | [S1171](https://rules.sonarsource.com/java/RSPEC-1171) |
| NonThreadSafeSingleton | Non thread safe singleton | MAJOR | MAJOR | DEPRECATED | Active | [S2444](https://rules.sonarsource.com/java/RSPEC-2444) |
| NullAssignment | Null assignment | MAJOR | MAJOR | Active | Active |  |
| OneDeclarationPerLine | One declaration per line | MAJOR | MINOR | DEPRECATED | Active | [S122](https://rules.sonarsource.com/java/RSPEC-122) |
| OnlyOneReturn | Only one return | MINOR | MAJOR | DEPRECATED | Active | [S1142](https://rules.sonarsource.com/java/RSPEC-1142) |
| OptimizableToArrayCall | Optimizable to array call | MAJOR | MAJOR | Active | Active |  |
| OverrideBothEqualsAndHashcode | Override both equals and hashcode | BLOCKER | MAJOR | DEPRECATED | Active | [S1206](https://rules.sonarsource.com/java/RSPEC-1206) |
| PackageCase | Package case | MAJOR | MAJOR | DEPRECATED | Active | [S120](https://rules.sonarsource.com/java/RSPEC-120) |
| PrematureDeclaration | Premature declaration | MAJOR | MAJOR | DEPRECATED | Active | [S1941](https://rules.sonarsource.com/java/RSPEC-1941) |
| PreserveStackTrace | Preserve stack trace | MAJOR | MAJOR | DEPRECATED | Active | [S1166](https://rules.sonarsource.com/java/RSPEC-1166) |
| ProperCloneImplementation | Proper clone implementation | CRITICAL | CRITICAL | DEPRECATED | Active | [S1182](https://rules.sonarsource.com/java/RSPEC-1182) |
| ProperLogger | Proper logger | MAJOR | MAJOR | DEPRECATED | Active | [S1312](https://rules.sonarsource.com/java/RSPEC-1312) |
| RedundantFieldInitializer | Redundant field initializer | MAJOR | MAJOR | Active | Active |  |
| RemoteInterfaceNamingConvention | Remote interface naming convention | MAJOR | MINOR | Active | Active |  |
| RemoteSessionInterfaceNamingConvention | Remote session interface naming convention | MAJOR | MINOR | Active | Active |  |
| ReplaceEnumerationWithIterator | Replace enumeration with iterator | MAJOR | MAJOR | DEPRECATED | Active | [S1150](https://rules.sonarsource.com/java/RSPEC-1150) |
| ReplaceHashtableWithMap | Replace hashtable with map | MAJOR | MAJOR | DEPRECATED | Active | [S1149](https://rules.sonarsource.com/java/RSPEC-1149) |
| ReplaceVectorWithList | Replace vector with list | MAJOR | MAJOR | DEPRECATED | Active | [S1149](https://rules.sonarsource.com/java/RSPEC-1149) |
| ReturnFromFinallyBlock | Return from finally block | MAJOR | MAJOR | DEPRECATED | Active | [S1143](https://rules.sonarsource.com/java/RSPEC-1143) |
| ShortClassName | Short class name | MINOR | MINOR | DEPRECATED | Active | [S101](https://rules.sonarsource.com/java/RSPEC-101) |
| ShortMethodName | Short method name | MAJOR | MAJOR | DEPRECATED | Active | [S100](https://rules.sonarsource.com/java/RSPEC-100) |
| ShortVariable | Short variable | MAJOR | MAJOR | DEPRECATED | Active | [S117](https://rules.sonarsource.com/java/RSPEC-117) |
| SignatureDeclareThrowsException | Signature declare throws exception | MAJOR | MAJOR | DEPRECATED | Active | [S112](https://rules.sonarsource.com/java/RSPEC-112) |
| SimpleDateFormatNeedsLocale | Simple date format needs locale | MAJOR | MAJOR | Active | Active |  |
| SimplifiedTernary | Simplified ternary | MAJOR | MAJOR | Active | Active |  |
| SimplifyBooleanExpressions | Simplify boolean expressions | MAJOR | MAJOR | DEPRECATED | Active | [S1125](https://rules.sonarsource.com/java/RSPEC-1125) |
| SimplifyBooleanReturns | Simplify boolean returns | MINOR | MAJOR | DEPRECATED | Active | [S1126](https://rules.sonarsource.com/java/RSPEC-1126) |
| SimplifyConditional | Simplify conditional | MAJOR | MAJOR | Active | Active |  |
| SingleMethodSingleton | Single method singleton | CRITICAL | CRITICAL | Active | Active |  |
| SingletonClassReturningNewInstance | Singleton class returning new instance | MAJOR | CRITICAL | Active | Active |  |
| SingularField | Singular field | MINOR | MAJOR | Active | Active |  |
| StaticEJBFieldShouldBeFinal | Static EJBField should be final | MAJOR | MAJOR | Active | Active |  |
| StringBufferInstantiationWithChar | String buffer instantiation with char | MAJOR | MINOR | DEPRECATED | Active | [S1317](https://rules.sonarsource.com/java/RSPEC-1317) |
| StringInstantiation | String instantiation | MAJOR | CRITICAL | Active | Active |  |
| StringToString | String to string | MAJOR | MAJOR | DEPRECATED | Active | [S1858](https://rules.sonarsource.com/java/RSPEC-1858) |
| SuspiciousEqualsMethodName | Suspicious equals method name | CRITICAL | CRITICAL | DEPRECATED | Active | [S1201](https://rules.sonarsource.com/java/RSPEC-1201) |
| SuspiciousHashcodeMethodName | Suspicious hashcode method name | MAJOR | MAJOR | DEPRECATED | Active | [S1221](https://rules.sonarsource.com/java/RSPEC-1221) |
| SuspiciousOctalEscape | Suspicious octal escape | MAJOR | MAJOR | Active | Active |  |
| SwitchDensity | Switch density | MAJOR | MAJOR | DEPRECATED | Active | [S1151](https://rules.sonarsource.com/java/RSPEC-1151) |
| SystemPrintln | System println | MAJOR | CRITICAL | DEPRECATED | Active | [S106](https://rules.sonarsource.com/java/RSPEC-106) |
| TooManyFields | Too many fields | MAJOR | MAJOR | Active | Active |  |
| TooManyMethods | Too many methods | MAJOR | MAJOR | DEPRECATED | Active | [S1448](https://rules.sonarsource.com/java/RSPEC-1448) |
| TooManyStaticImports | Too many static imports | MAJOR | MAJOR | Active | Active |  |
| UncommentedEmptyConstructor | Uncommented empty constructor | MAJOR | MAJOR | DEPRECATED | Active | [S2094](https://rules.sonarsource.com/java/RSPEC-2094) |
| UncommentedEmptyMethodBody | Uncommented empty method body | MAJOR | MAJOR | DEPRECATED | Active | [S1186](https://rules.sonarsource.com/java/RSPEC-1186) |
| UnconditionalIfStatement | Unconditional if statement | CRITICAL | MAJOR | DEPRECATED | Active | [S2583](https://rules.sonarsource.com/java/RSPEC-2583) |
| UnnecessaryCaseChange | Unnecessary case change | MINOR | MAJOR | DEPRECATED | Active | [S1157](https://rules.sonarsource.com/java/RSPEC-1157) |
| UnnecessaryConstructor | Unnecessary constructor | MAJOR | MAJOR | DEPRECATED | Active | [S1186](https://rules.sonarsource.com/java/RSPEC-1186) |
| UnnecessaryConversionTemporary | Unnecessary conversion temporary | MAJOR | MAJOR | DEPRECATED | Active | [S1158](https://rules.sonarsource.com/java/RSPEC-1158) |
| UnnecessaryFullyQualifiedName | Unnecessary fully qualified name | MAJOR | MINOR | Active | Active |  |
| UnnecessaryLocalBeforeReturn | Unnecessary local before return | MAJOR | MAJOR | DEPRECATED | Active | [S1488](https://rules.sonarsource.com/java/RSPEC-1488) |
| UnnecessaryReturn | Unnecessary return | MINOR | MAJOR | Active | Active |  |
| UnusedAssignment | Unused assignment | MAJOR | MAJOR | Active | Active |  |
| UnusedFormalParameter | Unused formal parameter | MAJOR | MAJOR | DEPRECATED | Active | [S1172](https://rules.sonarsource.com/java/RSPEC-1172) |
| UnusedLocalVariable | Unused local variable | MAJOR | MAJOR | DEPRECATED | Active | [S1481](https://rules.sonarsource.com/java/RSPEC-1481) |
| UnusedNullCheckInEquals | Unused null check in equals | MAJOR | MAJOR | Active | Active |  |
| UnusedPrivateField | Unused private field | MAJOR | MAJOR | DEPRECATED | Active | [S1068](https://rules.sonarsource.com/java/RSPEC-1068) |
| UnusedPrivateMethod | Unused private method | MAJOR | MAJOR | DEPRECATED | Active | `java:UnusedPrivateMethod` |
| UseArrayListInsteadOfVector | Use array list instead of vector | MAJOR | MAJOR | DEPRECATED | Active | [S1149](https://rules.sonarsource.com/java/RSPEC-1149) |
| UseArraysAsList | Use arrays as list | MAJOR | MAJOR | Active | Active |  |
| UseCollectionIsEmpty | Use collection is empty | MINOR | MAJOR | DEPRECATED | Active | [S1155](https://rules.sonarsource.com/java/RSPEC-1155) |
| UseConcurrentHashMap | Use concurrent hash map | MAJOR | MAJOR | Active | Active |  |
| UseCorrectExceptionLogging | Use correct exception logging | MAJOR | MAJOR | DEPRECATED | Active | [S1166](https://rules.sonarsource.com/java/RSPEC-1166) |
| UseEqualsToCompareStrings | Use equals to compare strings | MAJOR | MAJOR | DEPRECATED | Active | `java:StringEqualityComparisonCheck`, [S1698](https://rules.sonarsource.com/java/RSPEC-1698) |
| UseIndexOfChar | Use index of char | MAJOR | MAJOR | Active | Active |  |
| UseLocaleWithCaseConversions | Use locale with case conversions | MAJOR | MAJOR | Active | Active |  |
| UseNotifyAllInsteadOfNotify | Use notify all instead of notify | MAJOR | MAJOR | DEPRECATED | Active | [S2446](https://rules.sonarsource.com/java/RSPEC-2446) |
| UseObjectForClearerAPI | Use object for clearer API | MINOR | MAJOR | DEPRECATED | Active | [S107](https://rules.sonarsource.com/java/RSPEC-107) |
| UseProperClassLoader | Use proper class loader | CRITICAL | MAJOR | Active | Active |  |
| UseStringBufferForStringAppends | Use string buffer for string appends | MAJOR | MAJOR | Active | Active |  |
| UseStringBufferLength | Use string buffer length | MINOR | MAJOR | Active | Active |  |
| UseUtilityClass | Use utility class | MAJOR | MAJOR | DEPRECATED | Active | [S1118](https://rules.sonarsource.com/java/RSPEC-1118) |
| UseVarargs | Use varargs | MAJOR | MINOR | Active | Active |  |
| UselessOperationOnImmutable | Useless operation on immutable | CRITICAL | MAJOR | Active | Active |  |
| UselessOverridingMethod | Useless overriding method | MAJOR | MAJOR | DEPRECATED | Active | [S1185](https://rules.sonarsource.com/java/RSPEC-1185) |
| UselessParentheses | Useless parentheses | INFO | MINOR | DEPRECATED | Active | `java:UselessParenthesesCheck` |
| UselessQualifiedThis | Useless qualified this | MAJOR | MAJOR | Active | Active |  |
| UselessStringValueOf | Useless string value of | MINOR | MAJOR | DEPRECATED | Active | [S1153](https://rules.sonarsource.com/java/RSPEC-1153) |

Report generated on Fri Jul 04 16:18:02 CEST 2025
