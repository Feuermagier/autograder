package de.firemage.autograder.api.loader;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.reflections.scanners.Scanners.SubTypes;

public class ImplementationBinder<T> {
    private static final Logger LOG = LoggerFactory.getLogger(ImplementationBinder.class);

    private static final Map<String, Class<?>> reflectionCache = new HashMap<>();

    private final Class<T> superType;
    private final List<Class<?>> parameterTypes = new ArrayList<>();
    private final List<Object> arguments = new ArrayList<>();
    private ClassLoader classLoader;

    public ImplementationBinder(Class<T> superType) {
        this.superType = superType;
    }

    public <P> ImplementationBinder<T> param(Class<P> type, P value) {
        parameterTypes.add(type);
        arguments.add(value);
        return this;
    }

    public ImplementationBinder<T> classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public T instantiate() {
        var implementation = findImplementation();

        try {
            return implementation.getConstructor(parameterTypes.toArray(new Class[0])).newInstance(arguments.toArray());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate " + implementation.getName() + " with a constructor with parameters " + parameterTypes, e);
        }
    }

    public <R> R callStatic(String methodName, Class<R> returnType) {
        var implementation = findImplementation();

        try {
            var method = implementation.getMethod(methodName, parameterTypes.toArray(new Class[0]));
            return returnType.cast(method.invoke(null, arguments.toArray()));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to call static method " + methodName + " on " + implementation.getName(), e);
        }
    }

    public Class<T> findImplementation() {
        if (this.classLoader == null) {
            this.classLoader = this.getClass().getClassLoader();
        }

        String superTypeName = superType.getName();
        if (!reflectionCache.containsKey(superTypeName)) {
            var config = new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("de.firemage.autograder", this.classLoader))
                    .forPackage("de.firemage.autograder")
                    .setClassLoaders(new ClassLoader[]{this.classLoader})
                    .setExpandSuperTypes(false)
                    .setScanners(SubTypes);
            var implementations = new Reflections(config).get(SubTypes.of(this.superType).asClass(this.classLoader));

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
        return implementation;
    }

    public static void invalidateCache() {
        reflectionCache.clear();
    }
}
