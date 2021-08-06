package de.firemage.codelinter.core.pmd;

import net.sourceforge.pmd.renderers.TextRenderer;

public class NoPathTextRenderer extends TextRenderer {
    private static final String PATH_END_ID = ".zip:";

    @Override
    protected String determineFileName(String inputFileName) {
        String path = super.determineFileName(inputFileName);
        return path.substring(path.indexOf(PATH_END_ID) + PATH_END_ID.length());
    }
}
