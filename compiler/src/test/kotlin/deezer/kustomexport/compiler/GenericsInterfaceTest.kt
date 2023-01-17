/*
 * Copyright 2021 Deezer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package deezer.kustomexport.compiler

import org.junit.Ignore
import org.junit.Test

class GenericsInterfaceTest {

    @Test
    @Ignore
    fun genericsInterface() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustomexport.KustomExport

            open class GenericsBase

            @KustomExport
            interface InterfaceGenerics<T : GenericsBase> {
                fun generateBase(): T
                fun check(input: T): String
            }
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/InterfaceGenerics.kt",
                content = """
                    package foo.bar.js
                    
                    import foo.bar.js.exportGenericsBase
                    import foo.bar.js.importGenericsBase
                    import kotlin.String
                    import kotlin.js.JsExport
                    import foo.bar.GenericsBase as CommonGenericsBase
                    import foo.bar.InterfaceGenerics as CommonInterfaceGenerics

                    @JsExport
                    public external interface InterfaceGenerics<__T : GenericsBase> {
                        public fun generateBase(): __T

                        public fun check(input: __T): String
                    }

                    private class ImportedInterfaceGenerics<T : CommonGenericsBase, __T : GenericsBase>(
                        internal val exported: InterfaceGenerics<__T>
                    ) : CommonInterfaceGenerics<T> {
                        public override fun generateBase(): T {
                            val result = exported.generateBase()
                            return result.importGenericsBase()
                        }

                        public override fun check(input: T): String {
                            val result = exported.check(
                                    input = input.exportGenericsBase()
                            )
                            return result
                        }
                    }

                    private class ExportedInterfaceGenerics<T : CommonGenericsBase, __T : GenericsBase>(
                        internal val common: CommonInterfaceGenerics<T>
                    ) : InterfaceGenerics<__T> {
                        public override fun generateBase(): __T {
                            val result = common.generateBase()
                            return result.exportGenericsBase()
                        }

                        public override fun check(input: __T): String {
                            val result = common.check(
                                    input = input.importGenericsBase()
                            )
                            return result
                        }
                    }

                    public fun <T : CommonGenericsBase, __T : GenericsBase>
                            CommonInterfaceGenerics<T>.exportInterfaceGenerics(): InterfaceGenerics<__T> = (this as?
                            ImportedInterfaceGenerics<T, __T>)?.exported ?: ExportedInterfaceGenerics(this)

                    public fun <T : CommonGenericsBase, __T : GenericsBase>
                            InterfaceGenerics<__T>.importInterfaceGenerics(): CommonInterfaceGenerics<T> = (this as?
                            ExportedInterfaceGenerics<T, __T>)?.common ?: ImportedInterfaceGenerics(this)
                """.trimIndent()
            )
        )
    }
}
