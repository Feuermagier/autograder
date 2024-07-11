package de.firemage.autograder.api;

import de.firemage.autograder.api.loader.ImplementationBinder;
import fluent.bundle.FluentBundle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public interface Linter {
    List<? extends Problem> checkFile(Path file, JavaVersion version, CheckConfiguration checkConfiguration, Consumer<Translatable> statusConsumer) throws LinterException, IOException;
    String translateMessage(Translatable translatable);
    FluentBundle getFluentBundle();

    static Builder builder(Locale locale) {
        return new Builder(locale);
    }

    class Builder {
        private final Locale locale;
        private TempLocation tempLocation;
        private int threads;
        private ClassLoader classLoader;
        private int maxProblemsPerCheck = -1;

        private Builder(Locale locale) {
            this.locale = locale;
        }

        public Builder tempLocation(TempLocation tempLocation) {
            this.tempLocation = tempLocation;
            return this;
        }

        public TempLocation getTempLocation() {
            return tempLocation;
        }

        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        public int getThreads() {
            return threads;
        }

        public Builder maxProblemsPerCheck(int maxProblemsPerCheck) {
            this.maxProblemsPerCheck = maxProblemsPerCheck;
            return this;
        }

        public int getMaxProblemsPerCheck() {
            return maxProblemsPerCheck;
        }

        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public Locale getLocale() {
            return locale;
        }
    }
}
