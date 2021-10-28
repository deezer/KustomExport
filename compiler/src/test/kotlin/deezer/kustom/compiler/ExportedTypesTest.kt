package deezer.kustom.compiler

import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import org.junit.Test

@KotlinPoetKspPreview
class ExportedTypesTest {

    @Test
    fun from_Long_to_Double() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustom.KustomExport

            @KustomExport
            interface MyLongInterface {
                var myLong: Long
            }
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/MyLongInterface.kt",
                content = """
        package foo.bar.js

        import kotlin.Double
        import kotlin.Long
        import kotlin.js.JsExport
        import foo.bar.MyLongInterface as CommonMyLongInterface
        
        @JsExport
        public external interface MyLongInterface {
            public var myLong: Double
        }
        
        private class ImportedMyLongInterface(
            internal val exported: MyLongInterface
        ) : CommonMyLongInterface {
            public override var myLong: Long
                get() = exported.myLong.toLong()
                set(`value`) {
                    exported.myLong = value.toDouble()
                }
        }
        
        private class ExportedMyLongInterface(
            internal val common: CommonMyLongInterface
        ) : MyLongInterface {
            public override var myLong: Double
                get() = common.myLong.toDouble()
                set(`value`) {
                    common.myLong = value.toLong()
                }
        }
        
        public fun CommonMyLongInterface.exportMyLongInterface() = (this as?
                ImportedMyLongInterface)?.exported ?: ExportedMyLongInterface(this)
        
        public fun MyLongInterface.importMyLongInterface() = (this as? ExportedMyLongInterface)?.common ?:
                ImportedMyLongInterface(this)
                """.trimIndent()
            )
        )
    }

    @Test
    fun from_nullableLong_to_nullableDouble() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustom.KustomExport

            @KustomExport
            interface MyLongInterface {
                var myLong: Long?
            }
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/MyLongInterface.kt",
                content = """
        package foo.bar.js
        
        import kotlin.Double
        import kotlin.Long
        import kotlin.js.JsExport
        import foo.bar.MyLongInterface as CommonMyLongInterface
        
        @JsExport
        public external interface MyLongInterface {
            public var myLong: Double?
        }
        
        private class ImportedMyLongInterface(
            internal val exported: MyLongInterface
        ) : CommonMyLongInterface {
            public override var myLong: Long?
                get() = exported.myLong?.toLong()
                set(`value`) {
                    exported.myLong = value?.toDouble()
                }
        }
        
        private class ExportedMyLongInterface(
            internal val common: CommonMyLongInterface
        ) : MyLongInterface {
            public override var myLong: Double?
                get() = common.myLong?.toDouble()
                set(`value`) {
                    common.myLong = value?.toLong()
                }
        }
        
        public fun CommonMyLongInterface.exportMyLongInterface() = (this as?
                ImportedMyLongInterface)?.exported ?: ExportedMyLongInterface(this)
        
        public fun MyLongInterface.importMyLongInterface() = (this as? ExportedMyLongInterface)?.common ?:
                ImportedMyLongInterface(this)
                """.trimIndent()
            )
        )
    }

    @Test
    fun from_List_to_Array() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustom.KustomExport

            @KustomExport
            interface MyStringsInterface {
                var myStrings: List<String>
            }
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/MyStringsInterface.kt",
                content = """
        package foo.bar.js
        
        import kotlin.Array
        import kotlin.String
        import kotlin.collections.List
        import kotlin.js.JsExport
        import foo.bar.MyStringsInterface as CommonMyStringsInterface
        
        @JsExport
        public external interface MyStringsInterface {
            public var myStrings: Array<String>
        }
        
        private class ImportedMyStringsInterface(
            internal val exported: MyStringsInterface
        ) : CommonMyStringsInterface {
            public override var myStrings: List<String>
                get() = exported.myStrings.map { it }
                set(`value`) {
                    exported.myStrings = value.map { it }.toTypedArray()
                }
        }
        
        private class ExportedMyStringsInterface(
            internal val common: CommonMyStringsInterface
        ) : MyStringsInterface {
            public override var myStrings: Array<String>
                get() = common.myStrings.map { it }.toTypedArray()
                set(`value`) {
                    common.myStrings = value.map { it }
                }
        }
        
        public fun CommonMyStringsInterface.exportMyStringsInterface() = (this as?
                ImportedMyStringsInterface)?.exported ?: ExportedMyStringsInterface(this)
        
        public fun MyStringsInterface.importMyStringsInterface() = (this as?
                ExportedMyStringsInterface)?.common ?: ImportedMyStringsInterface(this)
                """.trimIndent()
            )
        )
    }

    @Test
    fun from_ListLong_to_ArrayDouble() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustom.KustomExport

            @KustomExport
            interface MyLongsInterface {
                var myLongs: List<Long>
            }
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/MyLongsInterface.kt",
                content = """
        package foo.bar.js
        
        import kotlin.Array
        import kotlin.Double
        import kotlin.Long
        import kotlin.collections.List
        import kotlin.js.JsExport
        import foo.bar.MyLongsInterface as CommonMyLongsInterface
        
        @JsExport
        public external interface MyLongsInterface {
            public var myLongs: Array<Double>
        }
        
        private class ImportedMyLongsInterface(
            internal val exported: MyLongsInterface
        ) : CommonMyLongsInterface {
            public override var myLongs: List<Long>
                get() = exported.myLongs.map { it.toLong() }
                set(`value`) {
                    exported.myLongs = value.map { it.toDouble() }.toTypedArray()
                }
        }
        
        private class ExportedMyLongsInterface(
            internal val common: CommonMyLongsInterface
        ) : MyLongsInterface {
            public override var myLongs: Array<Double>
                get() = common.myLongs.map { it.toDouble() }.toTypedArray()
                set(`value`) {
                    common.myLongs = value.map { it.toLong() }
                }
        }
        
        public fun CommonMyLongsInterface.exportMyLongsInterface() = (this as?
                ImportedMyLongsInterface)?.exported ?: ExportedMyLongsInterface(this)
        
        public fun MyLongsInterface.importMyLongsInterface() = (this as? ExportedMyLongsInterface)?.common
                ?: ImportedMyLongsInterface(this)
                """.trimIndent()
            )
        )
    }

    @Test
    fun from_Unknown_to_UnknownJs() {
        assertCompilationOutput(
            inputFiles = listOf(
                InputFile(
                    "Pikachu.kt",
                    """
                package pokemon
                class Pikachu
                    """.trimIndent()
                ),
                InputFile(
                    "SachaInterface.kt",
                    """
                package pokedex
                
                import deezer.kustom.KustomExport
                import pokemon.Pikachu
    
                @KustomExport
                interface SachaInterface {
                    var pika: Pikachu
                }
    """
                )
            ),

            expectedOutputFiles = listOf(
                // Ignoring output for Pikachu, not interesting here as we're checking the .import()/.export() usage
                ExpectedOutputFile(
                    path = "pokedex/js/SachaInterface.kt",
                    content = """
                package pokedex.js
                
                import kotlin.js.JsExport
                import pokemon.js.Pikachu
                import pokemon.js.exportPikachu
                import pokemon.js.importPikachu
                import pokedex.SachaInterface as CommonSachaInterface
                import pokemon.Pikachu as CommonPikachu
                
                @JsExport
                public external interface SachaInterface {
                    public var pika: Pikachu
                }
                
                private class ImportedSachaInterface(
                    internal val exported: SachaInterface
                ) : CommonSachaInterface {
                    public override var pika: CommonPikachu
                        get() = exported.pika.importPikachu()
                        set(`value`) {
                            exported.pika = value.exportPikachu()
                        }
                }
                
                private class ExportedSachaInterface(
                    internal val common: CommonSachaInterface
                ) : SachaInterface {
                    public override var pika: Pikachu
                        get() = common.pika.exportPikachu()
                        set(`value`) {
                            common.pika = value.importPikachu()
                        }
                }
                
                public fun CommonSachaInterface.exportSachaInterface() = (this as? ImportedSachaInterface)?.exported
                        ?: ExportedSachaInterface(this)
                
                public fun SachaInterface.importSachaInterface() = (this as? ExportedSachaInterface)?.common ?:
                        ImportedSachaInterface(this)
                    """.trimIndent()
                )
            )
        )
    }
}
