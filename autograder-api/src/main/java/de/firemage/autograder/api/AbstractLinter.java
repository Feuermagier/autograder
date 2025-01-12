package de.firemage.autograder.api;

import fluent.bundle.FluentResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public interface AbstractLinter {
    List<? extends AbstractProblem> checkFile(Path file, JavaVersion version, CheckConfiguration checkConfiguration, Consumer<Translatable> statusConsumer) throws LinterException, IOException;
    String translateMessage(Translatable translatable);

    static Builder builder(Locale locale) {
        return new Builder(locale);
    }

    class Builder {
        private final Locale locale;
        private AbstractTempLocation tempLocation;
        private int threads;
        private ClassLoader classLoader;
        private int maxProblemsPerCheck = -1;
        private List<FluentResource> messageOverrides = new ArrayList<>();
        private Map<AbstractProblemType, List<String>> conditionalOverrides = new HashMap<>();

        private Builder(Locale locale) {
            this.locale = locale;
        }

        public Builder tempLocation(AbstractTempLocation tempLocation) {
            this.tempLocation = tempLocation;
            return this;
        }

        public AbstractTempLocation getTempLocation() {
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

        /**
         * Add message overrides that always apply, regardless of problem type. Conditional overrides take precedence.
         * @param bundle
         * @return this
         */
        public Builder messagesOverride(FluentResource bundle) {
            this.messageOverrides.add(bundle);
            return this;
        }

        public List<FluentResource> getMessageOverrides() {
            return this.messageOverrides;
        }

        /**
         * Add a message override that only applies if the message was emitted for the specified problem type.
         * Conditional overrides override all other overrides. The (problemType, key) pair must be unique.
         * @param problemType
         * @param key
         * @param value
         * @return this
         */
        public Builder conditionalOverride(AbstractProblemType problemType, String key, String value) {
            this.conditionalOverrides.computeIfAbsent(problemType, k -> new ArrayList<>()).add(key + " = " + value);
            return this;
        }

        /**
         * Sets all conditional overrides, discarding all previously set conditional overrides.
         * @param conditionalOverrides
         * @return
         */
        public Builder setConditionalOverrides(Map<AbstractProblemType, List<String>> conditionalOverrides) {
            this.conditionalOverrides = new HashMap<>(conditionalOverrides);
            return this;
        }

        public Map<AbstractProblemType, List<String>> getConditionalOverrides() {
            return this.conditionalOverrides;
        }
    }
}
