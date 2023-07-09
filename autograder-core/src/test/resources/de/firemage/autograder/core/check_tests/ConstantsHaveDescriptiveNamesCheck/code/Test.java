package de.firemage.autograder.core.check_tests.ConstantsHaveDescriptiveNamesCheck.code;

public class Test {
    private static final int ZERO = 0; /*@ not ok @*/
    private static final int ONE = 1; /*@ not ok @*/
    private static final int TWO = 2; /*@ not ok @*/
    private static final String ERROR = ""; /*@ not ok @*/
    private static final String REGEX = ""; /*@ not ok @*/
    private static final int VALUE_A = 1; /*@ ok @*/
    private static final Object d = null; //?# ok (covered by descriptive variable check)
    private static final boolean TRUE = true; /*@ not ok @*/
    private static final boolean FALSE = false; /*@ not ok @*/
    private static final String UP = "up"; /*@ not ok @*/
    private static final String DOWN = "down"; /*@ not ok @*/
    private static final String DEBUG_PATH = "debug-path"; /*@ not ok @*/
    private static final String LEFT = "left"; /*@ not ok @*/
    private static final String RIGHT = "right"; /*@ not ok @*/
    private static final String SPLIT_COLON = ":"; /*@ not ok @*/
    private static final String SPLIT_COMMA = ","; /*@ not ok @*/
    private static final String SPLIT_ARROW = "-->"; /*@ not ok @*/
    private static final String CONSTANT = "foo"; /*@ not ok @*/
    private static final int VALUE_OF_2 = 2; /*@ not ok @*/
    private static final String NOT_LOADED = "A simulation must be loaded first"; /*@ ok @*/
    // stress test (algorithm should be fast enough for this):
    private static final String LOREM_IPSUM_DOLOR_SIT_AMET_CONSECTETUR_ADIPISCING_ELIT_NULLAM_PRETIUM_BIBENDUM_RISUS_A_PELLENTESQUE_VESTIBULUM_QUIS_EROS_SIT_AMET_MAURIS_LACINIA_VOLUTPAT_VEL_EGET_MI_SED_TINCIDUNT_ELEIFEND_LACINIA_SUSPENDISSE_POTENTI_VIVAMUS_METUS_NISI_MOLLIS_SIT_AMET_NIBH_IN_COMMODO_SCELERISQUE_SAPIEN_AENEAN_TINCIDUNT_ELEIFEND_FRINGILLA_MAURIS_NON_MAXIMUS_LOREM_PELLENTESQUE_HABITANT_MORBI_TRISTIQUE_SENECTUS_ET_NETUS_ET_MALESUADA_FAMES_AC_TURPIS_EGESTAS_VIVAMUS_VITAE_DIAM_FINIBUS_GRAVIDA_MASSA_SED_SODALES_NEQUE_MORBI_NISL_MI_ULTRICIES_ID_ODIO_VEL_DIGNISSIM_ULTRICES_LIBERO_PRAESENT_VEL_MASSA_AT_LEO_POSUERE_VOLUTPAT_SED_NON_MI_AT_PURUS_VESTIBULUM_TINCIDUNT_ET_IN_LIBERO_MORBI_AT_VIVERRA_MI_VIVAMUS_QUIS_NISL_NISI = " ,.-:;_/abc123"; /*@ ok @*/
    private static final String EMPTY = " "; /*@ not ok @*/
    private static final String BLANK = " "; /*@ not ok @*/
    private static final int COMPARE = 0; /*@ not ok @*/
    private static final String ERROR_MESSAGE = "The start and the end of an edge can not be the same vertex."; /*@ ok @*/
    private static final String This_is_a_very_long_string = "This is a very long string, which, should, test, the, ability, of, the, code, to, handle, long, strings"; /*@ ok @*/
    private static final int FIRST_PARAMETER = 0; /*@ not ok @*/
    private static final String DATE_TIME_SEPARATOR = "T"; /*@ ok @*/
    private static final String T_VALUE = "T"; /*@ not ok @*/
}
