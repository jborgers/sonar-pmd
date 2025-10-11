package org.sonar.plugins.pmd.xml;

import org.sonar.plugins.pmd.PmdConstants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PmdRule {

    private String ref;
    private Integer priority;
    private String name;
    private String message;
    private String clazz;
    private String language;
    private List<PmdProperty> properties = new ArrayList<>();

    public PmdRule(String ref) {
        this(ref, null);
    }

    public PmdRule(String ref, @Nullable Integer priority) {
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
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
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

    public void processXpath(String sonarRuleKey) {
        if (PmdConstants.XPATH_CLASS.equals(ref)) {
            ref = null;
            PmdProperty xpathMessage = getProperty(PmdConstants.XPATH_MESSAGE_PARAM);
            if (xpathMessage == null) {
                throw new IllegalArgumentException("Property '" + PmdConstants.XPATH_MESSAGE_PARAM + "' should be set for PMD rule " + sonarRuleKey);
            }

            message = xpathMessage.getValue();
            removeProperty(PmdConstants.XPATH_MESSAGE_PARAM);
            PmdProperty xpathExp = getProperty(PmdConstants.XPATH_EXPRESSION_PARAM);

            if (xpathExp == null) {
                throw new IllegalArgumentException("Property '" + PmdConstants.XPATH_EXPRESSION_PARAM + "' should be set for PMD rule " + sonarRuleKey);
            }

            xpathExp.setCdataValue(xpathExp.getValue());
            clazz = PmdConstants.XPATH_CLASS;
            language = PmdConstants.LANGUAGE_JAVA_KEY;
            name = sonarRuleKey;
        }
    }
}
