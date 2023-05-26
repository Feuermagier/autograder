package de.firemage.autograder.core.check_tests.ConstantsHaveDescriptiveNamesCheck.code;

public class Test {
    private static final int ZERO = 0; // Not Ok
    private static final int ONE = 1; // Not Ok
    private static final int TWO = 2; // Not Ok
    private static final String ERROR = ""; // Not Ok
    private static final String REGEX = ""; // Not Ok

    private static final int VALUE_A = 1; // Ok

    private static final Object d = null; // Ok (covered by descriptive variable check)
    private static final boolean TRUE = true; // Not Ok
    private static final boolean FALSE = false; // Not Ok
    private static final String UP = "up"; // Not Ok
    private static final String DOWN = "down"; // Not Ok
    private static final String DEBUG_PATH = "debug-path"; // Not Ok
    private static final String LEFT = "left"; // Not Ok
    private static final String RIGHT = "right"; // Not Ok

    private static final String SPLIT_COLON = ":"; // Not Ok
    private static final String SPLIT_COMMA = ","; // Not Ok
    private static final String SPLIT_ARROW = "-->"; // Not OK
    private static final String CONSTANT = "foo"; // Not Ok
    private static final int VALUE_OF_2 = 2; // Not Ok
    private static final String NOT_LOADED = "A simulation must be loaded first"; // Ok
    // stress test (algorithm should be fast enough for this):
    private static final String LOREM_IPSUM_DOLOR_SIT_AMET_CONSECTETUR_ADIPISCING_ELIT_NULLAM_PRETIUM_BIBENDUM_RISUS_A_PELLENTESQUE_VESTIBULUM_QUIS_EROS_SIT_AMET_MAURIS_LACINIA_VOLUTPAT_VEL_EGET_MI_SED_TINCIDUNT_ELEIFEND_LACINIA_SUSPENDISSE_POTENTI_VIVAMUS_METUS_NISI_MOLLIS_SIT_AMET_NIBH_IN_COMMODO_SCELERISQUE_SAPIEN_AENEAN_TINCIDUNT_ELEIFEND_FRINGILLA_MAURIS_NON_MAXIMUS_LOREM_PELLENTESQUE_HABITANT_MORBI_TRISTIQUE_SENECTUS_ET_NETUS_ET_MALESUADA_FAMES_AC_TURPIS_EGESTAS_VIVAMUS_VITAE_DIAM_FINIBUS_GRAVIDA_MASSA_SED_SODALES_NEQUE_MORBI_NISL_MI_ULTRICIES_ID_ODIO_VEL_DIGNISSIM_ULTRICES_LIBERO_PRAESENT_VEL_MASSA_AT_LEO_POSUERE_VOLUTPAT_SED_NON_MI_AT_PURUS_VESTIBULUM_TINCIDUNT_ET_IN_LIBERO_MORBI_AT_VIVERRA_MI_VIVAMUS_QUIS_NISL_NISI = " ,.-:;_/abc123"; // Ok
}
