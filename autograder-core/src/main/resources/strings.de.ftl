# Statuses
status-compiling = Compiling
status-spotbugs = Running SpotBugs
status-pmd = Running PMD
status-cpd = Running Copy/Paste-Detection
status-model = Building the code model
status-docker = Building the Docker image
status-tests = Executing tests
status-integrated = Running integrated analysis

# Linters
linter-cpd = Copy/Paste-Detection
linter-spotbugs = SpotBugs
linter-pmd = PMD
linter-integrated = Integrated Analysis

# CPD
duplicate-code = Duplizierter Code ({$lines}): {$first-path}:{$first-start}-{$first-end} und {$second-path}:{$second-start}-{$second-end}

# API
is-empty-reimplemented-desc = Verwende isEmpty() statt size() == 0 oder ähnlichem Code
is-empty-reimplemented-exp = Verwende isEmpty()

old-collection-desc = Javas alte Collection-Typen sollten nicht verwendet werden (Vector -> ArrayList, Stack -> Deque, Hashtable -> HashMap)
old-collection-exp-vector = Verwende ArrayList statt Vector
old-collection-exp-hashtable = Verwende HashMap statt Hashtable
old-collection-exp-stack = Verwende Dequeue statt Stack

string-is-empty-desc = Verwende String#isEmpty statt '.equals("")' oder '.length() == 0' (bzw. die Negation)
string-is-empty-exp-emptiness = Benutze 'isEmpty()' statt '{$exp}' um auf Leerheit zu prüfen
string-is-empty-exp-non-emptiness = Benutze '!<...>isEmpty()' statt '{$exp}' um auf mindestens ein Element zu prüfen

use-string-formatted = `{$formatted}` ist schöner zu lesen.

optional-argument = Optional sollte nicht als Argument verwendet werden, da man dann 3 Zustände hat: null, Optional.empty() und Optional.of(..). Siehe https://stackoverflow.com/a/31924845/7766117
optional-tri-state = Statt einem Optional boolean, sollte man ein enum verwenden.

equals-hashcode-comparable-contract = Es müssen immer equals und hashCode zusammen überschrieben werden. Genauso muss wenn Comparable implementiert wird equals und hashCode überschrieben werden.

use-format-string = `{$formatted}` ist schöner zu lesen.

math-floor-division = Bei integer division wird immer abgerundet, daher ist das `Math.floor` unnötig.

# Comment
author-tag-invalid-desc = Der @author-Tag ist nicht valide
author-tag-invalid-exp = Der @author-tag ist nicht valide

commented-out-code-desc = Unbenutzter Code sollte entfernt werden
commented-out-code-exp = Dieser auskommentierte Code sollte entfernt werden

comment-language-desc = Alle Kommentare (einschließlich Javadoc and inline comments) müssen entweder auf Deutsch oder auf Englisch sein
comment-language-exp-invalid = Dieser Kommentar ist weder auf Deutsch noch auf Englisch, sondern scheint {$lang} zu sein
comment-language-exp-english = Der Code enthält deutsche und englische Kommentare. Dieser Kommentar ist auf Englisch. Ein deutscher Kommentar befindet sich bei {$path}:{$line}
comment-language-exp-german = Der Code enthält deutsche und englische Kommentare. Dieser Kommentar ist auf Deutsch. Ein englischer Kommentar befindet sich bei {$path}:{$line}

javadoc-method-desc = Methoden müssen valide JavaDoc-Kommentare haben
javadoc-method-exp-param-missing = Der Parameter '{$param}' wird im Javadoc-Kommentar nicht erwähnt
javadoc-method-exp-param-unknown = Der JavaDoc-Kommentar erwähnt den Parameter '{$param}', dieser wird allerdings nicht deklariert
javadoc-method-exp-unexpected-tag = JavaDoc-Kommentare von Methoden sollten keinen '@{$tag}'-Tag haben

javadoc-type-desc = Typen (Klassen, Schnittstellen, ...) müssen valide JavaDoc-Kommentare haben
javadoc-type-exp-unexpected-tag = JavaDoc-Kommentare von Typen sollten keinen '@{$tag}'-Tag haben
javadoc-type-exp-invalid-author = Im @author-Tag darf *ausschließlich* dein u-Kürzel stehen

javadoc-field-desc = Attribute müssen valide JavaDoc-Kommentare haben
javadoc-field-exp-unexpected-tag = JavaDoc-Kommentare von Attributen sollten keinen '@{$tag}'-Tag haben

javadoc-return-null-desc = Methoden müssen in dem @return-Tag angeben, wenn sie null zurückgeben können
javadoc-return-null-exp = Die Methode {$method} kann null zurückgeben, der @return-Tag erwähnt das aber nicht

javadoc-stub-desc = Automatisch generierte Javadoc-Kommentare müssen für die konkrete Methode angepasst werden
javadoc-stub-exp-desc = Die Beschreibung des Javadoc-Kommentars ist leer
javadoc-stub-exp-param = Nichtssagende Beschreibung für den Parameter '{$param}'
javadoc-stub-exp-return = Nichtssagende Beschreibung für den Rückgabewert
javadoc-stub-exp-throws = Nichtssagende Beschreibung für die Exception {$exp}

javadoc-undocumented-throws = Die Exception {$exp} wird geworfen, aber nicht im Javadoc-Kommentar erwähnt.

# Complexity
diamond-desc = Du kannst die Typen in `< A, B, ... >` entfernen und stattdessen `<>` verwenden, siehe https://docs.oracle.com/javase/tutorial/java/generics/genTypeInference.html und https://stackoverflow.com/a/16352848/7766117
diamond-exp = Du kannst die Typen in `< A, B, ... >` entfernen und stattdessen `<>` verwenden, siehe https://docs.oracle.com/javase/tutorial/java/generics/genTypeInference.html und https://stackoverflow.com/a/16352848/7766117

extends-object-desc = Explizit von Object zu erben ist unnötig
extends-object-exp = Unnötiges 'extends Object'

for-loop-var-desc = for-Schleifen sollten genau eine lokal deklarierte Kontrollvariable haben
for-loop-var-exp = for-Schleifen sollten genau eine lokal deklarierte Kontrollvariable haben

implicit-constructor-desc = Der Standard-Konstruktor wird durch den Compiler automatisch generiert wenn kein anderer Konstruktor vorhanden ist
implicit-constructor-exp = Unnötiger Standard-Konstruktor

redundant-if-for-bool-desc = Boolesche Werte können direkt zugewiesen bzw. zurückgegeben werden und müssen nicht in ifs geschachtelt werden
redundant-if-for-bool-exp-return = {$exp} kann direkt zurückgegeben werden
redundant-if-for-bool-exp-assign = '{$exp}' kann direkt zu '{$target}' zugewiesen werden

redundant-modifier-desc = Einige Modifizierer sind implizit
redundant-modifier-exp = Unnötiger Modifizierer

redundant-return-desc = 'return' am Ende einer Void-Methode ist implizit
redundant-return-exp = Unnötiges return

self-assignment-desc = Eine Variable sich selbst zuzuweisen ist unnötig
self-assignment-exp = Nutzlose Zuweisung von '{$rhs}' zu '{$lhs}'

redundant-local-return-desc = Unnötige Deklaration einer lokalen Variable, die sofort zurückgegeben wird
redundant-local-return-exp = Der Wert kann direkt zurückgegeben werden

unused-import-desc = Unbenutzter Import
unused-import-exp = Unbenutzter Import

wrapper-instantiation-desc = Wrapper-Klassen sollten nicht direkt instanziiert werden
wrapper-instantiation-exp = Wrapper-Klassen sollten nicht direkt instanziiert werden

repeated-math-operation = Einfache mathematische Operationen sollten nicht wiederholt werden, sondern durch die entsprechende Operation "ein Level höher" ersetzt werden (z.B. n + n + n => 3 * n; n * n * n => Math.pow(n, 3)).
repeated-math-operation-mul = Hier sollte Math.pow verwendet werden, anstatt '{$var}' {$count}-mal mit sich selbst zu multiplizieren.
repeated-math-operation-plus = Hier sollte eine Multiplikation mit {$count} verwendet werden, anstatt '{$var}' {$count}-mal mit sich selbst zu addieren.

redundant-neg-desc = '!(a == b)' kann durch 'a != b' ersetzt werden
redundant-neg-exp = '{$original}' kann zu '{$fixed}' vereinfacht werden

use-operator-assignment-desc = Zuweisungen der Form 'a = a + b' sollte man als 'a += b' schreiben
use-operator-assignment-exp = Zuweisung kann zu '{$simplified}' vereinfacht werden

merge-if-desc = Besteht ein else-Block nur aus einem if, kann auch else-if verwendet werden
merge-if-exp = Verwende 'else if (...) {"{"} ... {"}"}' statt 'else {"{"} if (...) {"{"} ... {"}"} {"}"}'

complex-regex = Nichttriviale Regex brauchen einen erklärenden Kommentar

# Debug
assert-used-desc = Assertions eignen sich nicht zur Fehlerbehandlung
assert-used-exp = Assertion benutzt

print-stack-trace-desc = Stack Traces sollten in der Abgabe nicht ausgegeben werden
print-stack-trace-exp = Stack Traces sollten in der Abgabe nicht ausgegeben werden

# Exceptions
custom-exception-inheritance-desc = Selbstdefinierte Exceptions sollten nicht von RuntimeException oder Error erben
custom-exception-inheritance-exp-runtime = Selbstdefinierte Exceptions sollten immer Checked Exceptions sein
custom-exception-inheritance-exp-error = Selbstdefinierte Exceptions sollten nicht von Error erben

empty-catch-desc = Alle Exceptions sollten angemessen behandelt werden
empty-catch-exp = Leerer catch-Block

exception-controlflow-desc = Exceptions sollten innerhalb von Methoden nicht für Kontrollfluss benutzt werden (z.B. durch Werfen und Fangen in derselben Methode)
exception-controlflow-exp-caught = {$exp} wird geworfen und im umgebenden Block sofort wieder gefangen

runtime-ex-caught-desc = RuntimeExceptions sollten niemals gefangen werden (abgesehen von NumberFormatException)
runtime-ex-caught-exp = RuntimeException vom Typ {$exp} gefangen

exception-message-desc = Geworfene Exceptions sollten immer eine Nachricht haben
exception-message-exp = Nachricht ('message') fehlt oder ist leer

type-has-descriptive-name-pre-suffix = Der Name enthält unnötige Präfixe oder Suffixe
type-has-descriptive-name-exception = Eine Klasse die von Exception erbt, sollte 'Exception' am Ende ihres Namens haben

# General
compare-objects-desc = Objekte sollten mit equals verglichen werden, anstatt sie zum Vergleich in Strings umzuwandeln
compare-objects-exp = Implementiere eine equals-Methode für den Typ {$type} und verwende sie zum Vergleichen

constant-naming-qualifier-desc = Konstanten sollten 'static final' sein und einen UPPER_SNAKE_CASE-Namen haben
constant-naming-qualifier-exp = Die Konstante '{$field}'  sollte statisch sein und einen UPPER_SNAKE_CASE-Namen haben

constants-interfaces-desc = Geteilte Konstanten sollten in Enums oder Utility-Klassen und nicht in Interfaces gespeichert werden
constants-interfaces-exp = Interfaces sollten keine Attribute haben

param-reassign-desc = Parameter sollten nicht neu zugewiesen werden
param-reassign-exp = Parameter sollten nicht neu zugewiesen werden

double-brace-desc = Die obskure 'Double Brace'-Syntax sollte vermieden werden
double-brace-exp = ie obskure 'Double Brace'-Syntax sollte vermieden werden

equals-handle-null-argument-desc = Die equals-Methode sollte null-Werte behandeln
equals-handle-null-argument-exp = Die equals-Methode sollte null-Werte behandeln

field-local-desc = Attribute sollten in lokale Variablen umgewandelt werden falls sie vor jedem Lesen überschrieben werden
field-local-exp = Das Attribut '{$field}' der Klasse {$class} sollte eine lokale Variable sein, da sie in jeder Methode vor dem ersten Lesen überschrieben wird

for-foreach-desc = for-Schleife sollte eine for-each-Schleife sein
for-foreach-exp = for-Schleife sollte eine for-each-Schleife sein

missing-override-desc = Fehlendes @Override
missing-override-exp = Fehlendes @Override

system-dependent-linebreak-desc = Es sollten immer systemunabhängige Zeilenumbrüche wie der Wert von System.lineSeparator() oder '%n' in format-Strings verwendet werden
system-dependent-linebreak-exp = Systemabhängiger Zeilenumbruch (\n) benutzt. Besser ist System.lineSeparator() oder (falls es sich um einen format-String handelt) '%n'.

field-final-desc = Konstante Attribute sollten final sein
field-final-exp = Das Attribut '{$name}' sollte final sein

string-cmp-desc = Strings müssen immer mit der equals-Methode verglichen werden
string-cmp-exp = Strings sollten nicht per Referenz, sonder mit der 'equals'-Methode verglichen werden: '{$lhs}.equals({$rhs})' statt '{$lhs} == {$rhs}'

do-not-use-raw-types-desc = Generische Typen sollten immer mit Typparameter angegeben werden und nie als Raw Type, siehe https://stackoverflow.com/a/2770692/7766117
do-not-use-raw-types-exp = Generische Typen sollten immer mit Typparameter angegeben werden und nie als Raw Type, siehe https://stackoverflow.com/a/2770692/7766117

avoid-labels = Labels sollten vermieden werden. Siehe https://stackoverflow.com/a/33689582/7766117.

avoid-shadowing = Die Variable '{$name}' verdeckt ein Attribut mit dem selben Namen. Abgesehen vom Konstruktor, sollte man das vermeiden.

suppress-warnings = @SuppressWarnings unterdrückt Warnungen des Compilers oder von Checkstyle, anstatt das unterliegende Problem zu beheben.

scanner-closed = Scanner sollte geschlossen werden

unchecked-type-cast = Es muss sicher gestellt werden, dass der Typ des Objekts mit dem Typ des Casts übereinstimmt. Ansonsten kann der Code abstürzen.

# Naming
bool-getter-name-desc = Getter für boolesche Werte sollten das Präfix 'is' haben
bool-getter-name-exp = Die Methode sollte isY() statt getY() heißen

constants-name-desc = Konstanten sollte aussagekräftige Namen haben - z.B. AUTHOR_INDEX statt FIRST_INDEX
constants-name-exp-string = Der Name '{$name}' ist nicht aussagekräftig gegeben den Wert '{$value}'
constants-name-exp-number = The name '{$name}' ist nicht aussagekräftig gegeben den Wert {$value}

linguistic-desc = Das Code-Element hat einen verwirrenden Namen. Siehe https://pmd.github.io/latest/pmd_rules_java_codestyle.html#linguisticnaming
linguistic-exp = Das Code-Element hat einen verwirrenden Namen. Siehe https://pmd.github.io/latest/pmd_rules_java_codestyle.html#linguisticnaming

variable-names-desc = Lokale Variablen sollten aussagekräftige Werte haben
variable-name-exp-single-letter = Der Bezeichner '{$name}' ist nicht aussagekräftig
variable-name-exp-type = Unnötige Abkürzung '{$name}'

# OOP
concrete-collection-desc = Statt konkreten Collections sollten immer allgemeine Interfaces verwendet werden (z.B. List statt ArrayList)
concrete-collection-exp = Statt konkreten Collections sollten immer allgemeine Interfaces verwendet werden (z.B. List statt ArrayList)

list-getter-desc = Veränderbare Collections müssen in Gettern kopiert werden
list-getter-exp = Die Collection ist veränderbar, wird aber nicht kopiert

method-abstract-desc = Leere Methoden in abstrakten Klassen sollten abstrakt sein
method-abstract-exp = {$type}::{$method} sollte abstrakt sein, anstatt eine Platzhalter-Implementierung anzugeben

utility-desc = Utility-Klassen müssen final sein und genau einen parameterlosen Konstruktor haben
utility-exp-final = Utility-Klasse ist nicht final
utility-exp-constructor = Utility-Klassen müssen genau einen privaten und parameterlosen Konstruktor haben
utility-exp-field = Utility-Klassen dürfen ausschließlich finale Attribute haben

static-field-desc = Statische Attribute dürfen nicht verändert werden
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

# Structure

default-package-desc = Das default-Paket sollte nicht verwendet werden
default-package-exp = Das default-Paket sollte nicht verwendet werden

# Unnecessary

empty-block-desc = Leerer Block (if / else / for / while / switch / try)
empty-block-exp-if = Leerer if/else-Block
empty-block-exp-while = Leerer while-Block
empty-block-exp-try = Leerer try-Block
empty-block-exp-finally = Leerer finally-Block
empty-block-exp-switch = Leerer switch-Block

unused-element-desc = Unbenutztes Code-Element (lokale Variable / Parameter / privates Attribut / private method)
unused-element-exp = Unbenutzt
