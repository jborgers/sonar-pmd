#!/usr/bin/env groovy
@Grab('org.apache.ant:ant:1.10.15')
@Grab('org.apache.ant:ant-launcher:1.10.15')
@Grab('org.apache.groovy:groovy-groovysh:4.0.26')
// We’ll resolve PMD dependencies programmatically to build a proper Ant classpath

import groovy.grape.Grape
import org.apache.tools.ant.Project
import org.apache.tools.ant.taskdefs.Javac
import org.apache.tools.ant.taskdefs.Jar
import org.apache.tools.ant.types.Path
import org.apache.tools.ant.types.FileSet

import java.nio.file.Files
import java.nio.file.Path as NioPath
import java.nio.file.StandardOpenOption

// 1) Resolve PMD Java + transitives to ensure AbstractJavaRule is available at compile time
// Pin to a PMD 7.x version. Adjust if needed to match your environment.
final Map coords = [group: 'net.sourceforge.pmd', module: 'pmd-java', version: '7.15.0']
final List<URI> resolvedUris = (List<URI>) Grape.resolve(
        [autoDownload: true, transitive: true, classLoader: this.class.classLoader],
        coords
)
final List<File> resolvedJars = resolvedUris.collect { new File(it) }

// 2) Prepare workspace
final File outputJar = new File('test-java-rule-extractor.jar')
final NioPath workDir = Files.createTempDirectory('rule-jar-build-')
final File srcDir = workDir.resolve('src').toFile()
final File classesDir = workDir.resolve('classes').toFile()
srcDir.mkdirs()
classesDir.mkdirs()

// 3) Write two Java rules: one with properties, one without
final String pkg = 'com.example.rules'
final File pkgDir = new File(srcDir, pkg.replace('.', File.separator))
pkgDir.mkdirs()

final String withPropsJava = """\
package ${pkg};

import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

public class WithPropsRule extends AbstractJavaRule {

    private static final PropertyDescriptor<String> STR_PROP =
            PropertyFactory.stringProperty("strProp")
                    .desc("A sample string property")
                    .defaultValue("default")
                    .build();

    private static final PropertyDescriptor<Integer> INT_PROP =
            PropertyFactory.intProperty("intProp")
                    .desc("A sample integer property")
                    .defaultValue(42)
                    .build();

    private static final PropertyDescriptor<Boolean> BOOL_PROP =
            PropertyFactory.booleanProperty("boolProp")
                    .desc("A sample boolean property")
                    .defaultValue(true)
                    .build();

    private static final PropertyDescriptor<java.util.List<String>> LIST_STR_PROP =
            PropertyFactory.stringListProperty("listStrProp")
                    .desc("A sample list of strings")
                    .emptyDefaultValue()
                    .build();

    public WithPropsRule() {
        definePropertyDescriptor(STR_PROP);
        definePropertyDescriptor(INT_PROP);
        definePropertyDescriptor(BOOL_PROP);
        definePropertyDescriptor(LIST_STR_PROP);
    }
}
""".stripIndent()

final String withoutPropsJava = """\
package ${pkg};

import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

public class WithoutPropsRule extends AbstractJavaRule {
    // Intentionally no properties
}
""".stripIndent()

Files.writeString(pkgDir.toPath().resolve('WithPropsRule.java'), withPropsJava, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
Files.writeString(pkgDir.toPath().resolve('WithoutPropsRule.java'), withoutPropsJava, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

// 4) Ant project and classpath
Project antProject = new Project()
antProject.init()

Path cp = new Path(antProject)
resolvedJars.each { jar ->
    cp.createPathElement().setPath(jar.absolutePath)
}

// 5) Compile sources with --release 17 (avoids module-path warning)
Javac javac = new Javac()
javac.setProject(antProject)
javac.setSrcdir(new Path(antProject, srcDir.absolutePath))
javac.setDestdir(classesDir)
javac.setIncludeantruntime(false)
javac.setClasspath(cp)
// Use "release" instead of source/target to align with JDK 17 modules cleanly
try {
    javac.setRelease('17')
} catch (Throwable ignore) {
    // Fallback if running on an older Ant: set source/target
    javac.setSource('17')
    javac.setTarget('17')
}
javac.execute()

// 6) Create JAR
if (outputJar.exists()) {
    outputJar.delete()
}
Jar jar = new Jar()
jar.setProject(antProject)
jar.setDestFile(outputJar)
FileSet fs = new FileSet()
fs.setDir(classesDir)
jar.addFileset(fs)
jar.execute()

println "Created JAR: ${outputJar.absolutePath}"
println "Contains:"
new File(classesDir, pkg.replace('.', File.separator)).eachFile { f ->
    if (f.name.endsWith('.class')) {
        println " - ${pkg}.${f.name.replace('.class','')}"
    }
}

// Move file to sonar-pmd-lib/src/test/resources
def moveToFile = new File('../sonar-pmd-lib/src/test/resources/' + outputJar.name)
new File(outputJar.absolutePath).renameTo(moveToFile)
println "Moved to: $moveToFile"

// Uncomment to remove the temp workspace after build
//Files.walk(workDir).sorted(Comparator.reverseOrder()).forEach { println "$it" }
Files.walk(workDir).sorted(Comparator.reverseOrder()).forEach { println "delete: $it"; it.toFile().delete() }