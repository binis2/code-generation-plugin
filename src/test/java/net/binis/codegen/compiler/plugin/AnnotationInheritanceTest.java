package net.binis.codegen.compiler.plugin;

/*-
 * #%L
 * code-generator-plugin
 * %%
 * Copyright (C) 2021 - 2026 Binis Belev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import net.binis.codegen.test.BaseCodeGenCompilerTest;
import org.junit.jupiter.api.Test;

import static net.binis.codegen.tools.Reflection.findMethod;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnnotationInheritanceTest extends BaseCodeGenCompilerTest {

    @Test
    void test() {
        var loader = testMulti("annotation/annotation1.java", "annotation/annotation2.java", "annotation/annotation3.java");
        var cls = loader.findClass("net.binis.codegen.javac.test.InheritAnnotation");
        assertNotNull(cls);
        assertNotNull(findMethod("test", cls));
        assertNotNull(findMethod("ordinal", cls));
    }

    @Test
    void testCompiled() {
        var loader = testMulti("annotation/annotation4.java", "annotation/annotation5.java");
        var cls = loader.findClass("net.binis.codegen.javac.test.InheritAnnotation");
        assertNotNull(cls);
        assertNotNull(findMethod("base", cls));
        assertNotNull(findMethod("basePath", cls));
    }


}
