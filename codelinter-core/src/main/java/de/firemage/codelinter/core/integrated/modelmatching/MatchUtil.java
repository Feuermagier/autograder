package de.firemage.codelinter.core.integrated.modelmatching;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.List;

public final class MatchUtil {
    public static final double SCORE_NAME_MATCHES_EXACTLY = 10;
    
    private MatchUtil() {
        
    }
    
    public static double nameMatchScore(String name, List<String> options) {
        name = name.toLowerCase();
        for (String option : options) {
            option = option.toLowerCase();
            if (name.equals(option)) {
                return SCORE_NAME_MATCHES_EXACTLY;
            } else {
                double distance = new LevenshteinDistance().apply(name, option);
                return SCORE_NAME_MATCHES_EXACTLY * (name.length() / distance);
            }
        }
        return 0;
    }
}
