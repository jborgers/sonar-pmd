package org.sonar.plugins.pmd.rule;

import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    /**
     * Extracts property information from Java rule classes in the specified jar file.
     *
     * @param jarFilePath Path to the PMD jar file
     * @return Map of rule class names to their property information
     */
    public Map<String, List<PropertyInfo>> extractProperties(String jarFilePath) {
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

                // prevent Zip bomb attack
                final int MAX_JAR_ENTRIES = 10000;
                int numEntries = 0;

                while (entries.hasMoreElements() && numEntries < MAX_JAR_ENTRIES) {
                    numEntries++;
                    JarEntry entry = entries.nextElement();
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
                            LOGGER.debug("Could not load class: " + className, e);
                        }
                    }
                }
                if (numEntries >= MAX_JAR_ENTRIES) {
                    LOGGER.warn("Too many entries in jar file: " + jarFilePath + ". Skipping rule extraction.");
                }
                LOGGER.info("Extracted " + result.size() + " rule properties from jar file: " + jarFilePath);
            }
        } catch (IOException e) {
            LOGGER.error("Error processing jar file: " + jarFilePath, e);
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

        // Check if class has accessible default constructor
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return !Modifier.isPrivate(constructor.getModifiers());
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
            LOGGER.info("Skip non instantiatable rule class: " + clazz.getName());
            return properties;
        }

        try {
            // Try to instantiate the rule class
            Object ruleInstance = clazz.getDeclaredConstructor().newInstance();

            // Try to find a method that returns property descriptors
            // Common method names that might exist in PMD's AbstractRule
            String[] methodNames = {
                "getPropertyDescriptors", 
                "getPropertiesDescriptors", 
                "getProperties",
                "getAllPropertyDescriptors"
            };

            for (String methodName : methodNames) {
                try {
                    java.lang.reflect.Method method = findMethod(clazz, methodName);
                    if (method != null) {
                        method.setAccessible(true);
                        Object result = method.invoke(ruleInstance);
                        if (result instanceof Collection) {
                            @SuppressWarnings("unchecked")
                            Collection<Object> descriptors = (Collection<Object>) result;
                            for (Object descriptor : descriptors) {
                                if (descriptor instanceof PropertyDescriptor) {
                                    PropertyDescriptor<?> propertyDescriptor = (PropertyDescriptor<?>) descriptor;
                                    PropertyInfo propertyInfo = createPropertyInfo(propertyDescriptor);
                                    if (propertyInfo != null) {
                                        properties.add(propertyInfo);
                                    }
                                }
                            }
                            // If we found and processed property descriptors, return them
                            if (!properties.isEmpty()) {
                                return properties;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore exceptions and try the next method
                    LOGGER.debug("Error invoking method: " + methodName);
                }
            }

            // If we couldn't find a method, try to find a field that might contain property descriptors
            String[] fieldNames = {
                "propertyDescriptors",
                "descriptors",
                "properties"
            };

            for (String fieldName : fieldNames) {
                try {
                    java.lang.reflect.Field field = findField(clazz, fieldName);
                    if (field != null) {
                        field.setAccessible(true);
                        Object fieldValue = field.get(ruleInstance);
                        if (fieldValue instanceof Collection) {
                            @SuppressWarnings("unchecked")
                            Collection<Object> descriptors = (Collection<Object>) fieldValue;
                            for (Object descriptor : descriptors) {
                                if (descriptor instanceof PropertyDescriptor) {
                                    PropertyDescriptor<?> propertyDescriptor = (PropertyDescriptor<?>) descriptor;
                                    PropertyInfo propertyInfo = createPropertyInfo(propertyDescriptor);
                                    if (propertyInfo != null) {
                                        properties.add(propertyInfo);
                                    }
                                }
                            }
                            // If we found and processed property descriptors, return them
                            if (!properties.isEmpty()) {
                                return properties;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore exceptions and try the next field
                    LOGGER.debug("Error accessing field: " + fieldName);
                }
            }

            // If we still haven't found any property descriptors, fall back to the original approach
            // Get all fields in the class
            for (Field field : clazz.getFields()) {
                try {
                    field.setAccessible(true);

                    // Check if the field is a PropertyDescriptor
                    if (isPropertyDescriptor(field.getType())) {
                        PropertyDescriptor<?> propertyDescriptor = (PropertyDescriptor<?>) field.get(null);
                        if (propertyDescriptor != null) {
                            PropertyInfo propertyInfo = createPropertyInfo(propertyDescriptor);
                            if (propertyInfo != null) {
                                properties.add(propertyInfo);
                            }
                        }
                    }
                } catch (IllegalAccessException | SecurityException e) {
                    LOGGER.warn("Error accessing field: " + field.getName());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error instantiating rule class: " + clazz.getName());

            // If we can't instantiate the class, fall back to the original approach
            // Get all fields in the class
            for (Field field : clazz.getFields()) {
                try {
                    field.setAccessible(true);

                    // Check if the field is a PropertyDescriptor
                    if (isPropertyDescriptor(field.getType())) {
                        PropertyDescriptor<?> propertyDescriptor = (PropertyDescriptor<?>) field.get(null);
                        if (propertyDescriptor != null) {
                            PropertyInfo propertyInfo = createPropertyInfo(propertyDescriptor);
                            if (propertyInfo != null) {
                                properties.add(propertyInfo);
                            }
                        }
                    }
                } catch (IllegalAccessException | SecurityException ex) {
                    LOGGER.warn("Error accessing field: " + field.getName(), ex);
                }
            }
        }

        return properties;
    }

    /**
     * Checks if the given class is a PropertyDescriptor.
     */
    private boolean isPropertyDescriptor(Class<?> clazz) {
        return clazz.isAssignableFrom(PropertyDescriptor.class);
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
     * Finds a method with the given name in the class hierarchy.
     */
    private java.lang.reflect.Method findMethod(Class<?> clazz, String methodName) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException e) {
                // Try with parameters
                try {
                    java.lang.reflect.Method[] methods = currentClass.getDeclaredMethods();
                    for (java.lang.reflect.Method method : methods) {
                        if (method.getName().equals(methodName)) {
                            return method;
                        }
                    }
                } catch (SecurityException ex) {
                    // Ignore and continue with superclass
                }
                currentClass = currentClass.getSuperclass();
            } catch (SecurityException e) {
                // Ignore and continue with superclass
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Finds a field with the given name in the class hierarchy.
     */
    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            } catch (SecurityException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
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
            this.defaultValues = defaultValues;
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
    }
}
