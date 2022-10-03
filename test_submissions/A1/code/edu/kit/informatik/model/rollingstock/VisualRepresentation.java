package edu.kit.informatik.model.rollingstock;

import edu.kit.informatik.StringConstants;

import java.util.Arrays;
import java.util.List;

/**
 * The visual representation of a single rolling stock.
 *
 * @author uxxxx
 * @version 1.0
 */
public class VisualRepresentation {

    private final List<String> lines;
    private final String emptyLine;

    /**
     * Creates a new visual representation.
     *
     * @param lines The lines of the string representation. Th first line equals the top of the image.
     *              All lines must have the same length.
     */
    public VisualRepresentation(String... lines) {

        if (lines.length == 0 || !Arrays.stream(lines).allMatch(line -> line.length() == lines[0].length())) {
            throw new IllegalArgumentException("All lines must have the same length");
        }

        this.lines = Arrays.asList(lines);

        StringBuilder emptyLineBuilder = new StringBuilder();
        for (int i = 0; i < lines[0].length(); i++) {
            emptyLineBuilder.append(StringConstants.SPACE);
        }
        this.emptyLine = emptyLineBuilder.toString();
    }

    /**
     * Returns the line with the specified id. 0 is the bottom line. If the specified id is larger than the visual
     * representation, a blan´k line is returned
     *
     * @param index The index of the lines
     * @return The line of the image or a blank line
     */
    public String getLine(int index) {
        if (index >= lines.size()) {
            return emptyLine;
        } else {
            return lines.get(lines.size() - index - 1);
        }
    }

    /**
     * Returns the height of this visual representation (i.e. the index of the top line plus one)
     *
     * @return The height
     */
    public int getHeight() {
        return lines.size();
    }
}
