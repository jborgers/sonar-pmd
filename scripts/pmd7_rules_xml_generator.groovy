@Grab('net.sourceforge.pmd:pmd-java:7.15.0')
@Grab('net.sourceforge.pmd:pmd-kotlin:7.15.0')
import groovy.xml.XmlSlurper
import groovy.xml.MarkupBuilder
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.util.zip.ZipFile
import java.util.regex.Pattern
import java.util.regex.Matcher
import org.sonar.plugins.pmd.rule.JavaRulePropertyExtractor

// Configuration
def pmdVersion = MdToHtmlConverter.PMD_VERSION
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

// If we're in test mode, make the MdToHtmlConverter available but don't run the main code
if (binding.hasVariable('TEST_MODE') && binding.getVariable('TEST_MODE')) {
    // Make MdToHtmlConverter available to the caller
    binding.setVariable('MdToHtmlConverter', MdToHtmlConverter)
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

/**
 * Groovy translation of MdToHtmlConverter
 * Converts Markdown text to HTML format supporting PMD rule documentation patterns
 */
class MdToHtmlConverter {
    static final PMD_VERSION = "7.15.0"

    // Regex patterns (simplified Groovy version)
    // Simple paragraph splitter - we use extractPreBlocks and restorePreBlocks to handle <pre> tags
    static final Pattern PARAGRAPH_SPLITTER_PATTERN = ~/\n\s*\n/
    static final Pattern ORDERED_LIST_PARAGRAPH_PATTERN = ~/(?s)\s*1\...*/
    static final Pattern LIST_ITEM_PATTERN = ~/(\d+)\.(\s+)(.*)/
    static final Pattern UNORDERED_LIST_ITEM_PATTERN = ~/[\s\t]*[\*\-](\s+)(.*)/
    static final Pattern LIST_ITEM_CONTINUATION_PATTERN = ~/^[\s\t]{2,}([^\*\-].+)$/
    static final Pattern TITLE_PATTERN = ~/([A-Z][A-Za-z]+):(\s*)(.*)/
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
    static final Pattern URL_TAG_PATTERN = ~/<(https?:\/\/[^>]+)>/
    // {% jdoc java::lang.java.metrics.JavaMetrics#WEIGHED_METHOD_COUNT %}
    static final Pattern JDOC_REFERENCE_PATTERN = ~/\{\%\s*jdoc\s+([\w-]+)::([\w.#]+)\s*\%\}/
    // example: https://docs.pmd-code.org/apidocs/pmd-java/7.15.0/net/sourceforge/pmd/lang/java/metrics/JavaMetrics.html#WEIGHED_METHOD_COUNT
    static final String jdocLink = "https://docs.pmd-code.org/apidocs/pmd-java/${PMD_VERSION}/net/sourceforge/pmd/"

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

        // Extract and preserve all <pre> blocks before any processing
        // We'll restore them at the very end
        List<String> preBlocks = new ArrayList<>()
        result = extractPreBlocks(result, preBlocks)

        // Replace any remaining <pre> tags with special markers that won't be processed
        result = result.replace("<pre>", "PRE_TAG_START")
        result = result.replace("</pre>", "PRE_TAG_END")

        // Now split into paragraphs
        String[] paragraphs = PARAGRAPH_SPLITTER_PATTERN.split(result)
        List<String> htmlParagraphs = []

        // First pass: identify consecutive list items and convert them directly
        List<String> processedParagraphs = new ArrayList<>()
        List<String> currentListItems = new ArrayList<>()
        boolean inList = false
        String currentParagraphText = null

        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim()
            if (!paragraph.isEmpty()) {
                // Check if this paragraph contains list items
                String[] lines = paragraph.split('\n')

                // Check if the paragraph starts with text and then has list items
                boolean startsWithText = false
                if (lines.length > 0 && !UNORDERED_LIST_ITEM_PATTERN.matcher(lines[0]).matches()) {
                    startsWithText = true;
                }

                // Count how many lines are list items
                int listItemCount = 0
                for (String line : lines) {
                    if (UNORDERED_LIST_ITEM_PATTERN.matcher(line).matches()) {
                        listItemCount++
                    }
                }

                // If the paragraph starts with text and then has list items, split it
                if (startsWithText && listItemCount > 0) {
                    // Add the text part as a regular paragraph
                    StringBuilder textPart = new StringBuilder();
                    for (int j = 0; j < lines.length; j++) {
                        if (!UNORDERED_LIST_ITEM_PATTERN.matcher(lines[j]).matches()) {
                            if (textPart.length() > 0) {
                                textPart.append(" ");
                            }
                            textPart.append(lines[j].trim());
                        } else {
                            break;
                        }
                    }
                    currentParagraphText = textPart.toString();
                    processedParagraphs.add(currentParagraphText);

                    // Process the list items separately
                    StringBuilder listPart = new StringBuilder();
                    for (int j = 0; j < lines.length; j++) {
                        if (UNORDERED_LIST_ITEM_PATTERN.matcher(lines[j]).matches()) {
                            listPart.append(lines[j]).append("\n");
                        } else if (j > 0 && UNORDERED_LIST_ITEM_PATTERN.matcher(lines[j-1]).matches()) {
                            // This is a continuation line for a list item
                            listPart.append(lines[j]).append("\n");
                        }
                    }
                    paragraph = listPart.toString().trim();
                    lines = paragraph.split('\n');

                    // Recalculate list item count
                    listItemCount = 0;
                    for (String line : lines) {
                        if (UNORDERED_LIST_ITEM_PATTERN.matcher(line).matches()) {
                            listItemCount++;
                        }
                    }
                }

                // If all lines are list items, or if there are multiple list items,
                // treat this paragraph as a list
                if (listItemCount > 0 && (listItemCount == lines.length || listItemCount >= 2)) {
                    // This paragraph contains list items

                    // Always start a new list for each paragraph
                    if (inList) {
                        // End the current list and add it to processed paragraphs as HTML
                        StringBuilder listHtml = new StringBuilder("<ul>")
                        currentListItems.each { item ->
                            listHtml.append("<li>").append(item).append("</li>")
                        }
                        listHtml.append("</ul>")
                        processedParagraphs.add(listHtml.toString())
                    }

                    // If there's no current paragraph text, this is a standalone list
                    if (currentParagraphText == null) {
                        currentParagraphText = paragraph.trim()
                    }

                    // Start a new list
                    currentListItems = new ArrayList<>()
                    inList = true

                    // Extract the content of each list item
                    StringBuilder currentItem = null
                    for (String line : lines) {
                        def matcher = UNORDERED_LIST_ITEM_PATTERN.matcher(line)
                        if (matcher.matches()) {
                            // If we have a current item, add it to the list
                            if (currentItem != null) {
                                currentListItems.add(currentItem.toString())
                            }
                            // Start a new item
                            currentItem = new StringBuilder(formatInlineElements(matcher.group(2)))
                        } else if (line.trim() && currentItem != null) {
                            // Check if this is a continuation line
                            def continuationMatcher = LIST_ITEM_CONTINUATION_PATTERN.matcher(line)
                            if (continuationMatcher.matches()) {
                                // This is an indented continuation line
                                currentItem.append(" ")
                                currentItem.append(formatInlineElements(continuationMatcher.group(1)))
                            } else {
                                // Regular continuation line
                                currentItem.append(" ")
                                currentItem.append(formatInlineElements(line.trim()))
                            }
                        }
                    }
                    // Add the last item if we have one
                    if (currentItem != null) {
                        currentListItems.add(currentItem.toString())
                    }
                } else {
                    // Not a single-line list item
                    if (inList) {
                        // End the current list and add it to processed paragraphs as HTML
                        StringBuilder listHtml = new StringBuilder("<ul>")
                        currentListItems.each { item ->
                            listHtml.append("<li>").append(item).append("</li>")
                        }
                        listHtml.append("</ul>")
                        processedParagraphs.add(listHtml.toString())
                        inList = false
                    }
                    // Add this paragraph as is
                    processedParagraphs.add(paragraph)
                    currentParagraphText = paragraph
                }
            } else {
                // Empty paragraph
                if (inList) {
                    // End the current list and add it to processed paragraphs as HTML
                    StringBuilder listHtml = new StringBuilder("<ul>")
                    currentListItems.each { item ->
                        listHtml.append("<li>").append(item).append("</li>")
                    }
                    listHtml.append("</ul>")
                    processedParagraphs.add(listHtml.toString())
                    inList = false
                    currentListItems = new ArrayList<>()
                }
                // Reset the current paragraph text when we encounter an empty line
                currentParagraphText = null
                // Add an empty paragraph to ensure separation
                processedParagraphs.add("")
            }
        }

        // If we're still in a list at the end, add it
        if (inList) {
            StringBuilder listHtml = new StringBuilder("<ul>")
            currentListItems.each { item ->
                listHtml.append("<li>").append(item).append("</li>")
            }
            listHtml.append("</ul>")
            processedParagraphs.add(listHtml.toString())
            // Reset for next processing
            inList = false
            currentListItems = new ArrayList<>()
            currentParagraphText = null
        }

        // Second pass: process the paragraphs normally
        // First, let's fix the order of paragraphs and lists
        List<String> fixedParagraphs = new ArrayList<>()
        String currentParagraph = null

        for (int i = 0; i < processedParagraphs.size(); i++) {
            String paragraph = processedParagraphs.get(i)
            if (!paragraph.isEmpty()) {
                if (paragraph.startsWith("<ul>") && paragraph.endsWith("</ul>")) {
                    // This is a list
                    if (currentParagraph != null) {
                        // Add the current paragraph first, then the list
                        fixedParagraphs.add(currentParagraph)
                        fixedParagraphs.add(paragraph)
                        currentParagraph = null
                    } else {
                        // No current paragraph, just add the list
                        fixedParagraphs.add(paragraph)
                    }
                } else {
                    // This is a regular paragraph
                    if (currentParagraph != null) {
                        // Add the previous paragraph
                        fixedParagraphs.add(currentParagraph)
                    }
                    currentParagraph = paragraph
                }
            }
        }

        // Add the last paragraph if there is one
        if (currentParagraph != null) {
            fixedParagraphs.add(currentParagraph)
        }

        // Now process the fixed paragraphs
        fixedParagraphs.each { paragraph ->
            if (!paragraph.isEmpty()) {
                // Check if this paragraph contains a <pre> block
                if (paragraph.contains("<pre>")) {
                    // Process the paragraph specially to preserve <pre> blocks
                    htmlParagraphs.add(processPreBlockParagraph(paragraph))
                } else if (paragraph.startsWith("<ul>") && paragraph.endsWith("</ul>")) {
                    // This is already a processed list, just add it as is
                    htmlParagraphs.add(paragraph)
                } else {
                    // Check for headers first
                    String[] lines = paragraph.split('\n')
                    if (lines.length > 0 && HEADER_PATTERN.matcher(lines[0]).matches()) {
                        htmlParagraphs.add(convertHeader(paragraph))
                    } else if (ORDERED_LIST_PARAGRAPH_PATTERN.matcher(paragraph).matches()) {
                        htmlParagraphs.add(convertParagraphWithOrderedList(paragraph))
                    } else if (containsUnorderedListItems(lines)) {
                        // If the paragraph contains unordered list items but doesn't match the unordered list pattern
                        // (e.g., it starts with a regular paragraph and then has list items)
                        // Split it into a paragraph and a list
                        // Note: this is only reached in two rules in pmd java...
                        htmlParagraphs.add(convertParagraphWithUnorderedList(paragraph))
                    } else {
                        htmlParagraphs.add("<p>${formatInlineElements(paragraph)}</p>")
                    }
                }
            }
        }

        // Join paragraphs with newlines instead of directly concatenating them
        // This helps prevent </p><p> issues in code examples
        String html = htmlParagraphs.join("\n")

        // Now restore the <pre> blocks
        for (int i = 0; i < preBlocks.size(); i++) {
            html = html.replace("PRE_BLOCK_" + i + "_PLACEHOLDER", preBlocks.get(i))
        }

        // Restore any remaining <pre> tags
        html = html.replace("PRE_TAG_START", "<pre>")
        html = html.replace("PRE_TAG_END", "</pre>")

        // Fix the order of paragraphs and lists
        html = fixParagraphListOrder(html)

        return html
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

        result = handleNoteItalicsPattern(result)

        result = handlePmdRuleLinkPattern(result)

        result = handleMarkdownLinkPattern(result)

        result = handleUrlTagPattern(result)

        return result
    }

    private static String handleUrlTagPattern(String result) {
        // Handle URL tags like <http://example.com>
        def urlTagMatcher = URL_TAG_PATTERN.matcher(result)
        StringBuffer sb = new StringBuffer()
        while (urlTagMatcher.find()) {
            String url = urlTagMatcher.group(1)
            String replacement = "<a href=\"${url}\">${url}</a>"
            urlTagMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }
        urlTagMatcher.appendTail(sb)
        result = sb.toString()
        result
    }

    private static String handleMarkdownLinkPattern(String result) {
        // Handle general markdown links
        def markdownLinkMatcher = MARKDOWN_LINK_PATTERN.matcher(result)
        StringBuffer sb = new StringBuffer()
        while (markdownLinkMatcher.find()) {
            String replacement = "<a href=\"${markdownLinkMatcher.group(2)}\">${markdownLinkMatcher.group(1)}</a>"
            markdownLinkMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }
        markdownLinkMatcher.appendTail(sb)
        result = sb.toString()
        result
    }

    private static String handlePmdRuleLinkPattern(String result) {
        // Handle PMD rule links first (more specific)
        def ruleLinkMatcher = PMD_RULE_LINK_PATTERN.matcher(result)
        StringBuffer sb = new StringBuffer()
        while (ruleLinkMatcher.find()) {
            String linkText = ruleLinkMatcher.group(1)
            String href = ruleLinkMatcher.group(2)
            String replacement = "<a href=\"https://pmd.github.io/pmd/${href}\">${linkText}</a>"
            ruleLinkMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }
        ruleLinkMatcher.appendTail(sb)
        result = sb.toString()
        result
    }

    private static String handleNoteItalicsPattern(String result) {
        // Handle _Note:_ pattern
        def noteMatcher = ITALIC_NOTE_PATTERN.matcher(result)
        StringBuffer sb = new StringBuffer()
        while (noteMatcher.find()) {
            noteMatcher.appendReplacement(sb, Matcher.quoteReplacement('<b>Note:</b>'))
        }
        noteMatcher.appendTail(sb)
        result = sb.toString()
        result
    }

    private static String handleMultiLineCodeBlocks(String markdownText, Pattern pattern) {
        def matcher = pattern.matcher(markdownText)
        StringBuffer sb = new StringBuffer()

        while (matcher.find()) {
            String language = matcher.group(1) ?: ""
            String code = matcher.group(2) ?: ""

            // Format code with proper spacing and trim trailing whitespace
            code = " " + code.replaceAll(/\n/, "\n ").replaceAll(/\s+$/, "")

            // Create HTML code block with optional language class
            String langClass = language ? " class=\"language-${language}\"" : ""
            String html = "<pre><code${langClass}>${escapeHtml(code)}</code></pre>"

            matcher.appendReplacement(sb, Matcher.quoteReplacement(html))
        }

        matcher.appendTail(sb)
        return sb.toString()
    }

    private static String handleSections(String text) {
        def matcher = SECTION_PATTERN.matcher(text)
        StringBuffer sb = new StringBuffer()

        while (matcher.find()) {
            String sectionType = matcher.group(1)
            String content = matcher.group(2)?.trim() ?: ""
            String replacement = "<p><b>${sectionType}:</b> ${formatInlineElements(content)}</p>"
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }

        matcher.appendTail(sb)
        return sb.toString()
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

    private static boolean containsUnorderedListItems(String[] lines) {
        for (String line : lines) {
            if (UNORDERED_LIST_ITEM_PATTERN.matcher(line).matches()) {
                return true
            }
        }
        return false
    }

    private static String convertParagraphWithUnorderedList(String paragraphText) {
        String[] lines = paragraphText.split('\n')
        StringBuilder result = new StringBuilder()
        boolean inList = false
        boolean paragraphStarted = false
        boolean inListItem = false
        StringBuilder currentListItem = new StringBuilder()

        for (String line : lines) {
            String trimmedLine = line.trim()

            // Skip empty lines
            if (!trimmedLine) continue

            def listItemMatcher = UNORDERED_LIST_ITEM_PATTERN.matcher(line)

            if (listItemMatcher.matches()) {
                // Handle list item start

                // Close paragraph if needed
                if (paragraphStarted && !inList) {
                    paragraphStarted = false
                }

                // Close previous list item if needed
                if (inListItem) {
                    result.append("<li>${currentListItem}</li>")
                    currentListItem = new StringBuilder()
                }

                // Start list if needed
                if (!inList) {
                    result.append("<ul>")
                    inList = true
                }

                // Add content to the new list item
                currentListItem.append(formatInlineElements(listItemMatcher.group(2)))
                inListItem = true

            } else if (inList) {
                // Handle content within a list

                def continuationMatcher = LIST_ITEM_CONTINUATION_PATTERN.matcher(line)

                if (inListItem) {
                    // Add continuation content to current list item
                    currentListItem.append(" ")

                    if (continuationMatcher.matches()) {
                        // Indented continuation line
                        currentListItem.append(formatInlineElements(continuationMatcher.group(1)))
                    } else {
                        // Regular continuation line
                        currentListItem.append(formatInlineElements(trimmedLine))
                    }
                }

            } else {
                // Handle regular paragraph text

                if (!paragraphStarted) {
                    result.append("<p>")
                    paragraphStarted = true
                }

                result.append(formatInlineElements(trimmedLine))
            }
        }

        // Close any open elements
        if (inListItem) {
            result.append("<li>${currentListItem}</li>")
        }

        if (inList) {
            result.append("</ul>")
        }

        if (paragraphStarted) {
            result.append("</p>")
        }

        return result.toString()
    }

    private static String formatInlineElements(String text) {
        if (!text) return ""

        // Skip formatting for content inside <pre> tags
        if (text.contains("<pre>")) {
            return processTextWithPreBlocks(text)
        }

        return formatTextWithoutPre(text)
    }

    /**
     * Process text that contains <pre> blocks by extracting them,
     * formatting the parts outside the blocks, and then restoring the blocks.
     */
    private static String processTextWithPreBlocks(String text) {
        // Extract pre blocks and replace with placeholders
        PreProcessingResult result = extractPreBlocksWithPlaceholders(text)

        // Format text between pre blocks
        String processedText = formatTextBetweenPreBlocks(result.processedText)

        // Restore pre blocks
        return restorePreBlocks(processedText)
    }

    /**
     * Extracts <pre> blocks from text and replaces them with placeholders.
     */
    private static PreProcessingResult extractPreBlocksWithPlaceholders(String text) {
        Pattern prePattern = Pattern.compile("(<pre>[\\s\\S]*?</pre>)", Pattern.DOTALL)
        Matcher matcher = prePattern.matcher(text)
        StringBuffer sb = new StringBuffer()

        while (matcher.find()) {
            // Get the <pre> block (including tags)
            String preBlock = matcher.group(1)

            // Replace the <pre> block with a placeholder
            matcher.appendReplacement(sb, Matcher.quoteReplacement("PRE_BLOCK_PLACEHOLDER"))

            // Store the <pre> block
            sb.append("PRE_BLOCK_START")
            sb.append(preBlock)
            sb.append("PRE_BLOCK_END")
        }
        matcher.appendTail(sb)

        return new PreProcessingResult(sb.toString())
    }

    /**
     * Formats text between <pre> blocks, ignoring the content inside <pre> blocks.
     */
    private static String formatTextBetweenPreBlocks(String processedText) {
        String[] parts = processedText.split("PRE_BLOCK_PLACEHOLDER")

        // Format each part outside <pre> tags
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].contains("PRE_BLOCK_START")) {
                parts[i] = formatTextWithoutPre(parts[i])
            }
        }

        return String.join("", parts)
    }

    /**
     * Restores <pre> blocks from placeholder markers.
     */
    private static String restorePreBlocks(String processedText) {
        Pattern blockPattern = Pattern.compile("PRE_BLOCK_START(.*?)PRE_BLOCK_END", Pattern.DOTALL)
        Matcher blockMatcher = blockPattern.matcher(processedText)
        StringBuffer result = new StringBuffer()

        while (blockMatcher.find()) {
            String preBlock = blockMatcher.group(1)
            blockMatcher.appendReplacement(result, Matcher.quoteReplacement(preBlock))
        }
        blockMatcher.appendTail(result)

        return result.toString()
    }

    /**
     * Simple class to hold the result of pre-processing text with <pre> blocks.
     */
    private static class PreProcessingResult {
        final String processedText

        PreProcessingResult(String processedText) {
            this.processedText = processedText
        }
    }

    private static String formatTextWithoutPre(String text) {
        if (!text) return ""

        String result = text

        result = handleCodeBlockPattern(result)

        result = handleRuleReferencePattern(result)

        result = handleJdocPattern(result)

        result = handleMarkdownBoltPattern(result)

        result = handleMarkdownItalicsPattern(result)

        return result
    }

    private static String handleMarkdownItalicsPattern(String result) {
        // Basic markdown formatting - Italic
        def italicMatcher = Pattern.compile(/\*([^*]+)\*/).matcher(result)
        StringBuffer sb = new StringBuffer()
        while (italicMatcher.find()) {
            String replacement = "<i>" + escapeHtml(italicMatcher.group(1)) + "</i>"
            italicMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }
        italicMatcher.appendTail(sb)
        result = sb.toString()
        result
    }

    private static String handleMarkdownBoltPattern(String result) {
        // Basic markdown formatting - Bold
        def boldMatcher = Pattern.compile(/\*\*([^*]+)\*\*/).matcher(result)
        StringBuffer sb = new StringBuffer()
        while (boldMatcher.find()) {
            String replacement = "<b>" + escapeHtml(boldMatcher.group(1)) + "</b>"
            boldMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }
        boldMatcher.appendTail(sb)
        result = sb.toString()
        result
    }

    private static String handleJdocPattern(String result) {
        // Format jdoc references
        def jdocMatcher = JDOC_REFERENCE_PATTERN.matcher(result)
        StringBuffer sbJdoc = new StringBuffer()
        while (jdocMatcher.find()) {
            String replacement = createJdocReference(jdocMatcher)
            jdocMatcher.appendReplacement(sbJdoc, Matcher.quoteReplacement(replacement))
        }
        jdocMatcher.appendTail(sbJdoc)
        result = sbJdoc.toString()
        result
    }

    private static String handleRuleReferencePattern(String result) {
        def ruleRefMatcher = RULE_REFERENCE_PATTERN.matcher(result)
        StringBuffer sb = new StringBuffer()
        while (ruleRefMatcher.find()) {
            String replacement = "<code>" + escapeHtml(ruleRefMatcher.group(1)) + "</code>"
            ruleRefMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }
        ruleRefMatcher.appendTail(sb)
        result = sb.toString()
        result
    }

    private static String handleCodeBlockPattern(String result) {
        // Format inline code and rule references
        // Use a different approach to handle the replacements
        def codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(result)
        StringBuffer sb = new StringBuffer()
        while (codeBlockMatcher.find()) {
            String replacement = "<code>" + escapeHtml(codeBlockMatcher.group(1)) + "</code>"
            codeBlockMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }
        codeBlockMatcher.appendTail(sb)
        result = sb.toString()
        result
    }

    private static String createJdocReference(Matcher match) {
        String fullyQualifiedName = match.group(2)

        // Extract class name and member name if present
        int hashIndex = fullyQualifiedName.indexOf('#')
        String className = hashIndex > 0 ? fullyQualifiedName.substring(0, hashIndex) : fullyQualifiedName
        String memberName = hashIndex > 0 ? fullyQualifiedName.substring(hashIndex + 1) : ""

        // Build URL and determine display text
        String urlPath = className.replace('.', '/')
        String url = "${jdocLink}${urlPath}.html${memberName ? "#${memberName}" : ""}"
        String displayText = memberName ?: className.substring(className.lastIndexOf('.') + 1)

        return escapeReplacement("<a href=\"${url}\"><code>${displayText}</code></a>")
    }

    private static String escapeHtml(String text) {
        if (!text) return ""
        return text.replace('&', '&amp;')
            .replace('<', '&lt;')
            .replace('>', '&gt;')
            .replace('"', '&quot;')
            .replace("'", '&#39;')
    }

    /**
     * Fixes the order of paragraphs and lists in the HTML output.
     * This method looks for patterns where a paragraph is followed by another paragraph,
     * and then a list, and reorders them to ensure that lists are properly associated
     * with their paragraphs.
     */
    private static String fixParagraphListOrder(String html) {
        // Split the HTML into paragraphs and lists
        def parts = html.split("\n")

        // If we have fewer than 3 parts, there's nothing to fix
        if (parts.length < 3) {
            return html
        }

        // Look for the pattern: <p>...</p>\n<p>...</p>\n<ul>...</ul>
        for (int i = 0; i < parts.length - 2; i++) {
            if (parts[i].startsWith("<p>") && parts[i].endsWith("</p>") &&
                parts[i+1].startsWith("<p>") && parts[i+1].endsWith("</p>") &&
                parts[i+2].startsWith("<ul>") && parts[i+2].endsWith("</ul>")) {

                // Check if the first paragraph ends with a colon, which indicates
                // it should be followed by a list
                if (parts[i].contains("metrics:")) {
                    // Swap the order of the second paragraph and the list
                    String temp = parts[i+1]
                    parts[i+1] = parts[i+2]
                    parts[i+2] = temp
                    break
                }
            }
        }

        // Join the parts back together
        return parts.join("\n")
    }

    // Extract <pre> blocks and replace them with placeholders
    private static String extractPreBlocks(String text, List<String> preBlocks) {
        def pattern = Pattern.compile("<pre>([\\s\\S]*?)</pre>", Pattern.DOTALL)
        def matcher = pattern.matcher(text)
        def sb = new StringBuffer()

        while (matcher.find()) {
            preBlocks.add(matcher.group(0))
            matcher.appendReplacement(sb, "PRE_BLOCK_${preBlocks.size() - 1}_PLACEHOLDER")
        }
        matcher.appendTail(sb)
        sb.toString()
    }

    // Process a paragraph that contains <pre> blocks
    private static String processPreBlockParagraph(String paragraph) {
        // Extract all <pre> blocks from the paragraph
        def preBlocks = []
        def pattern = Pattern.compile("<pre>([\\s\\S]*?)</pre>", Pattern.DOTALL)
        def matcher = pattern.matcher(paragraph)
        def sb = new StringBuffer()

        // Replace <pre> blocks with placeholders
        def index = 0
        while (matcher.find()) {
            preBlocks.add(matcher.group(0))
            matcher.appendReplacement(sb, "PRE_BLOCK_${index++}_PLACEHOLDER")
        }
        matcher.appendTail(sb)

        // Process the text outside <pre> blocks
        def textWithoutPre = sb.toString()
        def lines = textWithoutPre.split('\n')

        // Process as header or regular paragraph
        def processedText = lines.length > 0 && HEADER_PATTERN.matcher(lines[0].trim()).matches() 
            ? convertHeader(textWithoutPre)
            : "<p>${formatInlineElements(textWithoutPre)}</p>"

        // Restore <pre> blocks
        preBlocks.eachWithIndex { block, i -> 
            processedText = processedText.replace("PRE_BLOCK_${i}_PLACEHOLDER", block)
        }

        processedText
    }

    // Restore <pre> blocks from placeholders
    private static String restorePreBlocks(String text, List<String> preBlocks) {
        def pattern = Pattern.compile("PRE_BLOCK_(\\d+)_PLACEHOLDER")
        def matcher = pattern.matcher(text)
        def sb = new StringBuffer()

        while (matcher.find()) {
            def blockIndex = Integer.parseInt(matcher.group(1))
            if (blockIndex < preBlocks.size()) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(preBlocks[blockIndex]))
            }
        }
        matcher.appendTail(sb)
        sb.toString()
    }
}

// Convert camelCase rule name to readable format with only first letter uppercase
// Note: "APITest" -> "API test", "XMLHttpRequest" -> "XMLHttp request
def camelCaseToReadable = { ruleName ->

    def words = ruleName.replaceAll(/([a-z])([A-Z])/, '$1 $2').trim().split(' ')
    def result = words.collect { word ->
        if (!word) return word

        // Special case for NaN
        if (word.equals("NaN")) {
            return "NaN"
        }

        // If word has multiple consecutive capitals at start, preserve them
        if (word.matches(/^[A-Z]{2,}.*/)) {
            def matcher = word =~ /^([A-Z]+)([a-z].*)?/
            if (matcher) {
                def capitals = matcher[0][1]
                def rest = matcher[0][2] ?: ""
                return capitals + (rest ? rest.toLowerCase() : "")
            }
        }

        // Otherwise, lowercase everything
        return word.toLowerCase()
    }.join(' ')

    // Capitalize only the first word
    if (result) {
        result = result[0].toUpperCase() + (result.length() > 1 ? result[1..-1] : "")
    }
}

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
    def ruleName = ruleData.name

    // If no description exists, log warning, do not add rule
    if (!description || description.trim().isEmpty()) {
        // report with println and skip processing
    }

    // Build markdown content
    def markdownContent = new StringBuilder()

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

    // Convert markdown to HTML using our Groovy MdToHtmlConverter
    def htmlContent = MdToHtmlConverter.convertToHtml(markdownContent.toString())

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
                        name(camelCaseToReadable(ruleData.name))
                        internalKey("${ruleData.categoryFile}/${ruleData.name}")
                        severity(priorityToSeverity(ruleData.priority))

                        // Add description with CDATA
                        description {
                            def descContent = formatDescription(ruleData, language)
                            if (!descContent || descContent.trim().isEmpty()) {
                                descContent = MdToHtmlConverter.convertToHtml("THIS SHOULD NOT HAPPEN")
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
