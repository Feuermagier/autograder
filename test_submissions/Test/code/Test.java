/**
 * Diese Klasse ist der Kontrolle des Eingabestring gewidmet.
 * Die untenstehenden Methoden werden alle in der "Obermethode" 
 * "checkInput" verwendet.
 * Ziel ist es, mithilfe der Methoden sicherzustellen, dass nur Eingaben die
 * der kontextfreien Grammatik entsprechen, verrechnet werden.

 * @author ugjqb
 *
 *
 */


public class Test {

	private Test() {
	}

	private static final String OPENING_SQUARE_BRACKET = "Error, missing opening square bracket.";
	private static final String CLOSING_SQUARE_BRACKET = "Error, missing closing square bracket.";

	private static final String OPENING_ROUND_BRACKET = "Error, missing opening round bracket.";
	private static final String CLOSING_ROUND_BRACKET = "Error, missing closing round bracket.";

	private static final String UNEXPECTED_INPUT_START = "Error, input has an incorrect start.";

	private static final String NUMBER_FORMAT = "Error, a complex number is not as specified.";
	private static final String UNEXPECTED_I_POSITION = "Error, unexpected I position.";

	private static final String NO_COMPLEX_NUMBER = "Error, no intact Complex Number detected.";

	/**
	 * Diese Methode prueft, ob sich ein String Abschnitt in eine Long 
	 * Zahl umwandeln laesst. Ein passender boolean Wert wird zurueckgegeben.

	 * @param input Der String wird übergeben.
	 * @return Ein entsprechender Boolean Wert wird zurueckgegeben.
	 */
	private static boolean checkForLong(String input) {

		try {
			long parsedNumber = Long.parseLong(input);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * Diese Methode prüft, ob vor der schließenden runden Klammer das
	 * passende i steht.

	 * @param input Der zu kontrollierende String wird uebergeben.
	 * @param index Der Index der schliessenden Klammer
	 * @return Ein entsprechender boolean Wert wird zurueckgegeben.
	 */
	private static boolean checkForI(String input, int index) {
		if (input.charAt(index - 1) == 'i') {
			return true;
		}
		return false;
	}
	/**
	 * Die Methode prüft, ob die gleiche Anzahl öffnender wie schließender Klammern (eckig)
	 *  vorhanden ist.

	 * @param input In diesem String wird geprüft.
	 * @return Ein entsprechender Boolean Wert wird zurueckgegeben.
	 */
	private static boolean checkSquareBrackets(String input) {
		int bracketCounter = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '[') {
				bracketCounter++;
			}
			if (input.charAt(i) == ']') {
				bracketCounter--;
			}
		}
		if (bracketCounter < 0) {
			System.out.println(OPENING_SQUARE_BRACKET);
			return false;
		} else if (bracketCounter > 0) {
			System.out.println(CLOSING_SQUARE_BRACKET);
			return false;
		}
		return true;

	}

	/**
	 * Die Methode prüft, ob die gleiche Anzahl öffnender wie schließender Klammern (rund)
	 * vorhanden ist.

	 * @param input input In diesem String wird geprüft.
	 * @return Ein entsprechender Boolean Wert wird zurueckgegeben.
	 */
	private static boolean checkRoundBrackets(String input) {
		int bracketCounter = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '(') {
				bracketCounter++;
			}
			if (input.charAt(i) == ')') {
				bracketCounter--;
			}
		}
		if (bracketCounter < 0) {
			System.out.println(OPENING_ROUND_BRACKET);
			return false;
		} else if (bracketCounter > 0) {
			System.out.println(CLOSING_ROUND_BRACKET);
			return false;
		}
		return true;

	}

	/**
	 * In dieser Methode wird ein String auf grammatilische Korrektheit geprueft.
	 * Dabei wird vor allem der korrekte Aufbau einer komplexen Zahl geprueft.
	 * Die oben beschriebenen untermethoden werden verwendet.

	 * @param input In diesem String wird geprüft.
	 * @return Ein entsprechender Boolean Wert wird zurueckgegeben.
	 */
	private static boolean checkComplex(String input) {
		if (input.charAt(0) != '[' && input.charAt(0) != '(') {
			System.out.println(UNEXPECTED_INPUT_START);
			return false;
		}

		int indexOpeningBracket = -1;
		int indexPlus = -1;
		int indexClosingBracket = -1;

		int intactComplexNumbers = 0;

		marke:
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '(' && indexOpeningBracket == -1) {
				indexOpeningBracket = i;
			} else if (input.charAt(i) == '+' && indexOpeningBracket < i  && indexOpeningBracket != -1) {
				indexPlus = i;

			} else if (input.charAt(i) == ')' && indexPlus < i && indexPlus != -1) {
				indexClosingBracket = i;
				intactComplexNumbers++;


				if (!checkForLong(input.substring(indexOpeningBracket + 1, indexPlus))) {
					System.out.println(NUMBER_FORMAT);
					return false;
				} else if (!checkForLong(input.substring(indexPlus + 1, indexClosingBracket - 1))) {
					System.out.println(NUMBER_FORMAT);
					return false;
				} else if (!checkForI(input, indexClosingBracket)) {
					System.out.println(UNEXPECTED_I_POSITION);
					return false;
				}

				i = indexClosingBracket;
				indexClosingBracket = indexOpeningBracket = indexPlus = -1;
				continue marke;
			}
			if (i == input.length() - 1 && intactComplexNumbers == 0) {
				System.out.println(NO_COMPLEX_NUMBER);
				return false;
			}
		}
		return true;

	}
	/**
	 * Beschreibung.

	 * @param input Beschreibung.
	 * @return Beschreibung.
	 */
	public static boolean checkInput(String input) {
		if (checkSquareBrackets(input) && checkRoundBrackets(input) && checkComplex(input)) {
			return true;
		}

		return false;


	}


}
