#!/usr/bin/env groovy
/**
 * Generates a SonarQube quality profile XML (pmd-all-rules.xml) that enables all PMD rules from the
 * plugin's rules-java.xml. This uses default rule severities and does not set any parameters (defaults apply).
 *
 * Usage:
 *   groovy scripts/generate_pmd_all_rules_profile.groovy [outputPath]
 * Defaults:
 *   - Reads rules from: sonar-pmd-plugin/src/main/resources/org/sonar/plugins/pmd/rules-java.xml
 *   - Writes to: integration-test/src/test/resources/com/sonar/it/java/PmdTest/pmd-all-rules.xml
 */
import groovy.xml.MarkupBuilder
import groovy.xml.XmlParser

// Determine project root based on the script location.
// When run as a script, codeSource.location points to the script file (or compiled class); handle both cases.
def scriptLocation = this.class.protectionDomain?.codeSource?.location
File scriptFile
try {
  scriptFile = scriptLocation ? new File(scriptLocation.toURI()) : null
} catch (Exception ignored) {
  scriptFile = null
}
File scriptDir = (scriptFile != null && scriptFile.exists()) ? (scriptFile.isFile() ? scriptFile.parentFile : scriptFile) : new File(".").canonicalFile
// scripts/.. -> project root
File projectRoot = scriptDir.parentFile ?: new File(".").canonicalFile

File rulesFile = new File(projectRoot, "sonar-pmd-plugin/src/main/resources/org/sonar/plugins/pmd/rules-java.xml")
File defaultOut = new File(projectRoot, "integration-test/src/test/resources/com/sonar/it/java/PmdTest/pmd-all-rules.xml")

File outFile = (this.args && this.args.length > 0) ? new File(this.args[0]) : defaultOut

if (!rulesFile.isFile()) {
  System.err.println("Could not find rules file: " + rulesFile)
  System.exit(1)
}

println "Reading rules from: ${rulesFile.canonicalPath}"
println "Writing profile to: ${outFile.canonicalPath}"

def root = new XmlParser(false, false).parse(rulesFile)

def ruleDefs = []
root.rule.each { r ->
  def key = r.key?.text()?.trim()
  def severity = r.severity?.text()?.trim()
  if (key) {
    // collect parameters with default values
    def params = []
    r.param.each { p ->
      def pKey = p.key?.text()?.trim()
      def pDef = p.defaultValue?.text()
      if (pKey && pDef != null && pDef.toString().trim().length() > 0) {
        params << [key: pKey, value: pDef.toString().trim()]
      }
    }
    ruleDefs << [key: key, severity: (severity ?: 'MAJOR'), params: params]
  }
}

// sort for stable output
ruleDefs.sort { it.key }

outFile.parentFile.mkdirs()

def sw = new StringWriter()
def xml = new MarkupBuilder(sw)
xml.mkp.xmlDeclaration(version: '1.0', encoding: 'UTF-8')
xml.profile {
  name('pmd-all-rules-profile')
  language('java')
  rules {
    ruleDefs.each { rd ->
      rule {
        repositoryKey('pmd')
        key(rd.key)
        priority(rd.severity)
        if (rd.params && rd.params.size() > 0) {
          parameters {
            rd.params.each { param ->
              parameter {
                key(param.key)
                value(param.value)
              }
            }
          }
        } else {
          parameters {}
        }
      }
    }
  }
}

outFile.text = sw.toString() + System.lineSeparator()
println "Generated ${ruleDefs.size()} rules into ${outFile}"