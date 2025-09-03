/*
 * SonarQube PMD7 Plugin
 * Copyright (C) 2012-2021 SonarSource SA and others
 * mailto:jborgers AT jpinpoint DOT com; peter.paul.bakker AT stokpop DOT nl
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pmd.rule.util;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility to format PMD rule parameter metadata (description, select lists, etc.).
 */
public final class RuleParamFormatter {

    private RuleParamFormatter() {
        // utility
    }

    // Precompiled, safe regex:
    // - Possessive quantifiers (*+, ++) avoid backtracking on whitespace/content.
    // - Atomic alternation (?>...) avoids backtracking between "Possible" and "Allowed".
    // - Character class [^]]*+ guarantees linear scan inside brackets.
    private static final Pattern VALUES_FRAGMENT_PATTERN = Pattern.compile(
            "\\s*+(?:(?>Possible|Allowed))\\s++values:\\s*+\\[[^]]*+]\\.?+\\s*+",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Build a parameter description based on an existing XML description and/or a description
     * from the extracted property info, and optionally append a standardized list of allowed values.
     *
     * @param existingPropDescription The description from PMD XML (if present).
     * @param propInfoDescription     The description coming from the PropertyInfo (fallback).
     * @param acceptedValues          Accepted values for the parameter (optional).
     * @param multiple                Whether multiple values can be selected.
     * @return The normalized description text to include in the rules XML.
     */
    public static String buildDescription(@Nullable String existingPropDescription,
                                          @Nullable String propInfoDescription,
                                          @Nullable List<String> acceptedValues,
                                          boolean multiple) {
        String baseDesc = normalizeWhitespace(firstNonBlank(existingPropDescription, propInfoDescription, ""));

        boolean useSelect = acceptedValues != null && !acceptedValues.isEmpty();
        if (useSelect) {
            // Remove any pre-existing Allowed/Possible values fragments to avoid duplication
            baseDesc = VALUES_FRAGMENT_PATTERN.matcher(baseDesc).replaceAll(" ").trim();

            String suffix = multiple ? " Select one or more values." : " Select one of the values.";
            String joinedValues = acceptedValues.stream().filter(Objects::nonNull).collect(Collectors.joining(","));
            baseDesc = baseDesc + sentenceSeparator(baseDesc) + "Allowed values: [" + joinedValues + "]." + suffix;
        }

        return baseDesc;
    }

    /**
     * Determines the sentence separator to append after the given text:
     * - Returns "" if the text is empty
     * - Returns " " if the text already ends with terminal punctuation (., !, ?)
     * - Returns ". " otherwise
     */
    private static String sentenceSeparator(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        char last = text.charAt(text.length() - 1);
        boolean endsWithTerminal = last == '.' || last == '!' || last == '?';
        return endsWithTerminal ? " " : ". ";
    }


    /**
     * Construct the RuleParamType token for a select list.
     * Example: SINGLE_SELECT_LIST,multiple=true,values="a,b,c"
     */
    public static String buildSelectTypeToken(List<String> acceptedValues, boolean multiple) {
        String innerCsv = acceptedValues.stream()
                .map(v -> v == null ? "" : v.replace("\"", "\"\""))
                .collect(Collectors.joining(","));
        String valuesToken = '"' + innerCsv + '"';
        if (multiple) {
            return "SINGLE_SELECT_LIST,multiple=true,values=" + valuesToken;
        }
        return "SINGLE_SELECT_LIST,values=" + valuesToken;
    }

    private static String firstNonBlank(String a, String b, String fallback) {
        if (a != null && !a.trim().isEmpty()) return a;
        if (b != null && !b.trim().isEmpty()) return b;
        return fallback;
    }

    private static String normalizeWhitespace(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ").trim();
    }
}
