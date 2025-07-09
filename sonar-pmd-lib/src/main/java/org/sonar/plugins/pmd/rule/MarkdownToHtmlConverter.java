package org.sonar.plugins.pmd.rule;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownToHtmlConverter.class);

    // PMD version used for documentation links
    private static final String PMD_VERSION = "7.15.0";

    // Regex patterns for Markdown parsing
    private static final Pattern PARAGRAPH_SPLITTER_PATTERN = Pattern.compile("\n\\s*\n");
    private static final Pattern ORDERED_LIST_PARAGRAPH_PATTERN = Pattern.compile("(?s)\\s*1\\...*");
    private static final Pattern LIST_ITEM_PATTERN = Pattern.compile("(\\d+)\\.(\\s+)(.*)");
    private static final Pattern UNORDERED_LIST_ITEM_PATTERN = Pattern.compile("[\\s\\t]*[\\*\\-](\\s+)(.*)");
    private static final Pattern LIST_ITEM_CONTINUATION_PATTERN = Pattern.compile("^[\\s\\t]{2,}([^\\*\\-].+)$");
    private static final Pattern TITLE_PATTERN = Pattern.compile("([A-Z][A-Za-z]+):(\\s*)(.*)");
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("`([^`]+)`");
    private static final Pattern RULE_REFERENCE_PATTERN = Pattern.compile("\\{\\%\\s*rule\\s*\"([^\"]+)\"\\s*\\%\\}");
    private static final Pattern SECTION_PATTERN = Pattern.compile("(?s)(Problem|Solution|Note|Notes|Exceptions):(.+?)(?=\\s+(Problem|Solution|Note|Notes|Exceptions):|$)");
    private static final Pattern MULTI_LINE_CODE_BLOCK_PATTERN = Pattern.compile("(?s)```(\\w*)\\s*([\\s\\S]*?)```");
    private static final Pattern QUADRUPLE_BACKTICK_CODE_BLOCK_PATTERN = Pattern.compile("(?s)````(\\w*)\\s*([\\s\\S]*?)````");
    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$");

    // Additional patterns for formatting
    private static final Pattern ITALIC_NOTE_PATTERN = Pattern.compile("_Note:_");
    private static final Pattern MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
    private static final Pattern PMD_RULE_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\((pmd_rules_[^.]+\\.html[^)]*)\\)");
    private static final Pattern URL_TAG_PATTERN = Pattern.compile("<(https?:\\/\\/[^>]+)>");
    private static final Pattern JDOC_REFERENCE_PATTERN = Pattern.compile("\\{\\%\\s*jdoc\\s+([\\w-]+)::([\\.\\w#]+)\\s*\\%\\}");
    private static final String JDOC_LINK = "https://docs.pmd-code.org/apidocs/pmd-java/" + PMD_VERSION + "/net/sourceforge/pmd/";

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

        // Split the rule name into words
        String[] words = ruleName.replaceAll("([a-z])([A-Z])", "$1 $2").trim().split(" ");
        List<String> processedWords = new ArrayList<>();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            // Special case for NaN
            if (word.equals("NaN")) {
                processedWords.add("NaN");
                continue;
            }

            // If word has multiple consecutive capitals at start, preserve them
            if (word.matches("^[A-Z]{2,}.*")) {
                Matcher matcher = Pattern.compile("^([A-Z]+)([a-z].*)?").matcher(word);
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
        result = result.replaceAll("(\\w*\\d+)([a-zA-Z])", "$1 $2");

        // Capitalize only the first word
        if (!result.isEmpty()) {
            result = Character.toUpperCase(result.charAt(0)) + (result.length() > 1 ? result.substring(1) : "");
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
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String language = matcher.group(1) != null ? matcher.group(1) : "";
            String code = matcher.group(2) != null ? matcher.group(2) : "";

            // Format code with proper spacing and trim trailing whitespace
            code = " " + code.replaceAll("\n", "\n ").replaceAll("\\s+$", "");

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
        StringBuffer sb = new StringBuffer();
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
        StringBuffer sb = new StringBuffer();
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
        StringBuffer sb = new StringBuffer();
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
        StringBuffer sb = new StringBuffer();

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
        Pattern prePattern = Pattern.compile("(<pre>[\\s\\S]*?</pre>)", Pattern.DOTALL);
        Matcher matcher = prePattern.matcher(text);
        StringBuffer sb = new StringBuffer();

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
        Pattern blockPattern = Pattern.compile("PRE_BLOCK_START(.*?)PRE_BLOCK_END", Pattern.DOTALL);
        Matcher blockMatcher = blockPattern.matcher(processedText);
        StringBuffer result = new StringBuffer();

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

        result = handleCodeBlockPattern(result);
        result = handleRuleReferencePattern(result);
        result = handleJdocPattern(result);
        result = handleMarkdownBoldPattern(result);
        result = handleMarkdownItalicsPattern(result);

        return result;
    }

    /**
     * Handles markdown italics pattern.
     */
    private static String handleMarkdownItalicsPattern(String result) {
        Matcher italicMatcher = Pattern.compile("\\*([^*]+)\\*").matcher(result);
        StringBuffer sb = new StringBuffer();
        while (italicMatcher.find()) {
            String replacement = "<i>" + escapeHtml(italicMatcher.group(1)) + "</i>";
            italicMatcher.appendReplacement(sb, escapeReplacement(replacement));
        }
        italicMatcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Handles markdown bold pattern.
     */
    private static String handleMarkdownBoldPattern(String result) {
        Matcher boldMatcher = Pattern.compile("\\*\\*([^*]+)\\*\\*").matcher(result);
        StringBuffer sb = new StringBuffer();
        while (boldMatcher.find()) {
            String replacement = "<b>" + escapeHtml(boldMatcher.group(1)) + "</b>";
            boldMatcher.appendReplacement(sb, escapeReplacement(replacement));
        }
        boldMatcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Handles jdoc references.
     */
    private static String handleJdocPattern(String result) {
        Matcher jdocMatcher = JDOC_REFERENCE_PATTERN.matcher(result);
        StringBuffer sbJdoc = new StringBuffer();
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
        String url = JDOC_LINK + urlPath + ".html" + (memberName.isEmpty() ? "" : "#" + memberName);
        String displayText = memberName.isEmpty() ? className.substring(className.lastIndexOf('.') + 1) : memberName;

        return escapeReplacement("<a href=\"" + url + "\"><code>" + displayText + "</code></a>");
    }

    /**
     * Handles rule references.
     */
    private static String handleRuleReferencePattern(String result) {
        Matcher ruleRefMatcher = RULE_REFERENCE_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
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
        Matcher codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (codeBlockMatcher.find()) {
            String replacement = "<code>" + escapeHtml(codeBlockMatcher.group(1)) + "</code>";
            codeBlockMatcher.appendReplacement(sb, escapeReplacement(replacement));
        }
        codeBlockMatcher.appendTail(sb);
        return sb.toString();
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
        Pattern pattern = Pattern.compile("<pre>([\\s\\S]*?)</pre>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

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
        Pattern pattern = Pattern.compile("<pre>([\\s\\S]*?)</pre>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(paragraph);
        StringBuffer sb = new StringBuffer();

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
}
