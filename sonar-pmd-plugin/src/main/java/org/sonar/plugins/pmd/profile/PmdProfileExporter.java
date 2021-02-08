/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2021 SonarSource SA and others
 * mailto:jens AT gerdes DOT digital
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
package org.sonar.plugins.pmd.profile;

import java.io.Writer;

import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.plugins.pmd.PmdConstants;
import org.sonar.plugins.pmd.xml.PmdRuleSet;
import org.sonar.plugins.pmd.xml.PmdRuleSets;

/**
 * ServerSide component that is able to export all currently active PMD rules as XML.
 */
public class PmdProfileExporter extends ProfileExporter {

    private static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";

    public PmdProfileExporter() {
        super(PmdConstants.REPOSITORY_KEY, PmdConstants.PLUGIN_NAME);
        setSupportedLanguages(PmdConstants.LANGUAGE_KEY);
        setMimeType(CONTENT_TYPE_APPLICATION_XML);
    }

    @Override
    public void exportProfile(RulesProfile profile, Writer writer) {

        final PmdRuleSet tree = PmdRuleSets.from(profile, PmdConstants.REPOSITORY_KEY);

        try {
            tree.writeTo(writer);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("An exception occurred while generating the PMD configuration file from profile: " + profile.getName(), e);
        }
    }
}
