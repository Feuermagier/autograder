# Statuses
status-compiling = Compiling
status-spotbugs = Running SpotBugs
status-pmd = Running PMD
status-error-prone = Running error-prone
status-model = Building the code model
status-integrated = Running integrated analysis

# Linters
linter-pmd = PMD
linter-integrated = Integrated Analysis
linter-error-prone = error-prone

merged-problems = {$message} Weitere Probleme in {$locations}.

# CPD
duplicate-code = Duplizierter Code: {$left} und {$right}.

# API

suggest-replacement = Verwende '{$suggestion}' statt '{$original}'.
common-reimplementation = Der Code kann vereinfacht werden zu '{$suggestion}'.

use-string-formatted = '{$formatted}' ist schöner zu lesen.
use-format-string = '{$formatted}' ist schöner zu lesen.

optional-tri-state = Statt einem Optional boolean, sollte man ein enum verwenden.

equals-hashcode-comparable-contract = Es müssen immer equals und hashCode zusammen überschrieben werden. Genauso muss wenn Comparable implementiert wird equals und hashCode überschrieben werden.

use-enum-collection = Bei Maps wenn ein Enum als Key ist und bei Sets als Wert, sollte man EnumMap/EnumSet verwenden.

compare-to-zero = Das Ergebnis von compareTo oder compare sollte nur mit 0 verglichen werden.
                Es ist eine Implementierungsdetail, ob ein gegebener Typ streng die Werte
                '-1, 0, +1' oder andere zurückgibt.
equals-using-hashcode = Equals nur mit hashCode zu implementieren ist fehleranfällig.
                        Hashes kollidieren häufig, was zu falschen Ergebnissen in equals führt.
equals-unsafe-cast = Im Javadoc von equals steht, dass es false für inkompatible Typen zurückgeben soll.
                    Diese Implementierung kann eine ClassCastException werfen.
equals-incompatible-type = Ein Vergleich zwischen Objekten mit inkompatiblen Typen gibt immer false zurück
inconsistent-hashcode = Das hashCode-Verhalten ist inkonsistent. Es wird in equals nicht verglichen, aber in hashCode verwendet.
undefined-equals = Es ist nicht garantiert, dass dieser Typ eine sinnvolle equals-Methode implementiert.
non-overriding-equals = equals-Methode überschreibt nicht Object.equals
equals-broken-for-null = equals kann eine NullPointerException werfen, wenn null übergeben wird
array-hash-code = hashCode auf arrays, hasht nicht den Inhalt des Arrays
equals-reference = == sollte in equals verwendet werden, um Gleichheit zu sich selbst zu prüfen, sonst entsteht eine Endlosschleife.
array-as-key-of-set-or-map = Arrays überschreiben weder equals noch hashCode. Dementsprechend werden Vergleiche basierend
                            auf der Referenz gemacht und nicht auf dem Inhalt. Verwende stattdessen eine Liste.

implement-comparable = Der Typ '{$name}' sollte `Comparable<{$name}>` implementieren, dann kann man sich den `Comparator` sparen.

# Comment
commented-out-code = Dieser auskommentierte Code sollte entfernt werden

comment-language-exp-invalid = Dieser Kommentar ist weder auf Deutsch noch auf Englisch, sondern scheint {$lang} zu sein
comment-language-exp-english = Der Code enthält deutsche und englische Kommentare. Dieser Kommentar ist auf Englisch. Ein deutscher Kommentar befindet sich bei {$path}:{$line}
comment-language-exp-german = Der Code enthält deutsche und englische Kommentare. Dieser Kommentar ist auf Deutsch. Ein englischer Kommentar befindet sich bei {$path}:{$line}

unnecessary-comment-empty = Dieser Kommentar ist leer und sollte daher entfernt werden

javadoc-method-exp-param-missing = Der Parameter '{$param}' wird im Javadoc-Kommentar nicht erwähnt
javadoc-method-exp-param-unknown = Der JavaDoc-Kommentar erwähnt den Parameter '{$param}', dieser wird allerdings nicht deklariert

javadoc-unexpected-tag = Der JavaDoc-Kommentar sollte keinen '@{$tag}'-Tag haben.

javadoc-type-exp-invalid-author = Im @author-Tag sollte dein u-Kürzel stehen: {$authors}

javadoc-stub-exp-desc = Die Beschreibung des Javadoc-Kommentars ist leer
javadoc-stub-exp-param = Nichtssagende Beschreibung für den Parameter '{$param}'
javadoc-stub-exp-return = Nichtssagende Beschreibung für den Rückgabewert
javadoc-stub-exp-throws = Nichtssagende Beschreibung für die Exception {$exp}

javadoc-undocumented-throws = Die Exception {$exp} wird geworfen, aber nicht im Javadoc-Kommentar erwähnt.

todo-comment = TODOs sollten nicht in der finalen Abgabe vorhanden sein.

# Complexity
use-diamond-operator = Du kannst die Typen in '< A, B, ... >' entfernen und stattdessen '<>' verwenden, siehe https://docs.oracle.com/javase/tutorial/java/generics/genTypeInference.html und https://stackoverflow.com/a/16352848/7766117

extends-object = Explizit von Object zu erben ist unnötig

for-loop-var = for-Schleifen sollten genau eine lokal deklarierte Kontrollvariable haben

implicit-constructor-exp = Unnötiger Standard-Konstruktor

redundant-modifier = Die folgenden Modifier sind redundant und sollten deswegen entfernt werden: {$modifier}.

redundant-return-exp = Unnötiges return

redundant-boolean-equal = Es ist unnötig explizit zu überprüfen, ob eine Bedingung gleich true oder false ist. Schreibe stattdessen '{$suggestion}'.

self-assignment-exp = Nutzlose Zuweisung von '{$rhs}' zu '{$lhs}'

too-many-exceptions = Das Projekt definiert {$count} Exceptions. Das sind zu viele.

redundant-variable = Die Variable '{$name}' ist unnötig, verwende stattdessen den Wert: '{$suggestion}'.

unused-import = Der Import '{$import}' wird nicht verwendet und sollte deswegen entfernt werden.

use-operator-assignment-exp = Zuweisung kann zu '{$simplified}' vereinfacht werden

complex-regex = Nichttriviale Regex brauchen einen erklärenden Kommentar (Score ist {$score}, maximal erlaubt ist {$max})

redundant-catch = Eine exception sollte nicht gefangen werden, um sie dann direkt wieder zu werfen.

redundant-uninitialized-variable = Die Variable '{$variable}' wurde deklariert, aber der Wert '{$value}' wird nicht direkt zugewiesen. Schreibe stattdessen '{$suggestion}'.

multiple-inline-statements = Es sollten nicht mehrere Aussagen in einer Zeile stehen. Also keine Deklarationen von mehreren Variablen oder Zuweisungen in einer Zeile.

multi-threading = Multithreading ist nicht Teil der Vorlesung. Code der nur einem Thread ausgeführt wird, thread-safe zu schreiben, macht den Code unnötig komplex.

try-catch-complexity = Die Komplexität von try-catch-Blöcken sollte möglichst gering gehalten werden. Es sollten weniger als {$max} Statements vorhanden sein. Versuche den Code in mehrere Methoden aufzuteilen bzw. nicht dringend benötigte Statements aus dem try-Block zu entfernen.

redundant-else = Die 'else' ist unnötig. Schreibe '{$expected}' statt '{$given}'.

redundant-assignment = Der Variable '{$variable}' wird hier ein Wert zugewiesen, der dann nie verwendet wird. Deswegen kann die Zuweisung entfernt werden.

# Debug
assert-used-exp = Assertions lassen das gesamte Programm abstürzen, wenn sie false sind.
    Außerdem können sie deaktiviert werden, weswegen man sich nicht darauf verlassen kann,
    dass bestimmte Bedingungen zutreffen. Sie sind super für Testzwecke, sollten aber nicht
    Teil der finalen Lösung sein. Wenn du eine Invariante dokumentieren willst, verwende
    einen Kommentar.

print-stack-trace = Stack Traces sollten in der Abgabe nicht ausgegeben werden

# Exceptions
custom-exception-inheritance-error = Die selbstdefinierte Exception '{$name}' sollte nicht von 'Error' erben.
custom-exception-inheritance-runtime = Die selbstdefinierte Exception '{$name}' sollte von 'Exception' erben und nicht 'RuntimeException'.

empty-catch-block = Leerer catch-Block

exception-controlflow-caught = {$exception} wird geworfen und im umgebenden Block sofort wieder gefangen
exception-controlflow-should-not-be-caught = {$exception} sollte man niemals fangen

runtime-exception-caught = RuntimeExceptions '{$exception}' sollten nicht gefangen werden

exception-message = Eine Exception sollte immer eine Nachricht dabei haben, die erklärt was der Fehler ist und im Idealfall wie es zu dem Fehler kam.

number-format-exception-ignored = NumberFormatException sollte gefangen werden und entweder behandelt oder durch eine eigene Exception ersetzt werden.

# General

compare-objects-exp = Implementiere eine equals-Methode für den Typ {$type} und verwende sie zum Vergleichen

variable-should-be = Die Variable '{$variable}' sollte '{$suggestion}' sein.

reassigned-parameter = Dem Parameter '{$name}' sollte kein neuer Wert zugewiesen werden.

double-brace-init = Die obskure 'Double Brace'-Syntax sollte vermieden werden

missing-override = '{$name}' sollte eine '@Override'-Annotation haben, siehe https://stackoverflow.com/a/94411/7766117.

system-specific-linebreak = Systemabhängiger Zeilenumbruch (\n) benutzt. Besser ist System.lineSeparator() oder (falls es sich um einen format-String handelt) '%n'.

field-should-be-final = Das Attribut '{$name}' sollte final sein.

string-cmp-exp = Strings sollten nicht per Referenz, sonder mit der 'equals'-Methode verglichen werden: '{$lhs}.equals({$rhs})' statt '{$lhs} == {$rhs}'

do-not-use-raw-types-exp = Generische Typen sollten immer mit Typparameter angegeben werden und nie als Raw Type, siehe https://stackoverflow.com/a/2770692/7766117

avoid-labels = Labels sollten vermieden werden. Siehe https://stackoverflow.com/a/33689582/7766117.

avoid-shadowing = Die Variable '{$name}' verdeckt ein Attribut mit dem selben Namen. Abgesehen vom Konstruktor, sollte man das vermeiden.

suppress-warnings = @SuppressWarnings unterdrückt Warnungen des Compilers oder von Checkstyle, anstatt das unterliegende Problem zu beheben.

scanner-closed = Scanner sollte geschlossen werden

unchecked-type-cast = Es muss sicher gestellt werden, dass der Typ des Objekts mit dem Typ des Casts übereinstimmt. Ansonsten kann der Code abstürzen.

compare-char-value = Hier wird '{$expression}' vom Typ char mit dem Wert {$intValue} verglichen. Es ist nicht offensichtlich für welchen Buchstabe der Wert steht, schreibe stattdessen '{$charValue}'.

use-guard-clauses = Der Code bricht den normalen Kontrollfluss durch zum Beispiel ein return ab. if-else-Blöcke mit solchen Abbrüchen kann man mithilfe von sogenannten guard-clauses schöner schreiben. Das hat unter anderem den Vorteil, dass man doppelten Code leichter erkennt. Siehe für eine detaillierte Erklärung https://medium.com/@scadge/if-statements-design-guard-clauses-might-be-all-you-need-67219a1a981a oder https://deviq.com/design-patterns/guard-clause

import-types = Statt den Pfad zum Typ anzugeben, sollte '{$type}' importiert werden. Datentypen aus dem selben Paket oder 'java.lang' müssen nicht explizit importiert werden.

use-different-visibility = Die Sichtbarkeit von '{$name}' sollte '{$suggestion}' sein.
use-different-visibility-field = Die Sichtbarkeit vom Feld '{$name}' sollte immer '{$suggestion}' sein.

avoid-recompiling-regex = Die Konstante wird nur mit 'Pattern.compile' oder 'Pattern.matches' verwendet. Konvertiere die Konstante zu einem Pattern mit dem Wert '{$suggestion}'.

merge-nested-if = Die Verschachtelte if kann mit der äußeren if kombiniert werden. Die Bedingung der äußeren if sollte '{$suggestion}' sein.

binary-operator-on-boolean = Statt '|' und '&' sollte man '||' und '&&' verwenden.

object-datatype = Statt dem Datentyp 'Object', sollte die Variable '{$variable}' einen konkreten oder generischen Datentyp haben.

magic-string = Der String '{$value}' sollte in eine Konstante ausgelagert werden. Siehe Wiki Artikel zu Magic Strings.

loop-should-be-do-while = Diese Schleife sollte eine do-while Schleife sein, weil der Code vor der Schleife, der gleiche wie in der Schleife ist: {$suggestion}

loop-should-be-for = Diese Schleife sollte eine Zählschleife (for) sein: {$suggestion}

# Naming
bool-getter-name = Für boolean getter bietet es sich an ein Verb als Präfix zu verwenden. Zum Beispiel '{$newName}' statt '{$oldName}'.

constants-name-exp = Der Name '{$name}' ist nicht aussagekräftig gegeben den Wert '{$value}'
constants-name-exp-value = Der Wert '{$value}' der Konstante '{$name}' sollte nicht im Namen vorkommen

linguistic-naming-boolean = Der Name von '{$name}' deutet an, dass es vom Typ boolean ist oder diesen zurückgibt, stattdessen ist der Typ '{$type}'.
linguistic-naming-getter = Der Name von '{$name}' deutet an, dass es einen Wert zurückgibt, was aber nicht der Fall ist.
linguistic-naming-setter = Der Name von '{$name}' deutet an, dass es ein setter ist, welcher kein Wert zurückgeben sollte.


variable-name-single-letter = Der Bezeichner '{$name}' ist nicht aussagekräftig
variable-is-abbreviation = Unnötige Abkürzung '{$name}'
variable-name-type-in-name = Der Bezeichner '{$name}' sollte nicht den Typ im Namen haben
similar-identifier = Der Bezeichner '{$left}' ist sehr ähnlich zu '{$right}'. Das kann zu Verwechslungen und Tippfehlern führen, weswegen man diesen umbenennen sollte.

type-has-descriptive-name-pre-suffix = Der Name enthält unnötige Präfixe oder Suffixe
type-has-descriptive-name-exception = Eine Klasse die von Exception erbt, sollte 'Exception' am Ende ihres Namens haben

package-naming-convention = Der Name eines Pakets sollte am besten ein Wort sein und alle Buchstaben sollten nach Konvention
                            klein sein. Außer dem Zeichen '_' sollten zudem keine Sonderzeichen auftreten. An folgenden Stellen wird das
                            nicht eingehalten: '{$positions}'

variable-redundant-number-suffix = Der Bezeichner '{$name}' enthält eine redundante Zahl am Ende.

# OOP
concrete-collection = Der Typ '{$type}' sollte durch eine Schnittstelle wie zum Beispiel 'List' oder 'Set' ersetzt werden.

leaked-collection-return = Die Methode '{$method}' gibt eine Referenz zu dem Feld '{$field}' zurück. Dadurch ist es möglich das Feld von außerhalb zu verändern. Gebe stattdessen eine Kopie zurück.
leaked-collection-constructor = Der Konstruktor '{$signature}' weißt dem Feld '{$field}' eine Referenz zu, dadurch ist es möglich das Feld von außerhalb zu verändern. Weise stattdessen eine Kopie dem Feld zu.
leaked-collection-assign = Die Methode '{$method}' weißt dem Feld '{$field}' eine Referenz zu, dadurch ist es möglich das Feld von außerhalb zu verändern. Weise stattdessen eine Kopie dem Feld zu.

method-abstract-exp = {$type}::{$method} sollte abstrakt sein, anstatt eine Platzhalter-Implementierung anzugeben

method-should-be-static = Die Methode '{$name}' sollte statisch sein, da sie auf keine Instanzattribute oder Methoden zugreift.

utility-exp-final = Utility-Klasse ist nicht final
utility-exp-constructor = Utility-Klassen müssen genau einen privaten und parameterlosen Konstruktor haben

static-field-should-be-instance = Das statische Attribut '{$name}' sollte ein Instanzattribut sein.

constants-class-exp = Konstanten sollten in der Klasse gespeichert werden in der sie auch verwendet werden und nicht in einer separaten Klasse. Siehe https://stackoverflow.com/a/15056462/7766117

empty-interface-exp = Interfaces sollten nicht leer sein.

ui-input-separation = Eingaben sollten nicht im Programm verteilt sein. Wurde auch verwendet in {$first}.
ui-output-separation = Ausgaben sollten nicht im Programm verteilt sein. Wurde auch verwendet in {$first}.

do-not-use-system-exit = System.exit() darf nicht verwendet werden. Strukturiere deinen Code so, dass er sich natürlich beendet.

avoid-inner-classes = Jede Klasse sollte in einer eigenen Datei sein. Innere-Klassen sollten vermieden werden.

mutable-enum = Enums sollten nicht veränderbar sein. Siehe https://stackoverflow.com/a/41199773/7766117

should-be-enum-attribute = Die Werte vom switch sollten Attribute des enums sein. Alternativ sollte man eine Map verwenden.

closed-set-of-values-switch = Der Switch hat nur endlich viele cases. Dabei handelt es sich um eine abgeschlossene Menge, die als enum modelliert werden sollte. Die Werte sind: '{$values}'.
closed-set-of-values-list = Die Auflistung hat nur endlich viele Werte und sollte deswegen als enum modelliert werden. Die Werte sind: '{$values}'.
closed-set-of-values-method = Die Methode gibt nur die Konstanten Werte '{$values}' zurück. Dabei handelt es sich um endlich viele, weswegen man das als enum modellieren sollte.

do-not-use-instanceof = instanceof sollte nicht verwendet werden. Siehe Ilias Wiki.
do-not-use-instanceof-emulation = instanceof sollte nicht verwendet werden und auch nicht durch getClass oder ClassCastException emuliert werden. Siehe Ilias Wiki.

abstract-class-without-abstract-method = Abstrakte Klassen sollten mindestens eine abstrakte Methode haben.
composition-over-inheritance = Die Oberklasse hat nur Felder. Statt Vererbung sollte hier Komposition verwendet werden. Zum Beispiel ein interface mit dem getter: '{$suggestion}'.
should-be-interface = Die Klasse hat nur Methoden und keine Felder. Statt Vererbung sollte hier ein Interface mit Standard-Implementierungen verwendet werden.

avoid-static-blocks = Statische Blöcke sollten vermieden werden, da sie keine objekt-orientierte Lösung darstellen und schlecht erweiterbar sind. Statische Blöcke sollten durch eine objektorientierte Lösung ersetzt werden (bspw. Konstruktoren).

# Structure

default-package = Das default-Paket sollte nicht verwendet werden. Die folgenden Klassen sind im default-Paket: {$positions}
too-few-packages = Das Projekt hat mehr als {$max} Klassen, aber nur ein Paket. Du solltest dir eine sinnvolle Einteilung der Klassen in mehrere Pakete überlegen (bspw. nach den Kriterien commands, logic, ui, ...).

# Unnecessary

empty-block = Leere Blöcke sollten entfernt werden oder einen Kommentar haben, der erklärt warum sie leer sind.

unused-element = '{$name}' wird nicht verwendet und sollte deswegen entfernt werden
