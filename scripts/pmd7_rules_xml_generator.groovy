@Grab('net.sourceforge.pmd:pmd-java:7.15.0')
@Grab('net.sourceforge.pmd:pmd-kotlin:7.15.0')
@Grab('org.sonarsource.pmd:sonar-pmd-lib:4.1.0-SNAPSHOT')
import groovy.xml.XmlSlurper
import groovy.xml.MarkupBuilder
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.util.zip.ZipFile
import java.util.regex.Pattern
import java.util.regex.Matcher
import org.sonar.plugins.pmd.rule.JavaRulePropertyExtractor
import org.sonar.plugins.pmd.rule.MarkdownToHtmlConverter

// Configuration
def pmdVersion = MarkdownToHtmlConverter.PMD_VERSION
def pmdJavaJarPath = "${System.getProperty("user.home")}/.m2/repository/net/sourceforge/pmd/pmd-java/${pmdVersion}/pmd-java-${pmdVersion}.jar"
def pmdKotlinJarPath = "${System.getProperty("user.home")}/.m2/repository/net/sourceforge/pmd/pmd-kotlin/${pmdVersion}/pmd-kotlin-${pmdVersion}.jar"
def javaCategoriesPropertiesPath = "category/java/categories.properties"
def kotlinCategoriesPropertiesPath = "category/kotlin/categories.properties"
// Define language-specific rule alternatives paths
def javaRuleAlternativesPath = "scripts/rule-alternatives-java.json"
def kotlinRuleAlternativesPath = "scripts/rule-alternatives-kotlin.json"

// Extract Java rule properties from the jar file
println "Extracting Java rule properties from ${pmdJavaJarPath}"
def javaRulePropertyExtractor = new JavaRulePropertyExtractor()
def javaRuleProperties = javaRulePropertyExtractor.extractProperties(pmdJavaJarPath)
println "Found properties for ${javaRuleProperties.size()} Java rule classes"

// Extract Kotlin rule properties from the jar file
println "Extracting Kotlin rule properties from ${pmdKotlinJarPath}"
def kotlinRulePropertyExtractor = new JavaRulePropertyExtractor()
def kotlinRuleProperties = kotlinRulePropertyExtractor.extractProperties(pmdKotlinJarPath)
println "Found properties for ${kotlinRuleProperties.size()} Kotlin rule classes"

// Function to read rule alternatives from a JSON file
def readRuleAlternatives = { filePath ->
    def alternatives = [:]
    try {
        def alternativesFile = new File(filePath)
        if (alternativesFile.exists()) {
            def jsonSlurper = new JsonSlurper()
            def alternativesData = jsonSlurper.parse(alternativesFile)
            alternatives = alternativesData.ruleAlternatives
            println "Loaded ${alternatives.size()} rule alternatives from ${filePath}"
        } else {
            println "WARNING: Rule alternatives file not found at ${filePath}"
        }
    } catch (Exception e) {
        println "ERROR reading rule alternatives: ${e.message}"
    }
    return alternatives
}

// Read Java rule alternatives
def javaRuleAlternatives = readRuleAlternatives(javaRuleAlternativesPath)

// Read Kotlin rule alternatives (for future use)
def kotlinRuleAlternatives = readRuleAlternatives(kotlinRuleAlternativesPath)

// If we're in test mode, make the MarkdownToHtmlConverter available but don't run the main code
if (binding.hasVariable('TEST_MODE') && binding.getVariable('TEST_MODE')) {
    // Make MarkdownToHtmlConverter available to the caller
    binding.setVariable('MarkdownToHtmlConverter', MarkdownToHtmlConverter)
    return // Skip the rest of the script
}

// Get output directory from binding variable (set by Maven) or use a default directory
// The 'outputDir' variable is passed from Maven's groovy-maven-plugin configuration
def defaultOutputDir = new File("sonar-pmd-plugin/src/main/resources/org/sonar/plugins/pmd").exists() ? 
    "sonar-pmd-plugin/src/main/resources/org/sonar/plugins/pmd" : "."
def outputDirPath = binding.hasVariable('outputDir') ? outputDir : defaultOutputDir
def javaOutputFileName = "rules-java.xml"
def kotlinOutputFileName = "rules-kotlin.xml"
def javaOutputFilePath = new File(outputDirPath, javaOutputFileName)
def kotlinOutputFilePath = new File(outputDirPath, kotlinOutputFileName)

println "PMD ${pmdVersion} Rules XML Generator"
println "=" * 50
println "Java output file: ${javaOutputFilePath}"
println "Kotlin output file: ${kotlinOutputFilePath}"

// The MdToHtmlConverter class has been moved to sonar-pmd-lib as MarkdownToHtmlConverter

// The camelCaseToReadable function has been moved to sonar-pmd-lib as MarkdownToHtmlConverter.camelCaseToReadable

// We no longer need to check for replacement placeholders since we're using camelCase for all rules

// Function to read rules from a PMD JAR
def readRulesFromJar = { jarPath, categoriesPath ->
    def jarFile = new File(jarPath)
    if (!jarFile.exists()) {
        println "ERROR: PMD JAR not found at: ${jarPath}"
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
            println "Found ${categoryFiles.size()} category files in PMD JAR: ${jarPath}"
        } else {
            println "WARNING: ${categoriesPath} not found in PMD JAR: ${jarPath}"
        }

        // Process each category file
        categoryFiles.each { categoryFile ->
            def entry = zipFile.getEntry(categoryFile)
            if (!entry) {
                println "  - WARNING: Category file not found: ${categoryFile}"
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
                println "  - Processed ${categoryFile}: found ${categoryXml.rule.size()} rules"
            } catch (Exception e) {
                println "  - ERROR processing ${categoryFile}: ${e.message}"
            }
        }

        zipFile.close()
        return rules
    } catch (Exception e) {
        println "ERROR reading PMD JAR: ${e.message}"
        return []
    }
}

// Read Java rules
println "Reading Java rules from ${pmdJavaJarPath}"
def javaRules = readRulesFromJar(pmdJavaJarPath, javaCategoriesPropertiesPath)
println "Found ${javaRules.size()} total Java rules"
println ""

// Read Kotlin rules
println "Reading Kotlin rules from ${pmdKotlinJarPath}"
def kotlinRules = readRulesFromJar(pmdKotlinJarPath, kotlinCategoriesPropertiesPath)
println "Found ${kotlinRules.size()} total Kotlin rules"
println ""

// Helper function to convert priority to severity
def priorityToSeverity = { priority ->
    switch (priority) {
        case "1": return "BLOCKER"
        case "2": return "CRITICAL"
        case "3": return "MAJOR"
        case "4": return "MINOR"
        case "5": return "INFO"
        default: return "MAJOR"
    }
}

// Helper function to escape XML content for CDATA
def escapeForCdata = { text ->
    if (!text) return ""
    return text.replaceAll(/\]\]>/, "]]]]><![CDATA[>")
}


// Helper function to format description with examples using MdToHtmlConverter
def formatDescription = { ruleData, language ->
    def description = ruleData.description ?: ""
    def examples = ruleData.examples ?: []
    def externalInfoUrl = ruleData.externalInfoUrl ?: ""
    def message = ruleData.message ?: ""
    def ruleName = ruleData.name

    // If no description exists, log warning, do not add rule
    if (!description || description.trim().isEmpty()) {
        // report with println and skip processing
    }

    // Build markdown content
    def markdownContent = new StringBuilder()

    // Add the message as a title at the top of the description
    if (message && !message.trim().isEmpty()) {
        // Process message: replace placeholders with code tags and fix double quotes
        def processedMessage = message
            .replaceAll(/\{(\d+)\}/, '<code>{$1}</code>')  // Wrap {0}, {1}, etc. in code tags
            .replaceAll(/''/,"\'")  // Replace two subsequent single quotes with a single single quote

        markdownContent.append("## Title of issues: ").append(processedMessage).append("\n\n")
    }

    // Add the main description
    markdownContent.append(description)

    // Add examples section if available
    if (examples && !examples.isEmpty()) {
        markdownContent.append(examples.size() > 1 ? "\n\n## Examples\n\n" : "\n\n## Example\n\n")
        examples.eachWithIndex { example, index ->
            if (examples.size() > 1) {
                markdownContent.append("### Example ${index + 1}\n\n")
            }
            // Ensure the code example is properly formatted with proper line breaks
            // and no paragraph tags inside code blocks
            markdownContent.append("```java\n")
            markdownContent.append(example)
            markdownContent.append("\n```\n\n")
        }
    }

    // Convert markdown to HTML using MarkdownToHtmlConverter
    def htmlContent = MarkdownToHtmlConverter.convertToHtml(markdownContent.toString())

    // Add Sonar alternative rules if available, based on language
    def ruleAlternativesForLanguage = language == "Java" ? javaRuleAlternatives : kotlinRuleAlternatives
    if (ruleAlternativesForLanguage && ruleAlternativesForLanguage.containsKey(ruleName)) {
        def alternatives = ruleAlternativesForLanguage[ruleName]
        if (alternatives && !alternatives.isEmpty()) {
            def alternativesHtml = new StringBuilder("<p><b>Alternative " + (alternatives.size() == 1 ? "rule" : "rules") + ":</b> ")
            alternatives.eachWithIndex { alt, index ->
                if (index > 0) {
                    alternativesHtml.append(", ")
                }

                def internalLink = "./coding_rules?rule_key=${URLEncoder.encode(alt.key, 'UTF-8')}&open=${URLEncoder.encode(alt.key, 'UTF-8')}"
                alternativesHtml.append("<a href=\"${internalLink}\">${alt.key}</a>")
            }
            alternativesHtml.append("</p>")
            htmlContent += "\n" + alternativesHtml.toString()
        }
    }

    // Add external info URL as the last paragraph if available
    if (externalInfoUrl) {
        // Same a in IntelliJ PMD Plugin
        def linkText = "Full documentation"
        htmlContent += "\n<p><a href=\"${externalInfoUrl}\">${linkText}</a></p>"
    }

    return htmlContent
}

// Function to generate XML file
def generateXmlFile = { outputFile, rules, language ->
    def rulesWithoutDescription = 0
    def skippedRules = 0
    def rulesWithDeprecatedAndRef = []

    try {
        // Generate XML file
        outputFile.withWriter('UTF-8') { writer ->
            def xml = new MarkupBuilder(writer)
            xml.setDoubleQuotes(true)

            // Write XML declaration manually
            writer.println('<?xml version="1.0" encoding="UTF-8"?>')

            xml.rules {
                rules.sort { it.name }.each { ruleData ->
                    // Skip rules with deprecated=true and ref attribute
                    if (ruleData.deprecated && ruleData.ref) {
                        skippedRules++
                        rulesWithDeprecatedAndRef << ruleData
                        return // Skip this rule
                    }

                    rule {
                        key(ruleData.name)
                        name(MarkdownToHtmlConverter.camelCaseToReadable(ruleData.name))
                        internalKey("${ruleData.categoryFile}/${ruleData.name}")
                        severity(priorityToSeverity(ruleData.priority))

                        // Add description with CDATA
                        description {
                            def descContent = formatDescription(ruleData, language)
                            if (!descContent || descContent.trim().isEmpty()) {
                                descContent = MarkdownToHtmlConverter.convertToHtml("THIS SHOULD NOT HAPPEN")
                                rulesWithoutDescription++
                            }
                            mkp.yieldUnescaped("<![CDATA[${escapeForCdata(descContent)}]]>")
                        }

                        // Add status if deprecated
                        if (ruleData.deprecated) {
                            status("DEPRECATED")
                        }

                        // Add tags
                        tag("pmd")
                        tag(ruleData.category)

                        // Add has-sonar-alternative tag if the rule has alternatives
                        def ruleAlternativesForLanguage = language == "Java" ? javaRuleAlternatives : kotlinRuleAlternatives
                        if (ruleAlternativesForLanguage && ruleAlternativesForLanguage.containsKey(ruleData.name)) {
                            tag("has-sonar-alternative")
                        }

                        // Add parameters from XML rule definition
                        if (ruleData.class.equals("net.sourceforge.pmd.lang.rule.xpath.XPathRule")) {
                                ruleData.properties.findAll { prop ->
                                    prop.name && prop.description
                            }.each { prop ->
                                param {
                                    key(prop.name)
                                    description {
                                        mkp.yieldUnescaped("<![CDATA[${escapeForCdata(prop.description)}]]>")
                                    }
                                    if (prop.value) defaultValue(prop.value)
                                    if (prop.type) {
                                        // Map LIST[STRING] and REGEX to STRING
                                        if (prop.type.toUpperCase() == "LIST[STRING]" || prop.type.toUpperCase() == "REGEX") {
                                            type("STRING")
                                        } else {
                                            type(prop.type.toUpperCase())
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            // Add parameters from Java rule classes
                            def ruleClass = ruleData.class

                            if (ruleClass) {
                                def rulePropertiesMap = language == "Java" ? javaRuleProperties : kotlinRuleProperties
                                def ruleProperties = rulePropertiesMap.get(ruleClass)
                                if (ruleProperties.size()) {
                                    println "  - Found ${ruleProperties.size()} properties for rule ${ruleData.name} (${ruleClass})"
                                    ruleProperties.each { propInfo ->
                                        // Check if this property is already defined in the XML
                                        def existingProp = ruleData.properties.find { it.name == propInfo.name }
                                        if (!existingProp) {
                                            param {
                                                key(propInfo.name)
                                                description {
                                                    mkp.yieldUnescaped("<![CDATA[${escapeForCdata(propInfo.description)}]]>")
                                                }

                                                def defVal = propInfo.defaultValuesAsString
                                                if (defVal == "[]") {
                                                    println("WRONG $defVal for $propInfo")
                                                }
                                                defaultValue(defVal)
                                                def propType = propInfo.type
                                                println "### TYPE: $propType"
                                                if (propType == "Integer") {
                                                    type("INTEGER")
                                                } else if (propType == "Boolean") {
                                                    type("BOOLEAN")
                                                } else if (propType == "Double") {
                                                    type("FLOAT")
                                                } else {
                                                    type("STRING")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Print summary information
        def activeRules = rules.count { !it.deprecated }
        def deprecatedRules = rules.count { it.deprecated }
        def categoryStats = rules.groupBy { it.category }

        println """
Successfully generated ${outputFile.name}
Total ${language} rules: ${rules.size()}
Active ${language} rules: ${activeRules}
Deprecated ${language} rules: ${deprecatedRules}
${skippedRules > 0 ? "Skipped ${language} rules (deprecated with ref): ${skippedRules}" : ""}
${rulesWithoutDescription > 0 ? "${language} rules with generated fallback descriptions: ${rulesWithoutDescription}" : ""}
Using camelCase transformation for all rule names

${language} rules by category:"""

        categoryStats.sort { it.key }.each { category, categoryRules ->
            def activeCount = categoryRules.count { !it.deprecated }
            def deprecatedCount = categoryRules.count { it.deprecated }
            println "  - ${category}: ${categoryRules.size()} total (${activeCount} active, ${deprecatedCount} deprecated)"
        }

        println "\nTags that will be applied for ${language} rules:"
        println "  - pmd: ${rules.size()} rules"
        categoryStats.sort { it.key }.each { category, categoryRules ->
            println "  - ${category}: ${categoryRules.size()} rules"
        }

        // Verify no empty descriptions
        def outputXml = new XmlSlurper().parse(outputFile)
        def emptyDescriptions = outputXml.rule.findAll { 
            !it.description.text() || it.description.text().trim().isEmpty() 
        }

        if (emptyDescriptions.size() > 0) {
            println "\nWARNING: Found ${emptyDescriptions.size()} ${language} rules with empty descriptions:"
            emptyDescriptions.each { rule ->
                println "  - ${rule.key.text()}"
            }
        } else {
            println "\n✓ All ${language} rules have descriptions"
        }

        // Print warnings for skipped rules with deprecated=true and ref attribute
        if (skippedRules > 0) {
            println "\nWARNING: Skipped ${skippedRules} ${language} rules with deprecated=true and ref attribute:"
            rulesWithDeprecatedAndRef.each { rule ->
                println "  - ${rule.name} (ref: ${rule.ref})"
            }

            // Generate JSON file with skipped rules information
            def skippedRulesData = [
                language: language,
                count: skippedRules,
                rules: rulesWithDeprecatedAndRef.collect { rule ->
                    [
                        name: rule.name,
                        ref: rule.ref,
                        category: rule.category,
                        categoryFile: rule.categoryFile,
                        since: rule.since,
                        message: rule.message
                    ]
                }
            ]

            def jsonBuilder = new JsonBuilder(skippedRulesData)
            def skippedRulesFile = new File(outputFile.getParentFile(), "skipped-${language.toLowerCase()}-rules.json")
            skippedRulesFile.write(jsonBuilder.toPrettyString())
            println "Generated skipped rules information in ${skippedRulesFile.absolutePath}"
        }

        return true
    } catch (Exception e) {
        println "ERROR generating ${language} XML file: ${e.message}"
        e.printStackTrace()
        return false
    }
}

// Generate Java rules XML file
println ""
println "Generating Java rules XML file..."
println "=" * 30
def javaSuccess = generateXmlFile(javaOutputFilePath, javaRules, "Java")

// Generate Kotlin rules XML file
println ""
println "Generating Kotlin rules XML file..."
println "=" * 30
def kotlinSuccess = generateXmlFile(kotlinOutputFilePath, kotlinRules, "Kotlin")

println ""
if (javaSuccess && kotlinSuccess) {
    println "XML generation completed successfully for both Java and Kotlin rules!"
} else {
    println "XML generation completed with errors. Please check the logs above."
}
