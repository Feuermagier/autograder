package de.firemage.autograder.api.loader;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImplementationBinder<T> {
    private static final Logger LOG = LoggerFactory.getLogger(ImplementationBinder.class);

    private static final Map<String, Class<?>> reflectionCache = new HashMap<>();

    private final Class<T> superType;
    private final List<Class<?>> parameterTypes = new ArrayList<>();
    private final List<Object> arguments = new ArrayList<>();
    private final List<ClassLoader> classLoaders = new ArrayList<>();

    public ImplementationBinder(Class<T> superType) {
        this.superType = superType;
        this.classLoaders.add(getClass().getClassLoader());
    }

    public <P> ImplementationBinder<T> param(Class<P> type, P value) {
        parameterTypes.add(type);
        arguments.add(value);
        return this;
    }

    public ImplementationBinder<T> classLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            this.classLoaders.add(classLoader);
        }
        return this;
    }

    public T instantiate() {
        String superTypeName = superType.getName();
        if (!reflectionCache.containsKey(superTypeName)) {
            var config = new ConfigurationBuilder()
                    .forPackage("de.firemage.autograder.")
                    .setClassLoaders(this.classLoaders.toArray(new ClassLoader[0]))
                    .setScanners(Scanners.SubTypes);
            var implementations = new Reflections(config).getSubTypesOf(superType);

            if (implementations.isEmpty()) {
                throw new IllegalStateException("No implementation found for " + superTypeName + ". Check your classpath.");
            }
            if (implementations.size() > 1) {
                throw new IllegalStateException("More than one implementation found for " + superTypeName + ". Check your classpath.");
            }

            LOG.info("Found implementation {} for {}", implementations.iterator().next().getName(), superTypeName);
            reflectionCache.put(superTypeName, implementations.iterator().next());
        }

        @SuppressWarnings("unchecked")
        var implementation = (Class<T>) reflectionCache.get(superType.getName());

        try {
            return implementation.getConstructor(parameterTypes.toArray(new Class[0])).newInstance(arguments.toArray());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate " + implementation.getName() + " with a constructor with parameters " + parameterTypes, e);
        }
    }

    public static void invalidateCache() {
        reflectionCache.clear();
    }
}
