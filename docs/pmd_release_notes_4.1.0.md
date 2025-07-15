# PMD Rules Release Notes for version 4.1.0
_Do not edit this generated file._

## Summary
- Total rules in old version (4.0.3): 206
- Total rules in new version (4.1.0): 281
- Rules added: 80
- Rules removed: 5
- Rules unchanged: 46
- Rules updated: 154
- Rules renamed: 11

## Added Rules
The following rules have been added in the new version:

| Rule Key | Name | Severity | Category |
|----------|------|----------|----------|
| AccessorMethodGeneration | Accessor method generation | Medium | bestpractices |
| AvoidCalendarDateCreation | Avoid calendar date creation | Medium | performance |
| AvoidFileStream | Avoid file stream | Blocker | performance |
| AvoidMessageDigestField | Avoid message digest field | Medium | bestpractices |
| AvoidReassigningCatchVariables | Avoid reassigning catch variables | Medium | bestpractices |
| AvoidReassigningLoopVariables | Avoid reassigning loop variables | Medium | bestpractices |
| AvoidSynchronizedStatement | Avoid synchronized statement | Medium | multithreading |
| AvoidUncheckedExceptionsInSignatures | Avoid unchecked exceptions in signatures | Medium | design |
| CloneMethodMustImplementCloneable | Clone method must implement cloneable | Medium | errorprone |
| CognitiveComplexity | Cognitive complexity | Medium | design |
| ComparisonWithNaN | Comparison with na n | Medium | errorprone |
| ConfusingArgumentToVarargsMethod | Confusing argument to varargs method | Medium | errorprone |
| ConstantsInInterface | Constants in interface | Medium | bestpractices |
| ControlStatementBraces | Control statement braces | Medium | codestyle |
| DataClass | Data class | Medium | design |
| DefaultLabelNotLastInSwitch | Default label not last in switch | Medium | bestpractices |
| DetachedTestCase | Detached test case | Medium | errorprone |
| DoNotExtendJavaLangThrowable | Do not extend java lang throwable | Medium | errorprone |
| DoNotTerminateVM | Do not terminate VM | Medium | errorprone |
| DoubleBraceInitialization | Double brace initialization | Medium | bestpractices |
| EmptyControlStatement | Empty control statement | Medium | codestyle |
| ExhaustiveSwitchHasDefault | Exhaustive switch has default | Medium | bestpractices |
| FieldNamingConventions | Field naming conventions | Blocker | codestyle |
| FinalParameterInAbstractMethod | Final parameter in abstract method | Blocker | codestyle |
| ForLoopCanBeForeach | For loop can be foreach | Medium | bestpractices |
| ForLoopVariableCount | For loop variable count | Medium | bestpractices |
| FormalParameterNamingConventions | Formal parameter naming conventions | Blocker | codestyle |
| HardCodedCryptoKey | Hard coded crypto key | Medium | security |
| IdenticalCatchBranches | Identical catch branches | Medium | codestyle |
| ImplicitFunctionalInterface | Implicit functional interface | High | bestpractices |
| ImplicitSwitchFallThrough | Implicit switch fall through | Medium | errorprone |
| InsecureCryptoIv | Insecure crypto iv | Medium | security |
| InvalidJavaBean | Invalid java bean | Medium | design |
| InvalidLogMessageFormat | Invalid log message format | Info | errorprone |
| JUnit4SuitesShouldUseSuiteAnnotation | JUnit4 suites should use suite annotation | Medium | bestpractices |
| JUnit5TestShouldBePackagePrivate | JUnit5 test should be package private | Medium | bestpractices |
| JUnitSpelling | JUnit spelling | Medium | errorprone |
| JUnitStaticSuite | JUnit static suite | Medium | errorprone |
| JUnitUseExpected | JUnit use expected | Medium | bestpractices |
| LambdaCanBeMethodReference | Lambda can be method reference | Medium | codestyle |
| LinguisticNaming | Linguistic naming | Medium | codestyle |
| LiteralsFirstInComparisons | Literals first in comparisons | Medium | bestpractices |
| LocalVariableNamingConventions | Local variable naming conventions | Blocker | codestyle |
| LooseCoupling | Loose coupling | Medium | bestpractices |
| MissingOverride | Missing override | Medium | bestpractices |
| MutableStaticState | Mutable static state | Medium | design |
| NcssCount | Ncss count | Medium | design |
| NonCaseLabelInSwitch | Non case label in switch | Medium | errorprone |
| NonExhaustiveSwitch | Non exhaustive switch | Medium | bestpractices |
| NonSerializableClass | Non serializable class | Medium | errorprone |
| PrimitiveWrapperInstantiation | Primitive wrapper instantiation | Medium | bestpractices |
| ReturnEmptyCollectionRatherThanNull | Return empty collection rather than null | Blocker | errorprone |
| SimplifiableTestAssertion | Simplifiable test assertion | Medium | bestpractices |
| TestClassWithoutTestCases | Test class without test cases | Medium | errorprone |
| TooFewBranchesForSwitch | Too few branches for switch | Medium | performance |
| UnitTestAssertionsShouldIncludeMessage | Unit test assertions should include message | Medium | bestpractices |
| UnitTestContainsTooManyAsserts | Unit test contains too many asserts | Medium | bestpractices |
| UnitTestShouldIncludeAssert | Unit test should include assert | Medium | bestpractices |
| UnitTestShouldUseAfterAnnotation | Unit test should use after annotation | Medium | bestpractices |
| UnitTestShouldUseBeforeAnnotation | Unit test should use before annotation | Medium | bestpractices |
| UnitTestShouldUseTestAnnotation | Unit test should use test annotation | Medium | bestpractices |
| UnnecessaryAnnotationValueElement | Unnecessary annotation value element | Medium | codestyle |
| UnnecessaryBooleanAssertion | Unnecessary boolean assertion | Medium | errorprone |
| UnnecessaryBoxing | Unnecessary boxing | Medium | codestyle |
| UnnecessaryCast | Unnecessary cast | Medium | codestyle |
| UnnecessaryImport | Unnecessary import | Low | codestyle |
| UnnecessaryModifier | Unnecessary modifier | Medium | codestyle |
| UnnecessarySemicolon | Unnecessary semicolon | Medium | codestyle |
| UnnecessaryVarargsArrayCreation | Unnecessary varargs array creation | Medium | bestpractices |
| UnnecessaryWarningSuppression | Unnecessary warning suppression | Medium | bestpractices |
| UnsynchronizedStaticFormatter | Unsynchronized static formatter | Medium | multithreading |
| UseDiamondOperator | Use diamond operator | Medium | codestyle |
| UseEnumCollections | Use enum collections | Medium | bestpractices |
| UseExplicitTypes | Use explicit types | Medium | codestyle |
| UseIOStreamsWithApacheCommonsFileItem | Use IOStreams with apache commons file item | Medium | performance |
| UseShortArrayInitializer | Use short array initializer | Medium | codestyle |
| UseStandardCharsets | Use standard charsets | Medium | bestpractices |
| UseTryWithResources | Use try with resources | Medium | bestpractices |
| UseUnderscoresInNumericLiterals | Use underscores in numeric literals | Medium | codestyle |
| WhileLoopWithLiteralBoolean | While loop with literal boolean | Medium | bestpractices |

## Updated Rules
The following rules have been updated in the new version:

| Rule Key | Name | Old Priority | New Severity | Old Status | New Status | Alternatives | Category |
|----------|------|--------------|--------------|------------|------------|--------------|----------|
| AbstractClassWithoutAbstractMethod | Abstract class without abstract method |  | Medium | Deprecated | Active | [java:S1694](https://rules.sonarsource.com/java/RSPEC-1694) | bestpractices |
| AbstractClassWithoutAnyMethod | Abstract class without any method | Medium | Blocker | Deprecated | Active | [java:S1694](https://rules.sonarsource.com/java/RSPEC-1694) | design |
| AppendCharacterWithChar | Append character with char | Low | Medium |  | Active |  | performance |
| ArrayIsStoredDirectly | Array is stored directly | High | Medium | Deprecated | Active | [java:S2384](https://rules.sonarsource.com/java/RSPEC-2384) | bestpractices |
| AssignmentInOperand | Assignment in operand |  | Medium | Deprecated | Active | [java:S1121](https://rules.sonarsource.com/java/RSPEC-1121) | errorprone |
| AtLeastOneConstructor | At least one constructor |  | Medium | Deprecated | Active | [java:S1118](https://rules.sonarsource.com/java/RSPEC-1118), [java:S1258](https://rules.sonarsource.com/java/RSPEC-1258) | codestyle |
| AvoidAssertAsIdentifier | Avoid assert as identifier | Medium | High | Deprecated | Active | [java:S1190](https://rules.sonarsource.com/java/RSPEC-1190) | errorprone |
| AvoidBranchingStatementAsLastInLoop | Avoid branching statement as last in loop | Medium | High |  | Active |  | errorprone |
| AvoidCallingFinalize | Avoid calling finalize |  | Medium | Deprecated | Active |  | errorprone |
| AvoidCatchingGenericException | Avoid catching generic exception |  | Medium | Deprecated | Active | [java:S2221](https://rules.sonarsource.com/java/RSPEC-2221) | design |
| AvoidCatchingNPE | Avoid catching NPE |  | Medium | Deprecated | Active | [java:S1696](https://rules.sonarsource.com/java/RSPEC-1696) | errorprone |
| AvoidCatchingThrowable | Avoid catching throwable | High | Medium | Deprecated | Active | [java:S1181](https://rules.sonarsource.com/java/RSPEC-1181) | errorprone |
| AvoidDecimalLiteralsInBigDecimalConstructor | Avoid decimal literals in big decimal constructor |  | Medium | Deprecated | Active | [java:S2111](https://rules.sonarsource.com/java/RSPEC-2111) | errorprone |
| AvoidDeeplyNestedIfStmts | Avoid deeply nested if stmts |  | Medium | Deprecated | Active | [java:S134](https://rules.sonarsource.com/java/RSPEC-134) | design |
| AvoidDollarSigns | Avoid dollar signs | Low | Medium | Deprecated | Active | [java:S114](https://rules.sonarsource.com/java/RSPEC-114), [java:S115](https://rules.sonarsource.com/java/RSPEC-115), [java:S116](https://rules.sonarsource.com/java/RSPEC-116), [java:S117](https://rules.sonarsource.com/java/RSPEC-117) | codestyle |
| AvoidDuplicateLiterals | Avoid duplicate literals |  | Medium | Deprecated | Active | [java:S1192](https://rules.sonarsource.com/java/RSPEC-1192) | errorprone |
| AvoidEnumAsIdentifier | Avoid enum as identifier | Medium | High | Deprecated | Active | [java:S1190](https://rules.sonarsource.com/java/RSPEC-1190) | errorprone |
| AvoidFieldNameMatchingMethodName | Avoid field name matching method name |  | Medium | Deprecated | Active | [java:S1845](https://rules.sonarsource.com/java/RSPEC-1845) | errorprone |
| AvoidFieldNameMatchingTypeName | Avoid field name matching type name |  | Medium | Deprecated | Active | [java:S1700](https://rules.sonarsource.com/java/RSPEC-1700) | errorprone |
| AvoidInstanceofChecksInCatchClause | Avoid instanceof checks in catch clause | Low | Medium | Deprecated | Active | [java:S1193](https://rules.sonarsource.com/java/RSPEC-1193) | errorprone |
| AvoidInstantiatingObjectsInLoops | Avoid instantiating objects in loops | Low | Medium |  | Active |  | performance |
| AvoidLiteralsInIfCondition | Avoid literals in if condition |  | Medium | Deprecated | Active | [java:S109](https://rules.sonarsource.com/java/RSPEC-109) | errorprone |
| AvoidLosingExceptionInformation | Avoid losing exception information | Medium | High | Deprecated | Active | [java:S1166](https://rules.sonarsource.com/java/RSPEC-1166) | errorprone |
| AvoidMultipleUnaryOperators | Avoid multiple unary operators | Medium | High | Deprecated | Active | [java:S881](https://rules.sonarsource.com/java/RSPEC-881) | errorprone |
| AvoidPrintStackTrace | Avoid print stack trace |  | Medium | Deprecated | Active | [java:S1148](https://rules.sonarsource.com/java/RSPEC-1148) | bestpractices |
| AvoidProtectedFieldInFinalClass | Avoid protected field in final class |  | Medium | Deprecated | Active | [java:S2156](https://rules.sonarsource.com/java/RSPEC-2156) | codestyle |
| AvoidProtectedMethodInFinalClassNotExtending | Avoid protected method in final class not extending |  | Medium | Deprecated | Active | [java:S2156](https://rules.sonarsource.com/java/RSPEC-2156) | codestyle |
| AvoidReassigningParameters | Avoid reassigning parameters | Medium | High | Deprecated | Active | [java:S1226](https://rules.sonarsource.com/java/RSPEC-1226) | bestpractices |
| AvoidRethrowingException | Avoid rethrowing exception |  | Medium | Deprecated | Active | [java:S1166](https://rules.sonarsource.com/java/RSPEC-1166) | design |
| AvoidStringBufferField | Avoid string buffer field |  | Medium | Deprecated | Active | [java:S1149](https://rules.sonarsource.com/java/RSPEC-1149) | bestpractices |
| AvoidThreadGroup | Avoid thread group | High | Medium |  | Active |  | multithreading |
| AvoidThrowingNewInstanceOfSameException | Avoid throwing new instance of same exception |  | Medium | Deprecated | Active | [java:S1166](https://rules.sonarsource.com/java/RSPEC-1166) | design |
| AvoidThrowingNullPointerException | Avoid throwing null pointer exception | Medium | Blocker | Deprecated | Active | [java:S1695](https://rules.sonarsource.com/java/RSPEC-1695) | design |
| AvoidThrowingRawExceptionTypes | Avoid throwing raw exception types | Medium | Blocker | Deprecated | Active | [java:S112](https://rules.sonarsource.com/java/RSPEC-112) | design |
| AvoidUsingHardCodedIP | Avoid using hard coded IP |  | Medium | Deprecated | Active | [java:S1313](https://rules.sonarsource.com/java/RSPEC-1313) | bestpractices |
| AvoidUsingNativeCode | Avoid using native code | Medium | High |  | Active |  | codestyle |
| AvoidUsingOctalValues | Avoid using octal values |  | Medium | Deprecated | Active | [java:S1314](https://rules.sonarsource.com/java/RSPEC-1314) | errorprone |
| AvoidUsingVolatile | Avoid using volatile | Medium | High |  | Active |  | multithreading |
| BooleanGetMethodName | Boolean get method name | Medium | Low |  | Active |  | codestyle |
| BrokenNullCheck | Broken null check |  | High | Deprecated | Active | [java:S1697](https://rules.sonarsource.com/java/RSPEC-1697) | errorprone |
| CallSuperInConstructor | Call super in constructor | Low | Medium |  | Active |  | codestyle |
| CheckSkipResult | Check skip result | Low | Medium | Deprecated | Active | [java:S2674](https://rules.sonarsource.com/java/RSPEC-2674) | errorprone |
| ClassNamingConventions | Class naming conventions | Medium | Blocker | Deprecated | Active | [java:S101](https://rules.sonarsource.com/java/RSPEC-101), [java:S114](https://rules.sonarsource.com/java/RSPEC-114) | codestyle |
| ClassWithOnlyPrivateConstructorsShouldBeFinal | Class with only private constructors should be final | Medium | Blocker | Deprecated | Active | [java:S2974](https://rules.sonarsource.com/java/RSPEC-2974) | design |
| CloseResource | Close resource | High | Medium | Deprecated | Active | [java:S2095](https://rules.sonarsource.com/java/RSPEC-2095) | errorprone |
| CollapsibleIfStatements | Collapsible if statements | Low | Medium | Deprecated | Active | [java:S1066](https://rules.sonarsource.com/java/RSPEC-1066) | design |
| CommentContent | Comment content | Low | Medium |  | Active |  | documentation |
| CommentRequired | Comment required | Low | Medium |  | Active |  | documentation |
| CommentSize | Comment size | Low | Medium |  | Active |  | documentation |
| CompareObjectsWithEquals | Compare objects with equals |  | Medium | Deprecated | Active | [java:S1698](https://rules.sonarsource.com/java/RSPEC-1698) | errorprone |
| ConsecutiveLiteralAppends | Consecutive literal appends | Low | Medium |  | Active |  | performance |
| ConstructorCallsOverridableMethod | Constructor calls overridable method | Medium | Blocker | Deprecated | Active | [java:S1699](https://rules.sonarsource.com/java/RSPEC-1699) | errorprone |
| CouplingBetweenObjects | Coupling between objects |  | Medium | Deprecated | Active | [java:S1200](https://rules.sonarsource.com/java/RSPEC-1200) | design |
| CyclomaticComplexity | Cyclomatic complexity |  | Medium | Deprecated | Active | [java:S1541](https://rules.sonarsource.com/java/RSPEC-1541) | design |
| DoNotCallGarbageCollectionExplicitly | Do not call garbage collection explicitly |  | High | Deprecated | Active | [java:S1215](https://rules.sonarsource.com/java/RSPEC-1215) | errorprone |
| DoNotExtendJavaLangError | Do not extend java lang error |  | Medium | Deprecated | Active | [java:S1194](https://rules.sonarsource.com/java/RSPEC-1194) | design |
| DoNotThrowExceptionInFinally | Do not throw exception in finally | Medium | Low | Deprecated | Active | [java:S1163](https://rules.sonarsource.com/java/RSPEC-1163) | errorprone |
| DontCallThreadRun | Dont call thread run | Medium | Low | Deprecated | Active | [java:S1217](https://rules.sonarsource.com/java/RSPEC-1217) | multithreading |
| DontImportSun | Dont import sun |  | Low | Deprecated | Active | [java:S1191](https://rules.sonarsource.com/java/RSPEC-1191) | errorprone |
| DoubleCheckedLocking | Double checked locking | Medium | Blocker |  | Active |  | multithreading |
| EmptyCatchBlock | Empty catch block | High | Medium | Deprecated | Active | [java:S108](https://rules.sonarsource.com/java/RSPEC-108) | errorprone |
| EmptyFinalizer | Empty finalizer |  | Medium | Deprecated | Active | [java:S1186](https://rules.sonarsource.com/java/RSPEC-1186) | errorprone |
| EmptyMethodInAbstractClassShouldBeAbstract | Empty method in abstract class should be abstract | Medium | Blocker |  | Active |  | codestyle |
| EqualsNull | Equals null | High | Blocker | Deprecated | Active | [java:S2159](https://rules.sonarsource.com/java/RSPEC-2159) | errorprone |
| ExceptionAsFlowControl | Exception as flow control |  | Medium | Deprecated | Active | [java:S1141](https://rules.sonarsource.com/java/RSPEC-1141) | design |
| ExcessiveImports | Excessive imports |  | Medium | Deprecated | Active | [java:S1200](https://rules.sonarsource.com/java/RSPEC-1200) | design |
| ExcessiveParameterList | Excessive parameter list |  | Medium | Deprecated | Active | [java:S107](https://rules.sonarsource.com/java/RSPEC-107) | design |
| ExcessivePublicCount | Excessive public count |  | Medium | Deprecated | Active | [java:S1448](https://rules.sonarsource.com/java/RSPEC-1448) | design |
| ExtendsObject | Extends object |  | Low | Deprecated | Active | [java:S1939](https://rules.sonarsource.com/java/RSPEC-1939) | codestyle |
| FieldDeclarationsShouldBeAtStartOfClass | Field declarations should be at start of class | Low | Medium | Deprecated | Active | [java:S1213](https://rules.sonarsource.com/java/RSPEC-1213) | codestyle |
| FinalFieldCouldBeStatic | Final field could be static | Low | Medium | Deprecated | Active | [java:S1170](https://rules.sonarsource.com/java/RSPEC-1170) | design |
| FinalizeDoesNotCallSuperFinalize | Finalize does not call super finalize |  | Medium | Deprecated | Active |  | errorprone |
| FinalizeOnlyCallsSuperFinalize | Finalize only calls super finalize |  | Medium | Deprecated | Active | [java:S1185](https://rules.sonarsource.com/java/RSPEC-1185) | errorprone |
| FinalizeOverloaded | Finalize overloaded |  | Medium | Deprecated | Active | [java:S1175](https://rules.sonarsource.com/java/RSPEC-1175) | errorprone |
| FinalizeShouldBeProtected | Finalize should be protected |  | Medium | Deprecated | Active | [java:S1174](https://rules.sonarsource.com/java/RSPEC-1174) | errorprone |
| ForLoopShouldBeWhileLoop | For loop should be while loop | Low | Medium | Deprecated | Active | [java:S1264](https://rules.sonarsource.com/java/RSPEC-1264) | codestyle |
| GenericsNaming | Generics naming | Medium | Low | Deprecated | Active | [java:S119](https://rules.sonarsource.com/java/RSPEC-119) | codestyle |
| IdempotentOperations | Idempotent operations |  | Medium | Deprecated | Active | [java:S1656](https://rules.sonarsource.com/java/RSPEC-1656) | errorprone |
| InstantiationToGetClass | Instantiation to get class | Medium | Low | Deprecated | Active | [java:S2133](https://rules.sonarsource.com/java/RSPEC-2133) | errorprone |
| JumbledIncrementer | Jumbled incrementer |  | Medium | Deprecated | Active | [java:S1994](https://rules.sonarsource.com/java/RSPEC-1994) | errorprone |
| LocalHomeNamingConvention | Local home naming convention | Medium | Low |  | Active |  | codestyle |
| LocalInterfaceSessionNamingConvention | Local interface session naming convention | Medium | Low |  | Active |  | codestyle |
| LocalVariableCouldBeFinal | Local variable could be final | Low | Medium |  | Active |  | codestyle |
| LogicInversion | Logic inversion | Low | Medium | Deprecated | Active | [java:S1940](https://rules.sonarsource.com/java/RSPEC-1940) | design |
| LongVariable | Long variable |  | Medium | Deprecated | Active | [java:S117](https://rules.sonarsource.com/java/RSPEC-117) | codestyle |
| LoosePackageCoupling | Loose package coupling |  | Medium | Deprecated | Active |  | design |
| MDBAndSessionBeanNamingConvention | MDBAnd session bean naming convention | Medium | Low |  | Active |  | codestyle |
| MethodArgumentCouldBeFinal | Method argument could be final | Low | Medium | Deprecated | Active | [java:S1226](https://rules.sonarsource.com/java/RSPEC-1226) | codestyle |
| MethodNamingConventions | Method naming conventions | Medium | Blocker | Deprecated | Active | [java:S100](https://rules.sonarsource.com/java/RSPEC-100) | codestyle |
| MethodReturnsInternalArray | Method returns internal array | High | Medium | Deprecated | Active | [java:S2384](https://rules.sonarsource.com/java/RSPEC-2384) | bestpractices |
| MethodWithSameNameAsEnclosingClass | Method with same name as enclosing class |  | Medium | Deprecated | Active | [java:S1223](https://rules.sonarsource.com/java/RSPEC-1223) | errorprone |
| MisplacedNullCheck | Misplaced null check | High | Medium | Deprecated | Active | [java:S1697](https://rules.sonarsource.com/java/RSPEC-1697), [java:S2259](https://rules.sonarsource.com/java/RSPEC-2259) | errorprone |
| MissingSerialVersionUID | Missing serial version UID |  | Medium | Deprecated | Active | [java:S2057](https://rules.sonarsource.com/java/RSPEC-2057) | errorprone |
| MoreThanOneLogger | More than one logger | Medium | High | Deprecated | Active | [java:S1312](https://rules.sonarsource.com/java/RSPEC-1312) | errorprone |
| NoPackage | No package |  | Medium | Deprecated | Active | [java:S1220](https://rules.sonarsource.com/java/RSPEC-1220) | codestyle |
| NonStaticInitializer | Non static initializer |  | Medium | Deprecated | Active | [java:S1171](https://rules.sonarsource.com/java/RSPEC-1171) | errorprone |
| NonThreadSafeSingleton | Non thread safe singleton |  | Medium | Deprecated | Active | [java:S2444](https://rules.sonarsource.com/java/RSPEC-2444) | multithreading |
| OneDeclarationPerLine | One declaration per line | Medium | Low | Deprecated | Active | [java:S122](https://rules.sonarsource.com/java/RSPEC-122) | bestpractices |
| OnlyOneReturn | Only one return | Low | Medium | Deprecated | Active | [java:S1142](https://rules.sonarsource.com/java/RSPEC-1142) | codestyle |
| OverrideBothEqualsAndHashcode | Override both equals and hashcode | Blocker | Medium | Deprecated | Active | [java:S1206](https://rules.sonarsource.com/java/RSPEC-1206) | errorprone |
| PackageCase | Package case |  | Medium | Deprecated | Active | [java:S120](https://rules.sonarsource.com/java/RSPEC-120) | codestyle |
| PrematureDeclaration | Premature declaration |  | Medium | Deprecated | Active | [java:S1941](https://rules.sonarsource.com/java/RSPEC-1941) | codestyle |
| PreserveStackTrace | Preserve stack trace |  | Medium | Deprecated | Active | [java:S1166](https://rules.sonarsource.com/java/RSPEC-1166) | bestpractices |
| ProperCloneImplementation | Proper clone implementation |  | High | Deprecated | Active | [java:S1182](https://rules.sonarsource.com/java/RSPEC-1182) | errorprone |
| ProperLogger | Proper logger |  | Medium | Deprecated | Active | [java:S1312](https://rules.sonarsource.com/java/RSPEC-1312) | errorprone |
| RemoteInterfaceNamingConvention | Remote interface naming convention | Medium | Low |  | Active |  | codestyle |
| RemoteSessionInterfaceNamingConvention | Remote session interface naming convention | Medium | Low |  | Active |  | codestyle |
| ReplaceEnumerationWithIterator | Replace enumeration with iterator |  | Medium | Deprecated | Active | [java:S1150](https://rules.sonarsource.com/java/RSPEC-1150) | bestpractices |
| ReplaceHashtableWithMap | Replace hashtable with map |  | Medium | Deprecated | Active | [java:S1149](https://rules.sonarsource.com/java/RSPEC-1149) | bestpractices |
| ReplaceVectorWithList | Replace vector with list |  | Medium | Deprecated | Active | [java:S1149](https://rules.sonarsource.com/java/RSPEC-1149) | bestpractices |
| ReturnFromFinallyBlock | Return from finally block |  | Medium | Deprecated | Active | [java:S1143](https://rules.sonarsource.com/java/RSPEC-1143) | errorprone |
| ShortClassName | Short class name |  | Low | Deprecated | Active | [java:S101](https://rules.sonarsource.com/java/RSPEC-101) | codestyle |
| ShortMethodName | Short method name |  | Medium | Deprecated | Active | [java:S100](https://rules.sonarsource.com/java/RSPEC-100) | codestyle |
| ShortVariable | Short variable |  | Medium | Deprecated | Active | [java:S117](https://rules.sonarsource.com/java/RSPEC-117) | codestyle |
| SignatureDeclareThrowsException | Signature declare throws exception |  | Medium | Deprecated | Active | [java:S112](https://rules.sonarsource.com/java/RSPEC-112) | design |
| SimplifyBooleanExpressions | Simplify boolean expressions |  | Medium | Deprecated | Active | [java:S1125](https://rules.sonarsource.com/java/RSPEC-1125) | design |
| SimplifyBooleanReturns | Simplify boolean returns | Low | Medium | Deprecated | Active | [java:S1126](https://rules.sonarsource.com/java/RSPEC-1126) | design |
| SingletonClassReturningNewInstance | Singleton class returning new instance | Medium | High |  | Active |  | errorprone |
| SingularField | Singular field | Low | Medium |  | Active |  | design |
| StringBufferInstantiationWithChar | String buffer instantiation with char | Medium | Low | Deprecated | Active | [java:S1317](https://rules.sonarsource.com/java/RSPEC-1317) | errorprone |
| StringInstantiation | String instantiation | Medium | High |  | Active |  | performance |
| StringToString | String to string |  | Medium | Deprecated | Active | [java:S1858](https://rules.sonarsource.com/java/RSPEC-1858) | performance |
| SuspiciousEqualsMethodName | Suspicious equals method name |  | High | Deprecated | Active | [java:S1201](https://rules.sonarsource.com/java/RSPEC-1201) | errorprone |
| SuspiciousHashcodeMethodName | Suspicious hashcode method name |  | Medium | Deprecated | Active | [java:S1221](https://rules.sonarsource.com/java/RSPEC-1221) | errorprone |
| SwitchDensity | Switch density |  | Medium | Deprecated | Active | [java:S1151](https://rules.sonarsource.com/java/RSPEC-1151) | design |
| SystemPrintln | System println | Medium | High | Deprecated | Active | [java:S106](https://rules.sonarsource.com/java/RSPEC-106) | bestpractices |
| TooManyMethods | Too many methods |  | Medium | Deprecated | Active | [java:S1448](https://rules.sonarsource.com/java/RSPEC-1448) | design |
| UncommentedEmptyConstructor | Uncommented empty constructor |  | Medium | Deprecated | Active | [java:S2094](https://rules.sonarsource.com/java/RSPEC-2094) | documentation |
| UncommentedEmptyMethodBody | Uncommented empty method body |  | Medium | Deprecated | Active | [java:S1186](https://rules.sonarsource.com/java/RSPEC-1186) | documentation |
| UnconditionalIfStatement | Unconditional if statement | High | Medium | Deprecated | Active | [java:S2583](https://rules.sonarsource.com/java/RSPEC-2583) | errorprone |
| UnnecessaryCaseChange | Unnecessary case change | Low | Medium | Deprecated | Active | [java:S1157](https://rules.sonarsource.com/java/RSPEC-1157) | errorprone |
| UnnecessaryConstructor | Unnecessary constructor |  | Medium | Deprecated | Active | [java:S1186](https://rules.sonarsource.com/java/RSPEC-1186) | codestyle |
| UnnecessaryConversionTemporary | Unnecessary conversion temporary |  | Medium | Deprecated | Active | [java:S1158](https://rules.sonarsource.com/java/RSPEC-1158) | errorprone |
| UnnecessaryFullyQualifiedName | Unnecessary fully qualified name | Medium | Low |  | Active |  | codestyle |
| UnnecessaryLocalBeforeReturn | Unnecessary local before return |  | Medium | Deprecated | Active | [java:S1488](https://rules.sonarsource.com/java/RSPEC-1488) | codestyle |
| UnnecessaryReturn | Unnecessary return | Low | Medium |  | Active |  | codestyle |
| UnusedFormalParameter | Unused formal parameter |  | Medium | Deprecated | Active | [java:S1172](https://rules.sonarsource.com/java/RSPEC-1172) | bestpractices |
| UnusedLocalVariable | Unused local variable |  | Medium | Deprecated | Active | [java:S1481](https://rules.sonarsource.com/java/RSPEC-1481) | bestpractices |
| UnusedPrivateField | Unused private field |  | Medium | Deprecated | Active | [java:S1068](https://rules.sonarsource.com/java/RSPEC-1068) | bestpractices |
| UnusedPrivateMethod | Unused private method |  | Medium | Deprecated | Active |  | bestpractices |
| UseArrayListInsteadOfVector | Use array list instead of vector |  | Medium | Deprecated | Active | [java:S1149](https://rules.sonarsource.com/java/RSPEC-1149) | performance |
| UseCollectionIsEmpty | Use collection is empty | Low | Medium | Deprecated | Active | [java:S1155](https://rules.sonarsource.com/java/RSPEC-1155) | bestpractices |
| UseCorrectExceptionLogging | Use correct exception logging |  | Medium | Deprecated | Active | [java:S1166](https://rules.sonarsource.com/java/RSPEC-1166) | errorprone |
| UseEqualsToCompareStrings | Use equals to compare strings |  | Medium | Deprecated | Active | [java:S1698](https://rules.sonarsource.com/java/RSPEC-1698) | errorprone |
| UseNotifyAllInsteadOfNotify | Use notify all instead of notify |  | Medium | Deprecated | Active | [java:S2446](https://rules.sonarsource.com/java/RSPEC-2446) | multithreading |
| UseObjectForClearerAPI | Use object for clearer API | Low | Medium | Deprecated | Active | [java:S107](https://rules.sonarsource.com/java/RSPEC-107) | design |
| UseProperClassLoader | Use proper class loader | High | Medium |  | Active |  | errorprone |
| UseStringBufferLength | Use string buffer length | Low | Medium |  | Active |  | performance |
| UseUtilityClass | Use utility class |  | Medium | Deprecated | Active | [java:S1118](https://rules.sonarsource.com/java/RSPEC-1118) | design |
| UseVarargs | Use varargs | Medium | Low |  | Active |  | bestpractices |
| UselessOperationOnImmutable | Useless operation on immutable | High | Medium |  | Active |  | errorprone |
| UselessOverridingMethod | Useless overriding method |  | Medium | Deprecated | Active | [java:S1185](https://rules.sonarsource.com/java/RSPEC-1185) | design |
| UselessParentheses | Useless parentheses | Info | Low | Deprecated | Active | [java:S1110](https://rules.sonarsource.com/java/RSPEC-1110) | codestyle |
| UselessStringValueOf | Useless string value of | Low | Medium | Deprecated | Active | [java:S1153](https://rules.sonarsource.com/java/RSPEC-1153) | performance |

## Unchanged Rules
The following rules exist in both versions with no changes:

| Rule Key | Name | Severity | Status | Alternatives | Category |
|----------|------|----------|--------|--------------|----------|
| AccessorClassGeneration | Accessor class generation | Medium | Active |  | bestpractices |
| AddEmptyString | Add empty string | Medium | Active |  | performance |
| AssignmentToNonFinalStatic | Assignment to non final static | Medium | Active |  | errorprone |
| AvoidAccessibilityAlteration | Avoid accessibility alteration | Medium | Active |  | errorprone |
| AvoidArrayLoops | Avoid array loops | Medium | Active |  | performance |
| AvoidSynchronizedAtMethodLevel | Avoid synchronized at method level | Medium | Active |  | multithreading |
| BigIntegerInstantiation | Big integer instantiation | Medium | Active |  | performance |
| CallSuperFirst | Call super first | Medium | Active |  | errorprone |
| CallSuperLast | Call super last | Medium | Active |  | errorprone |
| CheckResultSet | Check result set | Medium | Active |  | bestpractices |
| ClassCastExceptionWithToArray | Class cast exception with to array | Medium | Active |  | errorprone |
| CloneMethodMustBePublic | Clone method must be public | Medium | Active |  | errorprone |
| CloneMethodReturnTypeMustMatchClassName | Clone method return type must match class name | Medium | Active |  | errorprone |
| CommentDefaultAccessModifier | Comment default access modifier | Medium | Active |  | codestyle |
| ConfusingTernary | Confusing ternary | Medium | Active |  | codestyle |
| ConsecutiveAppendsShouldReuse | Consecutive appends should reuse | Medium | Active |  | performance |
| DoNotHardCodeSDCard | Do not hard code SDCard | Medium | Active |  | errorprone |
| DoNotUseThreads | Do not use threads | Medium | Active |  | multithreading |
| DontUseFloatTypeForLoopIndices | Dont use float type for loop indices | Medium | Active |  | errorprone |
| GodClass | God class | Medium | Active |  | design |
| ImmutableField | Immutable field | Medium | Active |  | design |
| InefficientEmptyStringCheck | Inefficient empty string check | Medium | Active |  | performance |
| InefficientStringBuffering | Inefficient string buffering | Medium | Active |  | performance |
| InsufficientStringBufferDeclaration | Insufficient string buffer declaration | Medium | Active |  | performance |
| LawOfDemeter | Law of demeter | Medium | Active |  | design |
| MissingStaticMethodInNonInstantiatableClass | Missing static method in non instantiatable class | Medium | Active |  | errorprone |
| NPathComplexity | NPath complexity | Medium | Active |  | design |
| NullAssignment | Null assignment | Medium | Active |  | errorprone |
| OptimizableToArrayCall | Optimizable to array call | Medium | Active |  | performance |
| RedundantFieldInitializer | Redundant field initializer | Medium | Active |  | performance |
| SimpleDateFormatNeedsLocale | Simple date format needs locale | Medium | Active |  | errorprone |
| SimplifiedTernary | Simplified ternary | Medium | Active |  | design |
| SimplifyConditional | Simplify conditional | Medium | Active |  | design |
| SingleMethodSingleton | Single method singleton | High | Active |  | errorprone |
| StaticEJBFieldShouldBeFinal | Static EJBField should be final | Medium | Active |  | errorprone |
| SuspiciousOctalEscape | Suspicious octal escape | Medium | Active |  | errorprone |
| TooManyFields | Too many fields | Medium | Active |  | design |
| TooManyStaticImports | Too many static imports | Medium | Active |  | codestyle |
| UnusedAssignment | Unused assignment | Medium | Active |  | bestpractices |
| UnusedNullCheckInEquals | Unused null check in equals | Medium | Active |  | errorprone |
| UseArraysAsList | Use arrays as list | Medium | Active |  | performance |
| UseConcurrentHashMap | Use concurrent hash map | Medium | Active |  | multithreading |
| UseIndexOfChar | Use index of char | Medium | Active |  | performance |
| UseLocaleWithCaseConversions | Use locale with case conversions | Medium | Active |  | errorprone |
| UseStringBufferForStringAppends | Use string buffer for string appends | Medium | Active |  | performance |
| UselessQualifiedThis | Useless qualified this | Medium | Active |  | codestyle |

## Renamed Rules
The following rules have new names:

| Rule name | New rule name | Category |
|-----------|---------------|----------|
| DefaultLabelNotLastInSwitchStmt | DefaultLabelNotLastInSwitch | bestpractices |
| GuardLogStatementJavaUtil | GuardLogStatement | bestpractices |
| JUnit4TestShouldUseAfterAnnotation | UnitTestShouldUseAfterAnnotation | bestpractices |
| JUnit4TestShouldUseBeforeAnnotation | UnitTestShouldUseBeforeAnnotation | bestpractices |
| JUnit4TestShouldUseTestAnnotation | UnitTestShouldUseTestAnnotation | bestpractices |
| JUnitAssertionsShouldIncludeMessage | UnitTestAssertionsShouldIncludeMessage | bestpractices |
| JUnitTestContainsTooManyAsserts | UnitTestContainsTooManyAsserts | bestpractices |
| JUnitTestsShouldIncludeAssert | UnitTestShouldIncludeAssert | bestpractices |
| NonCaseLabelInSwitchStatement | NonCaseLabelInSwitch | errorprone |
| SwitchStmtsShouldHaveDefault | NonExhaustiveSwitch | bestpractices |
| TooFewBranchesForASwitchStatement | TooFewBranchesForSwitch | performance |

## Removed Rules
The following rules have been removed in the new version:

| Rule Key | Priority | Status | Category |
|----------|----------|--------|----------|
| AvoidConstantsInterface | Medium | Deprecated | bestpractices |
| CloneMethodMustImplementCloneableWithTypeResolution | Medium | Deprecated | errorprone |
| LooseCouplingWithTypeResolution | Medium | Deprecated | bestpractices |
| UnnecessaryParentheses | Low | Deprecated | codestyle |
| XPathRule | Medium | Deprecated |  |

Report generated on Tue Jul 15 17:33:57 CEST 2025
