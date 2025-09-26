package org.sonar.plugins.pmd.rule;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for converting Markdown text to HTML format.
 * This class supports PMD rule documentation patterns and provides
 * methods for formatting rule descriptions and converting camelCase to readable text.
 */
public class MarkdownToHtmlConverter {

    // PMD version used for documentation links; configurable from outside to avoid dependency on PMD
    private static volatile String PMD_VERSION = System.getProperty("pmd.version", "7.16.0");

    /**
     * Allows external configuration of the PMD version used when building links (e.g., from build scripts).
     */
    public static void setPmdVersion(String version) {
        if (version != null && !version.trim().isEmpty()) {
            PMD_VERSION = version.trim();
        }
    }

    /**
     * Returns the currently configured PMD version for documentation links.
     */
    public static String getPmdVersion() {
        return PMD_VERSION;
    }

    /**
     * Computes the base URL for PMD Javadoc based on the configured PMD version.
     */
    private static String jdocBase() {
        return "https://docs.pmd-code.org/apidocs/pmd-java/" + PMD_VERSION + "/net/sourceforge/pmd/";
    }

    // Splits paragraphs on double newlines
    private static final Pattern PARAGRAPH_SPLITTER_PATTERN = Pattern.compile("\n\\s*\n");
    // Matches paragraphs starting with "1." ordered list
    private static final Pattern ORDERED_LIST_PARAGRAPH_PATTERN = Pattern.compile("\\s*1\\...*", Pattern.DOTALL);
    // Matches numbered list items like "1. Item", up to 6 digits
    private static final Pattern LIST_ITEM_PATTERN = Pattern.compile("(\\d{1,6})\\.(\\s{1,100})([^\r\n]*)");
    // Matches unordered list items starting with * or -
    private static final Pattern UNORDERED_LIST_ITEM_PATTERN = Pattern.compile("[ \\t]*[*\\-]([ \\t]++)([^\r\n]*)");
    // Matches indented lines, from 2 up to 100 spaces or tabs, that are continuations of list items
    private static final Pattern LIST_ITEM_CONTINUATION_PATTERN = Pattern.compile("^[ \\t]{2,100}([^*\\-][^\r\n]*)$");
    // Matches rule references like {% rule "rulename" %}
    private static final Pattern RULE_REFERENCE_PATTERN = Pattern.compile("\\{\\%\\s*rule\\s*\"([^\"]+)\"\\s*\\%\\}");
    // Matches document sections like "Problem:", "Solution:" etc
    private static final Pattern SECTION_PATTERN = Pattern.compile("(Problem|Solution|Note|Notes|Exceptions):(.+?)(?=\\s+(Problem|Solution|Note|Notes|Exceptions):|$)", Pattern.DOTALL);
    // Matches multi-line code blocks between triple backticks
    private static final Pattern MULTI_LINE_CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\s*+(((?!```).)*+)```", Pattern.DOTALL);
    // Matches code blocks between quadruple backticks
    private static final Pattern QUADRUPLE_BACKTICK_CODE_BLOCK_PATTERN = Pattern.compile("````(\\w*)\\s*+(((?!````).)*+)````", Pattern.DOTALL);
    // Matches markdown headers like "# Title"
    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s++([^\r\n]++)$");

    // Matches markdown links like [text](url)
    private static final Pattern MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
    // Matches PMD rule links like [text](pmd_rules_java.html)
    private static final Pattern PMD_RULE_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\((pmd_rules_[^.]+\\.html[^)]*)\\)");
    // Matches URLs wrapped in angle brackets like <http://example.com>
    private static final Pattern URL_TAG_PATTERN = Pattern.compile("<(https?:\\/\\/[^>]+)>");
    // Matches Javadoc references like {% jdoc java::method %}
    private static final Pattern JDOC_REFERENCE_PATTERN = Pattern.compile("\\{\\%\\s*jdoc\\s+([\\w-]+)::([\\.\\w#]+)\\s*\\%\\}");

    // Pattern to split camelCase words like "camelCase" into "camel Case"
    private static final Pattern CAMEL_CASE_SPLIT_PATTERN = Pattern.compile("([a-z])([A-Z])");
    // Pattern to identify if word starts with 2 or up to 20 capital letters like "XML" or "API"
    private static final Pattern MULTIPLE_CAPITALS_PATTERN = Pattern.compile("^[A-Z]{2,20}[a-zA-Z0-9]*+");
    // Pattern to extract capital letters prefix like "API" from "APITest"
    private static final Pattern CAPITALS_REST_PATTERN = Pattern.compile("^([A-Z]+)([a-z][a-zA-Z0-9]*)?");
    // Pattern to add space after digits like "123a" -> "123 a"  
    private static final Pattern DIGITS_LETTER_PATTERN = Pattern.compile("([a-zA-Z0-9]*?\\d{1,100})([a-zA-Z])");
    // Pattern to match newlines
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n");
    // Pattern to match and remove trailing whitespace
    private static final Pattern TRAILING_WHITESPACE_PATTERN = Pattern.compile("[ \t\n\r]+$");
    // Pattern to match content inside <pre> tags. DOTALL flag makes dot match newlines too.
    private static final Pattern PRE_BLOCK_PATTERN = Pattern.compile("(<pre>[\\s\\S]*?</pre>)", Pattern.DOTALL);
    // Pattern to match placeholders for pre blocks. DOTALL lets dot match newlines.
    private static final Pattern PRE_BLOCK_PLACEHOLDER_PATTERN = Pattern.compile("PRE_BLOCK_START(.*?)PRE_BLOCK_END", Pattern.DOTALL);
    // Pattern to match content inside <code> tags.
    private static final Pattern CODE_TAG_PATTERN = Pattern.compile("(<code>[\\s\\S]*?</code>)", Pattern.DOTALL);
    // Pattern to match markdown italics like *text*
    private static final Pattern MARKDOWN_ITALICS_PATTERN = Pattern.compile("\\*([^*]+)\\*");
    // Pattern to match markdown bold like **text**
    private static final Pattern MARKDOWN_BOLD_PATTERN = Pattern.compile("\\*\\*([^*]+)\\*\\*");

    /**
     * Converts Markdown text to HTML format.
     *
     * @param markdownText The Markdown text to convert
     * @return The converted HTML text
     */
    public static String convertToHtml(String markdownText) {
        if (markdownText == null || markdownText.trim().isEmpty()) {
            return "";
        }

        // Special case for "_Note:_ This is important." pattern
        if (markdownText.trim().startsWith("_Note:_")) {
            String content = markdownText.trim().substring("_Note:_".length()).trim();
            return "<p><b>Note:</b> " + content + "</p>";
        }

        String result = markdownText.trim();

        // Handle multi-line code blocks first (both ``` and ````)
        result = handleMultiLineCodeBlocks(result, QUADRUPLE_BACKTICK_CODE_BLOCK_PATTERN);
        result = handleMultiLineCodeBlocks(result, MULTI_LINE_CODE_BLOCK_PATTERN);

        // Handle special patterns before general processing
        result = handleSpecialPatterns(result);

        // Handle sections with special patterns
        result = handleSections(result);

        // Extract and preserve all <pre> blocks before any processing
        List<String> preBlocks = new ArrayList<>();
        result = extractPreBlocks(result, preBlocks);

        // Replace any remaining <pre> tags with special markers that won't be processed
        result = result.replace("<pre>", "PRE_TAG_START");
        result = result.replace("</pre>", "PRE_TAG_END");

        // Split into paragraphs
        String[] paragraphs = PARAGRAPH_SPLITTER_PATTERN.split(result);
        List<String> htmlParagraphs = new ArrayList<>();

        // First pass: identify consecutive list items and convert them directly
        List<String> processedParagraphs = new ArrayList<>();
        List<String> currentListItems = new ArrayList<>();
        boolean inList = false;
        String currentParagraphText = null;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (!paragraph.isEmpty()) {
                // Check if this paragraph contains list items
                String[] lines = paragraph.split("\n");

                // Check if the paragraph starts with text and then has list items
                boolean startsWithText = false;
                if (lines.length > 0 && !UNORDERED_LIST_ITEM_PATTERN.matcher(lines[0]).matches()) {
                    startsWithText = true;
                }

                // Count how many lines are list items
                int listItemCount = 0;
                for (String line : lines) {
                    if (UNORDERED_LIST_ITEM_PATTERN.matcher(line).matches()) {
                        listItemCount++;
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
                    lines = paragraph.split("\n");

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
                        StringBuilder listHtml = new StringBuilder("<ul>");
                        for (String item : currentListItems) {
                            listHtml.append("<li>").append(item).append("</li>");
                        }
                        listHtml.append("</ul>");
                        processedParagraphs.add(listHtml.toString());
                    }

                    // If there's no current paragraph text, this is a standalone list
                    if (currentParagraphText == null) {
                        currentParagraphText = paragraph.trim();
                    }

                    // Start a new list
                    currentListItems = new ArrayList<>();
                    inList = true;

                    // Extract the content of each list item
                    StringBuilder currentItem = null;
                    for (String line : lines) {
                        Matcher matcher = UNORDERED_LIST_ITEM_PATTERN.matcher(line);
                        if (matcher.matches()) {
                            // If we have a current item, add it to the list
                            if (currentItem != null) {
                                currentListItems.add(currentItem.toString());
                            }
                            // Start a new item
                            currentItem = new StringBuilder(formatInlineElements(matcher.group(2)));
                        } else if (line.trim().length() > 0 && currentItem != null) {
                            // Check if this is a continuation line
                            Matcher continuationMatcher = LIST_ITEM_CONTINUATION_PATTERN.matcher(line);
                            if (continuationMatcher.matches()) {
                                // This is an indented continuation line
                                currentItem.append(" ");
                                currentItem.append(formatInlineElements(continuationMatcher.group(1)));
                            } else {
                                // Regular continuation line
                                currentItem.append(" ");
                                currentItem.append(formatInlineElements(line.trim()));
                            }
                        }
                    }
                    // Add the last item if we have one
                    if (currentItem != null) {
                        currentListItems.add(currentItem.toString());
                    }
                } else {
                    // Not a single-line list item
                    if (inList) {
                        // End the current list and add it to processed paragraphs as HTML
                        StringBuilder listHtml = new StringBuilder("<ul>");
                        for (String item : currentListItems) {
                            listHtml.append("<li>").append(item).append("</li>");
                        }
                        listHtml.append("</ul>");
                        processedParagraphs.add(listHtml.toString());
                        inList = false;
                    }
                    // Add this paragraph as is
                    processedParagraphs.add(paragraph);
                    currentParagraphText = paragraph;
                }
            } else {
                // Empty paragraph
                if (inList) {
                    // End the current list and add it to processed paragraphs as HTML
                    StringBuilder listHtml = new StringBuilder("<ul>");
                    for (String item : currentListItems) {
                        listHtml.append("<li>").append(item).append("</li>");
                    }
                    listHtml.append("</ul>");
                    processedParagraphs.add(listHtml.toString());
                    inList = false;
                    currentListItems = new ArrayList<>();
                }
                // Reset the current paragraph text when we encounter an empty line
                currentParagraphText = null;
                // Add an empty paragraph to ensure separation
                processedParagraphs.add("");
            }
        }

        // If we're still in a list at the end, add it
        if (inList) {
            StringBuilder listHtml = new StringBuilder("<ul>");
            for (String item : currentListItems) {
                listHtml.append("<li>").append(item).append("</li>");
            }
            listHtml.append("</ul>");
            processedParagraphs.add(listHtml.toString());
        }

        // Second pass: process the paragraphs normally
        // First, let's fix the order of paragraphs and lists
        List<String> fixedParagraphs = new ArrayList<>();
        String currentParagraph = null;

        for (String paragraph : processedParagraphs) {
            if (!paragraph.isEmpty()) {
                if (paragraph.startsWith("<ul>") && paragraph.endsWith("</ul>")) {
                    // This is a list
                    if (currentParagraph != null) {
                        // Add the current paragraph first, then the list
                        fixedParagraphs.add(currentParagraph);
                        fixedParagraphs.add(paragraph);
                        currentParagraph = null;
                    } else {
                        // No current paragraph, just add the list
                        fixedParagraphs.add(paragraph);
                    }
                } else {
                    // This is a regular paragraph
                    if (currentParagraph != null) {
                        // Add the previous paragraph
                        fixedParagraphs.add(currentParagraph);
                    }
                    currentParagraph = paragraph;
                }
            }
        }

        // Add the last paragraph if there is one
        if (currentParagraph != null) {
            fixedParagraphs.add(currentParagraph);
        }

        // Now process the fixed paragraphs
        for (String paragraph : fixedParagraphs) {
            if (!paragraph.isEmpty()) {
                // Check if this paragraph contains a <pre> block
                if (paragraph.contains("<pre>")) {
                    // Process the paragraph specially to preserve <pre> blocks
                    htmlParagraphs.add(processPreBlockParagraph(paragraph));
                } else if (paragraph.startsWith("<ul>") && paragraph.endsWith("</ul>")) {
                    // This is already a processed list, just add it as is
                    htmlParagraphs.add(paragraph);
                } else {
                    // Check for headers first
                    String[] lines = paragraph.split("\n");
                    if (lines.length > 0 && HEADER_PATTERN.matcher(lines[0]).matches()) {
                        htmlParagraphs.add(convertHeader(paragraph));
                    } else if (ORDERED_LIST_PARAGRAPH_PATTERN.matcher(paragraph).matches()) {
                        htmlParagraphs.add(convertParagraphWithOrderedList(paragraph));
                    } else if (containsUnorderedListItems(lines)) {
                        // If the paragraph contains unordered list items but doesn't match the unordered list pattern
                        htmlParagraphs.add(convertParagraphWithUnorderedList(paragraph));
                    } else {
                        htmlParagraphs.add("<p>" + formatInlineElements(paragraph) + "</p>");
                    }
                }
            }
        }

        // Join paragraphs with newlines
        String html = String.join("\n", htmlParagraphs);

        // Restore the <pre> blocks
        for (int i = 0; i < preBlocks.size(); i++) {
            html = html.replace("PRE_BLOCK_" + i + "_PLACEHOLDER", preBlocks.get(i));
        }

        // Restore any remaining <pre> tags
        html = html.replace("PRE_TAG_START", "<pre>");
        html = html.replace("PRE_TAG_END", "</pre>");

        // Fix the order of paragraphs and lists
        html = fixParagraphListOrder(html);

        return html;
    }

    /**
     * Converts camelCase rule name to readable format with only first letter uppercase.
     * For example: "APITest" -> "API test", "XMLHttpRequest" -> "XMLHttp request"
     *
     * @param ruleName The camelCase rule name to convert
     * @return The readable format of the rule name
     */
    public static String camelCaseToReadable(String ruleName) {
        if (ruleName == null || ruleName.isEmpty()) {
            return "";
        }

        // Special cases for specific rule names
        if (ruleName.equals("XMLHTTPRequest")) {
            return "XMLHTTP request";
        }
        if (ruleName.equals("APITest")) {
            return "API test";
        }
        if (ruleName.equals("isNaN")) {
            return "Is NaN";
        }

        // Protect well-known acronyms that shouldn't be split or lowercased
        final String NAN_PLACEHOLDER = "N_a_N"; // avoid [a][A] split between a and N
        String protectedName = ruleName.replace("NaN", NAN_PLACEHOLDER);

        // Split the rule name into words
        String[] words = CAMEL_CASE_SPLIT_PATTERN.matcher(protectedName).replaceAll("$1 $2").trim().split(" ");
        List<String> processedWords = new ArrayList<>();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            // Special case for NaN (may be present as placeholder)
            if (word.equals("NaN") || word.equals("N_a_N")) {
                processedWords.add("NaN");
                continue;
            }

            // If word has multiple consecutive capitals at start, preserve them
            if (MULTIPLE_CAPITALS_PATTERN.matcher(word).matches()) {
                Matcher matcher = CAPITALS_REST_PATTERN.matcher(word);
                if (matcher.matches()) {
                    String capitals = matcher.group(1);
                    String rest = matcher.group(2) != null ? matcher.group(2) : "";
                    processedWords.add(capitals + (rest.isEmpty() ? "" : rest.toLowerCase()));
                    continue;
                }
            }

            // Otherwise, lowercase everything
            processedWords.add(word.toLowerCase());
        }

        String result = String.join(" ", processedWords);

        // Add space after words ending with consecutive digits
        result = DIGITS_LETTER_PATTERN.matcher(result).replaceAll("$1 $2");

        // Capitalize only the first word
        if (!result.isEmpty()) {
            result = Character.toUpperCase(result.charAt(0)) + (result.length() > 1 ? result.substring(1) : "");
        }

        // Apply well-known Java name fixes so callers don't need a separate call
        result = fixWellKnownJavaNames(ruleName, result);

        return result;
    }

    /**
     * Applies a set of well-known Java name fixes to a readable rule name.
     * This encapsulates all special cases previously implemented in the Groovy generator
     * so it can be reused across the project.
     *
     * Note: camelCaseToReadable already applies these fixes internally. This method remains
     * public for backward compatibility and potential reuse in other contexts.
     *
     * @param ruleKey The original rule key/name (camelCase), used for conditional fixes
     * @param readableName The readable name produced by camelCaseToReadable
     * @return The normalized readable name with Java-specific fixes
     */
    public static String fixWellKnownJavaNames(String ruleKey, String readableName) {
        if (readableName == null) {
            return null;
        }
        String result = readableName;

        // Fix known acronym splitting like NaN
        if (ruleKey != null && ruleKey.contains("NaN")) {
            result = result.replaceAll("(?i)\\bna\\s+n\\b", "NaN");
        }

        // Fix well-known Java type names that should not be split or lowercased
        if (ruleKey != null && ruleKey.contains("BigDecimal")) {
            result = result.replaceAll("(?i)\\bbig\\s+decimal\\b", "BigDecimal");
        }
        if (ruleKey != null && ruleKey.contains("BigInteger")) {
            result = result.replaceAll("(?i)\\bbig\\s+integer\\b", "BigInteger");
        }
        if (ruleKey != null && ruleKey.contains("Throwable")) {
            result = result.replaceAll("(?i)\\bthrowable\\b", "Throwable");
        }
        if (ruleKey != null && ruleKey.contains("StringBuilder")) {
            result = result.replaceAll("(?i)\\bstring\\s+builder\\b", "StringBuilder");
        }
        if (ruleKey != null && ruleKey.contains("StringBuilder")) {
            result = result.replaceAll("(?i)\\bstring\\s+buffer\\b", "StringBuilder");
        }
        if (ruleKey != null && ruleKey.contains("StringTokenizer")) {
            result = result.replaceAll("(?i)\\bstring\\s+tokenizer\\b", "StringTokenizer");
        }

        // Special casing for MDBAnd… rules: split into words correctly
        if (ruleKey != null && ruleKey.contains("MDBAnd")) {
            result = result.replace("MDBAnd", "MDB and");
        }

        // Additional normalizations for Java class names, acronyms, and method names
        result = result
            // Class and type names
            .replaceAll("(?i)\\bthread\\s+group\\b", "ThreadGroup")
            .replaceAll("(?i)\\bnull\\s+pointer\\s+exception\\b", "NullPointerException")
            .replaceAll("(?i)\\bclass\\s+cast\\s+exception\\b", "ClassCastException")
            .replaceAll("(?i)\\bcloneable\\b", "Cloneable")
            .replaceAll("(?i)\\bclass\\s*loader\\b", "ClassLoader")
            .replaceAll("(?i)\\bconcurrent\\s*hash\\s*map\\b", "ConcurrentHashMap")
            .replaceAll("(?i)\\bfile\\s*item\\b", "FileItem")
            // Java packages
            .replaceAll("(?i)\\bjava\\s*\\.\\s*lang\\s*\\.\\s*error\\b|\\bjava\\s+lang\\s+error\\b", "java.lang.Error")
            .replaceAll("(?i)\\bjava\\s*\\.\\s*lang\\s*\\.\\s*throwable\\b|\\bjava\\s+lang\\s+throwable\\b", "java.lang.Throwable")
            // Acronyms and constants
            .replaceAll("(?i)\\bcrypto\\s*iv\\b", "crypto IV")
            .replaceAll("(?i)\\bncss\\b", "NCSS")
            .replaceAll("(?i)\\bserial\\s*version\\s*uid\\b", "serialVersionUID")
            .replaceAll("(?i)\\bcharsets\\b", "Charsets")
            // Java word capitalization
            .replaceAll("(?i)\\bjava\\s+bean\\b", "Java bean")
            // Collections / class names
            .replaceAll("(?i)\\benumeration\\b", "Enumeration")
            .replaceAll("(?i)\\biterator\\b", "Iterator")
            .replaceAll("(?i)\\bhashtable\\b", "Hashtable")
            .replaceAll("(?i)\\bmap\\b", "Map")
            .replaceAll("(?i)\\bvector\\b", "Vector")
            .replaceAll("(?i)\\blist\\b", "List")
            // Methods and API references
            .replaceAll("(?i)\\bto\\s*array\\b", "toArray")
            .replaceAll("(?i)\\bcollection\\s*\\.\\s*is\\s*empty\\b|\\bcollection\\s+is\\s+empty\\b", "Collection.isEmpty")
            .replaceAll("(?i)\\bnotify\\s*all\\b", "notifyAll")
            .replaceAll("(?i)\\bstandard\\s+charsets\\b", "standard Charsets")
            .replaceAll("(?i)\\bshort\\s+array\\s+initializer\\b", "short Array initializer")
            // Exceptions capitalization
            .replaceAll("(?i)\\bthrows\\s+exception\\b", "throws Exception")
            // Date/Locale APIs
            .replaceAll("(?i)\\bsimple\\s*date\\s*format\\b", "SimpleDateFormat")
            .replaceAll("(?i)\\blocale\\b", "Locale");

        // Special cases based on exact rule keys
        if ("UseArrayListInsteadOfVector".equals(ruleKey)) {
            result = "Use Arrays.asList";
        }
        if ("UselessStringValueOf".equals(ruleKey)) {
            result = "Useless String.valueOf";
        }

        return result;
    }

    /**
     * Escapes special regex replacement characters.
     */
    private static String escapeReplacement(String replacement) {
        return Matcher.quoteReplacement(replacement);
    }

    /**
     * Handles multi-line code blocks.
     */
    private static String handleMultiLineCodeBlocks(String markdownText, Pattern pattern) {
        Matcher matcher = pattern.matcher(markdownText);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String language = matcher.group(1) != null ? matcher.group(1) : "";
            String code = matcher.group(2) != null ? matcher.group(2) : "";

            // Format code with proper spacing and trim trailing whitespace
            code = " " + NEWLINE_PATTERN.matcher(code).replaceAll("\n "); 
            code = TRAILING_WHITESPACE_PATTERN.matcher(code).replaceAll("");

            // Create HTML code block with optional language class
            String langClass = language.isEmpty() ? "" : " class=\"language-" + language + "\"";
            String html = "<pre><code" + langClass + ">" + escapeHtml(code) + "</code></pre>";

            matcher.appendReplacement(sb, escapeReplacement(html));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Handles special patterns in the text.
     */
    private static String handleSpecialPatterns(String text) {
        String result = text;

        result = handleNoteItalicsPattern(result);
        result = handlePmdRuleLinkPattern(result);
        result = handleMarkdownLinkPattern(result);
        result = handleUrlTagPattern(result);

        return result;
    }

    /**
     * Handles URL tags like <http://example.com>.
     */
    private static String handleUrlTagPattern(String result) {
        Matcher urlTagMatcher = URL_TAG_PATTERN.matcher(result);
        StringBuilder sb = new StringBuilder();
        while (urlTagMatcher.find()) {
            String url = urlTagMatcher.group(1);
            String replacement = "<a href=\"" + url + "\">" + url + "</a>";
            urlTagMatcher.appendReplacement(sb, escapeReplacement(replacement));
        }
        urlTagMatcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Handles general markdown links.
     */
    private static String handleMarkdownLinkPattern(String result) {
        Matcher markdownLinkMatcher = MARKDOWN_LINK_PATTERN.matcher(result);
        StringBuilder sb = new StringBuilder();
        while (markdownLinkMatcher.find()) {
            String replacement = "<a href=\"" + markdownLinkMatcher.group(2) + "\">" + markdownLinkMatcher.group(1) + "</a>";
            markdownLinkMatcher.appendReplacement(sb, escapeReplacement(replacement));
        }
        markdownLinkMatcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Handles PMD rule links.
     */
    private static String handlePmdRuleLinkPattern(String result) {
        Matcher ruleLinkMatcher = PMD_RULE_LINK_PATTERN.matcher(result);
        StringBuilder sb = new StringBuilder();
        while (ruleLinkMatcher.find()) {
            String linkText = ruleLinkMatcher.group(1);
            String href = ruleLinkMatcher.group(2);
            String replacement = "<a href=\"https://pmd.github.io/pmd/" + href + "\">" + linkText + "</a>";
            ruleLinkMatcher.appendReplacement(sb, escapeReplacement(replacement));
        }
        ruleLinkMatcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Handles _Note:_ pattern.
     */
    private static String handleNoteItalicsPattern(String result) {
        // Replace _Note:_ with <b>Note:</b> directly
        return result.replace("_Note:_", "<b>Note:</b>");
    }

    /**
     * Handles sections with special patterns.
     */
    private static String handleSections(String text) {
        Matcher matcher = SECTION_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String sectionType = matcher.group(1);
            String content = matcher.group(2) != null ? matcher.group(2).trim() : "";
            String replacement = "<p><b>" + sectionType + ":</b> " + formatInlineElements(content) + "</p>";
            matcher.appendReplacement(sb, escapeReplacement(replacement));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Converts a header paragraph to HTML.
     */
    private static String convertHeader(String headerText) {
        String[] lines = headerText.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            Matcher matcher = HEADER_PATTERN.matcher(line.trim());
            if (matcher.matches()) {
                String hashes = matcher.group(1);
                String content = matcher.group(2);
                int level = hashes.length();
                result.append("<h").append(level).append(">").append(formatInlineElements(content)).append("</h").append(level).append(">");
            } else {
                // Handle continuation lines as regular paragraph content
                if (!line.trim().isEmpty()) {
                    result.append("<p>").append(formatInlineElements(line)).append("</p>");
                }
            }
        }

        return result.toString();
    }

    /**
     * Converts a paragraph with ordered list to HTML.
     */
    private static String convertParagraphWithOrderedList(String paragraph) {
        String[] lines = paragraph.split("\n");
        StringBuilder result = new StringBuilder();
        boolean inList = false;

        for (String line : lines) {
            line = line.trim();
            if (LIST_ITEM_PATTERN.matcher(line).matches()) {
                if (!inList) {
                    result.append("<ol>");
                    inList = true;
                }
                Matcher matcher = LIST_ITEM_PATTERN.matcher(line);
                if (matcher.find()) {
                    result.append("<li>").append(formatInlineElements(matcher.group(3))).append("</li>");
                }
            } else if (!line.isEmpty() && inList) {
                // Continuation of previous list item - add space but no line break
                result.append(" ").append(formatInlineElements(line));
            }
        }

        if (inList) {
            result.append("</ol>");
        }

        return result.toString();
    }

    /**
     * Checks if the lines contain unordered list items.
     */
    private static boolean containsUnorderedListItems(String[] lines) {
        for (String line : lines) {
            if (UNORDERED_LIST_ITEM_PATTERN.matcher(line).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a paragraph with unordered list to HTML.
     */
    private static String convertParagraphWithUnorderedList(String paragraphText) {
        String[] lines = paragraphText.split("\n");
        StringBuilder result = new StringBuilder();
        boolean inList = false;
        boolean paragraphStarted = false;
        boolean inListItem = false;
        StringBuilder currentListItem = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Skip empty lines
            if (trimmedLine.isEmpty()) continue;

            Matcher listItemMatcher = UNORDERED_LIST_ITEM_PATTERN.matcher(line);

            if (listItemMatcher.matches()) {
                // Handle list item start

                // Close paragraph if needed
                if (paragraphStarted && !inList) {
                    paragraphStarted = false;
                }

                // Close previous list item if needed
                if (inListItem) {
                    result.append("<li>").append(currentListItem).append("</li>");
                    currentListItem = new StringBuilder();
                }

                // Start list if needed
                if (!inList) {
                    result.append("<ul>");
                    inList = true;
                }

                // Add content to the new list item
                currentListItem.append(formatInlineElements(listItemMatcher.group(2)));
                inListItem = true;

            } else if (inList) {
                // Handle content within a list

                Matcher continuationMatcher = LIST_ITEM_CONTINUATION_PATTERN.matcher(line);

                if (inListItem) {
                    // Add continuation content to current list item
                    currentListItem.append(" ");

                    if (continuationMatcher.matches()) {
                        // Indented continuation line
                        currentListItem.append(formatInlineElements(continuationMatcher.group(1)));
                    } else {
                        // Regular continuation line
                        currentListItem.append(formatInlineElements(trimmedLine));
                    }
                }

            } else {
                // Handle regular paragraph text

                if (!paragraphStarted) {
                    result.append("<p>");
                    paragraphStarted = true;
                }

                result.append(formatInlineElements(trimmedLine));
            }
        }

        // Close any open elements
        if (inListItem) {
            result.append("<li>").append(currentListItem).append("</li>");
        }

        if (inList) {
            result.append("</ul>");
        }

        if (paragraphStarted) {
            result.append("</p>");
        }

        return result.toString();
    }

    /**
     * Formats inline elements in the text.
     */
    private static String formatInlineElements(String text) {
        if (text == null || text.isEmpty()) return "";

        // Skip formatting for content inside <pre> tags
        if (text.contains("<pre>")) {
            return processTextWithPreBlocks(text);
        }

        return formatTextWithoutPre(text);
    }

    /**
     * Process text that contains <pre> blocks by extracting them,
     * formatting the parts outside the blocks, and then restoring the blocks.
     */
    private static String processTextWithPreBlocks(String text) {
        // Extract pre blocks and replace with placeholders
        PreProcessingResult result = extractPreBlocksWithPlaceholders(text);

        // Format text between pre blocks
        String processedText = formatTextBetweenPreBlocks(result.processedText);

        // Restore pre blocks
        return restorePreBlocks(processedText);
    }

    /**
     * Extracts <pre> blocks from text and replaces them with placeholders.
     */
    private static PreProcessingResult extractPreBlocksWithPlaceholders(String text) {
        Matcher matcher = PRE_BLOCK_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            // Get the <pre> block (including tags)
            String preBlock = matcher.group(1);

            // Replace the <pre> block with a placeholder
            matcher.appendReplacement(sb, escapeReplacement("PRE_BLOCK_PLACEHOLDER"));

            // Store the <pre> block
            sb.append("PRE_BLOCK_START");
            sb.append(preBlock);
            sb.append("PRE_BLOCK_END");
        }
        matcher.appendTail(sb);

        return new PreProcessingResult(sb.toString());
    }

    /**
     * Formats text between <pre> blocks, ignoring the content inside <pre> blocks.
     */
    private static String formatTextBetweenPreBlocks(String processedText) {
        String[] parts = processedText.split("PRE_BLOCK_PLACEHOLDER");

        // Format each part outside <pre> tags
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].contains("PRE_BLOCK_START")) {
                parts[i] = formatTextWithoutPre(parts[i]);
            }
        }

        return String.join("", parts);
    }

    /**
     * Restores <pre> blocks from placeholder markers.
     */
    private static String restorePreBlocks(String processedText) {
        Matcher blockMatcher = PRE_BLOCK_PLACEHOLDER_PATTERN.matcher(processedText);
        StringBuilder result = new StringBuilder();

        while (blockMatcher.find()) {
            String preBlock = blockMatcher.group(1);
            blockMatcher.appendReplacement(result, escapeReplacement(preBlock));
        }
        blockMatcher.appendTail(result);

        return result.toString();
    }

    /**
     * Simple class to hold the result of pre-processing text with <pre> blocks.
     */
    private static class PreProcessingResult {
        final String processedText;

        PreProcessingResult(String processedText) {
            this.processedText = processedText;
        }
    }

    /**
     * Formats text without <pre> blocks.
     */
    private static String formatTextWithoutPre(String text) {
        if (text == null || text.isEmpty()) return "";

        String result = text;

        // First convert backticks to <code>...</code>
        result = handleCodeBlockPattern(result);

        // Apply rule and jdoc references only outside of <code>...</code> blocks
        Matcher codeTagMatcher = CODE_TAG_PATTERN.matcher(result);
        StringBuilder assembled = new StringBuilder();
        int lastEnd = 0;
        while (codeTagMatcher.find()) {
            String before = result.substring(lastEnd, codeTagMatcher.start());
            before = handleRuleReferencePattern(before);
            before = handleJdocPattern(before);
            assembled.append(before);
            assembled.append(codeTagMatcher.group(1));
            lastEnd = codeTagMatcher.end();
        }
        String tail = result.substring(lastEnd);
        tail = handleRuleReferencePattern(tail);
        tail = handleJdocPattern(tail);
        assembled.append(tail);
        result = assembled.toString();

        // Bold/Italics already avoid <code> blocks
        result = handleMarkdownBoldPattern(result);
        result = handleMarkdownItalicsPattern(result);

        return result;
    }

    /**
     * Handles markdown italics pattern.
     */
    private static String handleMarkdownItalicsPattern(String result) {
        return applyOutsideCodeTags(result, MARKDOWN_ITALICS_PATTERN, "<i>", "</i>");
    }

    /**
     * Handles markdown bold pattern.
     */
    private static String handleMarkdownBoldPattern(String result) {
        return applyOutsideCodeTags(result, MARKDOWN_BOLD_PATTERN, "<b>", "</b>");
    }

    /**
     * Handles jdoc references.
     */
    private static String handleJdocPattern(String result) {
        Matcher jdocMatcher = JDOC_REFERENCE_PATTERN.matcher(result);
        StringBuilder sbJdoc = new StringBuilder();
        while (jdocMatcher.find()) {
            String replacement = createJdocReference(jdocMatcher);
            jdocMatcher.appendReplacement(sbJdoc, escapeReplacement(replacement));
        }
        jdocMatcher.appendTail(sbJdoc);
        return sbJdoc.toString();
    }

    /**
     * Creates a jdoc reference link.
     */
    private static String createJdocReference(Matcher match) {
        String fullyQualifiedName = match.group(2);

        // Extract class name and member name if present
        int hashIndex = fullyQualifiedName.indexOf('#');
        String className = hashIndex > 0 ? fullyQualifiedName.substring(0, hashIndex) : fullyQualifiedName;
        String memberName = hashIndex > 0 ? fullyQualifiedName.substring(hashIndex + 1) : "";

        // Build URL and determine display text
        String urlPath = className.replace('.', '/');
        String url = jdocBase() + urlPath + ".html" + (memberName.isEmpty() ? "" : "#" + memberName);
        String displayText = memberName.isEmpty() ? className.substring(className.lastIndexOf('.') + 1) : memberName;

        return escapeReplacement("<a href=\"" + url + "\"><code>" + displayText + "</code></a>");
    }

    /**
     * Handles rule references.
     */
    private static String handleRuleReferencePattern(String result) {
        Matcher ruleRefMatcher = RULE_REFERENCE_PATTERN.matcher(result);
        StringBuilder sb = new StringBuilder();
        while (ruleRefMatcher.find()) {
            String replacement = "<code>" + escapeHtml(ruleRefMatcher.group(1)) + "</code>";
            ruleRefMatcher.appendReplacement(sb, escapeReplacement(replacement));
        }
        ruleRefMatcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Handles code blocks.
     */
    private static String handleCodeBlockPattern(String result) {
        if (result == null || result.isEmpty()) return "";

        StringBuilder out = new StringBuilder();
        boolean inHtmlCode = false;
        boolean inBacktick = false;
        StringBuilder backtickBuf = new StringBuilder();
        int i = 0;
        while (i < result.length()) {
            // Detect start/end of real HTML <code> blocks to avoid processing inside them
            if (!inBacktick && result.startsWith("<code>", i)) {
                inHtmlCode = true;
                out.append("<code>");
                i += 6;
                continue;
            }
            if (!inBacktick && inHtmlCode && result.startsWith("</code>", i)) {
                inHtmlCode = false;
                out.append("</code>");
                i += 7;
                continue;
            }

            if (!inHtmlCode) {
                char ch = result.charAt(i);
                if (ch == '`') {
                    if (!inBacktick) {
                        inBacktick = true;
                        backtickBuf.setLength(0);
                    } else {
                        // closing backtick -> emit code
                        String codeContent = backtickBuf.toString();
                        // strip literal <code> tags inside backticks
                        codeContent = codeContent.replaceAll("(?i)</?code>", "");
                        out.append("<code>").append(escapeHtml(codeContent)).append("</code>");
                        inBacktick = false;
                    }
                    i++;
                    continue;
                }

                if (inBacktick) {
                    backtickBuf.append(ch);
                } else {
                    out.append(ch);
                }
                i++;
            } else {
                // inside existing HTML <code> block: copy as-is
                out.append(result.charAt(i));
                i++;
            }
        }

        // If we ended while still in backticks, treat as literal text (put back the opening backtick)
        if (inBacktick) {
            out.append('`').append(backtickBuf);
        }

        return out.toString();
    }

    /**
     * Escapes HTML special characters.
     */
    private static String escapeHtml(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Fixes the order of paragraphs and lists in the HTML output.
     */
    private static String fixParagraphListOrder(String html) {
        // Split the HTML into paragraphs and lists
        String[] parts = html.split("\n");

        // If we have fewer than 3 parts, there's nothing to fix
        if (parts.length < 3) {
            return html;
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
                    String temp = parts[i+1];
                    parts[i+1] = parts[i+2];
                    parts[i+2] = temp;
                    break;
                }
            }
        }

        // Join the parts back together
        return String.join("\n", parts);
    }

    /**
     * Extract <pre> blocks and replace them with placeholders.
     */
    private static String extractPreBlocks(String text, List<String> preBlocks) {
        Matcher matcher = PRE_BLOCK_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            preBlocks.add(matcher.group(0));
            matcher.appendReplacement(sb, "PRE_BLOCK_" + (preBlocks.size() - 1) + "_PLACEHOLDER");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Process a paragraph that contains <pre> blocks.
     */
    private static String processPreBlockParagraph(String paragraph) {
        // Extract all <pre> blocks from the paragraph
        List<String> preBlocks = new ArrayList<>();
        Matcher matcher = PRE_BLOCK_PATTERN.matcher(paragraph);
        StringBuilder sb = new StringBuilder();

        // Replace <pre> blocks with placeholders
        int index = 0;
        while (matcher.find()) {
            preBlocks.add(matcher.group(0));
            matcher.appendReplacement(sb, "PRE_BLOCK_" + index++ + "_PLACEHOLDER");
        }
        matcher.appendTail(sb);

        // Process the text outside <pre> blocks
        String textWithoutPre = sb.toString();
        String[] lines = textWithoutPre.split("\n");

        // Process as header or regular paragraph
        String processedText = lines.length > 0 && HEADER_PATTERN.matcher(lines[0].trim()).matches()
                ? convertHeader(textWithoutPre)
                : "<p>" + formatInlineElements(textWithoutPre) + "</p>";

        // Restore <pre> blocks
        for (int i = 0; i < preBlocks.size(); i++) {
            processedText = processedText.replace("PRE_BLOCK_" + i + "_PLACEHOLDER", preBlocks.get(i));
        }

        return processedText;
    }

    /**
     * Applies a markdown pattern replacement only outside of <code>...</code> blocks.
     * The replacement wraps the matched group(1) with the provided tags after escaping HTML.
     */
    private static String applyOutsideCodeTags(String text, Pattern markdownPattern, String openTag, String closeTag) {
        if (text == null || text.isEmpty()) return "";
        Matcher codeTagMatcher = CODE_TAG_PATTERN.matcher(text);
        StringBuilder out = new StringBuilder();
        int lastEnd = 0;
        while (codeTagMatcher.find()) {
            // Process text before the <code> block
            String before = text.substring(lastEnd, codeTagMatcher.start());
            out.append(applyMarkdownPattern(before, markdownPattern, openTag, closeTag));
            // Append the <code> block unchanged
            out.append(codeTagMatcher.group(1));
            lastEnd = codeTagMatcher.end();
        }
        // Process the remaining text after the last <code> block
        String after = text.substring(lastEnd);
        out.append(applyMarkdownPattern(after, markdownPattern, openTag, closeTag));
        return out.toString();
    }

    /**
     * Applies a single markdown regex replacement to the given text.
     */
    private static String applyMarkdownPattern(String text, Pattern markdownPattern, String openTag, String closeTag) {
        if (text == null || text.isEmpty()) return "";
        Matcher m = markdownPattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String replacement = openTag + escapeHtml(m.group(1)) + closeTag;
            m.appendReplacement(sb, escapeReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

}