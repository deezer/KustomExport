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
            import deezer.kmp.Export

            @Export
            interface MyLongInterface {
                var myLong: Long
            }
    """, ExpectedOutputFile(
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
            internal val myLongInterface: MyLongInterface
        ) : CommonMyLongInterface {
            public override var myLong: Long
                get() = myLongInterface.myLong.toLong()
                set(`value`) {
                    myLongInterface.myLong = value.toDouble()
                }
        }
        
        private class ExportedMyLongInterface(
            internal val myLongInterface: CommonMyLongInterface
        ) : MyLongInterface {
            public override var myLong: Double
                get() = myLongInterface.myLong.toDouble()
                set(`value`) {
                    myLongInterface.myLong = value.toLong()
                }
        }
        
        public fun CommonMyLongInterface.export() = (this as? ImportedMyLongInterface)?.myLongInterface ?:
                ExportedMyLongInterface(this)
        
        public fun MyLongInterface.`import`() = (this as? ExportedMyLongInterface)?.myLongInterface ?:
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
            import deezer.kmp.Export

            @Export
            interface MyLongInterface {
                var myLong: Long?
            }
    """, ExpectedOutputFile(
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
            internal val myLongInterface: MyLongInterface
        ) : CommonMyLongInterface {
            public override var myLong: Long?
                get() = myLongInterface.myLong?.toLong()
                set(`value`) {
                    myLongInterface.myLong = value?.toDouble()
                }
        }
        
        private class ExportedMyLongInterface(
            internal val myLongInterface: CommonMyLongInterface
        ) : MyLongInterface {
            public override var myLong: Double?
                get() = myLongInterface.myLong?.toDouble()
                set(`value`) {
                    myLongInterface.myLong = value?.toLong()
                }
        }
        
        public fun CommonMyLongInterface.export() = (this as? ImportedMyLongInterface)?.myLongInterface ?:
                ExportedMyLongInterface(this)
        
        public fun MyLongInterface.`import`() = (this as? ExportedMyLongInterface)?.myLongInterface ?:
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
            import deezer.kmp.Export

            @Export
            interface MyStringsInterface {
                var myStrings: List<String>
            }
    """, ExpectedOutputFile(
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
            internal val myStringsInterface: MyStringsInterface
        ) : CommonMyStringsInterface {
            public override var myStrings: List<String>
                get() = myStringsInterface.myStrings.map { it }
                set(`value`) {
                    myStringsInterface.myStrings = value.map { it }.toTypedArray()
                }
        }
        
        private class ExportedMyStringsInterface(
            internal val myStringsInterface: CommonMyStringsInterface
        ) : MyStringsInterface {
            public override var myStrings: Array<String>
                get() = myStringsInterface.myStrings.map { it }.toTypedArray()
                set(`value`) {
                    myStringsInterface.myStrings = value.map { it }
                }
        }
        
        public fun CommonMyStringsInterface.export() = (this as?
                ImportedMyStringsInterface)?.myStringsInterface ?: ExportedMyStringsInterface(this)
        
        public fun MyStringsInterface.`import`() = (this as? ExportedMyStringsInterface)?.myStringsInterface
                ?: ImportedMyStringsInterface(this)
    """.trimIndent()
            )
        )
    }

    @Test
    fun from_ListLong_to_ArrayDouble() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kmp.Export

            @Export
            interface MyLongsInterface {
                var myLongs: List<Long>
            }
    """, ExpectedOutputFile(
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
            internal val myLongsInterface: MyLongsInterface
        ) : CommonMyLongsInterface {
            public override var myLongs: List<Long>
                get() = myLongsInterface.myLongs.map { it.toLong() }
                set(`value`) {
                    myLongsInterface.myLongs = value.map { it.toDouble() }.toTypedArray()
                }
        }
        
        private class ExportedMyLongsInterface(
            internal val myLongsInterface: CommonMyLongsInterface
        ) : MyLongsInterface {
            public override var myLongs: Array<Double>
                get() = myLongsInterface.myLongs.map { it.toDouble() }.toTypedArray()
                set(`value`) {
                    myLongsInterface.myLongs = value.map { it.toLong() }
                }
        }
        
        public fun CommonMyLongsInterface.export() = (this as? ImportedMyLongsInterface)?.myLongsInterface
                ?: ExportedMyLongsInterface(this)
        
        public fun MyLongsInterface.`import`() = (this as? ExportedMyLongsInterface)?.myLongsInterface ?:
                ImportedMyLongsInterface(this)
    """.trimIndent()
            )
        )
    }

    @Test
    fun from_Unknown_to_UnknownJs() {
        assertCompilationOutput(
            inputFiles = listOf(
                InputFile(
                    "Pikachu.kt", """
                package pokemon
                class Pikachu
            """.trimIndent()
                ), InputFile(
                    "SachaInterface.kt", """
                package pokedex
                
                import deezer.kmp.Export
                import pokemon.Pikachu
    
                @Export
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
                import pokedex.SachaInterface as CommonSachaInterface
                
                @JsExport
                public external interface SachaInterface {
                    public var pika: Pikachu
                }
                
                private class ImportedSachaInterface(
                    internal val sachaInterface: SachaInterface
                ) : CommonSachaInterface {
                    public override var pika: pokemon.Pikachu
                        get() = sachaInterface.pika.import()
                        set(`value`) {
                            sachaInterface.pika = value.export()
                        }
                }
                
                private class ExportedSachaInterface(
                    internal val sachaInterface: CommonSachaInterface
                ) : SachaInterface {
                    public override var pika: Pikachu
                        get() = sachaInterface.pika.export()
                        set(`value`) {
                            sachaInterface.pika = value.import()
                        }
                }
                
                public fun CommonSachaInterface.export() = (this as? ImportedSachaInterface)?.sachaInterface ?:
                        ExportedSachaInterface(this)
                
                public fun SachaInterface.`import`() = (this as? ExportedSachaInterface)?.sachaInterface ?:
                        ImportedSachaInterface(this)
    """.trimIndent()
                )
            )
        )
    }
}