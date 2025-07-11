#!/usr/bin/env groovy

/**
 * PMD Rules Release Notes Generator
 * 
 * This script compares two PMD rule XML files (typically an old version and a new version)
 * and generates a Markdown report highlighting:
 * 1. Rules that have been removed (present in old file but not in new file)
 * 2. Rules that have been added (present in new file but not in old file)
 * 3. Rules that remain unchanged (present in both files)
 * 
 * Usage:
 *   ./generate_release_notes.groovy [options]
 * 
 * Options:
 *   -o, --old <file>         Old rules XML file path (default: scripts/old-rules-$oldVersion.xml)
 *   -n, --new <file>         New rules XML file path (default: sonar-pmd-plugin/src/main/resources/org/sonar/plugins/pmd/rules-java.xml)
 *   -r, --report <file>      Output report file path (default: docs/pmd_release_notes_$version.md)
 *   -v, --version <version>  Version to use in the title
 *   -ov, --oldversion <ver>  Old version to use for old rules file name
 *   -h, --help               Show usage information
 */

import groovy.xml.XmlSlurper
import groovy.cli.commons.CliBuilder
import groovy.json.JsonSlurper
import java.util.regex.Pattern
import java.util.regex.Matcher

// Parse command line arguments
def cli = new CliBuilder(usage: 'generate_release_notes.groovy [options]')
cli.with {
    o(longOpt: 'old', args: 1, argName: 'file', 'Old rules XML file path')
    n(longOpt: 'new', args: 1, argName: 'file', 'New rules XML file path')
    r(longOpt: 'report', args: 1, argName: 'file', 'Output report file path')
    v(longOpt: 'version', args: 1, argName: 'version', 'Version to use in the title')
    ov(longOpt: 'oldversion', args: 1, argName: 'ver', 'Old version to use for old rules file name')
    h(longOpt: 'help', 'Show usage information')
}

def options = cli.parse(args)
if (options.h) {
    cli.usage()
    return
}

// Define file paths (use command line args if provided, otherwise use defaults)
def oldVersion = options.ov ?: "UNKNOWN"
def version = options.v ?: "UNKNOWN"
def oldRulesPath = options.o ?: "scripts/old-rules-${oldVersion}.xml"
def newRulesPath = options.n ?: "sonar-pmd-plugin/src/main/resources/org/sonar/plugins/pmd/rules-java.xml"
def outputPath = options.r ?: "docs/pmd_release_notes_${version}.md"

// Validate file paths
def oldRulesFile = new File(oldRulesPath)
def newRulesFile = new File(newRulesPath)

if (!oldRulesFile.exists()) {
    println "Error: Old rules file not found at ${oldRulesPath}"
    System.exit(1)
}

if (!newRulesFile.exists()) {
    println "Error: New rules file not found at ${newRulesPath}"
    System.exit(1)
}

// Create a writer for the output file
def writer

try {
    // Ensure the directory exists
    def outputFile = new File(outputPath)
    def parentDir = outputFile.getParentFile()
    if (parentDir != null && !parentDir.exists()) {
        println "Creating directory: ${parentDir.absolutePath}"
        parentDir.mkdirs()
    }

    writer = outputFile.newWriter()
} catch (Exception e) {
    println "Error creating output file: ${e.message}"
    System.exit(1)
}

// Parse XML files
def oldRulesXml
def newRulesXml

try {
    oldRulesXml = new XmlSlurper().parse(oldRulesFile)
    newRulesXml = new XmlSlurper().parse(newRulesFile)
} catch (Exception e) {
    writer.close()
    println "Error parsing XML files: ${e.message}"
    System.exit(1)
}

// Extract rule keys from old rules (they are attributes)
def oldRules = []
oldRulesXml.rule.each { rule ->
    // Skip commented out rules
    if (rule.@key.toString()) {
        oldRules << [
            key: rule.@key.toString(),
            configKey: rule.configKey.text(),
            priority: rule.priority.text(),
            status: rule.status.text()
        ]
    }
}

// Extract rule keys from new rules (they are elements)
def newRules = []
newRulesXml.rule.each { rule ->
    newRules << [
        key: rule.key.text(),
        internalKey: rule.internalKey.text(),
        severity: rule.severity.text(),
        name: rule.name.text(),
        status: rule.status.text()
    ]
}

// Function to extract alternative rule references from markdown files
def extractAlternativeRule(ruleKey) {
    def mdFile = new File("docs/rules/${ruleKey}.md")
    if (!mdFile.exists()) {
        return ""
    }

    def content = mdFile.text
    def pattern = Pattern.compile(":warning: This rule is \\*\\*deprecated\\*\\* in favour of ((?:\\[[^\\]]+\\]\\([^)]+\\)|`[^`]+`)(?:, (?:\\[[^\\]]+\\]\\([^)]+\\)|`[^`]+`))*)")
    def matcher = pattern.matcher(content)

    if (matcher.find()) {
        def alternatives = matcher.group(1)
        // Extract all alternatives from the matched group, preserving the original markdown format
        def altPattern = Pattern.compile("(\\[[^\\]]+\\]\\([^)]+\\))|((`[^`]+`))")
        def altMatcher = altPattern.matcher(alternatives)
        def result = []
        while (altMatcher.find()) {
            result << (altMatcher.group(1) ?: altMatcher.group(3))
        }
        return result.join(", ")
    }

    return ""
}

// Get sets of rule keys for comparison
def oldRuleKeys = oldRules.collect { it.key }
def newRuleKeys = newRules.collect { it.key }

// Find rules that are in old but not in new
def removedRules = oldRules.findAll { !newRuleKeys.contains(it.key) }

// Find rules that are in new but not in old
def addedRules = newRules.findAll { !oldRuleKeys.contains(it.key) }

// Find rules that exist in both
def commonRuleKeys = oldRuleKeys.intersect(newRuleKeys)
def commonRules = commonRuleKeys.collect { key ->
    def oldRule = oldRules.find { it.key == key }
    def newRule = newRules.find { it.key == key }
    def alternative = extractAlternativeRule(key)
    [
        key: key,
        oldConfigKey: oldRule.configKey,
        newInternalKey: newRule.internalKey,
        oldPriority: oldRule.priority,
        newSeverity: newRule.severity,
        name: newRule.name,
        oldStatus: oldRule.status,
        newStatus: newRule.status,
        alternative: alternative
    ]
}

// Initialize and populate skippedRules variable before it's used in the summary
def skippedRules = []

// Check for skipped rules information
def oldRulesDir = new File(oldRulesPath).getParentFile() ?: new File(".")
def skippedJavaRulesFile = new File(oldRulesDir, "skipped-java-rules.json")
def skippedKotlinRulesFile = new File(oldRulesDir, "skipped-kotlin-rules.json")

// Read skipped Java rules if file exists
if (skippedJavaRulesFile.exists()) {
    try {
        def jsonSlurper = new JsonSlurper()
        def skippedJavaRules = jsonSlurper.parse(skippedJavaRulesFile)
        skippedRules.addAll(skippedJavaRules.rules)
        println "Found ${skippedJavaRules.count} skipped Java rules"
    } catch (Exception e) {
        println "Warning: Error reading skipped Java rules file: ${e.message}"
    }
}

// Read skipped Kotlin rules if file exists
if (skippedKotlinRulesFile.exists()) {
    try {
        def jsonSlurper = new JsonSlurper()
        def skippedKotlinRules = jsonSlurper.parse(skippedKotlinRulesFile)
        skippedRules.addAll(skippedKotlinRules.rules)
        println "Found ${skippedKotlinRules.count} skipped Kotlin rules"
    } catch (Exception e) {
        println "Warning: Error reading skipped Kotlin rules file: ${e.message}"
    }
}

// Generate report
writer.writeLine("# PMD Rules Release Notes for version $version")
writer.writeLine("_Do not edit this generated file._")
writer.writeLine("\n## Summary")
writer.writeLine("- Total rules in old version ($oldVersion): ${oldRules.size()}")
writer.writeLine("- Total rules in new version ($version): ${newRules.size()}")
writer.writeLine("- Rules added: ${addedRules.size()}")
writer.writeLine("- Rules removed: ${removedRules.size()}")
writer.writeLine("- Rules unchanged: ${commonRules.size()}")
writer.writeLine("- Rules renamed: ${skippedRules.size()}")

writer.writeLine("\n## Removed Rules")
if (removedRules.isEmpty()) {
    writer.writeLine("No rules were removed.")
} else {
    writer.writeLine("The following rules have been removed in the new version:")
    writer.writeLine("\n| Rule Key | Priority | Status |")
    writer.writeLine("|----------|----------|--------|")
    removedRules.sort { it.key }.each { rule ->
        writer.writeLine("| ${rule.key} | ${rule.priority} | ${rule.status ?: 'Active'} |")
    }
}

writer.writeLine("\n## Added Rules")
if (addedRules.isEmpty()) {
    writer.writeLine("No new rules were added.")
} else {
    writer.writeLine("The following rules have been added in the new version:\n")
    writer.writeLine("| Rule Key | Name | Severity | Status |")
    writer.writeLine("|----------|------|----------|--------|")
    addedRules.sort { it.key }.each { rule ->
        writer.writeLine("| ${rule.key} | ${rule.name} | ${rule.severity} | ${rule.status ?: 'Active'} |")
    }
}

writer.writeLine("\n## Unchanged Rules")
if (commonRules.isEmpty()) {
    writer.writeLine("No rules remain unchanged between versions.")
} else {
    writer.writeLine("The following rules exist in both versions:\n")
    writer.writeLine("| Rule Key | Name | Old Priority | New Severity | Old Status | New Status | Alternatives |")
    writer.writeLine("|----------|------|--------------|--------------|------------|------------|--------------|")
    commonRules.sort { it.key }.each { rule ->
        writer.writeLine("| ${rule.key} | ${rule.name} | ${rule.oldPriority} | ${rule.newSeverity} | ${rule.oldStatus ?: 'Active'} | ${rule.newStatus ?: 'Active'} | ${rule.alternative} |")
    }
}

// Skipped rules have already been processed before generating the summary


// Add skipped rules section if any skipped rules were found
if (!skippedRules.isEmpty()) {
    writer.writeLine("\n## Renamed Rules")
    writer.writeLine("The following rules have new names:\n")
    writer.writeLine("| Rule name | New rule name | Category |")
    writer.writeLine("|-----------|---------------|----------|")
    skippedRules.sort { it.name }.each { rule ->
        writer.writeLine("| ${rule.name} | ${rule.ref} | ${rule.category} |")
    }
}

writer.writeLine("\nReport generated on ${new Date()}")

// Close the writer
try {
    writer.close()
    // Print a message to the console
    println "Release notes generated in ${outputPath}"
} catch (Exception e) {
    println "Warning: Error closing output file: ${e.message}"
    // Still inform the user that the report was generated
    println "Release notes generated in ${outputPath}"
}
