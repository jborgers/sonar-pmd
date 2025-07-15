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
        // Extract category from configKey if available
        def configKey = rule.configKey.text()
        def category = ""
        if (configKey) {
            def parts = configKey.split('/')
            if (parts.length >= 3) {
                category = parts[2].split('\\.')[0]
            }
        }

        oldRules << [
            key: rule.@key.toString(),
            configKey: configKey,
            priority: rule.priority.text(),
            status: rule.status.text(),
            category: category
        ]
    }
}

// Extract rule keys from new rules (they are elements)
def newRules = []
newRulesXml.rule.each { rule ->
    // Extract category from internalKey if available
    def internalKey = rule.internalKey.text()
    def category = ""
    if (internalKey) {
        def parts = internalKey.split('/')
        if (parts.length >= 3) {
            category = parts[2].split('\\.')[0]
        }
    }

    newRules << [
        key: rule.key.text(),
        internalKey: internalKey,
        severity: rule.severity.text(),
        name: rule.name.text(),
        status: rule.status.text(),
        category: category
    ]
}

// Function to map priority/severity to SonarQube display values
def mapSeverity(severity) {
    switch (severity) {
        case "BLOCKER": return "Blocker"
        case "CRITICAL": return "High"
        case "MAJOR": return "Medium"
        case "MINOR": return "Low"
        case "INFO": return "Info"
        default: return severity
    }
}

// Function to map status values
def mapStatus(status) {
    switch (status) {
        case "DEPRECATED": return "Deprecated"
        default: return status
    }
}

// Function to extract alternative rule references from rule-alternatives-java.json file
def extractAlternativeRule(ruleKey) {
    def alternativesFile = new File("scripts/rule-alternatives-java.json")
    if (!alternativesFile.exists()) {
        return ""
    }

    try {
        def jsonSlurper = new JsonSlurper()
        def alternativesData = jsonSlurper.parse(alternativesFile)
        def alternatives = alternativesData.ruleAlternatives[ruleKey]

        if (alternatives) {
            def result = []
            alternatives.each { alt ->
                result << "[${alt.key}](${alt.link})"
            }
            return result.join(", ")
        }
    } catch (Exception e) {
        println "Warning: Error reading alternatives from JSON file: ${e.message}"
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
        alternative: alternative,
        category: newRule.category, // Use the category from the new rule
        isUpdated: mapSeverity(oldRule.priority) != mapSeverity(newRule.severity) || 
                  (oldRule.status ?: 'Active') != (newRule.status ?: 'Active')
    ]
}

// Split common rules into updated and unchanged
def updatedRules = commonRules.findAll { it.isUpdated }
def unchangedRules = commonRules.findAll { !it.isUpdated }

// Initialize and populate renamedRules variable before it's used in the summary
def renamedRules = []

// Check for renamed rules information
def oldRulesDir = new File(oldRulesPath).getParentFile() ?: new File(".")
def renamedJavaRulesFile = new File(oldRulesDir, "renamed-java-rules.json")
def renamedKotlinRulesFile = new File(oldRulesDir, "renamed-kotlin-rules.json")

// Special handling for GuardLogStatementJavaUtil which is renamed to GuardLogStatement
// but not via the deprecated and ref way as the others
def guardLogStatementJavaUtil = removedRules.find { it.key == "GuardLogStatementJavaUtil" }
def guardLogStatement = addedRules.find { it.key == "GuardLogStatement" }

if (guardLogStatementJavaUtil && guardLogStatement) {
    // Add to renamed rules
    renamedRules << [
        name: "GuardLogStatementJavaUtil",
        ref: "GuardLogStatement",
        category: guardLogStatement.category ?: "bestpractices"
    ]

    // Remove from removed and added rules to avoid duplication
    removedRules.removeIf { it.key == "GuardLogStatementJavaUtil" }
    addedRules.removeIf { it.key == "GuardLogStatement" }

    println "Special handling: GuardLogStatementJavaUtil renamed to GuardLogStatement"
}

// Read renamed Java rules if file exists
if (renamedJavaRulesFile.exists()) {
    try {
        def jsonSlurper = new JsonSlurper()
        def renamedJavaRules = jsonSlurper.parse(renamedJavaRulesFile)
        renamedRules.addAll(renamedJavaRules.rules)
        println "Found ${renamedJavaRules.count} renamed Java rules"
    } catch (Exception e) {
        println "Warning: Error reading renamed Java rules file: ${e.message}"
    }
}

// Read renamed Kotlin rules if file exists
if (renamedKotlinRulesFile.exists()) {
    try {
        def jsonSlurper = new JsonSlurper()
        def renamedKotlinRules = jsonSlurper.parse(renamedKotlinRulesFile)
        renamedRules.addAll(renamedKotlinRules.rules)
        println "Found ${renamedKotlinRules.count} renamed Kotlin rules"
    } catch (Exception e) {
        println "Warning: Error reading renamed Kotlin rules file: ${e.message}"
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
writer.writeLine("- Rules unchanged: ${unchangedRules.size()}")
writer.writeLine("- Rules updated: ${updatedRules.size()}")
writer.writeLine("- Rules renamed: ${renamedRules.size()}")

writer.writeLine("\n## Added Rules")
if (addedRules.isEmpty()) {
    writer.writeLine("No new rules were added.")
} else {
    writer.writeLine("The following rules have been added in the new version:\n")
    writer.writeLine("| Rule Key | Name | Severity | Category |")
    writer.writeLine("|----------|------|----------|----------|")
    addedRules.sort { it.key }.each { rule ->
        writer.writeLine("| ${rule.key} | ${rule.name} | ${mapSeverity(rule.severity)} | ${rule.category ?: ''} |")
    }
}

writer.writeLine("\n## Updated Rules")
if (updatedRules.isEmpty()) {
    writer.writeLine("No rules have been updated between versions.")
} else {
    writer.writeLine("The following rules have been updated in the new version:\n")
    writer.writeLine("| Rule Key | Name | Old Priority | New Severity | Old Status | New Status | Alternatives | Category |")
    writer.writeLine("|----------|------|--------------|--------------|------------|------------|--------------|----------|")
    updatedRules.sort { it.key }.each { rule ->
        def oldPriorityDisplay = mapSeverity(rule.oldPriority) == mapSeverity(rule.newSeverity) ? "" : mapSeverity(rule.oldPriority)
        def oldStatusDisplay = mapStatus(rule.oldStatus ?: 'Active') == mapStatus(rule.newStatus ?: 'Active') ? "" : mapStatus(rule.oldStatus ?: 'Active')
        writer.writeLine("| ${rule.key} | ${rule.name} | ${oldPriorityDisplay} | ${mapSeverity(rule.newSeverity)} | ${oldStatusDisplay} | ${mapStatus(rule.newStatus) ?: 'Active'} | ${rule.alternative} | ${rule.category ?: ''} |")
    }
}

writer.writeLine("\n## Unchanged Rules")
if (unchangedRules.isEmpty()) {
    writer.writeLine("No rules remain unchanged between versions.")
} else {
    writer.writeLine("The following rules exist in both versions with no changes:\n")
    writer.writeLine("| Rule Key | Name | Severity | Status | Alternatives | Category |")
    writer.writeLine("|----------|------|----------|--------|--------------|----------|")
    unchangedRules.sort { it.key }.each { rule ->
        writer.writeLine("| ${rule.key} | ${rule.name} | ${mapSeverity(rule.newSeverity)} | ${mapStatus(rule.newStatus) ?: 'Active'} | ${rule.alternative} | ${rule.category ?: ''} |")
    }
}

// Renamed rules have already been processed before generating the summary


// Add renamed rules section if any renamed rules were found
if (!renamedRules.isEmpty()) {
    writer.writeLine("\n## Renamed Rules")
    writer.writeLine("The following rules have new names:\n")
    writer.writeLine("| Rule name | New rule name | Category |")
    writer.writeLine("|-----------|---------------|----------|")
    renamedRules.sort { it.name }.each { rule ->
        writer.writeLine("| ${rule.name} | ${rule.ref} | ${rule.category} |")
    }
}

writer.writeLine("\n## Removed Rules")
if (removedRules.isEmpty()) {
    writer.writeLine("No rules were removed.")
} else {
    writer.writeLine("The following rules have been removed in the new version:")
    writer.writeLine("\n| Rule Key | Priority | Status | Category |")
    writer.writeLine("|----------|----------|--------|----------|")
    removedRules.sort { it.key }.each { rule ->
        writer.writeLine("| ${rule.key} | ${mapSeverity(rule.priority)} | ${mapStatus(rule.status) ?: 'Active'} | ${rule.category ?: ''} |")
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
