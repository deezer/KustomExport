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

import org.junit.Test

class ExportInterfaceTest {

    @Test
    fun emptyInterface() {
        assertCompilationOutput(
            """
            package flux
            
            import deezer.kustomexport.KustomExport

            @KustomExport
            interface Exportable
    """,
            ExpectedOutputFile(
                path = "flux/js/Exportable.kt",
                content = """
            package flux.js
    
            import kotlin.js.JsExport
            import flux.Exportable as CommonExportable
            
            @JsExport
            public external interface Exportable
            
            private class ImportedExportable(
                internal val exported: Exportable,
            ) : CommonExportable
            
            private class ExportedExportable(
                internal val common: CommonExportable,
            ) : Exportable
            
            public fun CommonExportable.exportExportable(): Exportable =
                    (this as? ImportedExportable)?.exported ?: ExportedExportable(this)
            
            public fun Exportable.importExportable(): CommonExportable =
                    (this as? ExportedExportable)?.common ?: ImportedExportable(this)
                """.trimIndent()
            )
        )
    }

    @Test
    fun withVal() {
        assertCompilationOutput(
            """
            package flux
            
            import deezer.kustomexport.KustomExport

            @KustomExport
            interface BasicInterface {
                val flex: String
            }
    """,
            ExpectedOutputFile(
                path = "flux/js/BasicInterface.kt",
                content = """
            package flux.js
            
            import kotlin.String
            import kotlin.js.JsExport
            import flux.BasicInterface as CommonBasicInterface
            
            @JsExport
            public external interface BasicInterface {
                public val flex: String
            }
            
            private class ImportedBasicInterface(
                internal val exported: BasicInterface,
            ) : CommonBasicInterface {
                public override val flex: String
                    get() = exported.flex
            }
            
            private class ExportedBasicInterface(
                internal val common: CommonBasicInterface,
            ) : BasicInterface {
                public override val flex: String
                    get() = common.flex
            }
            
            public fun CommonBasicInterface.exportBasicInterface(): BasicInterface =
                    (this as? ImportedBasicInterface)?.exported ?: ExportedBasicInterface(this)
            
            public fun BasicInterface.importBasicInterface(): CommonBasicInterface =
                    (this as? ExportedBasicInterface)?.common ?: ImportedBasicInterface(this)
                """.trimIndent()
            )
        )
    }

    @Test
    fun withVar() {
        assertCompilationOutput(
            """
            package flux
            
            import deezer.kustomexport.KustomExport

            @KustomExport
            interface BasicInterface {
                var canChange: String
            }
    """,
            ExpectedOutputFile(
                path = "flux/js/BasicInterface.kt",
                content = """
            package flux.js
            
            import kotlin.String
            import kotlin.js.JsExport
            import flux.BasicInterface as CommonBasicInterface
            
            @JsExport
            public external interface BasicInterface {
                public var canChange: String
            }
            
            private class ImportedBasicInterface(
                internal val exported: BasicInterface,
            ) : CommonBasicInterface {
                public override var canChange: String
                    get() = exported.canChange
                    set(setValue) {
                        exported.canChange = setValue
                    }
            }
            
            private class ExportedBasicInterface(
                internal val common: CommonBasicInterface,
            ) : BasicInterface {
                public override var canChange: String
                    get() = common.canChange
                    set(setValue) {
                        common.canChange = setValue
                    }
            }
            
            public fun CommonBasicInterface.exportBasicInterface(): BasicInterface =
                    (this as? ImportedBasicInterface)?.exported ?: ExportedBasicInterface(this)
            
            public fun BasicInterface.importBasicInterface(): CommonBasicInterface =
                    (this as? ExportedBasicInterface)?.common ?: ImportedBasicInterface(this)
                """.trimIndent()
            )
        )
    }

    @Test
    fun withGenericVar() {
        assertCompilationOutput(
            """
            package flux
            
            import deezer.kustomexport.KustomExport

            @KustomExport
            interface BasicInterface {
                var numbers: List<Long>
            }
    """,
            ExpectedOutputFile(
                path = "flux/js/BasicInterface.kt",
                content = """
                package flux.js
                
                import kotlin.Array
                import kotlin.Double
                import kotlin.Long
                import kotlin.collections.List
                import kotlin.collections.toTypedArray
                import kotlin.js.JsExport
                import flux.BasicInterface as CommonBasicInterface
                
                @JsExport
                public external interface BasicInterface {
                    public var numbers: Array<Double>
                }
                
                private class ImportedBasicInterface(
                    internal val exported: BasicInterface,
                ) : CommonBasicInterface {
                    public override var numbers: List<Long>
                        get() = exported.numbers.map { it.toLong() }
                        set(setValue) {
                            exported.numbers = setValue.map { it.toDouble() }.toTypedArray()
                        }
                }
                
                private class ExportedBasicInterface(
                    internal val common: CommonBasicInterface,
                ) : BasicInterface {
                    public override var numbers: Array<Double>
                        get() = common.numbers.map { it.toDouble() }.toTypedArray()
                        set(setValue) {
                            common.numbers = setValue.map { it.toLong() }
                        }
                }
                
                public fun CommonBasicInterface.exportBasicInterface(): BasicInterface =
                        (this as? ImportedBasicInterface)?.exported ?: ExportedBasicInterface(this)
                
                public fun BasicInterface.importBasicInterface(): CommonBasicInterface =
                        (this as? ExportedBasicInterface)?.common ?: ImportedBasicInterface(this)
                """.trimIndent()
            )
        )
    }

    @Test
    fun withFunction() {
        assertCompilationOutput(
            inputFiles = listOf(
                InputFile(
                    path = "Bar.kt",
                    content = """
                        package bar
                        
                        class Bar
                    """
                ),
                InputFile(
                    path = "BasicInterface.kt",
                    content = """
                        package flux
                        
                        import deezer.kustomexport.KustomExport
                        import bar.Bar
            
                        @KustomExport
                        interface BasicInterface {
                            fun foo(bar: Bar)
                        }
                        """
                )
            ),
            expectedOutputFiles = listOf(
                ExpectedOutputFile(
                    path = "flux/js/BasicInterface.kt",
                    content = """
                    package flux.js

                    import bar.js.Bar
                    import bar.js.exportBar
                    import bar.js.importBar
                    import kotlin.Unit
                    import kotlin.js.JsExport
                    import flux.BasicInterface as CommonBasicInterface
                    
                    @JsExport
                    public external interface BasicInterface {
                        public fun foo(bar: Bar): Unit
                    }
                    
                    private class ImportedBasicInterface(
                        internal val exported: BasicInterface,
                    ) : CommonBasicInterface {
                        public override fun foo(bar: bar.Bar): Unit {
                            val result = exported.foo(
                                bar = bar.exportBar(),
                            )
                            return result
                        }
                    }
                    
                    private class ExportedBasicInterface(
                        internal val common: CommonBasicInterface,
                    ) : BasicInterface {
                        public override fun foo(bar: Bar): Unit {
                            val result = common.foo(
                                bar = bar.importBar(),
                            )
                            return result
                        }
                    }
                    
                    public fun CommonBasicInterface.exportBasicInterface(): BasicInterface =
                            (this as? ImportedBasicInterface)?.exported ?: ExportedBasicInterface(this)
                    
                    public fun BasicInterface.importBasicInterface(): CommonBasicInterface =
                            (this as? ExportedBasicInterface)?.common ?: ImportedBasicInterface(this)
                    """.trimIndent()
                )
            )
        )
    }

    @Test
    fun functionWithParameterizedReturnType() {
        assertCompilationOutput(
            inputFiles = listOf(
                InputFile(
                    path = "Bar.kt",
                    content = """
                        package bar
                        
                        class Bar
                    """
                ),
                InputFile(
                    path = "BasicInterface.kt",
                    content = """
                        package flux
                        
                        import deezer.kustomexport.KustomExport
                        import bar.Bar
            
                        @KustomExport
                        interface BasicInterface {
                            fun foo(bar: Bar): List<Long>
                        }
                        """
                )
            ),
            expectedOutputFiles = listOf(
                ExpectedOutputFile(
                    path = "flux/js/BasicInterface.kt",
                    content = """
                        package flux.js
                        
                        import bar.js.Bar
                        import bar.js.exportBar
                        import bar.js.importBar
                        import kotlin.Array
                        import kotlin.Double
                        import kotlin.Long
                        import kotlin.collections.List
                        import kotlin.collections.toTypedArray
                        import kotlin.js.JsExport
                        import flux.BasicInterface as CommonBasicInterface
                        
                        @JsExport
                        public external interface BasicInterface {
                            public fun foo(bar: Bar): Array<Double>
                        }
                        
                        private class ImportedBasicInterface(
                            internal val exported: BasicInterface,
                        ) : CommonBasicInterface {
                            public override fun foo(bar: bar.Bar): List<Long> {
                                val result = exported.foo(
                                    bar = bar.exportBar(),
                                )
                                return result.map { it.toLong() }
                            }
                        }
                        
                        private class ExportedBasicInterface(
                            internal val common: CommonBasicInterface,
                        ) : BasicInterface {
                            public override fun foo(bar: Bar): Array<Double> {
                                val result = common.foo(
                                    bar = bar.importBar(),
                                )
                                return result.map { it.toDouble() }.toTypedArray()
                            }
                        }
                        
                        public fun CommonBasicInterface.exportBasicInterface(): BasicInterface =
                                (this as? ImportedBasicInterface)?.exported ?: ExportedBasicInterface(this)
                        
                        public fun BasicInterface.importBasicInterface(): CommonBasicInterface =
                                (this as? ExportedBasicInterface)?.common ?: ImportedBasicInterface(this)
                    """.trimIndent()
                )
            )
        )
    }
}
