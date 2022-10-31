package de.firemage.autograder.core.check.comment;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
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

public class CommentLanguageCheck extends IntegratedCheck {
    private static final String DESCRIPTION =
        "All comments (including Javadoc and inline comments) must be either in English or in German. Mixing languages is not allowed.";
    private final LanguageDetector detector;

    public CommentLanguageCheck() {
        this(0.075);
    }

    public CommentLanguageCheck(double threshold) {
        super(DESCRIPTION);
        this.detector = LanguageDetectorBuilder.fromAllLanguages().withMinimumRelativeDistance(threshold).build();
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
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
                        "The language of this comment is neither English nor German but seems to be " +
                            language.language.name(), ProblemType.INVALID_COMMENT_LANGUAGE);
                }
            }
        });

        if (!englishComments.isEmpty() && !germanComments.isEmpty()) {
            CtComment bestEnglish = englishComments.stream()
                .max(Comparator.comparingDouble(a -> a.confidence))
                .get().comment;
            CodePosition englishPosition = IntegratedInCodeProblem.mapSourceToCode(bestEnglish, this.getRoot());

            CtComment bestGerman = germanComments.stream()
                .max(Comparator.comparingDouble(a -> a.confidence))
                .get().comment;
            CodePosition germanPosition = IntegratedInCodeProblem.mapSourceToCode(bestGerman, this.getRoot());

            addLocalProblem(bestEnglish,
                String.format(
                    "The code contains comments in German and in English. This comment is in English. A German comment can be found at %s:%d",
                    germanPosition.file(), germanPosition.startLine()),
                ProblemType.INCONSISTENT_COMMENT_LANGUAGE);


            addLocalProblem(bestGerman,
                String.format("The code contains comments in German and in English. This comment is in German. An English comment can be found at %s:%d",
                    englishPosition.file(), englishPosition.startLine()),
                ProblemType.INCONSISTENT_COMMENT_LANGUAGE);
        }
    }

    private static record CommentLanguageResult(CtComment comment, Language language, double confidence) {

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

            if (content.toString().split(" *").length <= 2) {
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
