package de.firemage.autograder.core.cpd;

import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.check.general.CopyPasteCheck;
import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.JavaLanguage;
import net.sourceforge.pmd.cpd.SourceCode;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CPDLinter {

    public List<Problem> lint(UploadedFile file, List<CopyPasteCheck> checks) throws IOException {
        List<Problem> problems = new ArrayList<>();
        for (CopyPasteCheck check : checks) {
            CPDConfiguration cpdConfig = new CPDConfiguration();
            cpdConfig.setFailOnViolation(false);
            cpdConfig.setLanguage(new JavaLanguage());
            cpdConfig.setMinimumTileSize(check.getTokenCount());

            // NOTE: CPD is marked for removal, but there is no replacement yet
            // (CpdAnalysis should be added in the future)
            CPD cpd = new CPD(cpdConfig);

            for (JavaFileObject compilationUnit : file.getSource().compilationUnits()) {
                cpd.add(new SourceCode(new SourceCode.ReaderCodeLoader(
                    compilationUnit.openReader(true),
                    compilationUnit.getName()
                )));
            }
            cpd.go();
            cpd.getMatches().forEachRemaining(match -> problems.add(new CPDInCodeProblem(check, match, file.getSource().getPath())));
        }

        return problems;
    }
}
