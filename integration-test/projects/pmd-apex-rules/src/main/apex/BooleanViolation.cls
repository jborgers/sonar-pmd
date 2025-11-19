// Violates the rule: Uses a Boolean parameter
public class BooleanViolation {
  public static void doSomething(Boolean isSomething) {
    if (isSomething == true) {
      isSomething = false;
    } else {
      isSomething = null;
    }
  }
}
