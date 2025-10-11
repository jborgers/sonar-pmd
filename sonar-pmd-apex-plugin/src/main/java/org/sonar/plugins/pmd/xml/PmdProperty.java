package org.sonar.plugins.pmd.xml;

public class PmdProperty {
    private final String name;
    private String value;
    private String cdataValue;

    public PmdProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setCdataValue(String cdataValue) {
        this.cdataValue = cdataValue;
    }

    public String getCdataValue() {
        return cdataValue;
    }

    public boolean isCdataValue() {
        return cdataValue != null;
    }
}
