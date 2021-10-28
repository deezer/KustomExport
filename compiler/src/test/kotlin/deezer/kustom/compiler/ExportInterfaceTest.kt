package deezer.kustom.compiler

import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import org.junit.Test

@KotlinPoetKspPreview
class ExportInterfaceTest {

    @Test
    fun emptyInterface() {
        assertCompilationOutput(
            """
            package flux
            
            import deezer.kustom.KustomExport

            @KustomExport
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
                internal val exported: Exportable
            ) : CommonExportable
            
            private class ExportedExportable(
                internal val common: CommonExportable
            ) : Exportable
            
            public fun CommonExportable.exportExportable() = (this as? ImportedExportable)?.exported ?:
                    ExportedExportable(this)
            
            public fun Exportable.importExportable() = (this as? ExportedExportable)?.common ?:
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
            
            import deezer.kustom.KustomExport

            @KustomExport
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
                internal val exported: BasicInterface
            ) : CommonBasicInterface {
                public override val flex: String
                    get() = exported.flex
            }
            
            private class ExportedBasicInterface(
                internal val common: CommonBasicInterface
            ) : BasicInterface {
                public override val flex: String
                    get() = common.flex
            }
            
            public fun CommonBasicInterface.exportBasicInterface() = (this as? ImportedBasicInterface)?.exported
                    ?: ExportedBasicInterface(this)
            
            public fun BasicInterface.importBasicInterface() = (this as? ExportedBasicInterface)?.common ?:
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
            
            import deezer.kustom.KustomExport

            @KustomExport
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
                internal val exported: BasicInterface
            ) : CommonBasicInterface {
                public override var canChange: String
                    get() = exported.canChange
                    set(`value`) {
                        exported.canChange = value
                    }
            }
            
            private class ExportedBasicInterface(
                internal val common: CommonBasicInterface
            ) : BasicInterface {
                public override var canChange: String
                    get() = common.canChange
                    set(`value`) {
                        common.canChange = value
                    }
            }
            
            public fun CommonBasicInterface.exportBasicInterface() = (this as? ImportedBasicInterface)?.exported
                    ?: ExportedBasicInterface(this)
            
            public fun BasicInterface.importBasicInterface() = (this as? ExportedBasicInterface)?.common ?:
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
            
            import deezer.kustom.KustomExport

            @KustomExport
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
                internal val exported: BasicInterface
            ) : CommonBasicInterface {
                public override var numbers: List<Long>
                    get() = exported.numbers.map { it.toLong() }
                    set(`value`) {
                        exported.numbers = value.map { it.toDouble() }.toTypedArray()
                    }
            }
            
            private class ExportedBasicInterface(
                internal val common: CommonBasicInterface
            ) : BasicInterface {
                public override var numbers: Array<Double>
                    get() = common.numbers.map { it.toDouble() }.toTypedArray()
                    set(`value`) {
                        common.numbers = value.map { it.toLong() }
                    }
            }
            
            public fun CommonBasicInterface.exportBasicInterface() = (this as? ImportedBasicInterface)?.exported
                    ?: ExportedBasicInterface(this)
            
            public fun BasicInterface.importBasicInterface() = (this as? ExportedBasicInterface)?.common ?:
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
                        
                        import deezer.kustom.KustomExport
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
                        import bar.Bar as CommonBar
                        import flux.BasicInterface as CommonBasicInterface
                        
                        @JsExport
                        public external interface BasicInterface {
                            public fun foo(bar: Bar): Unit
                        }
                        
                        private class ImportedBasicInterface(
                            internal val exported: BasicInterface
                        ) : CommonBasicInterface {
                            public override fun foo(bar: CommonBar): Unit = exported.foo(
                                bar = bar.exportBar()
                            )
                        }
                        
                        private class ExportedBasicInterface(
                            internal val common: CommonBasicInterface
                        ) : BasicInterface {
                            public override fun foo(bar: Bar): Unit = common.foo(
                                bar = bar.importBar()
                            )
                        }
                        
                        public fun CommonBasicInterface.exportBasicInterface() = (this as? ImportedBasicInterface)?.exported
                                ?: ExportedBasicInterface(this)
                        
                        public fun BasicInterface.importBasicInterface() = (this as? ExportedBasicInterface)?.common ?:
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
                        
                        import deezer.kustom.KustomExport
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
                        import kotlin.js.JsExport
                        import bar.Bar as CommonBar
                        import flux.BasicInterface as CommonBasicInterface
                        
                        @JsExport
                        public external interface BasicInterface {
                            public fun foo(bar: Bar): Array<Double>
                        }
                        
                        private class ImportedBasicInterface(
                            internal val exported: BasicInterface
                        ) : CommonBasicInterface {
                            public override fun foo(bar: CommonBar): List<Long> = exported.foo(
                                bar = bar.exportBar()
                            ).map { it.toLong() }
                        }
                        
                        private class ExportedBasicInterface(
                            internal val common: CommonBasicInterface
                        ) : BasicInterface {
                            public override fun foo(bar: Bar): Array<Double> = common.foo(
                                bar = bar.importBar()
                            ).map { it.toDouble() }.toTypedArray()
                        }
                        
                        public fun CommonBasicInterface.exportBasicInterface() = (this as? ImportedBasicInterface)?.exported
                                ?: ExportedBasicInterface(this)
                        
                        public fun BasicInterface.importBasicInterface() = (this as? ExportedBasicInterface)?.common ?:
                                ImportedBasicInterface(this)
                """.trimIndent()
                )
            )
        )
    }
}
