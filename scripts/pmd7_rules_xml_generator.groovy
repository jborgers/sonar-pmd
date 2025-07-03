@Grab('net.sourceforge.pmd:pmd-java:7.15.0')
@Grab('net.sourceforge.pmd:pmd-kotlin:7.15.0')
import groovy.xml.XmlSlurper
import groovy.xml.MarkupBuilder
import java.util.zip.ZipFile
import java.util.regex.Pattern
import java.util.regex.Matcher

// Configuration
def pmdVersion = MdToHtmlConverter.PMD_VERSION
def pmdJavaJarPath = System.getProperty("user.home") + "/.m2/repository/net/sourceforge/pmd/pmd-java/${pmdVersion}/pmd-java-${pmdVersion}.jar"
def pmdKotlinJarPath = System.getProperty("user.home") + "/.m2/repository/net/sourceforge/pmd/pmd-kotlin/${pmdVersion}/pmd-kotlin-${pmdVersion}.jar"
def javaCategoriesPropertiesPath = "category/java/categories.properties"
def kotlinCategoriesPropertiesPath = "category/kotlin/categories.properties"

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
    static final Pattern UNORDERED_LIST_PARAGRAPH_PATTERN = ~/(?s)[\s\t]*[\*\-].*/
    static final Pattern SECTION_PARAGRAPH_PATTERN = ~/(?s)\s*[A-Za-z]+:\s*.*/
    static final Pattern LIST_ITEM_PATTERN = ~/(\d+)\.(\s+)(.*)/
    static final Pattern UNORDERED_LIST_ITEM_PATTERN = ~/[\s\t]*[\*\-](\s+)(.*)/
    static final Pattern LIST_ITEM_CONTINUATION_PATTERN = ~/^[\s\t]{2,}([^\*\-].+)$/
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
    static final Pattern URL_TAG_PATTERN = ~/<(https?:\/\/[^>]+)>/
    // {% jdoc java::lang.java.metrics.JavaMetrics#WEIGHED_METHOD_COUNT %}
    static final Pattern JDOC_REFERENCE_PATTERN = ~/\{\%\s*jdoc\s+([\w-]+)::([\w.#]+)\s*\%\}/
    // example: https://docs.pmd-code.org/apidocs/pmd-java/7.15.0/net/sourceforge/pmd/lang/java/metrics/JavaMetrics.html#WEIGHED_METHOD_COUNT
    static final String jdocLink = "https://docs.pmd-code.org/apidocs/pmd-java/${PMD_VERSION}/net/sourceforge/"

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

        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim()
            if (!paragraph.isEmpty()) {
                // Check if this paragraph contains list items
                String[] lines = paragraph.split('\n')

                // Count how many lines are list items
                int listItemCount = 0
                for (String line : lines) {
                    if (UNORDERED_LIST_ITEM_PATTERN.matcher(line).matches()) {
                        listItemCount++
                    }
                }

                // If all lines are list items, or if there are multiple list items,
                // treat this paragraph as a list
                if (listItemCount > 0 && (listItemCount == lines.length || listItemCount >= 2)) {
                    // This paragraph contains list items
                    if (!inList) {
                        // Start a new list
                        currentListItems = new ArrayList<>()
                        inList = true
                    }

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
                }
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
        }

        // Second pass: process the paragraphs normally
        processedParagraphs.each { paragraph ->
            if (!paragraph.isEmpty()) {
                // Check if this paragraph contains a <pre> block
                if (paragraph.contains("<pre>")) {
                    // Process the paragraph specially to preserve <pre> blocks
                    htmlParagraphs.add(processPreBlockParagraph(paragraph))
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
                    } else if (SECTION_PARAGRAPH_PATTERN.matcher(paragraph).matches()) {
                        htmlParagraphs.add(convertSection(paragraph))
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

        // Handle URL tags like <http://example.com>
        result = URL_TAG_PATTERN.matcher(result).replaceAll { match ->
            String url = match.group(1)
            String replacement = "<a href=\"${url}\">${url}</a>".toString()
            return escapeReplacement(replacement)
        }

        return result
    }

    private static String handleMultiLineCodeBlocks(String markdownText, Pattern pattern) {
        return pattern.matcher(markdownText).replaceAll { match ->
            String language = match.group(1) ?: ""
            // Don't trim the code to preserve leading spaces
            String code = match.group(2) ?: ""

            // Add a space at the beginning of each line (including the first line)
            // This ensures proper spacing in the HTML output for all code examples
            code = " " + code.replaceAll(/\n/, "\n ")

            // Only trim trailing whitespace
            code = code.replaceAll(/\s+$/, "")
            String langClass = language ? " class=\"language-${language}\"" : ""

            // Directly escape HTML without introducing paragraphs
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
            if (UNORDERED_LIST_ITEM_PATTERN.matcher(line).matches()) {
                // If we were in a paragraph, close it before starting the list
                if (paragraphStarted && !inList) {
                    paragraphStarted = false
                }

                // If we were in a list item, close it before starting a new one
                if (inListItem) {
                    result.append("<li>${currentListItem.toString()}</li>")
                    currentListItem = new StringBuilder()
                }

                // Start the list if not already in one
                if (!inList) {
                    result.append("<ul>")
                    inList = true
                }

                // Start a new list item
                def matcher = UNORDERED_LIST_ITEM_PATTERN.matcher(line)
                if (matcher.find()) {
                    currentListItem.append(formatInlineElements(matcher.group(2)))
                    inListItem = true
                }
            } else if (line.trim() && inList) {
                // Check if this is a continuation line (indented but not starting with * or -)
                def continuationMatcher = LIST_ITEM_CONTINUATION_PATTERN.matcher(line)
                if (continuationMatcher.matches()) {
                    // This is an indented continuation line
                    if (inListItem) {
                        // Just add a space and the continuation text
                        currentListItem.append(" ")
                        currentListItem.append(formatInlineElements(continuationMatcher.group(1)))
                    }
                } else if (inListItem) {
                    // Regular continuation line
                    currentListItem.append(" ")
                    currentListItem.append(formatInlineElements(line.trim()))
                }
            } else if (line.trim()) {
                // Regular paragraph text
                if (!paragraphStarted) {
                    result.append("<p>")
                    paragraphStarted = true
                }
                result.append(formatInlineElements(line.trim()))
            }
        }

        // Close the last list item if we're still in one
        if (inListItem) {
            result.append("<li>${currentListItem.toString()}</li>")
        }

        // Close any open tags
        if (inList) {
            result.append("</ul>")
        }
        if (paragraphStarted) {
            result.append("</p>")
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

        // Format inline code blocks
        result = CODE_BLOCK_PATTERN.matcher(result).replaceAll(escapeReplacement('<code>') + '$1' + escapeReplacement('</code>'))

        // Format rule references
        result = RULE_REFERENCE_PATTERN.matcher(result).replaceAll(escapeReplacement('<code>') + '$1' + escapeReplacement('</code>'))

        // Format jdoc references
        // <a href="https://docs.pmd-code.org/apidocs/pmd-java/7.15.0/net/sourceforge/pmd/lang/java/metrics/JavaMetrics.html#WEIGHED_METHOD_COUNT"><code>WEIGHED_METHOD_COUNT</code></a>
        result = JDOC_REFERENCE_PATTERN.matcher(result).replaceAll { match ->
            return createJdocReference(match)
        }

        // Format inline titles (like "Problem:" in the middle of text)
        result = INLINE_TITLE_PATTERN.matcher(result).replaceAll(escapeReplacement('<b>') + '$1' + escapeReplacement(':</b>') + '$2')

        // Basic markdown formatting
        result = result.replaceAll(/\*\*([^*]+)\*\*/, escapeReplacement('<b>') + '$1' + escapeReplacement('</b>'))  // Bold
        result = result.replaceAll(/\*([^*]+)\*/, escapeReplacement('<i>') + '$1' + escapeReplacement('</i>'))      // Italic

        // DON'T convert all newlines to <br/> - only paragraph breaks are handled by the paragraph splitter

        return result
    }

    private static String createJdocReference(Matcher match) {
        String language = match.group(1)
        String fullyQualifiedName = match.group(2)

        // Extract the class name and field/method reference
        String className = fullyQualifiedName
        String memberName = ""

        // Check if there's a hash symbol indicating a member reference
        int hashIndex = fullyQualifiedName.indexOf('#')
        if (hashIndex > 0) {
            className = fullyQualifiedName.substring(0, hashIndex)
            memberName = fullyQualifiedName.substring(hashIndex + 1)
        }

        // Convert dots to slashes for URL path
        String urlPath = className.replace('.', '/')

        // Build the full URL
        String url = "${jdocLink}${urlPath}.html"
        if (!memberName.isEmpty()) {
            url += "#${memberName}"
        }

        // Use just the member name or the last part of the class name for display
        String displayText = !memberName.isEmpty() ? memberName : className.substring(className.lastIndexOf('.') + 1)

        String replacement = "<a href=\"${url}\"><code>${displayText}</code></a>"
        return escapeReplacement(replacement)
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

    // Extract <pre> blocks and replace them with placeholders
    private static String extractPreBlocks(String text, List<String> preBlocks) {
        Pattern prePattern = Pattern.compile("<pre>([\\s\\S]*?)</pre>", Pattern.DOTALL)
        Matcher matcher = prePattern.matcher(text)
        StringBuffer sb = new StringBuffer()

        while (matcher.find()) {
            String preBlock = matcher.group(0)
            // Store the original pre block
            preBlocks.add(preBlock)
            // Replace with a placeholder that won't be split by paragraph splitter
            matcher.appendReplacement(sb, "PRE_BLOCK_" + (preBlocks.size() - 1) + "_PLACEHOLDER")
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    // Process a paragraph that contains <pre> blocks
    private static String processPreBlockParagraph(String paragraph) {
        // Extract all <pre> blocks from the paragraph
        List<String> preBlocks = new ArrayList<>()
        Pattern prePattern = Pattern.compile("<pre>([\\s\\S]*?)</pre>", Pattern.DOTALL)
        Matcher matcher = prePattern.matcher(paragraph)
        StringBuffer sb = new StringBuffer()

        // Replace <pre> blocks with placeholders
        int index = 0
        while (matcher.find()) {
            String preBlock = matcher.group(0)
            preBlocks.add(preBlock)
            matcher.appendReplacement(sb, "PRE_BLOCK_" + index + "_PLACEHOLDER")
            index++
        }
        matcher.appendTail(sb)

        // Process the text outside <pre> blocks
        String textWithoutPre = sb.toString()

        // Check if the paragraph starts with a header
        String[] lines = textWithoutPre.split('\n')
        if (lines.length > 0 && HEADER_PATTERN.matcher(lines[0].trim()).matches()) {
            // Process as a header
            String processedText = convertHeader(textWithoutPre)

            // Restore <pre> blocks
            for (int i = 0; i < preBlocks.size(); i++) {
                processedText = processedText.replace("PRE_BLOCK_" + i + "_PLACEHOLDER", preBlocks.get(i))
            }

            return processedText
        } else {
            // Process as a regular paragraph
            String processedText = "<p>" + formatInlineElements(textWithoutPre) + "</p>"

            // Restore <pre> blocks
            for (int i = 0; i < preBlocks.size(); i++) {
                processedText = processedText.replace("PRE_BLOCK_" + i + "_PLACEHOLDER", preBlocks.get(i))
            }

            return processedText
        }
    }

    // Restore <pre> blocks from placeholders
    private static String restorePreBlocks(String text, List<String> preBlocks) {
        Pattern placeholderPattern = Pattern.compile("PRE_BLOCK_(\\d+)_PLACEHOLDER")
        Matcher matcher = placeholderPattern.matcher(text)
        StringBuffer sb = new StringBuffer()

        while (matcher.find()) {
            int blockIndex = Integer.parseInt(matcher.group(1))
            if (blockIndex < preBlocks.size()) {
                String replacement = Matcher.quoteReplacement(preBlocks.get(blockIndex))
                matcher.appendReplacement(sb, replacement)
            }
        }
        matcher.appendTail(sb)
        return sb.toString()
    }
}

// Helper function to convert camelCase rule name to readable format with only first letter uppercase
def camelCaseToReadable = { ruleName ->
    def result = ruleName.replaceAll(/([A-Z])/, ' $1').trim()
    // Capitalize only the first letter and make the rest lowercase
    return result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase()
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

        // First, read the categories.properties to get the list of rule files
        def categoriesEntry = zipFile.getEntry(categoriesPath)
        if (categoriesEntry) {
            def categoriesProps = new Properties()
            categoriesProps.load(zipFile.getInputStream(categoriesEntry))

            def rulesetFilenames = categoriesProps.getProperty("rulesets.filenames", "")
            categoryFiles = rulesetFilenames.split(",").collect { it.trim() }

            println "Found ${categoryFiles.size()} category files in PMD JAR: ${jarPath}"
        } else {
            println "WARNING: ${categoriesPath} not found in PMD JAR: ${jarPath}"
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
                            rules << [
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

    // Add external info URL as the last paragraph if available
    if (externalInfoUrl) {
        // Extract a more readable link text from the URL
        def linkText = "PMD rule documentation"
        htmlContent += "\n<p>More information: <a href=\"${externalInfoUrl}\">${linkText}</a></p>"
    }

    return htmlContent
}

// Function to generate XML file
def generateXmlFile = { outputFile, rules, language ->
    def rulesWithoutDescription = 0

    try {
        outputFile.withWriter('UTF-8') { writer ->
            def xml = new MarkupBuilder(writer)
            xml.setDoubleQuotes(true)

            // Write XML declaration manually since MarkupBuilder doesn't handle it well
            writer.println('<?xml version="1.0" encoding="UTF-8"?>')

            xml.rules {
                rules.sort { it.name }.each { ruleData ->
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

        println "Successfully generated ${outputFile.name}"
        println "Total ${language} rules: ${rules.size()}"
        println "Active ${language} rules: ${rules.count { !it.deprecated }}"
        println "Deprecated ${language} rules: ${rules.count { it.deprecated }}"
        if (rulesWithoutDescription > 0) {
            println "${language} rules with generated fallback descriptions: ${rulesWithoutDescription}"
        }
        println "Using camelCase transformation for all rule names"

        // Show category breakdown
        def categoryStats = rules.groupBy { it.category }
        println ""
        println "${language} rules by category:"
        categoryStats.sort { it.key }.each { category, categoryRules ->
            def activeCount = categoryRules.count { !it.deprecated }
            def deprecatedCount = categoryRules.count { it.deprecated }
            println "  - ${category}: ${categoryRules.size()} total (${activeCount} active, ${deprecatedCount} deprecated)"
        }

        // Show tag distribution
        println ""
        println "Tags that will be applied for ${language} rules:"
        println "  - pmd: ${rules.size()} rules"
        categoryStats.sort { it.key }.each { category, categoryRules ->
            println "  - ${category}: ${categoryRules.size()} rules"
        }

        // Check for any rules that might still have empty descriptions (shouldn't happen now)
        def outputXml = new XmlSlurper().parse(outputFile)
        def emptyDescriptions = outputXml.rule.findAll { 
            !it.description.text() || it.description.text().trim().isEmpty() 
        }

        if (emptyDescriptions.size() > 0) {
            println ""
            println "WARNING: Found ${emptyDescriptions.size()} ${language} rules with empty descriptions:"
            emptyDescriptions.each { rule ->
                println "  - ${rule.key.text()}"
            }
        } else {
            println ""
            println "✓ All ${language} rules have descriptions"
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
