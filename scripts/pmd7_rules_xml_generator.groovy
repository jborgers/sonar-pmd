import groovy.xml.XmlSlurper
import groovy.xml.MarkupBuilder
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipFile
import groovy.grape.Grape
import groovy.transform.Field

// Resolve versions from pom.xml to have a single source of truth
@Field def pomFile = new File('pom.xml')
if (!pomFile.exists()) {
    pomFile = new File('../pom.xml') // allow running from scripts/ directory
}
if (!pomFile.exists()) {
    throw new RuntimeException('pom.xml not found to determine versions')
}

@Field def pom = new XmlSlurper().parse(pomFile)
@Field def pmdVersion = (pom.properties.'pmd.version'.text() ?: '').trim()
if (!pmdVersion) {
    throw new RuntimeException('pmd.version not found in pom.xml')
}

// Dynamically grab required dependencies matching pom-defined versions
Grape.grab([group: 'net.sourceforge.pmd', module: 'pmd-java', version: pmdVersion])
Grape.grab([group: 'net.sourceforge.pmd', module: 'pmd-kotlin', version: pmdVersion])
// Note: sonar-pmd-lib is provided on the classpath by the groovy-maven-plugin configuration; no Grape needed.

import org.sonar.plugins.pmd.rule.JavaRulePropertyExtractor
import org.sonar.plugins.pmd.rule.MarkdownToHtmlConverter
import org.sonar.plugins.pmd.rule.util.PmdSeverityMapper
import org.sonar.plugins.pmd.rule.util.RuleParamFormatter

// Simple logging utilities
@Field boolean DEBUG_LOG = Boolean.parseBoolean(System.getProperty('pmd7.rules.gen.debug', 'false'))

def logInfo(Object msg) {
    println String.valueOf(msg)
}

def logDebug(Object msg) {
    if (DEBUG_LOG) {
        println "DEBUG:  " + String.valueOf(msg)
    }
}

def logWarn(Object msg) {
    System.err.println "WARN:   " + String.valueOf(msg)
}

def logError(Object msg) {
    System.err.println "ERROR:  " + String.valueOf(msg)
}

// Configure PMD version for MarkdownToHtmlConverter to avoid lib dependency on PMD
MarkdownToHtmlConverter.setPmdVersion(pmdVersion)

// Configuration
@Field def pmdJavaJarPath = new File("${System.getProperty("user.home")}/.m2/repository/net/sourceforge/pmd/pmd-java/${pmdVersion}/pmd-java-${pmdVersion}.jar")
@Field def pmdKotlinJarPath = new File("${System.getProperty("user.home")}/.m2/repository/net/sourceforge/pmd/pmd-kotlin/${pmdVersion}/pmd-kotlin-${pmdVersion}.jar")
def javaCategoriesPropertiesPath = "category/java/categories.properties"
def kotlinCategoriesPropertiesPath = "category/kotlin/categories.properties"
// Define language-specific rule alternatives paths
@Field String javaRuleAlternativesPath = "scripts/rule-alternatives-java.json"
@Field String kotlinRuleAlternativesPath = "scripts/rule-alternatives-kotlin.json"

// Extract Java rule properties from the jar file
logInfo("extracting Java rule properties from ${pmdJavaJarPath}")
@Field def javaRulePropertyExtractor = new JavaRulePropertyExtractor()
@Field def javaRuleProperties = javaRulePropertyExtractor.extractProperties(pmdJavaJarPath)
logInfo("found properties for ${javaRuleProperties.size()} Java rule classes")

// Extract Kotlin rule properties from the jar file
logInfo("extracting Kotlin rule properties from ${pmdKotlinJarPath}")
@Field def kotlinRulePropertyExtractor = new JavaRulePropertyExtractor()
@Field def kotlinRuleProperties = kotlinRulePropertyExtractor.extractProperties(pmdKotlinJarPath)
logInfo("found properties for ${kotlinRuleProperties.size()} Kotlin rule classes")

// Function to read rule alternatives from a JSON file
def readRuleAlternatives(String filePath) {
    def alternatives = [:]
    try {
        def alternativesFile = new File(filePath)
        if (alternativesFile.exists()) {
            def jsonSlurper = new JsonSlurper()
            def alternativesData = jsonSlurper.parse(alternativesFile)
            alternatives = alternativesData.ruleAlternatives
            logInfo("loaded ${alternatives.size()} rule alternatives from ${filePath}")
        } else {
            logWarn("rule alternatives file not found at ${filePath}")
        }
    } catch (Exception e) {
        logError("reading rule alternatives: ${e.message}")
    }
    return alternatives
}

// Read Java rule alternatives
@Field Map javaRuleAlternatives = readRuleAlternatives(javaRuleAlternativesPath)

// Read Kotlin rule alternatives (for future use)
@Field Map kotlinRuleAlternatives = readRuleAlternatives(kotlinRuleAlternativesPath)

// If we're in test mode, make the MarkdownToHtmlConverter available but don't run the main code
if (binding.hasVariable('TEST_MODE') && binding.getVariable('TEST_MODE')) {
    // Make MarkdownToHtmlConverter available to the caller
    binding.setVariable('MarkdownToHtmlConverter', MarkdownToHtmlConverter)
    return // Skip the rest of the script
}

// Get output directory from binding variable (set by Maven) or use a default directory
// The 'outputDir' variable is passed from Maven's groovy-maven-plugin configuration
@Field def defaultOutputDir = new File("sonar-pmd-plugin/src/main/resources/org/sonar/plugins/pmd").exists() ? 
    "sonar-pmd-plugin/src/main/resources/org/sonar/plugins/pmd" : "."
@Field def outputDirPath = binding.hasVariable('outputDir') ? outputDir : defaultOutputDir
@Field def javaOutputFileName = "rules-java.xml"
@Field def kotlinOutputFileName = "rules-kotlin.xml"
@Field def javaOutputFilePath = new File(outputDirPath, javaOutputFileName)
@Field def kotlinOutputFilePath = new File(outputDirPath, kotlinOutputFileName)

logInfo("PMD ${pmdVersion} Rules XML Generator")
logInfo("=" * 50)
logInfo("Java output file: ${javaOutputFilePath}")
logInfo("Kotlin output file: ${kotlinOutputFilePath}")

// Function to read rules from a PMD JAR
def readRulesFromJar = { jarFile, categoriesPath ->
    if (!jarFile.exists()) {
        logError("PMD JAR not found at: ${jarFile}")
        return []
    }

    def rules = []
    def categoryFiles = []

    try {
        def zipFile = new ZipFile(jarFile)

        // Read categories.properties to get rule files
        def categoriesEntry = zipFile.getEntry(categoriesPath)
        if (categoriesEntry) {
            def categoriesProps = new Properties()
            categoriesProps.load(zipFile.getInputStream(categoriesEntry))
            categoryFiles = categoriesProps.getProperty("rulesets.filenames", "").split(",").collect { it.trim() }
            logInfo("found ${categoryFiles.size()} category files in PMD JAR: ${jarFile}")
        } else {
            logWarn("${categoriesPath} not found in PMD JAR: ${jarFile}")
        }

        // Process each category file
        categoryFiles.each { categoryFile ->
            def entry = zipFile.getEntry(categoryFile)
            if (!entry) {
                logWarn("  - category file not found: ${categoryFile}")
                return
            }

            try {
                def categoryXml = new XmlSlurper().parse(zipFile.getInputStream(entry))
                def categoryName = categoryFile.tokenize('/').last() - '.xml'

                // Process each rule in the category
                categoryXml.rule.each { rule ->
                    def ruleName = rule.@name.toString()
                    if (!ruleName) return

                    rules << [
                        name: ruleName,
                        category: categoryName,
                        categoryFile: categoryFile,
                        class: rule.@class.toString(),
                        deprecated: rule.@deprecated.toString() == "true",
                        ref: rule.@ref.toString(),
                        since: rule.@since.toString(),
                        externalInfoUrl: rule.@externalInfoUrl.toString(),
                        message: rule.@message.toString() ?: ruleName,
                        description: rule.description.text(),
                        priority: rule.priority.text() ?: "3",
                        examples: rule.example.collect { it.text() },
                        properties: rule.properties.property.collect { prop -> [
                            name: prop.@name.toString(),
                            description: prop.@description.toString(),
                            type: prop.@type.toString(),
                            value: prop.@value.toString(),
                            min: prop.@min.toString(),
                            max: prop.@max.toString()
                        ]}
                    ]
                }
                logInfo("  - processed ${categoryFile}: found ${categoryXml.rule.size()} rules")
            } catch (Exception e) {
                logError("  - processing ${categoryFile}: ${e.message}")
            }
        }

        zipFile.close()
        return rules
    } catch (Exception e) {
        logError("reading PMD JAR: ${e.message}")
        return []
    }
}

// Read Java rules
logInfo("reading Java rules from ${pmdJavaJarPath}")
def javaRules = readRulesFromJar(pmdJavaJarPath, javaCategoriesPropertiesPath)
logInfo("found ${javaRules.size()} total Java rules\n")

// Read Kotlin rules
logInfo("reading Kotlin rules from ${pmdKotlinJarPath}")
def kotlinRules = readRulesFromJar(pmdKotlinJarPath, kotlinCategoriesPropertiesPath)
logInfo("found ${kotlinRules.size()} total Kotlin rules\n")

// Helper function to escape XML content for CDATA
String escapeForCdata(String text) {
    if (!text) return ""
    // Split any occurrence of the CDATA closing sequence so the CDATA remains valid
    return text.replaceAll(/\]\]>/, "]]]]><![CDATA[>")
}

// Helper function to format description with examples using MdToHtmlConverter
def formatDescription(ruleData, String language) {
    def description = ruleData.description ?: ""
    def examples = ruleData.examples ?: []
    def externalInfoUrl = ruleData.externalInfoUrl ?: ""
    def message = ruleData.message ?: ""
    def ruleName = ruleData.name

    // Build markdown content
    def markdownContent = new StringBuilder()

    // Title and base description
    appendTitleSection(markdownContent, message)
    markdownContent.append(description ?: "")

    // Examples
    appendExamplesSection(markdownContent, examples, language)

    // Convert to HTML and enrich
    def htmlContent = MarkdownToHtmlConverter.convertToHtml(markdownContent.toString())
    htmlContent = enrichWithAlternatives(htmlContent, ruleName, language)
    htmlContent = appendExternalInfoLink(htmlContent, externalInfoUrl)

    return htmlContent
}

// Function to generate XML file (refactored to use helper methods for readability)
def generateXmlFile = { outputFile, rules, language ->
    def rulesWithoutDescription = 0
    def renamedRules = 0
    def rulesWithDeprecatedAndRef = []

    try {
        // Generate XML file
        outputFile.withWriter('UTF-8') { writer ->
            def xml = new MarkupBuilder(writer)
            xml.setDoubleQuotes(true)

            // Write XML declaration manually
            writer.println('<?xml version="1.0" encoding="UTF-8"?>')
            // Add generation banner (ignored by parser) for traceability and to reflect regeneration
            def ts = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE)
            writer.println("<!-- Generated by pmd7_rules_xml_generator.groovy on ${ts} using PMD ${pmdVersion} for ${language} -->")

            xml.rules {
                rules.sort { it.name }.each { ruleData ->
                    // Skip rules with deprecated=true and ref attribute
                    if (ruleData.deprecated && ruleData.ref) {
                        renamedRules++
                        rulesWithDeprecatedAndRef << ruleData
                        return // Skip this rule
                    }

                    rule {
                        key(ruleData.name)
                        def readableName = MarkdownToHtmlConverter.camelCaseToReadable(ruleData.name)
                        name(readableName)
                        internalKey("${ruleData.categoryFile}/${ruleData.name}")
                        severity(PmdSeverityMapper.priorityToSeverity(ruleData.priority, ruleData.category))

                        // Determine whether the rule message contains variable placeholders like {0}, {1}, ...
                        def hasVariablePlaceholders = (ruleData.message ?: "").find(/\{\d+\}/) != null

                        // Description
                        boolean usedFallback = writeDescription(xml, ruleData, language)
                        if (usedFallback) {
                            rulesWithoutDescription++
                        }

                        // Status if deprecated
                        if (ruleData.deprecated) {
                            status("DEPRECATED")
                        }

                        // Tags
                        tag("pmd")
                        tag(ruleData.category)
                        addAlternativeTagIfAny(xml, language, ruleData.name)

                        // Parameters
                        def existingParamKeys = new HashSet<String>(ruleData.properties.findAll { it.name }.collect { it.name })
                        addRuleParams(xml, ruleData, language, existingParamKeys)
                        addSuppressionParamIfNeeded(xml, hasVariablePlaceholders, existingParamKeys)
                }
            }
        }
        }
        
        // Print summary information
        def activeRules = rules.count { !it.deprecated }
        def deprecatedRules = rules.count { it.deprecated }
        def categoryStats = rules.groupBy { it.category }

        logInfo("""
successfully generated ${outputFile.name}
total ${language} rules: ${rules.size()}
active ${language} rules: ${activeRules}
deprecated ${language} rules: ${deprecatedRules}
${renamedRules > 0 ? "renamed ${language} rules (deprecated with ref): ${renamedRules}" : ""}
${rulesWithoutDescription > 0 ? "${language} rules with generated fallback descriptions: ${rulesWithoutDescription}" : ""}
using camelCase transformation for all rule names

${language} rules by category:""")

        categoryStats.sort { it.key }.each { category, categoryRules ->
            def activeCount = categoryRules.count { !it.deprecated }
            def deprecatedCount = categoryRules.count { it.deprecated }
            logInfo("  - ${category}: ${categoryRules.size()} total (${activeCount} active, ${deprecatedCount} deprecated)")
        }

        logInfo("tags that will be applied for ${language} rules:")
        logInfo("  - pmd: ${rules.size()} rules")
        categoryStats.sort { it.key }.each { category, categoryRules ->
            logInfo("  - ${category}: ${categoryRules.size()} rules")
        }

        // Verify no empty descriptions
        reportEmptyDescriptions(outputFile, language)

        // Print warnings for renamed rules with deprecated=true and ref attribute and write JSON
        if (renamedRules > 0) {
            handleRenamedRules(language, renamedRules, rulesWithDeprecatedAndRef)
        }

        return true
    } catch (Exception e) {
        logError("generating ${language} XML file: ${e.message}")
        e.printStackTrace()
        return false
    }
}

// Generate Java rules XML file
logInfo("")
logInfo("Generating Java rules XML file...")
logInfo("=" * 30)
def javaSuccess = generateXmlFile(javaOutputFilePath, javaRules, "Java")

// Generate Kotlin rules XML file
logInfo("")
logInfo("Generating Kotlin rules XML file...")
logInfo("=" * 30)
def kotlinSuccess = generateXmlFile(kotlinOutputFilePath, kotlinRules, "Kotlin")

// Add XPathRule as a special case to the Java rules XML file
addXPathRuleToJavaFile()

logInfo("")
if (javaSuccess && kotlinSuccess) {
    logInfo("XML generation completed successfully for both Java and Kotlin rules!")
} else {
    logInfo("XML generation completed with errors. Please check the logs above.")
}


// === Helper methods extracted for readability ===

def appendTitleSection(StringBuilder sb, String message) {
    if (message && !message.trim().isEmpty()) {
        def processedMessage =
                wrapPlaceholdersOutsideBackticks(message)
                .replaceAll(/''/, "'")
        sb.append("## Title of issues: ").append(processedMessage).append("\n\n")
    }
}

def wrapPlaceholdersOutsideBackticks(String text) {
    def parts = text.split('`', -1)  // -1 to keep empty trailing parts
    def result = new StringBuilder()

    for (int i = 0; i < parts.length; i++) {
        if (i % 2 == 0) {
            // Outside backticks - process placeholders
            def processed = parts[i].replaceAll(/\{(\d+)\}/, '`{$1}`')
            result.append(processed)
        } else {
            // Inside backticks - don't process
            result.append(parts[i])
        }

        // Add back the backtick separator (except for the last part)
        if (i < parts.length - 1) {
            result.append('`')
        }
    }

    return result.toString()
}



def appendExamplesSection(StringBuilder sb, List examples, String language) {
    if (examples && !examples.isEmpty()) {
        sb.append(examples.size() > 1 ? "\n\n## Examples\n\n" : "\n\n## Example\n\n")
        examples.eachWithIndex { example, index ->
            if (examples.size() > 1) {
                sb.append("### Example ").append(index + 1).append("\n\n")
            }
            sb.append("```").append(language.toLowerCase()).append("\n")
            sb.append(example)
            sb.append("\n```\n\n")
        }
    }
}

def enrichWithAlternatives(String htmlContent, String ruleName, String language) {
    def ruleAlternativesForLanguage = language == "Java" ? javaRuleAlternatives : kotlinRuleAlternatives
    if (ruleAlternativesForLanguage && ruleAlternativesForLanguage.containsKey(ruleName)) {
        def alternatives = ruleAlternativesForLanguage[ruleName]
        if (alternatives && !alternatives.isEmpty()) {
            def alternativesHtml = new StringBuilder("<p><b>Alternative " + (alternatives.size() == 1 ? "rule" : "rules") + ":</b> ")
            alternatives.eachWithIndex { alt, index ->
                if (index > 0) alternativesHtml.append(", ")
                def internalLink = "./coding_rules?rule_key=${URLEncoder.encode(alt.key, 'UTF-8')}&open=${URLEncoder.encode(alt.key, 'UTF-8')}"
                alternativesHtml.append("<a href=\"${internalLink}\">${alt.key}</a>")
            }
            alternativesHtml.append("</p>")
            htmlContent += "\n" + alternativesHtml.toString()
        }
    }
    return htmlContent
}

def appendExternalInfoLink(String htmlContent, String externalInfoUrl) {
    if (externalInfoUrl) {
        def linkText = "Full documentation"
        htmlContent += "\n<p><a href=\"${externalInfoUrl}\">${linkText}</a></p>"
    }
    return htmlContent
}

def writeDescription(xml, Map ruleData, String language) {
    def descContent = formatDescription(ruleData, language)
    boolean usedFallback = false
    if (!descContent || descContent.trim().isEmpty()) {
        descContent = MarkdownToHtmlConverter.convertToHtml("THIS SHOULD NOT HAPPEN")
        usedFallback = true
    }
    xml.description {
        xml.mkp.yieldUnescaped("<![CDATA[${escapeForCdata(descContent)}]]>")
    }
    return usedFallback
}

def addAlternativeTagIfAny(xml, String language, String ruleName) {
    def ruleAlternativesForLanguage = language == "Java" ? javaRuleAlternatives : kotlinRuleAlternatives
    if (ruleAlternativesForLanguage && ruleAlternativesForLanguage.containsKey(ruleName)) {
        xml.tag("has-sonar-alternative")
    }
}

def addParam(xml, String keyName, String descText, String defaultVal, String typeToken) {
    xml.param {
        key(keyName)
        description {
            xml.mkp.yieldUnescaped("<![CDATA[${escapeForCdata(descText ?: "")}]]>")
        }
        // Always emit an explicit empty default when the default is missing
        def effectiveDefault = defaultVal
        if (effectiveDefault == null) {
            effectiveDefault = ""
        }
        if (effectiveDefault != null) defaultValue(effectiveDefault)
        if (typeToken) type(typeToken)
    }
}

def addXmlDefinedRuleParams(xml, Map ruleData, Set existingParamKeys) {
    ruleData.properties.findAll { prop ->
        prop.name && prop.description
    }.each { prop ->
        String typeToken = null
        if (prop.type) {
            def t = prop.type.toUpperCase()
            if (t.startsWith("LIST[") || t.contains("REGEX") || t == "REGULAR_EXPRESSION") {
                typeToken = "STRING"
            } else {
                typeToken = t
            }
        }
        addParam(xml, prop.name, prop.description, prop.value, typeToken)
        existingParamKeys.add(prop.name)
    }
}

// --- Helpers for addClassDefinedRuleParams ---
def getRulePropertiesForClass(Map rulePropertiesMap, String ruleClass) {
    rulePropertiesMap.get(ruleClass)
}

def getUnwrappedType(propInfo) {
    def propType = propInfo.type
    try {
        return propInfo.getWrappedType()
    } catch (MissingMethodException ignore) {
        return propType
    }
}

def shouldLogProperty(String propName) {
    !(propName == "violationSuppressRegex" || propName == "violationSuppressXPath")
}

def handleSuppressionSpecialCases(xml, propInfo) {
    if (propInfo.name == "violationSuppressXPath") {
        // Skip adding this parameter as it's too complex/error-prone for users
        return true
    }
    if (propInfo.name == "violationSuppressRegex") {
        // Do not emit this parameter here. It will be added later by addSuppressionParamIfNeeded
        // only when the rule message contains variable placeholders. This keeps
        // keep the suppression regex description centralized and ensure the param
        // is only present when appropriate.
        return true
    }
    return false
}


def getAcceptedValues(propInfo) {
    try {
        return (propInfo.getAcceptedValues() ?: []) as List
    } catch (MissingMethodException ignore) {
        return []
    } catch (MissingPropertyException ignore) {
        return []
    }
}

def isMultiple(propInfo) {
    try {
        return propInfo.isMultiple()
    } catch (MissingMethodException ignore) {
        return false
    }
}

def computeBaseDescription(propInfo, List accepted, boolean multiple) {
    RuleParamFormatter.buildDescription(
        null,
        propInfo.description,
        accepted,
        multiple
    )
}

def determineTypeToken(List accepted, boolean multiple, String unwrappedType) {
    if (accepted && !accepted.isEmpty()) {
        return RuleParamFormatter.buildSelectTypeToken(accepted, multiple)
    }
    if (unwrappedType in ["Integer", "Long", "Short", "Byte", "BigInteger"]) {
        return "INTEGER"
    }
    if (unwrappedType in ["Double", "Float", "BigDecimal"]) {
        return "FLOAT"
    }
    if (unwrappedType?.equalsIgnoreCase("Boolean")) {
        return "BOOLEAN"
    }
    if (unwrappedType?.equalsIgnoreCase("Pattern")) {
        return "STRING"
    }
    return "STRING"
}

def computeDefaultValue(propInfo) {
    def defVal = (propInfo.defaultValuesAsString) ?: ""
    if (defVal == "[]") {
        logWarn("wrong $defVal for $propInfo")
    }
    return defVal
}

def addParamAndTrack(xml, String name, String desc, String defVal, String typeToken, Set existingParamKeys) {
    addParam(xml, name, desc, defVal, typeToken)
    existingParamKeys.add(name)
}

def processStandardProperty(xml, propInfo, Set existingParamKeys) {
    def accepted = getAcceptedValues(propInfo)
    def multiple = isMultiple(propInfo)
    def baseDesc = computeBaseDescription(propInfo, accepted, multiple)
    def unwrappedType = getUnwrappedType(propInfo)
    def typeToken = determineTypeToken(accepted, multiple, unwrappedType)
    def defVal = computeDefaultValue(propInfo)
    // If this is a select list, normalize enum-like default values to lowercase (e.g., ANYWHERE -> anywhere)
    if (typeToken != null && typeToken.startsWith("SINGLE_SELECT_LIST") && defVal instanceof String) {
        defVal = normalizeEnumToken(defVal)
    }
    addParamAndTrack(xml, propInfo.name, baseDesc, defVal, typeToken, existingParamKeys)
}

// Main orchestrator

def addClassDefinedRuleParams(xml, Map ruleData, String language, Set existingParamKeys) {
    def ruleClass = ruleData.class
    if (!ruleClass) return
    def rulePropertiesMap = language == "Java" ? javaRuleProperties : kotlinRuleProperties
    def ruleProperties = getRulePropertiesForClass(rulePropertiesMap, ruleClass)
    if (ruleProperties.size()) {
        logDebug("  - found ${ruleProperties.size()} properties for rule ${ruleData.name} (${ruleClass})")
        ruleProperties.each { propInfo ->
            def propType = propInfo.type
            def unwrappedType = getUnwrappedType(propInfo)
            if (shouldLogProperty(propInfo.name)) {
                logDebug("### PROP: ${propInfo.name} TYPE: ${propType} (wrapped: ${unwrappedType})")
            }
            if (handleSuppressionSpecialCases(xml, propInfo)) {
                return
            }
            processStandardProperty(xml, propInfo, existingParamKeys)
        }
    }
}

def addRuleParams(xml, Map ruleData, String language, Set existingParamKeys) {
    if (ruleData.class.equals("net.sourceforge.pmd.lang.rule.xpath.XPathRule")) {
        addXmlDefinedRuleParams(xml, ruleData, existingParamKeys)
    } else {
        addClassDefinedRuleParams(xml, ruleData, language, existingParamKeys)
    }
}

def addSuppressionParamIfNeeded(xml, boolean hasVariablePlaceholders, Set existingParamKeys) {
    if (hasVariablePlaceholders && !existingParamKeys.contains("violationSuppressRegex")) {
        addParam(xml,
            "violationSuppressRegex",
            "Suppress violations with messages matching a regular expression. WARNING: make sure the regular expression is correct, otherwise analysis will fail with unclear XML validation errors.",
            "",
            "STRING"
        )
    }
}

def reportEmptyDescriptions(File outputFile, String language) {
    def outputXml = new XmlSlurper().parse(outputFile)
    def emptyDescriptions = outputXml.rule.findAll {
        !it.description.text() || it.description.text().trim().isEmpty()
    }
    if (emptyDescriptions.size() > 0) {
        logWarn("found ${emptyDescriptions.size()} ${language} rules with empty descriptions:")
        emptyDescriptions.each { rule ->
            logInfo("  - ${rule.key.text()}")
        }
    } else {
        logInfo("all ${language} rules have descriptions")
    }
}

def handleRenamedRules(String language, int renamedRules, List rulesWithDeprecatedAndRef) {
    logWarn("renamed ${renamedRules} ${language} rules with deprecated=true and ref attribute:")
    rulesWithDeprecatedAndRef.each { rule ->
        logInfo("  - ${rule.name} (ref: ${rule.ref})")
    }
    def renamedRulesData = [
        language: language,
        count: renamedRules,
        rules: rulesWithDeprecatedAndRef.collect { rule -> [name: rule.name, ref: rule.ref, category: rule.category] }
    ]
    def jsonBuilder = new JsonBuilder(renamedRulesData)
    def renamedRulesFile = new File("scripts/renamed-${language.toLowerCase()}-rules.json")
    renamedRulesFile.write(jsonBuilder.toPrettyString())
    logInfo("generated renamed rules information in ${renamedRulesFile.absolutePath}")
}

def addXPathRuleToJavaFile() {
    addXPathRuleToJavaFile(javaOutputFilePath as File)
}

def addXPathRuleToJavaFile(File outFile) {
    logInfo("")
    logInfo("adding XPathRule to Java rules XML file...")
    logInfo("=" * 30)
    try {
        def xmlFile = outFile
        def xmlContent = xmlFile.text
        if (xmlContent.contains("<key>XPathRule</key>")) {
            logInfo("XPathRule already exists in the Java rules XML file.")
        } else {
            def closingTagIndex = xmlContent.lastIndexOf("</rules>")
            if (closingTagIndex != -1) {
                // Load XPathRule XML snippet from file; fail if missing
                def snippetPath = (binding?.hasVariable('xpathRuleSnippetPath') && binding.getVariable('xpathRuleSnippetPath')) ? binding.getVariable('xpathRuleSnippetPath').toString() : "scripts/xpath-rule-snippet.xml"
                def snippetFile = new File(snippetPath)
                if (!snippetFile.exists()) {
                    throw new FileNotFoundException("XPathRule snippet file not found at ${snippetFile.absolutePath}. Provide the file via 'xpathRuleSnippetPath' binding or create scripts/xpath-rule-snippet.xml")
                }
                logInfo("loading XPathRule snippet from ${snippetFile.absolutePath}")
                String xpathRuleXml = snippetFile.getText('UTF-8')
                def newXmlContent = xmlContent.substring(0, closingTagIndex) + xpathRuleXml + xmlContent.substring(closingTagIndex)
                xmlFile.text = newXmlContent
                logInfo("successfully added XPathRule to Java rules XML file.")
            } else {
                logError("could not find closing </rules> tag in Java rules XML file.")
            }
        }
    } catch (Exception e) {
        logError("adding XPathRule to Java rules XML file: ${e.message}")
        throw e
    }
}



// Helper to normalize enum-like tokens to lowercase (e.g., ANYWHERE -> anywhere, ON_TYPE -> on_type)
def normalizeEnumToken(val) {
    if (val == null) return null
    def v = val.toString().trim()
    return (v ==~ /[A-Z0-9_]+/) ? v.toLowerCase() : v
}
