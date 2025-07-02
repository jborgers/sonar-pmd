@Grab('net.sourceforge.pmd:pmd-java:7.15.0')
import groovy.xml.XmlSlurper
import groovy.xml.MarkupBuilder
import java.util.zip.ZipFile
import java.util.regex.Pattern
import java.util.regex.Matcher

// Configuration
def pmdVersion = "7.15.0"
def pmdJarPath = System.getProperty("user.home") + "/.m2/repository/net/sourceforge/pmd/pmd-java/${pmdVersion}/pmd-java-${pmdVersion}.jar"
def categoriesPropertiesPath = "category/java/categories.properties"
def outputFileName = "pmd-7-rules.xml"

println "PMD ${pmdVersion} Rules XML Generator"
println "=" * 50

/**
 * Groovy translation of MdToHtmlConverter
 * Converts Markdown text to HTML format supporting PMD rule documentation patterns
 */
class MdToHtmlConverter {
    // Regex patterns (simplified Groovy version)
    static final Pattern PARAGRAPH_SPLITTER_PATTERN = ~/\n\s*\n/
    static final Pattern ORDERED_LIST_PARAGRAPH_PATTERN = ~/(?s)\s*1\..*/
    static final Pattern UNORDERED_LIST_PARAGRAPH_PATTERN = ~/(?s)\s*\*.*/
    static final Pattern SECTION_PARAGRAPH_PATTERN = ~/(?s)\s*[A-Za-z]+:\s*.*/
    static final Pattern LIST_ITEM_PATTERN = ~/(\d+)\.(\s+)(.*)/
    static final Pattern UNORDERED_LIST_ITEM_PATTERN = ~/\*(\s+)(.*)/
    static final Pattern TITLE_PATTERN = ~/([A-Z][A-Za-z]+):(\s*)(.*)/
    static final Pattern INLINE_TITLE_PATTERN = ~/\b([A-Z][A-Za-z]+):(\s*)/
    static final Pattern CODE_BLOCK_PATTERN = ~/`([^`]+)`/
    static final Pattern RULE_REFERENCE_PATTERN = ~/\{\%\s*rule\s*"([^"]+)"\s*\%\}/
    static final Pattern SECTION_PATTERN = ~/(?s)(Problem|Solution|Note|Notes|Exceptions):(.*?)(?=\s+(Problem|Solution|Note|Notes|Exceptions):|$)/
    static final Pattern MULTI_LINE_CODE_BLOCK_PATTERN = ~/(?s)```(\w*)\s*([\s\S]*?)```/
    static final Pattern QUADRUPLE_BACKTICK_CODE_BLOCK_PATTERN = ~/(?s)````(\w*)\s*([\s\S]*?)````/
    static final Pattern HEADER_PATTERN = ~/^(#{1,6})\s+(.+)$/
    
    // New patterns for additional formatting
    static final Pattern ITALIC_NOTE_PATTERN = ~/_Note:_/
    static final Pattern MARKDOWN_LINK_PATTERN = ~/\[([^\]]+)\]\(([^)]+)\)/
    static final Pattern PMD_RULE_LINK_PATTERN = ~/\[([^\]]+)\]\((pmd_rules_[^.]+\.html[^)]*)\)/

    /**
     * Escapes special regex replacement characters
     */
    static String escapeReplacement(String replacement) {
        return Matcher.quoteReplacement(replacement)
    }

    static String convertToHtml(String markdownText) {
        if (!markdownText || markdownText.trim().isEmpty()) {
            return ""
        }

        String result = markdownText.trim()

        // Handle multi-line code blocks first (both ``` and ````)
        result = handleMultiLineCodeBlocks(result, QUADRUPLE_BACKTICK_CODE_BLOCK_PATTERN)
        result = handleMultiLineCodeBlocks(result, MULTI_LINE_CODE_BLOCK_PATTERN)

        // Handle special patterns before general processing
        result = handleSpecialPatterns(result)

        // Handle sections with special patterns
        result = handleSections(result)

        // Split into paragraphs and process each
        String[] paragraphs = PARAGRAPH_SPLITTER_PATTERN.split(result)
        List<String> htmlParagraphs = []

        paragraphs.each { paragraph ->
            paragraph = paragraph.trim()
            if (!paragraph.isEmpty()) {
                // Check for headers first
                String[] lines = paragraph.split('\n')
                if (lines.length > 0 && HEADER_PATTERN.matcher(lines[0]).matches()) {
                    htmlParagraphs.add(convertHeader(paragraph))
                } else if (ORDERED_LIST_PARAGRAPH_PATTERN.matcher(paragraph).matches()) {
                    htmlParagraphs.add(convertParagraphWithOrderedList(paragraph))
                } else if (UNORDERED_LIST_PARAGRAPH_PATTERN.matcher(paragraph).matches()) {
                    htmlParagraphs.add(convertUnorderedList(paragraph))
                } else if (SECTION_PARAGRAPH_PATTERN.matcher(paragraph).matches()) {
                    htmlParagraphs.add(convertSection(paragraph))
                } else {
                    htmlParagraphs.add("<p>${formatInlineElements(paragraph)}</p>")
                }
            }
        }

        return htmlParagraphs.join("")
    }

    private static String convertHeader(String headerText) {
        String[] lines = headerText.split('\n')
        StringBuilder result = new StringBuilder()
        
        lines.each { line ->
            def matcher = HEADER_PATTERN.matcher(line.trim())
            if (matcher.matches()) {
                String hashes = matcher.group(1)
                String content = matcher.group(2)
                int level = hashes.length()
                result.append("<h${level}>${formatInlineElements(content)}</h${level}>")
            } else {
                // Handle continuation lines as regular paragraph content
                if (line.trim()) {
                    result.append("<p>${formatInlineElements(line)}</p>")
                }
            }
        }
        
        return result.toString()
    }

    private static String handleSpecialPatterns(String text) {
        String result = text
        
        // Handle _Note:_ pattern
        result = ITALIC_NOTE_PATTERN.matcher(result).replaceAll(escapeReplacement('<b>Note:</b>'))
        
        // Handle PMD rule links first (more specific)
        result = PMD_RULE_LINK_PATTERN.matcher(result).replaceAll { match ->
            String linkText = match.group(1)
            String href = match.group(2)
            String fullUrl = "https://pmd.github.io/pmd/${href}".toString()
            String replacement = "<a href=\"${fullUrl}\">${linkText}</a>".toString()
            return escapeReplacement(replacement)
        }
        
        // Handle general markdown links
        result = MARKDOWN_LINK_PATTERN.matcher(result).replaceAll { match ->
            String linkText = match.group(1)
            String href = match.group(2)
            String replacement = "<a href=\"${href}\">${linkText}</a>".toString()
            return escapeReplacement(replacement)
        }
        
        return result
    }

    private static String handleMultiLineCodeBlocks(String markdownText, Pattern pattern) {
        return pattern.matcher(markdownText).replaceAll { match ->
            String language = match.group(1) ?: ""
            String code = match.group(2)?.trim() ?: ""
            String langClass = language ? " class=\"language-${language}\"" : ""
            String replacement = "<pre><code${langClass}>${escapeHtml(code)}</code></pre>".toString()
            return escapeReplacement(replacement)
        }
    }

    private static String handleSections(String text) {
        return SECTION_PATTERN.matcher(text).replaceAll { match ->
            String sectionType = match.group(1)
            String content = match.group(2)?.trim() ?: ""
            String replacement = "<p><b>${sectionType}:</b> ${formatInlineElements(content)}</p>".toString()
            return escapeReplacement(replacement)
        }
    }

    private static String convertParagraphWithOrderedList(String paragraph) {
        String[] lines = paragraph.split('\n')
        StringBuilder result = new StringBuilder()
        boolean inList = false

        lines.each { line ->
            line = line.trim()
            if (LIST_ITEM_PATTERN.matcher(line).matches()) {
                if (!inList) {
                    result.append("<ol>")
                    inList = true
                }
                def matcher = LIST_ITEM_PATTERN.matcher(line)
                if (matcher.find()) {
                    result.append("<li>${formatInlineElements(matcher.group(3))}</li>")
                }
            } else if (line && inList) {
                // Continuation of previous list item - add space but no line break
                result.append(" ${formatInlineElements(line)}")
            }
        }

        if (inList) {
            result.append("</ol>")
        }

        return result.toString()
    }

    private static String convertUnorderedList(String listText) {
        String[] lines = listText.split('\n')
        StringBuilder result = new StringBuilder()
        boolean inList = false

        lines.each { line ->
            line = line.trim()
            if (UNORDERED_LIST_ITEM_PATTERN.matcher(line).matches()) {
                if (!inList) {
                    result.append("<ul>")
                    inList = true
                }
                def matcher = UNORDERED_LIST_ITEM_PATTERN.matcher(line)
                if (matcher.find()) {
                    result.append("<li>${formatInlineElements(matcher.group(2))}</li>")
                }
            } else if (line && inList) {
                // Continuation of previous list item - add space but no line break
                result.append(" ${formatInlineElements(line)}")
            }
        }

        if (inList) {
            result.append("</ul>")
        }

        return result.toString()
    }

    private static String convertSection(String sectionText) {
        def matcher = TITLE_PATTERN.matcher(sectionText)
        if (matcher.find()) {
            String title = matcher.group(1)
            String content = matcher.group(3)?.trim() ?: ""
            return "<p><b>${title}:</b> ${formatInlineElements(content)}</p>".toString()
        }
        return "<p>${formatInlineElements(sectionText)}</p>".toString()
    }

    private static String formatInlineElements(String text) {
        if (!text) return ""

        String result = text
        
        // Format inline code blocks
        result = CODE_BLOCK_PATTERN.matcher(result).replaceAll(escapeReplacement('<code>') + '$1' + escapeReplacement('</code>'))
        
        // Format rule references
        result = RULE_REFERENCE_PATTERN.matcher(result).replaceAll(escapeReplacement('<code>') + '$1' + escapeReplacement('</code>'))
        
        // Format inline titles (like "Problem:" in the middle of text)
        result = INLINE_TITLE_PATTERN.matcher(result).replaceAll(escapeReplacement('<b>') + '$1' + escapeReplacement(':</b>') + '$2')
        
        // Basic markdown formatting
        result = result.replaceAll(/\*\*([^*]+)\*\*/, escapeReplacement('<b>') + '$1' + escapeReplacement('</b>'))  // Bold
        result = result.replaceAll(/\*([^*]+)\*/, escapeReplacement('<i>') + '$1' + escapeReplacement('</i>'))      // Italic
        
        // DON'T convert all newlines to <br/> - only paragraph breaks are handled by the paragraph splitter

        return result
    }

    private static String escapeHtml(String text) {
        if (!text) return ""
        return text
            .replace('&', '&amp;')
            .replace('<', '&lt;')
            .replace('>', '&gt;')
            .replace('"', '&quot;')
            .replace("'", '&#39;')
    }
}

// Helper function to convert camelCase rule name to readable format with only first letter uppercase
def camelCaseToReadable = { ruleName ->
    def result = ruleName.replaceAll(/([A-Z])/, ' $1').trim()
    // Capitalize only the first letter and make the rest lowercase
    return result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase()
}

// We no longer need to check for replacement placeholders since we're using camelCase for all rules

// Read PMD JAR and extract rule categories
def jarFile = new File(pmdJarPath)
if (!jarFile.exists()) {
    println "ERROR: PMD JAR not found at: ${pmdJarPath}"
    return
}

def allRules = []
def categoryFiles = []

try {
    def zipFile = new ZipFile(jarFile)
    
    // First, read the categories.properties to get the list of rule files
    def categoriesEntry = zipFile.getEntry(categoriesPropertiesPath)
    if (categoriesEntry) {
        def categoriesProps = new Properties()
        categoriesProps.load(zipFile.getInputStream(categoriesEntry))
        
        def rulesetFilenames = categoriesProps.getProperty("rulesets.filenames", "")
        categoryFiles = rulesetFilenames.split(",").collect { it.trim() }
        
        println "Found ${categoryFiles.size()} category files in PMD JAR"
    } else {
        println "WARNING: categories.properties not found in PMD JAR"
    }
    
    // Parse each category XML file to extract detailed rule information
    categoryFiles.each { categoryFile ->
        def entry = zipFile.getEntry(categoryFile)
        if (entry) {
            try {
                def categoryXml = new XmlSlurper().parse(zipFile.getInputStream(entry))
                def categoryName = categoryFile.tokenize('/').last().replace('.xml', '')
                
                categoryXml.rule.each { ruleElement ->
                    def ruleName = ruleElement.@name.toString()
                    def ruleClass = ruleElement.@class.toString()
                    def deprecated = ruleElement.@deprecated.toString() == "true"
                    def ref = ruleElement.@ref.toString()
                    def since = ruleElement.@since.toString()
                    def externalInfoUrl = ruleElement.@externalInfoUrl.toString()
                    def message = ruleElement.@message.toString()
                    
                    // Extract description
                    def description = ruleElement.description.text()
                    
                    // Extract priority
                    def priority = ruleElement.priority.text() ?: "3"
                    
                    // Extract examples
                    def examples = []
                    ruleElement.example.each { example ->
                        examples << example.text()
                    }
                    
                    // Extract properties
                    def properties = []
                    ruleElement.properties.property.each { prop ->
                        properties << [
                            name: prop.@name.toString(),
                            description: prop.@description.toString(),
                            type: prop.@type.toString(),
                            value: prop.@value.toString(),
                            min: prop.@min.toString(),
                            max: prop.@max.toString()
                        ]
                    }
                    
                    if (ruleName) {
                        allRules << [
                            name: ruleName,
                            category: categoryName,
                            categoryFile: categoryFile,
                            class: ruleClass,
                            deprecated: deprecated,
                            ref: ref,
                            since: since,
                            externalInfoUrl: externalInfoUrl,
                            message: message ?: ruleName, // Use rule name as fallback for message
                            description: description,
                            priority: priority,
                            examples: examples,
                            properties: properties
                        ]
                    }
                }
                println "  - Processed ${categoryFile}: found ${categoryXml.rule.size()} rules"
            } catch (Exception e) {
                println "  - ERROR processing ${categoryFile}: ${e.message}"
            }
        } else {
            println "  - WARNING: Category file not found: ${categoryFile}"
        }
    }
    
    zipFile.close()
} catch (Exception e) {
    println "ERROR reading PMD JAR: ${e.message}"
    return
}

println "Found ${allRules.size()} total rules"
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

// Helper function to generate a fallback description based on rule name and category
def generateFallbackDescription = { ruleName, category ->
    def categoryDescriptions = [
        'bestpractices': 'coding best practices',
        'codestyle': 'code style and formatting',
        'design': 'design and architecture',
        'documentation': 'documentation standards',
        'errorprone': 'error-prone constructs',
        'multithreading': 'multithreading and concurrency',
        'performance': 'performance optimization',
        'security': 'security vulnerabilities'
    ]
    
    def categoryDesc = categoryDescriptions[category] ?: 'code quality'
    
    // Convert camelCase rule name to readable format
    def readableName = ruleName.replaceAll(/([A-Z])/, ' $1').trim()
    
    return """Problem: This rule identifies issues related to ${categoryDesc}.

Solution: Review the flagged code and apply the recommended practices to improve code quality.

The rule "${readableName}" helps maintain better code standards in the ${category} category."""
}

// Helper function to format description with examples using MdToHtmlConverter
def formatDescription = { ruleData ->
    def description = ruleData.description ?: ""
    def examples = ruleData.examples ?: []
    def externalInfoUrl = ruleData.externalInfoUrl ?: ""
    
    // If no description exists, generate a fallback
    if (!description || description.trim().isEmpty()) {
        description = generateFallbackDescription(ruleData.name, ruleData.category)
    }
    
    // Build markdown content
    def markdownContent = new StringBuilder()
    
    // Add the main description
    markdownContent.append(description)
    
    // Add examples section if available
    if (examples && !examples.isEmpty()) {
        markdownContent.append("\n\n## Example\n\n")
        examples.eachWithIndex { example, index ->
            if (examples.size() > 1) {
                markdownContent.append("### Example ${index + 1}\n\n")
            }
            markdownContent.append("```java\n")
            markdownContent.append(example)
            markdownContent.append("\n```\n\n")
        }
    }
    
    // Add external info URL if available
    if (externalInfoUrl) {
        def linkText = externalInfoUrl.tokenize('/').last()
        markdownContent.append("\n\n**More information:** [${linkText}](${externalInfoUrl})")
    }
    
    // Convert markdown to HTML using our Groovy MdToHtmlConverter
    return MdToHtmlConverter.convertToHtml(markdownContent.toString())
}

// Generate the XML file
try {
    def outputFile = new File(outputFileName)
    def rulesWithoutDescription = 0
    
    outputFile.withWriter('UTF-8') { writer ->
        def xml = new MarkupBuilder(writer)
        xml.setDoubleQuotes(true)
        
        // Write XML declaration manually since MarkupBuilder doesn't handle it well
        writer.println('<?xml version="1.0" encoding="UTF-8"?>')
        
        xml.rules {
            allRules.sort { it.name }.each { ruleData ->
                rule {
                    key(ruleData.name)
                    
                    // Always use camelCase transformation for rule names
                    def readableName = camelCaseToReadable(ruleData.name)
                    name(readableName)
                    
                    internalKey("${ruleData.categoryFile}/${ruleData.name}")
                    severity(priorityToSeverity(ruleData.priority))
                    
                    // Add description with CDATA - ensure it's never empty
                    description {
                        def descContent = formatDescription(ruleData)
                        if (!descContent || descContent.trim().isEmpty()) {
                            descContent = MdToHtmlConverter.convertToHtml(generateFallbackDescription(ruleData.name, ruleData.category))
                            rulesWithoutDescription++
                        }
                        mkp.yieldUnescaped("<![CDATA[${escapeForCdata(descContent)}]]>")
                    }
                    
                    // Add status if deprecated
                    if (ruleData.deprecated) {
                        status("DEPRECATED")
                    }
                    
                    // Add tags - always include "pmd" tag first, then category tag
                    tag("pmd")
                    tag(ruleData.category)
                    
                    // Add parameters from properties
                    ruleData.properties.each { prop ->
                        if (prop.name && prop.description && !prop.name.startsWith("violation")) {
                            param {
                                key(prop.name)
                                description {
                                    mkp.yieldUnescaped("<![CDATA[${escapeForCdata(prop.description)}]]>")
                                }
                                if (prop.value) {
                                    defaultValue(prop.value)
                                }
                                if (prop.type) {
                                    type(prop.type.toUpperCase())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    println "Successfully generated ${outputFileName}"
    println "Total rules: ${allRules.size()}"
    println "Active rules: ${allRules.count { !it.deprecated }}"
    println "Deprecated rules: ${allRules.count { it.deprecated }}"
    if (rulesWithoutDescription > 0) {
        println "Rules with generated fallback descriptions: ${rulesWithoutDescription}"
    }
    println "Using camelCase transformation for all rule names"
    
    // Show category breakdown
    def categoryStats = allRules.groupBy { it.category }
    println ""
    println "Rules by category:"
    categoryStats.sort { it.key }.each { category, rules ->
        def activeCount = rules.count { !it.deprecated }
        def deprecatedCount = rules.count { it.deprecated }
        println "  - ${category}: ${rules.size()} total (${activeCount} active, ${deprecatedCount} deprecated)"
    }
    
    // Show tag distribution
    println ""
    println "Tags that will be applied:"
    println "  - pmd: ${allRules.size()} rules"
    categoryStats.sort { it.key }.each { category, rules ->
        println "  - ${category}: ${rules.size()} rules"
    }
    
    // We're now using camelCase transformation for all rule names
    
    // We're not using properties for names anymore, so no need to display missing entries
    
    // Check for any rules that might still have empty descriptions (shouldn't happen now)
    def outputXml = new XmlSlurper().parse(outputFile)
    def emptyDescriptions = outputXml.rule.findAll { 
        !it.description.text() || it.description.text().trim().isEmpty() 
    }
    
    if (emptyDescriptions.size() > 0) {
        println ""
        println "WARNING: Found ${emptyDescriptions.size()} rules with empty descriptions:"
        emptyDescriptions.each { rule ->
            println "  - ${rule.key.text()}"
        }
    } else {
        println ""
        println "✓ All rules have descriptions"
    }
    
} catch (Exception e) {
    println "ERROR generating XML file: ${e.message}"
    e.printStackTrace()
}

println ""
println "XML generation completed!"