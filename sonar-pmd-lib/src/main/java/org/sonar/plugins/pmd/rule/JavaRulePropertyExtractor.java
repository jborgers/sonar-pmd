package org.sonar.plugins.pmd.rule;

import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import net.sourceforge.pmd.properties.PropertySource;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to extract property information from Java rule classes in PMD jar files.
 * This class loads rule classes from the jar files, identifies those that extend AbstractJavaRule,
 * and extracts property information from them.
 */
public class JavaRulePropertyExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaRulePropertyExtractor.class);

    static final String ABSTRACT_RULE_CLASS_NAME = AbstractRule.class.getName();

    // Security thresholds to prevent ZIP bomb attacks
    private static final int THRESHOLD_ENTRIES = 10_000;
    private static final int THRESHOLD_SIZE_BYTES = 10_000_000; // 10 MB
    private static final double THRESHOLD_RATIO = 15; // Increased to accommodate legitimate JAR files

    /**
     * Extracts property information from Java rule classes in the specified jar file.
     *
     * @param jarFilePath Path to the PMD jar file
     * @return Map of rule class names to their property information
     */
    @SuppressWarnings("java:S5042") // security warning for ZIP bomb attack: implemented countermeasures
    public Map<String, List<PropertyInfo>> extractProperties(String jarFilePath) throws IOException {
        // Create a map that returns an empty list for any key that's not in the map
        Map<String, List<PropertyInfo>> result = new HashMap<String, List<PropertyInfo>>() {
            @Override
            public List<PropertyInfo> get(Object key) {
                List<PropertyInfo> value = super.get(key);
                return value != null ? value : Collections.emptyList();
            }
        };

        try (JarFile jarFile = new JarFile(new File(jarFilePath))) {
            // Create a class loader for the jar file
            URL[] urls = { new URL("file:" + jarFilePath) };
            try (URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader())) {
                // Find all class files in the jar
                Enumeration<JarEntry> entries = jarFile.entries();

                // Variables to track security thresholds for preventing ZIP bomb attacks
                int totalEntryArchive = 0;
                long totalSizeArchive = 0;

                while (entries.hasMoreElements() && totalEntryArchive < THRESHOLD_ENTRIES) {
                    totalEntryArchive++;
                    JarEntry entry = entries.nextElement();

                    // Check for ZIP bomb based on compression ratio
                    if (entry.getSize() > 0 && entry.getCompressedSize() > 0) {
                        double compressionRatio = (double) entry.getSize() / entry.getCompressedSize();
                        if (compressionRatio > THRESHOLD_RATIO) {
                            String msg = "Suspicious compression ratio detected in jar file: " + jarFilePath + ", entry: " + entry.getName() + ", ratio: " + compressionRatio + ". Possible ZIP bomb attack. Skipping rule extraction.";
                            LOGGER.error(msg,
                                    jarFilePath, entry.getName(), compressionRatio);
                            throw new PossibleZipBombException(msg);
                        }
                    }

                    // Track total uncompressed size
                    totalSizeArchive += entry.getSize();
                    if (totalSizeArchive > THRESHOLD_SIZE_BYTES) {
                        String msg = "Total uncompressed size exceeds threshold in jar file: " +jarFilePath + ". Possible ZIP bomb attack. Skipping rule extraction.";
                        LOGGER.error(msg);
                        throw new PossibleZipBombException(msg);
                    }

                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace('/', '.').replace(".class", "");
                        try {
                            // Load the class
                            Class<?> clazz = classLoader.loadClass(className);

                            // Check if the class extends AbstractJavaRule
                            if (isRuleClass(clazz)) {
                                // Extract property information
                                List<PropertyInfo> properties = extractPropertyInfo(clazz);
                                // Always add the rule to the map, even if it has no properties
                                result.put(className, properties);
                            }
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            // Skip classes that can't be loaded
                            LOGGER.debug("Could not load class: {}", className, e);
                        }
                    }
                }

                if (totalEntryArchive >= THRESHOLD_ENTRIES) {
                    String msg = "Too many entries in jar file: " + jarFilePath + ". Possible ZIP bomb attack. Skipping rule extraction.";
                    LOGGER.error(msg);
                    throw new PossibleZipBombException(msg);
                }
                LOGGER.info("Extracted {} rule properties from jar file: {}", result.size(), jarFilePath);
            }
        }

        return result;
    }

    /**
     * Checks if the given class is a rule class (extends AbstractJavaRule).
     */
    private boolean isRuleClass(Class<?> clazz) {
        Class<?> superClass = clazz.isAssignableFrom(AbstractRule.class) ? clazz : clazz.getSuperclass();
        while (superClass != null) {
            if (ABSTRACT_RULE_CLASS_NAME.equals(superClass.getName())) {
                return true;
            }
            superClass = superClass.getSuperclass();
        }
        return false;
    }

    private boolean canInstantiate(Class<?> clazz) {
        // Check if class is abstract
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }

        // Check if class is interface
        if (clazz.isInterface()) {
            return false;
        }

        // Non-static inner classes require an outer instance -> warn and skip
        Class<?> enclosing = clazz.getEnclosingClass();
        if (enclosing != null && !Modifier.isStatic(clazz.getModifiers())) {
            LOGGER.error("Skip non-static inner rule class: {}", clazz.getName());
            return false;
        }

        // Check if class has accessible default constructor
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            int mods = constructor.getModifiers();

            // private -> not instantiable
            if (Modifier.isPrivate(mods)) {
                return false;
            }

            // package-private (neither public, protected, nor private) -> warn and skip
            if (!Modifier.isPublic(mods) && !Modifier.isProtected(mods)) {
                LOGGER.error("Skip rule class with package-private default constructor: {}", clazz.getName());
                return false;
            }

            // public or protected -> allowed for now
            return true;
        } catch (NoSuchMethodException e) {
            return false; // No default constructor
        }

    }

    /**
     * Extracts property information from the given rule class.
     */
    private List<PropertyInfo> extractPropertyInfo(Class<?> clazz) {
        List<PropertyInfo> properties = new ArrayList<>();

        if (!canInstantiate(clazz)) {
            LOGGER.info("Skip non instantiatable rule class: {}", clazz.getName());
            return properties;
        }

        try {
            // Try to instantiate the rule class
            Object ruleInstance = clazz.getDeclaredConstructor().newInstance();

            // Use PMD's PropertySource API directly (PMD 7+)
            if (!(ruleInstance instanceof PropertySource)) {
                LOGGER.debug("Rule does not implement PropertySource: {}", clazz.getName());
                return properties;
            }

            List<? extends PropertyDescriptor<?>> descriptors =
                ((PropertySource) ruleInstance).getPropertyDescriptors();

            if (descriptors != null) {
                for (PropertyDescriptor<?> descriptor : descriptors) {
                    PropertyInfo propertyInfo = createPropertyInfo(descriptor);
                    if (propertyInfo != null) {
                        properties.add(propertyInfo);
                    }
                }
            }

            return properties;

        } catch (Exception e) {
            LOGGER.error("Error instantiating rule class: {}", clazz.getName());
        }

        return properties;
    }

    /**
     * Creates a PropertyInfo object from the given PropertyDescriptor object.
     */
    private PropertyInfo createPropertyInfo(PropertyDescriptor<?> propertyDescriptor) {
        try {
            // Use reflection to get property information
            String name = (String) invokeMethod(propertyDescriptor, "name");
            String description = (String) invokeMethod(propertyDescriptor, "description");
            String type = getPropertyType(propertyDescriptor);
            List<String> defaultValues = getDefaultValues(propertyDescriptor);
            return new PropertyInfo(name, description, type, defaultValues);
        } catch (Exception e) {
            LOGGER.warn("Error creating property info", e);
            return null;
        }
    }

    /**
     * Gets the property type from the given PropertyDescriptor object.
     */
    private String getPropertyType(PropertyDescriptor<?> propertyDescriptor) {
        Object o = propertyDescriptor.defaultValue();
        return o == null ? "null" : convertKnownTypes(o);
    }

    private static @NotNull String convertKnownTypes(Object o) {
        String simpleName = o.getClass().getSimpleName();
        if (simpleName.startsWith("Empty")) {
            simpleName = simpleName.substring("Empty".length());
        }
        return simpleName;
    }

    /**
     * Gets the default values from the given PropertyDescriptor object.
     */
    private List<String> getDefaultValues(PropertyDescriptor<?> propertyDescriptor) {
        try {
            // Get the default values from the PropertyDescriptor
            Object defaultValue = propertyDescriptor.defaultValue();
            if (defaultValue instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> defaultValueList = (List<Object>) defaultValue;
                List<String> result = new ArrayList<>();
                for (Object value : defaultValueList) {
                    String x = value.toString();
                    // workaround for label mapping AvoidUsingHardCodedIP AddressKinds enum
                    x = x.equals("IPV4") ? "IPv4" : x;
                    x = x.equals("IPV6") ? "IPv6" : x;
                    x = x.equals("IPV4_MAPPED_IPV6") ? "IPv4 mapped IPv6" : x;
                    result.add(x);
                }
                return result;
            } else if (defaultValue instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<Object> defaultValueSet = (Set<Object>) defaultValue;
                List<String> result = new ArrayList<>();
                for (Object value : defaultValueSet) {
                    String x = value.toString();
                    result.add(x);
                }
                return result;
            } else if (defaultValue instanceof Optional) {
                Optional<?> optional = (Optional<?>) defaultValue;
                if (optional.isPresent()) {
                    return Collections.singletonList(optional.get().toString());
                } else {
                    return Collections.emptyList();
                }
            } else if (defaultValue != null) {
                return Collections.singletonList(defaultValue.toString());
            }
        } catch (Exception e) {
            LOGGER.debug("Error getting default values", e);
        }
        return Collections.emptyList();
    }

    /**
     * Invokes a method on the given object.
     */
    private Object invokeMethod(Object obj, String methodName) throws Exception {
        return obj.getClass().getMethod(methodName).invoke(obj);
    }


    /**
     * Class to hold property information.
     */
    public static class PropertyInfo {
        private final String name;
        private final String description;
        private final String type;
        private final List<String> defaultValues;

        public PropertyInfo(String name, String description, String type, List<String> defaultValues) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.defaultValues = Collections.unmodifiableList(new ArrayList<>(defaultValues));
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }

        public List<String> getDefaultValues() {
            return defaultValues;
        }

        public String getDefaultValuesAsString() {
            return String.join(",", defaultValues);
        }

        @Override
        public String toString() {
            return "PropertyInfo [name=" + name + ", description=" + description + ", type=" + type + ", defaultValues=" + defaultValues + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PropertyInfo that = (PropertyInfo) o;
            return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(type, that.type) && Objects.equals(defaultValues, that.defaultValues);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, description, type, defaultValues);
        }
    }

    public static class PossibleZipBombException extends IOException {
        public PossibleZipBombException(String msg) {
            super(msg);
        }
    }
}
