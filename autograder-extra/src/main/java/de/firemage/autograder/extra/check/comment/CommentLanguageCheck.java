package de.firemage.autograder.extra.check.comment;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import de.firemage.autograder.core.CodePositionImpl;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.IntegratedInCodeProblem;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.INVALID_COMMENT_LANGUAGE, ProblemType.INCONSISTENT_COMMENT_LANGUAGE})
public class CommentLanguageCheck extends IntegratedCheck {
    // Lingua uses ai models for language detection that are backed into the jar.
    // It supports a lot of languages which is ~200MB extra data if we include all of them.
    //
    // Not all languages are needed for this check, so here is a list of supported languages and in
    // the maven pom.xml we exclude all other languages.
    private static final List<Language> SUPPORTED_LANGUAGES = List.of(Language.ENGLISH, Language.GERMAN, Language.CHINESE);
    private final LanguageDetector detector;

    public CommentLanguageCheck() {
        this(0.075);
    }

    public CommentLanguageCheck(double threshold) {
        super();
        this.detector = LanguageDetectorBuilder.fromLanguages(SUPPORTED_LANGUAGES.toArray(new Language[0]))
            .withMinimumRelativeDistance(threshold)
            .build();
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        List<CommentLanguageResult> englishComments = new ArrayList<>();
        List<CommentLanguageResult> germanComments = new ArrayList<>();

        staticAnalysis.processWith(new AbstractProcessor<CtComment>() {
            @Override
            public void process(CtComment comment) {
                var language = CommentLanguageResult.detect(comment, detector);

                switch (language.language) {
                    case ENGLISH -> englishComments.add(language);
                    case GERMAN -> germanComments.add(language);
                    case UNKNOWN -> {
                    }
                    default -> addLocalProblem(comment,
                        new LocalizedMessage("comment-language-exp-invalid", Map.of("lang", language.language.name())),
                        ProblemType.INVALID_COMMENT_LANGUAGE);
                }
            }
        });

        if (!englishComments.isEmpty() && !germanComments.isEmpty()) {
            CtComment bestEnglish = englishComments.stream()
                .max(Comparator.comparingDouble(a -> a.confidence))
                .get().comment;
            CodePositionImpl englishPosition = IntegratedInCodeProblem.mapSourceToCode(bestEnglish, this.getRoot());

            CtComment bestGerman = germanComments.stream()
                .max(Comparator.comparingDouble(a -> a.confidence))
                .get().comment;
            CodePositionImpl germanPosition = IntegratedInCodeProblem.mapSourceToCode(bestGerman, this.getRoot());

            addLocalProblem(bestEnglish,
                new LocalizedMessage(
                    "comment-language-exp-english",
                    Map.of("path", germanPosition.file().toString(), "line", String.valueOf(germanPosition.startLine()))
                ), ProblemType.INCONSISTENT_COMMENT_LANGUAGE);
            addLocalProblem(bestGerman,
                new LocalizedMessage(
                    "comment-language-exp-german",
                    Map.of("path", englishPosition.file().toString(), "line",
                        String.valueOf(englishPosition.startLine()))
                ), ProblemType.INCONSISTENT_COMMENT_LANGUAGE);
        }
    }

    private record CommentLanguageResult(CtComment comment, Language language, double confidence) {

        public static CommentLanguageResult detect(CtComment comment, LanguageDetector detector) {
            // Remove @see because it is always in English
            StringBuilder content = new StringBuilder(comment.getContent().replace("@see", ""));

            // For javadoc we want to include @param, @return and @throws tags
            if (comment instanceof CtJavaDoc javadoc) {
                for (CtJavaDocTag tag : javadoc.getTags()) {
                    if (tag.getType() == CtJavaDocTag.TagType.PARAM || tag.getType() == CtJavaDocTag.TagType.RETURN ||
                        tag.getType() == CtJavaDocTag.TagType.THROWS) {
                        content.append(". ").append(tag.getContent().replace("@see", ""));
                    }
                }
            }

            if (content.toString().split(" +").length <= 3) {
                // The string contains too few words
                return new CommentLanguageResult(comment, Language.UNKNOWN, 0);
            } else {
                Language language = detector.detectLanguageOf(content.toString());
                var confidences = detector.computeLanguageConfidenceValues(content.toString());
                double englishConfidence = confidences.getOrDefault(Language.ENGLISH, 0.0);
                double germanConfidence = confidences.getOrDefault(Language.GERMAN, 0.0);
                return new CommentLanguageResult(comment, language, Math.abs(englishConfidence - germanConfidence));
            }
        }
    }
}
