/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.pmd.xml;

import com.google.common.collect.Lists;

import javax.annotation.Nullable;

import java.util.List;

public class PmdRule {

  private String ref;

  private String priority;

  private String name;

  private String message;

  private List<PmdProperty> properties = Lists.newArrayList();

  private String clazz;

  private String language;

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

  public void setProperties(List<PmdProperty> properties) {
    this.properties = properties;
  }

  public List<PmdProperty> getProperties() {
    return properties;
  }

  public PmdProperty getProperty(String propertyName) {
    for (PmdProperty prop : properties) {
      if (propertyName.equals(prop.getName())) {
        return prop;
      }
    }
    return null;
  }

  public int compareTo(String o) {
    return o.compareTo(ref);
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

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setClazz(String clazz) {
    this.clazz = clazz;
  }

  public String getClazz() {
    return clazz;
  }

  public void setRef(@Nullable String ref) {
    this.ref = ref;
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
