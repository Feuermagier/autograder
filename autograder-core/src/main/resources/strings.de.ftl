# Statuses
status-compiling = Compiling
status-spotbugs = Running SpotBugs
status-pmd = Running PMD
status-cpd = Running Copy/Paste-Detection
status-error-prone = Running error-prone
status-model = Building the code model
status-docker = Building the Docker image
status-tests = Executing tests
status-integrated = Running integrated analysis

# Linters
linter-cpd = Copy/Paste-Detection
linter-spotbugs = SpotBugs
linter-pmd = PMD
linter-integrated = Integrated Analysis
linter-error-prone = error-prone

merged-problems = {$message} Weitere Probleme in {$locations}.

# CPD
duplicate-code = Duplizierter Code ({$lines}): {$first-path}:{$first-start}-{$first-end} und {$second-path}:{$second-start}-{$second-end}

# API
is-empty-reimplemented-exp = Verwende isEmpty()

old-collection-exp-vector = Verwende ArrayList statt Vector
old-collection-exp-hashtable = Verwende HashMap statt Hashtable
old-collection-exp-stack = Verwende Dequeue statt Stack

string-is-empty-exp-emptiness = Benutze 'isEmpty()' statt '{$exp}' um auf Leerheit zu prüfen
string-is-empty-exp-non-emptiness = Benutze '!<...>isEmpty()' statt '{$exp}' um auf mindestens ein Element zu prüfen

use-string-formatted = `{$formatted}` ist schöner zu lesen.

optional-argument = Optional sollte nicht als Argument verwendet werden, da man dann 3 Zustände hat: null, Optional.empty() und Optional.of(..). Siehe https://stackoverflow.com/a/31924845/7766117
optional-tri-state = Statt einem Optional boolean, sollte man ein enum verwenden.

equals-hashcode-comparable-contract = Es müssen immer equals und hashCode zusammen überschrieben werden. Genauso muss wenn Comparable implementiert wird equals und hashCode überschrieben werden.

use-format-string = `{$formatted}` ist schöner zu lesen.

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

common-reimplementation = Der Code kann vereinfacht werden zu '{$suggestion}'.

use-entry-set = Verwende hier 'entrySet' statt 'keySet'.

char-range = Der Code kann vereinfacht werden zu '{$suggestion}'.

# Comment
commented-out-code-exp = Dieser auskommentierte Code sollte entfernt werden

comment-language-exp-invalid = Dieser Kommentar ist weder auf Deutsch noch auf Englisch, sondern scheint {$lang} zu sein
comment-language-exp-english = Der Code enthält deutsche und englische Kommentare. Dieser Kommentar ist auf Englisch. Ein deutscher Kommentar befindet sich bei {$path}:{$line}
comment-language-exp-german = Der Code enthält deutsche und englische Kommentare. Dieser Kommentar ist auf Deutsch. Ein englischer Kommentar befindet sich bei {$path}:{$line}

javadoc-method-exp-param-missing = Der Parameter '{$param}' wird im Javadoc-Kommentar nicht erwähnt
javadoc-method-exp-param-unknown = Der JavaDoc-Kommentar erwähnt den Parameter '{$param}', dieser wird allerdings nicht deklariert
javadoc-method-exp-unexpected-tag = JavaDoc-Kommentare von Methoden sollten keinen '@{$tag}'-Tag haben

javadoc-type-exp-unexpected-tag = JavaDoc-Kommentare von Typen sollten keinen '@{$tag}'-Tag haben
javadoc-type-exp-invalid-author = Im @author-Tag sollte dein u-Kürzel stehen: {$authors}

javadoc-field-exp-unexpected-tag = JavaDoc-Kommentare von Attributen sollten keinen '@{$tag}'-Tag haben

javadoc-return-null-exp = Die Methode {$method} kann null zurückgeben, der @return-Tag erwähnt das aber nicht

javadoc-stub-exp-desc = Die Beschreibung des Javadoc-Kommentars ist leer
javadoc-stub-exp-param = Nichtssagende Beschreibung für den Parameter '{$param}'
javadoc-stub-exp-return = Nichtssagende Beschreibung für den Rückgabewert
javadoc-stub-exp-throws = Nichtssagende Beschreibung für die Exception {$exp}

javadoc-undocumented-throws = Die Exception {$exp} wird geworfen, aber nicht im Javadoc-Kommentar erwähnt.

# Complexity
use-diamond-operator = Du kannst die Typen in `< A, B, ... >` entfernen und stattdessen `<>` verwenden, siehe https://docs.oracle.com/javase/tutorial/java/generics/genTypeInference.html und https://stackoverflow.com/a/16352848/7766117

extends-object = Explizit von Object zu erben ist unnötig

for-loop-var = for-Schleifen sollten genau eine lokal deklarierte Kontrollvariable haben

implicit-constructor-exp = Unnötiger Standard-Konstruktor

redundant-if-for-bool-exp-return = {$exp} kann direkt zurückgegeben werden
redundant-if-for-bool-exp-assign = '{$exp}' kann direkt zu '{$target}' zugewiesen werden

redundant-modifier-desc = Einige Modifizierer sind implizit
redundant-modifier-exp = Unnötiger Modifizierer

redundant-return-exp = Unnötiges return

redundant-boolean-equal = Es ist unnötig explizit zu überprüfen, ob eine Bedingung gleich true oder false ist. Schreibe stattdessen '{$suggestion}'.

self-assignment-exp = Nutzlose Zuweisung von '{$rhs}' zu '{$lhs}'

redundant-local-return-exp = Der Wert kann direkt zurückgegeben werden

unused-import-exp = Unbenutzter Import

wrapper-instantiation-exp = Wrapper-Klassen sollten nicht direkt instanziiert werden

repeated-math-operation-mul = Hier sollte Math.pow verwendet werden, anstatt '{$var}' {$count}-mal mit sich selbst zu multiplizieren.
repeated-math-operation-plus = Hier sollte eine Multiplikation mit {$count} verwendet werden, anstatt '{$var}' {$count}-mal mit sich selbst zu addieren.

redundant-neg-exp = '{$original}' kann zu '{$fixed}' vereinfacht werden

use-operator-assignment-exp = Zuweisung kann zu '{$simplified}' vereinfacht werden

merge-else-if = Verwende 'else if (...) {"{"} ... {"}"}' statt 'else {"{"} if (...) {"{"} ... {"}"} {"}"}'

complex-regex = Nichttriviale Regex brauchen einen erklärenden Kommentar (Score ist {$score}, maximal erlaubt ist {$max})

redundant-catch = Eine exception sollte nicht gefangen werden, um sie dann direkt wieder zu werfen.

redundant-array-init = Die Zuweisung zu dem array ist unnötig und kann entfernt werden.

redundant-uninitialized-variable = Die Variable '{$variable}' wurde deklariert, aber der Wert '{$value}' wird nicht direkt zugewiesen. Schreibe stattdessen '{$suggestion}'.

multiple-inline-statements = Es sollten nicht mehrere Aussagen in einer Zeile stehen. Also keine Deklarationen von mehreren Variablen oder Zuweisungen in einer Zeile.

unnecessary-boxing = Statt dem boxed-type sollte man '{$suggestion}' verwenden.

multi-threading = Multithreading ist nicht Teil der Vorlesung. Code der nur einem Thread ausgeführt wird, thread-safe zu schreiben, macht den Code unnötig komplex.

# Debug
assert-used-exp = Assertions lassen das gesamte Programm abstürzen, wenn sie false sind.
    Außerdem können sie deaktiviert werden, weswegen man sich nicht darauf verlassen kann,
    dass bestimmte Bedingungen zutreffen. Sie sind super für Testzwecke, sollten aber nicht
    Teil der finalen Lösung sein. Wenn du eine Invariante dokumentieren willst, verwende
    einen Kommentar.

print-stack-trace-exp = Stack Traces sollten in der Abgabe nicht ausgegeben werden

# Exceptions
custom-exception-inheritance-exp-runtime = Selbstdefinierte Exceptions sollten immer Checked Exceptions sein
custom-exception-inheritance-exp-error = Selbstdefinierte Exceptions sollten nicht von Error erben

empty-catch-block = Leerer catch-Block

exception-controlflow-caught = {$exception} wird geworfen und im umgebenden Block sofort wieder gefangen
exception-controlflow-should-not-be-caught = {$exception} sollte man niemals fangen

runtime-exception-caught = RuntimeExceptions '{$exception}' sollten nicht gefangen werden

exception-message = Eine Exception sollte immer eine Nachricht dabei haben, die erklärt was der Fehler ist und im Idealfall wie es zu dem Fehler kam.

# General

compare-objects-exp = Implementiere eine equals-Methode für den Typ {$type} und verwende sie zum Vergleichen

variable-should-be = Die Variable '{$variable}' sollte '{$suggestion}' sein.

constants-interfaces-exp = Interfaces sollten keine Attribute haben

param-reassign-exp = Parameter sollten nicht neu zugewiesen werden

double-brace-init = Die obskure 'Double Brace'-Syntax sollte vermieden werden

equals-handle-null-argument-exp = Die equals-Methode sollte null-Werte behandeln

field-local-exp = Das Attribut '{$field}' der Klasse {$class} sollte eine lokale Variable sein, da sie in jeder Methode vor dem ersten Lesen überschrieben wird

for-foreach = for-Schleife sollte eine for-each-Schleife sein

missing-override-exp = Fehlendes @Override

system-dependent-linebreak-exp = Systemabhängiger Zeilenumbruch (\n) benutzt. Besser ist System.lineSeparator() oder (falls es sich um einen format-String handelt) '%n'.

field-final-exp = Das Attribut '{$name}' sollte final sein

string-cmp-exp = Strings sollten nicht per Referenz, sonder mit der 'equals'-Methode verglichen werden: '{$lhs}.equals({$rhs})' statt '{$lhs} == {$rhs}'

do-not-use-raw-types-exp = Generische Typen sollten immer mit Typparameter angegeben werden und nie als Raw Type, siehe https://stackoverflow.com/a/2770692/7766117

avoid-labels = Labels sollten vermieden werden. Siehe https://stackoverflow.com/a/33689582/7766117.

avoid-shadowing = Die Variable '{$name}' verdeckt ein Attribut mit dem selben Namen. Abgesehen vom Konstruktor, sollte man das vermeiden.

suppress-warnings = @SuppressWarnings unterdrückt Warnungen des Compilers oder von Checkstyle, anstatt das unterliegende Problem zu beheben.

scanner-closed = Scanner sollte geschlossen werden

unchecked-type-cast = Es muss sicher gestellt werden, dass der Typ des Objekts mit dem Typ des Casts übereinstimmt. Ansonsten kann der Code abstürzen.

compare-char-value = char-Werte im ASCII Bereich sollten als char-Werte verglichen werden, nicht als int-Werte.

use-guard-clauses = Der Code bricht den normalen Kontrollfluss durch zum Beispiel ein return ab. if-else-Blöcke mit solchen Abbrüchen kann man mithilfe von sogenannten guard-clauses schöner schreiben. Das hat unter anderem den Vorteil, dass man doppelten Code leichter erkennt. Siehe für eine detaillierte Erklärung https://medium.com/@scadge/if-statements-design-guard-clauses-might-be-all-you-need-67219a1a981a oder https://deviq.com/design-patterns/guard-clause

import-types = Statt den Pfad zum Typ anzugeben, sollte '{$type}' importiert werden. Datentypen aus dem selben Paket oder 'java.lang' müssen nicht explizit importiert werden.

use-different-visibility = Die Sichtbarkeit von '{$name}' sollte '{$suggestion}' sein.

avoid-recompiling-regex = Die Konstante wird nur mit 'Pattern.compile' oder 'Pattern.matches' verwendet. Konvertiere die Konstante zu einem Pattern mit dem Wert '{$suggestion}'.

merge-nested-if = Die Verschachtelte if kann mit der äußeren if kombiniert werden. Die Bedingung der äußeren if sollte '{$suggestion}' sein.

binary-operator-on-boolean = Statt '|' und '&' sollte man '||' und '&&' verwenden.

# Naming
bool-getter-name = Für boolean getter bietet es sich an ein Verb als Präfix zu verwenden. Zum Beispiel '{$newName}' statt '{$oldName}'.

constants-name-exp = Der Name '{$name}' ist nicht aussagekräftig gegeben den Wert '{$value}'
constants-name-exp-value = Der Wert '{$value}' der Konstante '{$name}' sollte nicht im Namen vorkommen

linguistic-desc = Das Code-Element hat einen verwirrenden Namen. Siehe https://pmd.github.io/latest/pmd_rules_java_codestyle.html#linguisticnaming

variable-name-single-letter = Der Bezeichner '{$name}' ist nicht aussagekräftig
variable-is-abbreviation = Unnötige Abkürzung '{$name}'
variable-name-type-in-name = Der Bezeichner '{$name}' sollte nicht den Typ im Namen haben
similar-identifier = Der Bezeichner '{$left}' ist sehr ähnlich zu '{$right}'. Das kann zu Verwechslungen und Tippfehlern führen, weswegen man diesen umbenennen sollte.

type-has-descriptive-name-pre-suffix = Der Name enthält unnötige Präfixe oder Suffixe
type-has-descriptive-name-exception = Eine Klasse die von Exception erbt, sollte 'Exception' am Ende ihres Namens haben

package-naming-convention = Der Name eines Pakets sollte am besten ein Wort sein und alle Buchstaben sollten nach Konvention
                            klein sein. Zudem sollten keine Sonderzeichen auftreten wie '_'. An folgenden Stellen wird das
                            nicht eingehalten: '{$positions}'

variable-redundant-number-suffix = Der Bezeichner '{$name}' enthält eine redundante Zahl am Ende.

# OOP
concrete-collection-exp = Statt konkreten Collections sollten immer allgemeine Interfaces verwendet werden (z.B. List statt ArrayList)

list-getter-exp = Kopiere diese veränderbare Collection bevor du sie zurückgibst, um unbeabsichtigte Veränderungen durch andere Klassen zu verhindern

method-abstract-exp = {$type}::{$method} sollte abstrakt sein, anstatt eine Platzhalter-Implementierung anzugeben

utility-exp-final = Utility-Klasse ist nicht final
utility-exp-constructor = Utility-Klassen müssen genau einen privaten und parameterlosen Konstruktor haben

static-field-exp = Das statische Attribut '{$name}' sollte ein Instanzattribut sein

constants-class-exp = Konstanten sollten in der Klasse gespeichert werden in der sie auch verwendet werden und nicht in einer separaten Klasse. Siehe https://stackoverflow.com/a/15056462/7766117

interface-static-method-exp = Interfaces sollte keine statischen Methoden haben, da sie nicht überschrieben werden können.
interface-static-exp = Interfaces müssen nicht static sein. Das Schlüsselwort 'static' ist redundant und sollte entfernt werden.

empty-interface-exp = Interfaces sollten nicht leer sein.

ui-input-separation = Eingaben sollten zentral in einer Klasse eingelesen werden. Wurde auch verwendet in {$first}.
ui-output-separation = Ausgaben sollten zentral in einer Klasse gemacht werden. Wurde auch verwendet in {$first}.

do-not-use-system-exit = System.exit() darf nicht verwendet werden. Strukturiere deinen Code so, dass er sich natürlich beendet.

avoid-inner-classes = Jede Klasse sollte in einer eigenen Datei sein. Innere-Klassen sollten vermieden werden.

mutable-enum = Enums sollten nicht veränderbar sein. Siehe https://stackoverflow.com/a/41199773/7766117

should-be-enum-attribute = Die Werte vom switch sollten Attribute des enums sein. Alternativ sollte man eine Map verwenden.

closed-set-of-values-switch = Ein switch hat nur endlich viele cases. Dabei handelt es sich um eine abgeschlossene Menge, die als enum modelliert werden sollte.
closed-set-of-values-list = Eine Auflistung von endlich vielen Werten sollte als enum modelliert werden.
closed-set-of-values-method = Die Methode gibt nur die Konstanten Werte '{$values}' zurück. Dabei handelt es sich um endlich viele, weswegen man das als enum modellieren sollte.

do-not-use-instanceof = instanceof sollte nicht verwendet werden. Siehe Ilias Wiki.
do-not-use-instanceof-emulation = instanceof sollte nicht verwendet werden und auch nicht durch getClass oder ClassCastException emuliert werden. Siehe Ilias Wiki.

abstract-class-without-abstract-method = Abstrakte Klassen sollten mindestens eine abstrakte Methode haben.
composition-over-inheritance = Die Oberklasse hat nur Felder. Statt Vererbung sollte hier Komposition verwendet werden. Zum Beispiel ein interface mit dem getter: '{$suggestion}'.
should-be-interface = Die Klasse hat nur Methoden und keine Felder. Statt Vererbung sollte hier ein Interface mit Standard-Implementierungen verwendet werden.

# Structure

default-package = Das default-Paket sollte nicht verwendet werden. Die folgenden Klassen sind im default-Paket: {$positions}

# Unnecessary

empty-block = Leere Blöcke sollten entfernt werden oder einen Kommentar haben, der erklärt warum sie leer sind.

unused-element = '{$name}' wird nicht verwendet und sollte deswegen entfernt werden
