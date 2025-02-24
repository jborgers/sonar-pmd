package pmd;

public class Bar extends Foo {
    public void method() {
        // PMD7-MIGRATION: added to force one violation in pmdShouldHaveAccessToExternalLibrariesInItsClasspath: is this testing the correct thing?
        if (true) System.out.println("violation on AvoidIfWithoutBrace");
    }
}
