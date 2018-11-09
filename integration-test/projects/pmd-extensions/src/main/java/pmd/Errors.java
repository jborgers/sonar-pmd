package pmd;

import java.lang.System;
import java.lang.Throwable;

public class Errors {

  public void standardPmdError() {
    try {

    } catch (Throwable t) {
      // violation on: AvoidCatchingThrowable
    }
  }

  public void pmdExtensionError() {
    if (true)
      System.out.println("violation on AvoidIfWithoutBrace");
  }

  public void tooManyMethods() {
    // violation on MaximumMethodsCountCheck (threshold is 2)
  }
}
