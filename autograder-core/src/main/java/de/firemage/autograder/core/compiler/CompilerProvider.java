package de.firemage.autograder.core.compiler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class CompilerProvider {
    /**
     * Tries to find the system Java compiler. This is a direct replacement for ToolProvider.getSystemJavaCompiler(),
     * which can fail in combination with some class loaders (as it is the case in IntelliJ plugins).
     * Of course, the compiler can only be found if it is available in the current class loader, i.e. if we are running
     * within a JDK.
     *
     * @return The system Java compiler. Never returns null, but throws an Exception instead.
     */
    public static JavaCompiler findSystemCompiler() {
        // First try the "official" method
        // This is hardcoded to use ClassLoader.getSystemClassLoader()
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler != null) {
            return compiler;
        }

        // Next, try to load it directly by name
        try {
            var compilerClass = Class.forName("com.sun.tools.javac.api.JavacTool");
            return (JavaCompiler) compilerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            // Ignored
        }

        // Finally, try to find it in the current class loader
        // This is a bit troublesome, since it may find the Eclipse JDT compiler in some environments
        try {
            var iterator = ServiceLoader.load(JavaCompiler.class, CompilerProvider.class.getClassLoader()).iterator();
            if (iterator.hasNext()) {
                // Not sure if the first one is always the right one
                var otherCompiler = iterator.next();
                if (otherCompiler != null) {
                    return otherCompiler;
                }
            }
        } catch (ServiceConfigurationError ex) {
            // Ignored
        }

        // Give up
        throw new IllegalStateException("No Java compiler found");
    }
}
