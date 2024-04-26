package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.errorprone.TempLocation;
import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.core.file.StringSourceInfo;
import de.firemage.autograder.core.file.UploadedFile;
import org.junit.jupiter.api.Test;
import spoon.reflect.code.CtLambda;
import spoon.reflect.declaration.CtMethod;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class MethodHierarchyTest {
    protected final TempLocation tempLocation;

    MethodHierarchyTest() {
        tempLocation = TempLocation.random();
    }

    @Test
    void testOverriddenMethod() throws LinterException, IOException {
        var model = buildCodeModel(StringSourceInfo.fromSourceStrings(JavaVersion.JAVA_17, Map.ofEntries(
                Map.entry(
                        "A",
                        """
                                public class A {
                                    public void foo() {}
                                }
                                """
                ),
                Map.entry(
                        "B",
                        """
                                public class B extends A {
                                    @Override
                                    public void foo() {}
                                }
                                """
                )
        )));

        var fooA = findMethod("foo()", "A", model);
        var fooB = findMethod("foo()", "B", model);

        assertEquals(Set.of(), model.getMethodHierarchy().getDirectSuperMethods(fooA));
        assertEquals(Set.of(method(fooB)), model.getMethodHierarchy().getDirectOverridingMethods(fooA));

        assertEquals(Set.of(method(fooA)), model.getMethodHierarchy().getDirectSuperMethods(fooB));
        assertEquals(Set.of(), model.getMethodHierarchy().getDirectOverridingMethods(fooB));
    }

    @Test
    void testTripleHierarchy() throws LinterException, IOException {
        var model = buildCodeModel(StringSourceInfo.fromSourceStrings(JavaVersion.JAVA_17, Map.ofEntries(
                Map.entry(
                        "A",
                        """
                                public class A {
                                    public void foo() {}
                                }
                                """
                ),
                Map.entry(
                        "B",
                        """
                                public class B extends A {
                                    @Override
                                    public void foo() {}
                                }
                                """
                ),
                Map.entry(
                        "C",
                        """
                                public class C extends B {
                                    @Override
                                    public void foo() {}
                                }
                                """
                )
        )));

        var fooA = findMethod("foo()", "A", model);
        var fooB = findMethod("foo()", "B", model);
        var fooC = findMethod("foo()", "C", model);

        assertEquals(Set.of(), model.getMethodHierarchy().getDirectSuperMethods(fooA));
        assertEquals(Set.of(method(fooB)), model.getMethodHierarchy().getDirectOverridingMethods(fooA));

        assertEquals(Set.of(method(fooA)), model.getMethodHierarchy().getDirectSuperMethods(fooB));
        assertEquals(Set.of(method(fooC)), model.getMethodHierarchy().getDirectOverridingMethods(fooB));

        assertEquals(Set.of(method(fooB)), model.getMethodHierarchy().getDirectSuperMethods(fooC));
        assertEquals(Set.of(), model.getMethodHierarchy().getDirectOverridingMethods(fooC));
    }

    @Test
    void testIndirectOverriddenMethod() throws LinterException, IOException {
        var model = buildCodeModel(StringSourceInfo.fromSourceStrings(JavaVersion.JAVA_17, Map.ofEntries(
                Map.entry(
                        "A",
                        """
                                public class A {
                                    public void foo() {}
                                }
                                """
                ),
                Map.entry(
                        "B",
                        """
                                public class B extends A {}
                                """
                ),
                Map.entry(
                        "C",
                        """
                                public class C extends B {
                                    @Override
                                    public void foo() {}
                                }
                                """
                )
        )));

        var fooA = findMethod("foo()", "A", model);
        var fooC = findMethod("foo()", "C", model);

        assertEquals(Set.of(), model.getMethodHierarchy().getDirectSuperMethods(fooA));
        assertEquals(Set.of(method(fooC)), model.getMethodHierarchy().getDirectOverridingMethods(fooA));

        assertEquals(Set.of(method(fooA)), model.getMethodHierarchy().getDirectSuperMethods(fooC));
        assertEquals(Set.of(), model.getMethodHierarchy().getDirectOverridingMethods(fooC));
    }

    @Test
    void testMultipleInterface() throws LinterException, IOException {
        var model = buildCodeModel(StringSourceInfo.fromSourceStrings(JavaVersion.JAVA_17, Map.ofEntries(
                Map.entry(
                        "A",
                        """
                                public class A implements B, C {
                                    public void foo() {}
                                }
                                """
                ),
                Map.entry(
                        "B",
                        """
                                public interface B { void foo(); }
                                """
                ),
                Map.entry(
                        "C",
                        """
                                public interface C { void foo(); }
                                """
                )
        )));

        var fooA = findMethod("foo()", "A", model);
        var fooB = findMethod("foo()", "B", model);
        var fooC = findMethod("foo()", "C", model);

        assertEquals(Set.of(method(fooB), method(fooC)), model.getMethodHierarchy().getDirectSuperMethods(fooA));
        assertEquals(Set.of(), model.getMethodHierarchy().getDirectOverridingMethods(fooA));

        assertEquals(Set.of(), model.getMethodHierarchy().getDirectSuperMethods(fooB));
        assertEquals(Set.of(method(fooA)), model.getMethodHierarchy().getDirectOverridingMethods(fooB));

        assertEquals(Set.of(), model.getMethodHierarchy().getDirectSuperMethods(fooC));
        assertEquals(Set.of(method(fooA)), model.getMethodHierarchy().getDirectOverridingMethods(fooC));
    }

    @Test
    void testInterfaceHierarchy() throws LinterException, IOException {
        var model = buildCodeModel(StringSourceInfo.fromSourceStrings(JavaVersion.JAVA_17, Map.ofEntries(
                Map.entry(
                        "A",
                        """
                                public class A implements B {
                                    public void foo() {}
                                }
                                """
                ),
                Map.entry(
                        "B",
                        """
                                public interface B extends C { void foo(); }
                                """
                ),
                Map.entry(
                        "C",
                        """
                                public interface C { void foo(); }
                                """
                )
        )));

        var fooA = findMethod("foo()", "A", model);
        var fooB = findMethod("foo()", "B", model);
        var fooC = findMethod("foo()", "C", model);

        assertEquals(Set.of(method(fooB)), model.getMethodHierarchy().getDirectSuperMethods(fooA));
        assertEquals(Set.of(), model.getMethodHierarchy().getDirectOverridingMethods(fooA));

        assertEquals(Set.of(method(fooC)), model.getMethodHierarchy().getDirectSuperMethods(fooB));
        assertEquals(Set.of(method(fooA)), model.getMethodHierarchy().getDirectOverridingMethods(fooB));

        assertEquals(Set.of(), model.getMethodHierarchy().getDirectSuperMethods(fooC));
        assertEquals(Set.of(method(fooB)), model.getMethodHierarchy().getDirectOverridingMethods(fooC));
    }

    @Test
    void testInterfaceHierarchyDoubleImplement() throws LinterException, IOException {
        var model = buildCodeModel(StringSourceInfo.fromSourceStrings(JavaVersion.JAVA_17, Map.ofEntries(
                Map.entry(
                        "A",
                        """
                                public class A implements B, C {
                                    public void foo() {}
                                }
                                """
                ),
                Map.entry(
                        "B",
                        """
                                public interface B extends C { void foo(); }
                                """
                ),
                Map.entry(
                        "C",
                        """
                                public interface C { void foo(); }
                                """
                )
        )));

        var fooA = findMethod("foo()", "A", model);
        var fooB = findMethod("foo()", "B", model);
        var fooC = findMethod("foo()", "C", model);

        assertEquals(Set.of(method(fooB), method(fooC)), model.getMethodHierarchy().getDirectSuperMethods(fooA));
        assertEquals(Set.of(), model.getMethodHierarchy().getDirectOverridingMethods(fooA));

        assertEquals(Set.of(method(fooC)), model.getMethodHierarchy().getDirectSuperMethods(fooB));
        assertEquals(Set.of(method(fooA)), model.getMethodHierarchy().getDirectOverridingMethods(fooB));

        assertEquals(Set.of(), model.getMethodHierarchy().getDirectSuperMethods(fooC));
        assertEquals(Set.of(method(fooA), method(fooB)), model.getMethodHierarchy().getDirectOverridingMethods(fooC));
    }

    @Test
    void testAnonymousOverride() throws LinterException, IOException {
        var model = buildCodeModel(StringSourceInfo.fromSourceStrings(JavaVersion.JAVA_17, Map.ofEntries(
                Map.entry(
                        "A",
                        """
                                public class A {
                                    public void foo() {
                                        var x = new B() {
                                            public void foo(int i) {}
                                        };
                                    }
                                    
                                    public void bar(B b) {}
                                }
                                """
                ),
                Map.entry(
                        "B",
                        """
                                public interface B { void foo(int i); }
                                """
                )
        )));

        var fooB = findMethod("foo(int)", "B", model);
        var fooAnon = findMethod("foo(int)", "A$1", model);

        assertEquals(Set.of(), model.getMethodHierarchy().getDirectSuperMethods(fooB));
        assertEquals(Set.of(method(fooAnon)), model.getMethodHierarchy().getDirectOverridingMethods(fooB));

        assertEquals(Set.of(method(fooB)), model.getMethodHierarchy().getDirectSuperMethods(fooAnon));
        assertEquals(Set.of(), model.getMethodHierarchy().getDirectOverridingMethods(fooAnon));
    }

    @Test
    void testLambdaOverride() throws LinterException, IOException {
        var model = buildCodeModel(StringSourceInfo.fromSourceStrings(JavaVersion.JAVA_17, Map.ofEntries(
                Map.entry(
                        "A",
                        """
                                public class A {
                                    public void foo() {
                                        bar(i -> {});
                                    }
                                    
                                    public void bar(B b) {}
                                }
                                """
                ),
                Map.entry(
                        "B",
                        """
                                public interface B { void foo(int i); }
                                """
                )
        )));

        var fooB = findMethod("foo(int)", "B", model);
        var fooAnon = (CtLambda<?>) model.getModel().filterChildren(e -> e instanceof CtLambda<?>).first();

        assertEquals(Set.of(), model.getMethodHierarchy().getDirectSuperMethods(fooB));
        assertEquals(Set.of(lambda(fooAnon)), model.getMethodHierarchy().getDirectOverridingMethods(fooB));
    }

    private CtMethod<?> findMethod(String signature, String type, CodeModel model) {
        return model.getModel()
                .filterChildren(e -> e instanceof CtMethod<?> m
                        && m.getDeclaringType().getQualifiedName().equals(type)
                        && m.getSignature().equals(signature))
                .first();
    }

    private CodeModel buildCodeModel(SourceInfo sourceInfo) throws LinterException, IOException {
        UploadedFile file = UploadedFile.build(sourceInfo, this.tempLocation, status -> {
        }, null);
        assertNotNull(file, "Could not compile the code");
        return file.getModel();
    }

    private static MethodHierarchy.MethodOrLambda<?> method(CtMethod<?> method) {
        return new MethodHierarchy.MethodOrLambda<>(method);
    }

    private static MethodHierarchy.MethodOrLambda<?> lambda(CtLambda<?> lambda) {
        return new MethodHierarchy.MethodOrLambda<>(lambda);
    }
}
