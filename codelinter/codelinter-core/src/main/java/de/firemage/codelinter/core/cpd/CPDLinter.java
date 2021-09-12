package de.firemage.codelinter.core.cpd;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.file.UploadedFile;
import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.JavaLanguage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CPDLinter {
    private static final int MINIMUM_TOKEN_COUNT = 100;

    public List<Problem> lint(UploadedFile file) throws IOException {
        CPDConfiguration cpdConfig = new CPDConfiguration();
        cpdConfig.setFailOnViolation(false);
        cpdConfig.setLanguage(new JavaLanguage());
        cpdConfig.setMinimumTileSize(MINIMUM_TOKEN_COUNT);
        CPD cpd = new CPD(cpdConfig);
        cpd.add(file.getJavaFiles());
        cpd.go();

        List<Problem> problems = new ArrayList<>();
        cpd.getMatches().forEachRemaining(match -> problems.add(new CPDInCodeProblem(match, file.getFile())));
        return problems;
    }
}
