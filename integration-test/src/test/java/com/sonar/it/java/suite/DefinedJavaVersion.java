package com.sonar.it.java.suite;

import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Provides the defined Java versions.
 * Copied from org.apache.commons.lang3.JavaVersion which didn't keep up with the new Java releases.
 * Adds the preview release compared to JavaVersion.
 */
public enum DefinedJavaVersion {
    JAVA_1_8(1.8F, "1.8"),
    /** @deprecated */
    @Deprecated
    JAVA_1_9(9.0F, "9"),
    JAVA_9(9.0F, "9"),
    JAVA_10(10.0F, "10"),
    JAVA_11(11.0F, "11"),
    JAVA_12(12.0F, "12"),
    JAVA_13(13.0F, "13"),
    JAVA_14(14.0F, "14"),
    JAVA_15(15.0F, "15"),
    JAVA_16(16.0F, "16"),
    JAVA_17(17.0F, "17"),
    JAVA_18(18.0F, "18"),
    JAVA_19(19.0F, "19"),
    JAVA_20(20.0F, "20"),
    JAVA_21(21.0F, "21"),
    JAVA_22(22.0F, "22"),
    JAVA_23(23.0F, "23"),
    JAVA_24(24.0F, "24"),
    JAVA_25(25.0F, "25"),
    JAVA_25_PREVIEW(25.5F, "25-preview"),
    JAVA_RECENT(maxVersion(), Float.toString(maxVersion()));

    private final float value;
    private final String name;

    static DefinedJavaVersion get(String versionStr) {
        if (versionStr == null) {
            return null;
        } else {
            switch (versionStr) {
                case "1.8":
                    return JAVA_1_8;
                case "9":
                    return JAVA_9;
                case "10":
                    return JAVA_10;
                case "11":
                    return JAVA_11;
                case "12":
                    return JAVA_12;
                case "13":
                    return JAVA_13;
                case "14":
                    return JAVA_14;
                case "15":
                    return JAVA_15;
                case "16":
                    return JAVA_16;
                case "17":
                    return JAVA_17;
                case "18":
                    return JAVA_18;
                case "19":
                    return JAVA_19;
                case "20":
                    return JAVA_20;
                case "21":
                    return JAVA_21;
                case "22":
                    return JAVA_22;
                case "23":
                    return JAVA_23;
                case "24":
                    return JAVA_24;
                case "25":
                    return JAVA_25;
                case "25-preview":
                    return JAVA_25_PREVIEW;
                default:
                    float v = toFloatVersion(versionStr);
                    if ((double)v - (double)1.0F < (double)1.0F) {
                        int firstComma = Math.max(versionStr.indexOf(46), versionStr.indexOf(44));
                        int end = Math.max(versionStr.length(), versionStr.indexOf(44, firstComma));
                        if (Float.parseFloat(versionStr.substring(firstComma + 1, end)) > 0.9F) {
                            return JAVA_RECENT;
                        }
                    } else if (v > 10.0F) {
                        return JAVA_RECENT;
                    }

                    return null;
            }
        }
    }

    static DefinedJavaVersion getJavaVersion(String versionStr) {
        return get(versionStr);
    }

    private static float maxVersion() {
        float v = toFloatVersion(SystemProperties.getJavaSpecificationVersion("99.0"));
        return v > 0.0F ? v : 99.0F;
    }

    static String[] split(String value) {
        return value.split("\\.");
    }

    private static float toFloatVersion(String value) {
        if (!value.contains(".")) {
            return NumberUtils.toFloat(value, -1.0F);
        } else {
            String[] toParse = split(value);
            return toParse.length >= 2 ? NumberUtils.toFloat(toParse[0] + '.' + toParse[1], -1.0F) : -1.0F;
        }
    }

    DefinedJavaVersion(float value, String name) {
        this.value = value;
        this.name = name;
    }

    public boolean atLeast(DefinedJavaVersion requiredVersion) {
        return this.value >= requiredVersion.value;
    }

    public boolean atMost(DefinedJavaVersion requiredVersion) {
        return this.value <= requiredVersion.value;
    }

    public String toString() {
        return this.name;
    }
}

