package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.LinterStatus;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.integrated.graph.GraphAnalysis;
import de.firemage.autograder.core.parallel.AnalysisScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class IntegratedAnalysis {
    private static final boolean ENSURE_NO_ORPHANS = false;
    private static final boolean ENSURE_NO_MODEL_CHANGES = false;
    private static final Logger logger = LoggerFactory.getLogger(IntegratedAnalysis.class);

    private final UploadedFile file;
    private final Path tmpPath;
    private final Map<String, FileSystem> openFileSystems = new HashMap<>();
    private final CtModel originalModel;
    private final StaticAnalysis staticAnalysis;
    private final GraphAnalysis graphAnalysis;

    public IntegratedAnalysis(UploadedFile file, Path tmpPath) {
        this.file = file;
        this.tmpPath = tmpPath;

        StaticAnalysis temporaryAnalysis = new StaticAnalysis(file.getModel(), file.getCompilationResult());
        this.originalModel = temporaryAnalysis.getModel();

        this.staticAnalysis = new StaticAnalysis(file.getModel(), file.getCompilationResult());
        //this.graphAnalysis = new GraphAnalysis(this.staticAnalysis.getCodeModel());
        this.graphAnalysis = null; //TODO

    }

    private Path toPath(URL resource) throws URISyntaxException, IOException {
        if (resource == null) {
            throw new IllegalArgumentException("URL is null");
        }
        URI uri = resource.toURI();
        if (!uri.toString().contains("!")) {
            return Path.of(uri);
        }
        // See https://stackoverflow.com/questions/22605666/java-access-files-in-jar-causes-java-nio-file-filesystemnotfoundexception
        String[] path = uri.toString().split("!", 2);
        @SuppressWarnings("resource") FileSystem fs = createFileSystem(path[0]);
        return fs.getPath(path[1]);
    }

    private FileSystem createFileSystem(String path) throws IOException {
        FileSystem existingFileSystem = openFileSystems.get(path);
        if (existingFileSystem != null) {
            return existingFileSystem;
        }

        FileSystem newFileSystem = FileSystems.newFileSystem(URI.create(path), new HashMap<>());
        openFileSystems.put(path, newFileSystem);
        return newFileSystem;
    }

    private void closeOpenFileSystems() {
        for (FileSystem openFileSystem : openFileSystems.values()) {
            try {
                openFileSystem.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        openFileSystems.clear();
    }

    public void lint(List<IntegratedCheck> checks, Consumer<LinterStatus> statusConsumer, AnalysisScheduler scheduler) {
        statusConsumer.accept(LinterStatus.BUILDING_CODE_MODEL);
        this.staticAnalysis.getCodeModel().ensureModelBuild();

        statusConsumer.accept(LinterStatus.RUNNING_INTEGRATED_CHECKS);

        scheduler.submitTask((s, reporter) -> {
            for (IntegratedCheck check : checks) {
                long beforeTime = System.nanoTime();
                reporter.reportProblems(check.run(
                    this.staticAnalysis,
                    this.file.getSource()
                ));
                long afterTime = System.nanoTime();
                logger.info("Completed check " + check.getClass().getSimpleName() + " in " + ((afterTime - beforeTime) / 1_000_000 + "ms"));
                this.assertModelIntegrity(check);
            }
        });
    }

    /**
     * This method checks that a check did not change the model in a way that would influence other checks.
     *
     * @param currentCheck the check that was just executed
     */
    private void assertModelIntegrity(Check currentCheck) {
        CtModel linterModel = this.staticAnalysis.getModel();

        String checkName = currentCheck.getClass().getSimpleName();

        if ((ENSURE_NO_MODEL_CHANGES || SpoonUtil.isInJunitTest()) && !isModelEqualTo(originalModel, linterModel)) {
            throw new IllegalStateException("The model was changed by the check: %s".formatted(checkName));
        }

        if (ENSURE_NO_ORPHANS || SpoonUtil.isInJunitTest()) {
            List<CtElement> orphans = findOrphans(linterModel);
            if (!orphans.isEmpty()) {
                throw new IllegalStateException("The check %s introduced new elements into the model without parents (did you forget to clone before passing the element to a setter?): %s".formatted(
                    checkName,
                    orphans.stream().map(element -> "%s(\"%s\")".formatted(element.getClass().getSimpleName(), element)).toList()
                ));
            }
        }
    }

    private Set<CtElement> cachedOriginalElements = null;
    private boolean isModelEqualTo(CtModel original, CtModel toCheck) {
        if (this.cachedOriginalElements == null) {
            this.cachedOriginalElements = new HashSet<>(original.getElements(new TypeFilter<>(CtElement.class)));
        }

        // TODO: this is very slow (~8s with a large model)

        // long beforeTime = System.nanoTime();
        Set<CtElement> originalElements = this.cachedOriginalElements;

        List<CtElement> changedElements = toCheck.getElements(element -> !originalElements.contains(element));

        /*System.out.println("Finished comparing models (%dms). Changed elements: %s".formatted(
            ((System.nanoTime() - beforeTime) / 1_000_000), changedElements.size())
        );*/

        return changedElements.isEmpty();
    }

    private static boolean isOrphan(CtElement ctElement) {
        // I think those are created by the shadow model? => ignored
        if (ctElement instanceof CtPackageReference ctPackage && ctPackage.getQualifiedName().startsWith("java.")) {
            return false;
        }

        // TODO: these should not be ignored
        if (ctElement instanceof CtTypeReference<?> || ctElement instanceof CtLiteral<?> || ctElement instanceof CtPackageReference) {
            return false;
        }

        // TODO: could be replaced with SpoonUtil.parents()
        CtElement root = ctElement.getFactory().getModel().getUnnamedModule();
        if (root == ctElement) {
            return false;
        }

        CtElement parent = ctElement.getParent();
        while (parent.isParentInitialized() && parent != root) {
            parent = parent.getParent();
        }

        return parent != root;
    }

    private static List<CtElement> findOrphans(CtModel ctModel) {
        return ctModel.getElements(IntegratedAnalysis::isOrphan);
    }

    public StaticAnalysis getStaticAnalysis() {
        return staticAnalysis;
    }
}
