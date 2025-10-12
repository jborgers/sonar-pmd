// Violates the rule: Uses a Boolean parameter
public class BooleanViolation {
  public static void doSomething(Boolean isSomething) {
    if (isSomething == true) {
      // Do something
    } else {
      // Do something else, or maybe do nothing if isSomething is null?
    }
  }
}
