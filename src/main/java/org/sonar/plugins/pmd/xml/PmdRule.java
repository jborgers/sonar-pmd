/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.plugins.pmd.xml;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class PmdRule {

    private String ref;
    private String priority;
    private String name;
    private String message;
    private String clazz;
    private String language;
    private List<PmdProperty> properties = new ArrayList<>();

    public PmdRule(String ref) {
        this(ref, null);
    }

    public PmdRule(String ref, @Nullable String priority) {
        this.ref = ref;
        this.priority = priority;
    }

    @Nullable
    public String getRef() {
        return ref;
    }

    public void setRef(@Nullable String ref) {
        this.ref = ref;
    }

    public List<PmdProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<PmdProperty> properties) {
        this.properties = properties;
    }

    public PmdProperty getProperty(String propertyName) {
        for (PmdProperty prop : properties) {
            if (propertyName.equals(prop.getName())) {
                return prop;
            }
        }
        return null;
    }

    @Nullable
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void addProperty(PmdProperty property) {
        properties.add(property);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public void removeProperty(String propertyName) {
        PmdProperty prop = getProperty(propertyName);
        properties.remove(prop);
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    @Nullable
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
