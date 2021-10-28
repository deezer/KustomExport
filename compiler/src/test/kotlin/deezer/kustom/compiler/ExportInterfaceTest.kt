package deezer.kustom.compiler

import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import org.junit.Test

@KotlinPoetKspPreview
class ExportInterfaceTest {


    @Test
    fun foo() {
        assertCompilationOutput(
            """
            package flux
            
            import deezer.kmp.Export

            @Export
            interface Exportable {
                fun bar(flux: List<Flux>)
            }
    """, ExpectedOutputFile(
                path = "flux/js/Exportable.kt",
                content = ""))
    }

    @Test
    fun emptyInterface() {
        assertCompilationOutput(
            """
            package flux
            
            import deezer.kmp.Export

            @Export
            interface Exportable
    """, ExpectedOutputFile(
                path = "flux/js/Exportable.kt",
                content = """
            package flux.js
    
            import kotlin.js.JsExport
            import flux.Exportable as CommonExportable
            
            @JsExport
            public external interface Exportable
            
            private class ImportedExportable(
                internal val exportable: Exportable
            ) : CommonExportable
            
            private class ExportedExportable(
                internal val exportable: CommonExportable
            ) : Exportable
            
            public fun CommonExportable.export() = (this as? ImportedExportable)?.exportable ?:
                    ExportedExportable(this)
            
            public fun Exportable.`import`() = (this as? ExportedExportable)?.exportable ?:
                    ImportedExportable(this)
    """.trimIndent()
            )
        )
    }

    @Test
    fun withVal() {
        assertCompilationOutput(
            """
            package flux
            
            import deezer.kmp.Export

            @Export
            interface BasicInterface {
                val flex: String
            }
    """, ExpectedOutputFile(
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
                internal val basicInterface: BasicInterface
            ) : CommonBasicInterface {
                public override val flex: String
                    get() = basicInterface.flex
            }
            
            private class ExportedBasicInterface(
                internal val basicInterface: CommonBasicInterface
            ) : BasicInterface {
                public override val flex: String
                    get() = basicInterface.flex
            }
            
            public fun CommonBasicInterface.export() = (this as? ImportedBasicInterface)?.basicInterface ?:
                    ExportedBasicInterface(this)
            
            public fun BasicInterface.`import`() = (this as? ExportedBasicInterface)?.basicInterface ?:
                    ImportedBasicInterface(this)
    """.trimIndent()
            )
        )
    }

    @Test
    fun withVar() {
        assertCompilationOutput(
            """
            package flux
            
            import deezer.kmp.Export

            @Export
            interface BasicInterface {
                var canChange: String
            }
    """, ExpectedOutputFile(
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
                internal val basicInterface: BasicInterface
            ) : CommonBasicInterface {
                public override var canChange: String
                    get() = basicInterface.canChange
                    set(`value`) {
                        basicInterface.canChange = value
                    }
            }
            
            private class ExportedBasicInterface(
                internal val basicInterface: CommonBasicInterface
            ) : BasicInterface {
                public override var canChange: String
                    get() = basicInterface.canChange
                    set(`value`) {
                        basicInterface.canChange = value
                    }
            }
            
            public fun CommonBasicInterface.export() = (this as? ImportedBasicInterface)?.basicInterface ?:
                    ExportedBasicInterface(this)
            
            public fun BasicInterface.`import`() = (this as? ExportedBasicInterface)?.basicInterface ?:
                    ImportedBasicInterface(this)
    """.trimIndent()
            )
        )
    }

    @Test
    fun withGenericVar() {
        assertCompilationOutput(
            """
            package flux
            
            import deezer.kmp.Export

            @Export
            interface BasicInterface {
                var numbers: List<Long>
            }
    """, ExpectedOutputFile(
                path = "flux/js/BasicInterface.kt",
                content = """
            package flux.js
    
            import kotlin.Array
            import kotlin.Double
            import kotlin.Long
            import kotlin.collections.List
            import kotlin.js.JsExport
            import flux.BasicInterface as CommonBasicInterface
            
            @JsExport
            public external interface BasicInterface {
                public var numbers: Array<Double>
            }
            
            private class ImportedBasicInterface(
                internal val basicInterface: BasicInterface
            ) : CommonBasicInterface {
                public override var numbers: List<Long>
                    get() = basicInterface.numbers.map { it.toLong() }
                    set(`value`) {
                        basicInterface.numbers = value.map { it.toDouble() }.toTypedArray()
                    }
            }
            
            private class ExportedBasicInterface(
                internal val basicInterface: CommonBasicInterface
            ) : BasicInterface {
                public override var numbers: Array<Double>
                    get() = basicInterface.numbers.map { it.toDouble() }.toTypedArray()
                    set(`value`) {
                        basicInterface.numbers = value.map { it.toLong() }
                    }
            }
            
            public fun CommonBasicInterface.export() = (this as? ImportedBasicInterface)?.basicInterface ?:
                    ExportedBasicInterface(this)
            
            public fun BasicInterface.`import`() = (this as? ExportedBasicInterface)?.basicInterface ?:
                    ImportedBasicInterface(this)
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
                        
                        import deezer.kmp.Export
                        import bar.Bar
            
                        @Export
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
                        import kotlin.Unit
                        import kotlin.js.JsExport
                        import bar.Bar as CommonBar
                        import flux.BasicInterface as CommonBasicInterface
                        
                        @JsExport
                        public external interface BasicInterface {
                            public fun foo(bar: Bar): Unit
                        }
                        
                        private class ImportedBasicInterface(
                            internal val basicInterface: BasicInterface
                        ) : CommonBasicInterface {
                            public override fun foo(bar: CommonBar): Unit = basicInterface.foo(
                                bar = bar.export()
                            )
                        }
                        
                        private class ExportedBasicInterface(
                            internal val basicInterface: CommonBasicInterface
                        ) : BasicInterface {
                            public override fun foo(bar: Bar): Unit = basicInterface.foo(
                                bar = bar.import()
                            )
                        }
                        
                        public fun CommonBasicInterface.export() = (this as? ImportedBasicInterface)?.basicInterface ?:
                                ExportedBasicInterface(this)
                        
                        public fun BasicInterface.`import`() = (this as? ExportedBasicInterface)?.basicInterface ?:
                                ImportedBasicInterface(this)
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
                        
                        import deezer.kmp.Export
                        import bar.Bar
            
                        @Export
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
                        import kotlin.Array
                        import kotlin.Double
                        import kotlin.Long
                        import kotlin.collections.List
                        import kotlin.js.JsExport
                        import bar.Bar as CommonBar
                        import flux.BasicInterface as CommonBasicInterface
                        
                        @JsExport
                        public external interface BasicInterface {
                            public fun foo(bar: Bar): Array<Double>
                        }
                        
                        private class ImportedBasicInterface(
                            internal val basicInterface: BasicInterface
                        ) : CommonBasicInterface {
                            public override fun foo(bar: CommonBar): List<Long> = basicInterface.foo(
                                bar = bar.export()
                            ).map { it.toLong() }
                        }
                        
                        private class ExportedBasicInterface(
                            internal val basicInterface: CommonBasicInterface
                        ) : BasicInterface {
                            public override fun foo(bar: Bar): Array<Double> = basicInterface.foo(
                                bar = bar.import()
                            ).map { it.toDouble() }.toTypedArray()
                        }
                        
                        public fun CommonBasicInterface.export() = (this as? ImportedBasicInterface)?.basicInterface ?:
                                ExportedBasicInterface(this)
                        
                        public fun BasicInterface.`import`() = (this as? ExportedBasicInterface)?.basicInterface ?:
                                ImportedBasicInterface(this)
                """.trimIndent()
                )
            )
        )
    }
}
