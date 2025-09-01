package org.sonar.plugins.pmd.rule;

import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.properties.PropertyConstraint;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
     * @param file Path to the PMD jar file
     * @return Map of rule class names to their property information
     */
    @SuppressWarnings("java:S5042") // security warning for ZIP bomb attack: implemented countermeasures
    public Map<String, List<PropertyInfo>> extractProperties(File file) throws IOException {
        // Create a map that returns an empty list for any key that's not in the map
        Map<String, List<PropertyInfo>> result = new HashMap<>() {
            @Override
            public List<PropertyInfo> get(Object key) {
                List<PropertyInfo> value = super.get(key);
                return value != null ? value : Collections.emptyList();
            }
        };

        try (JarFile jarFile = new JarFile(file)) {
            // Create a class loader for the jar file
            URL[] urls = { new URL("file:" + file) };
            try (URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader())) {
                // First, scan the JAR for potential ZIP bomb characteristics
                ZipBombProtection.scanJar(jarFile, file);

                // After validation, iterate through entries to find class files
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
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
                            LOGGER.debug("Could not load class: {}", className, e);
                        }
                    }
                }
                LOGGER.info("Extracted {} rule properties from jar file: {}", result.size(), file);
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

            LOGGER.info("Extracting properties for rule class: {}", clazz.getName());

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
            String name = propertyDescriptor.name();
            String description = propertyDescriptor.description();
            String type = resolvePropertyType(propertyDescriptor);
            List<String> defaultValues = getDefaultValues(propertyDescriptor);
            List<String> acceptedValues = determineAcceptedValues(propertyDescriptor, defaultValues);
            boolean multiple = determineMultiple(propertyDescriptor);
            String wrappedType = determineWrappedType(propertyDescriptor, type, defaultValues);
            return new PropertyInfo(name, description, type, defaultValues, acceptedValues, multiple, wrappedType);
        } catch (Exception e) {
            LOGGER.warn("Error creating property info", e);
            return null;
        }
    }

    /**
     * Gets the property type from the given PropertyDescriptor object.
     */
    private String resolvePropertyType(PropertyDescriptor<?> propertyDescriptor) {
        Object o = propertyDescriptor.defaultValue();
        return o == null ? "null" : convertKnownTypes(o);
    }

    private static @NotNull String convertKnownTypes(Object o) {
        String simpleName = o.getClass().getSimpleName();
        // is this needed? there is only: %%% found simplename with Empty: EmptySet
        if (simpleName.startsWith("Empty")) {
            LOGGER.info("%%% found simplename with Empty: {}", simpleName);
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
            @SuppressWarnings("unchecked")
            List<PropertyConstraint<?>> constraints = (List<PropertyConstraint<?>>) propertyDescriptor.serializer().getConstraints();
            if (!constraints.isEmpty()) {
                LOGGER.info("%%% found constraints: {} for {} (default value: {})", constraints.get(0).getConstraintDescription(), propertyDescriptor.name(), defaultValue);
            }
            if (defaultValue instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> defaultValueList = (List<Object>) defaultValue;
                if (!defaultValueList.isEmpty()) {
                    Object value = defaultValueList.get(0);
                    Class<?> aClass = value.getClass();
                    LOGGER.info("%%% found list with wrapped class: {} for {} (default value: {})", aClass.getSimpleName(), propertyDescriptor.name(), defaultValue);
                }
                else {
                    LOGGER.info("%%% found empty list, cannot determine wrapped type for {} (default value: {})", propertyDescriptor.name(), defaultValue);
                }
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
                if (!defaultValueSet.isEmpty()) {
                    LOGGER.info("%%% found set with wrapped class: {} for {} (default value: {})", defaultValueSet.iterator().next().getClass().getSimpleName(), propertyDescriptor.name(), defaultValue);
                }
                else {
                    LOGGER.info("%%% found empty set, cannot determine wrapped type for {} (default value: {})", propertyDescriptor.name(), defaultValue);
                }
                List<String> result = new ArrayList<>();
                for (Object value : defaultValueSet) {
                    String x = value.toString();
                    result.add(x);
                }
                return result;
            } else if (defaultValue instanceof Optional) {
                Optional<?> optional = (Optional<?>) defaultValue;
                if (optional.isPresent()) {
                    Object wrappedInOptional = optional.get();
                    LOGGER.info("%%% found optional with wrapped class: {}", wrappedInOptional.getClass().getSimpleName());
                    return Collections.singletonList(wrappedInOptional.toString());
                } else {
                    if (!(propertyDescriptor.name().equals("violationSuppressRegex") || propertyDescriptor.name().equals("violationSuppressXPath"))) {
                        LOGGER.info("%%% found empty optional for {}", propertyDescriptor);
                    }
                    return Collections.emptyList();
                }
            } else if (defaultValue != null) {
                LOGGER.info("%%% found default value: {} for {} (type: {})", defaultValue, propertyDescriptor.name(), defaultValue.getClass().getSimpleName());
                return Collections.singletonList(defaultValue.toString());
            }
        } catch (Exception e) {
            LOGGER.error("Error getting default values for {}", propertyDescriptor, e);
        }
        return Collections.emptyList();
    }

    private boolean determineMultiple(PropertyDescriptor<?> propertyDescriptor) {
        try {
            Object defaultValue = propertyDescriptor.defaultValue();
            return (defaultValue instanceof List) || (defaultValue instanceof Set);
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> determineAcceptedValues(PropertyDescriptor<?> propertyDescriptor, List<String> defaultValues) {
        List<String> result = new ArrayList<>();
        try {
            Object defaultValue = propertyDescriptor.defaultValue();

            // 1) If enum type is discoverable from default values, use enum constants
            Class<?> enumClass = null;
            if (defaultValue instanceof List) {
                List<?> list = (List<?>) defaultValue;
                if (!list.isEmpty()) {
                    Object first = list.get(0);
                    if (first != null && first.getClass().isEnum()) {
                        enumClass = first.getClass();
                    }
                }
            } else if (defaultValue instanceof Set) {
                Set<?> set = (Set<?>) defaultValue;
                if (!set.isEmpty()) {
                    Object first = set.iterator().next();
                    if (first != null && first.getClass().isEnum()) {
                        enumClass = first.getClass();
                    }
                }
            } else if (defaultValue instanceof Optional) {
                Optional<?> opt = (Optional<?>) defaultValue;
                if (opt.isPresent()) {
                    Object inner = opt.get();
                    if (inner != null && inner.getClass().isEnum()) {
                        enumClass = inner.getClass();
                    }
                }
            } else if (defaultValue != null && defaultValue.getClass().isEnum()) {
                enumClass = defaultValue.getClass();
            }

            if (enumClass != null) {
                Object[] constants = enumClass.getEnumConstants();
                if (constants != null) {
                    for (Object c : constants) {
                        result.add(normalizeLabel(c.toString()));
                    }
                }
            }

            // 2) Otherwise, parse constraints description: "Possible values: [...]" or "Allowed values: [...]"
            if (result.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<PropertyConstraint<?>> constraints = (List<PropertyConstraint<?>>) propertyDescriptor.serializer().getConstraints();
                for (PropertyConstraint<?> c : constraints) {
                    String desc = String.valueOf(c.getConstraintDescription());
                    List<String> fromDesc = parseValuesFromConstraintDescription(desc);
                    if (!fromDesc.isEmpty()) {
                        result.addAll(fromDesc);
                        break;
                    }
                }
            }

            return List.copyOf(result);
        } catch (Exception e) {
            LOGGER.debug("Could not determine accepted values for {}", propertyDescriptor.name(), e);
            return Collections.emptyList();
        }
    }

    private String determineWrappedType(PropertyDescriptor<?> propertyDescriptor, String fallbackType, List<String> defaultValues) {
        try {
            Object defaultValue = propertyDescriptor.defaultValue();
            if (defaultValue instanceof List) {
                List<?> list = (List<?>) defaultValue;
                if (!list.isEmpty() && list.get(0) != null) {
                    return list.get(0).getClass().getSimpleName();
                } else {
                    return "Object"; // unknown element type
                }
            } else if (defaultValue instanceof Set) {
                Set<?> set = (Set<?>) defaultValue;
                if (!set.isEmpty()) {
                    Object first = set.iterator().next();
                    if (first != null) return first.getClass().getSimpleName();
                }
                return "Object";
            } else if (defaultValue instanceof Optional) {
                Optional<?> opt = (Optional<?>) defaultValue;
                if (opt.isPresent() && opt.get() != null) {
                    return opt.get().getClass().getSimpleName();
                } else {
                    return fallbackType;
                }
            } else if (defaultValue != null) {
                return defaultValue.getClass().getSimpleName();
            }
        } catch (Exception ignored) {
        }
        return fallbackType;
    }

    private List<String> parseValuesFromConstraintDescription(String desc) {
        if (desc == null || desc.isEmpty()) return Collections.emptyList();
        // Look for "Possible values: [a, b, c]" or "Allowed values: [a, b]"
        String lower = desc.toLowerCase(Locale.ROOT);
        int idx = lower.indexOf("possible values:");
        if (idx < 0) idx = lower.indexOf("allowed values:");
        if (idx >= 0) {
            int open = desc.indexOf('[', idx);
            int close = desc.indexOf(']', open + 1);
            if (open > 0 && close > open) {
                String inner = desc.substring(open + 1, close);
                String[] parts = inner.split(",");
                List<String> values = new ArrayList<>();
                for (String p : parts) {
                    String v = normalizeLabel(p.trim());
                    if (!v.isEmpty()) values.add(v);
                }
                return values;
            }
        }
        return Collections.emptyList();
    }

    private String normalizeLabel(String x) {
        if (x == null) return "";
        // reuse special mappings used for defaults
        if ("IPV4".equals(x)) return "IPv4";
        if ("IPV6".equals(x)) return "IPv6";
        if ("IPV4_MAPPED_IPV6".equals(x)) return "IPv4 mapped IPv6";
        return x;
    }

    /**
     * Class to hold property information.
     */
    public static class PropertyInfo {
        private final String name;
        private final String description;
        private final String type;
        private final List<String> defaultValues;
        private final List<String> acceptedValues;
        private final boolean multiple;
        private final String wrappedType;

        public PropertyInfo(String name, String description, String type, List<String> defaultValues) {
            this(name, description, type, defaultValues, Collections.emptyList(), false, type);
        }

        public PropertyInfo(String name, String description, String type, List<String> defaultValues, List<String> acceptedValues, boolean multiple) {
            this(name, description, type, defaultValues, acceptedValues, multiple, type);
        }

        public PropertyInfo(String name, String description, String type, List<String> defaultValues, List<String> acceptedValues, boolean multiple, String wrappedType) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.defaultValues = List.copyOf(defaultValues);
            this.acceptedValues = acceptedValues == null ? Collections.emptyList() : List.copyOf(acceptedValues);
            this.multiple = multiple;
            this.wrappedType = wrappedType == null ? type : wrappedType;
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

        public List<String> getAcceptedValues() {
            return acceptedValues;
        }

        public boolean isMultiple() {
            return multiple;
        }

        public String getWrappedType() {
            return wrappedType;
        }

        @Override
        public String toString() {
            return "PropertyInfo [name=" + name + ", description=" + description + ", type=" + type + ", defaultValues=" + defaultValues + ", acceptedValues=" + acceptedValues + ", multiple=" + multiple + ", wrappedType=" + wrappedType + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PropertyInfo that = (PropertyInfo) o;
            return multiple == that.multiple && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(type, that.type) && Objects.equals(defaultValues, that.defaultValues) && Objects.equals(acceptedValues, that.acceptedValues) && Objects.equals(wrappedType, that.wrappedType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, description, type, defaultValues, acceptedValues, multiple, wrappedType);
        }
    }

}
