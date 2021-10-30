package deezer.kustom.compiler

import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import org.junit.Test

@KotlinPoetKspPreview
class ExportedTypesTest {

    @Test
    fun kotlinExportedTypeAreNotChanged() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustom.KustomExport

            @KustomExport
            class AllTypesHandledByKotlinJs {
                var myBoolean: Boolean
                var myByte: Byte
                var myChar: Char
                var myShort: Short
                var myInt: Int
                var myFloat: Float
                var myDouble: Double
                var myString: String
                var myBooleanArray: BooleanArray
                var myByteArray: ByteArray
                var myCharArray: CharArray
                var myShortArray: ShortArray
                var myIntArray: IntArray
                var myFloatArray: FloatArray
                var myDoubleArray: DoubleArray
            }
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/AllTypesHandledByKotlinJs.kt",
                content = """
                    package foo.bar.js
                    
                    import kotlin.Boolean
                    import kotlin.BooleanArray
                    import kotlin.Byte
                    import kotlin.ByteArray
                    import kotlin.Char
                    import kotlin.CharArray
                    import kotlin.Double
                    import kotlin.DoubleArray
                    import kotlin.Float
                    import kotlin.FloatArray
                    import kotlin.Int
                    import kotlin.IntArray
                    import kotlin.Short
                    import kotlin.ShortArray
                    import kotlin.String
                    import kotlin.js.JsExport
                    import foo.bar.AllTypesHandledByKotlinJs as CommonAllTypesHandledByKotlinJs
                    
                    @JsExport
                    public class AllTypesHandledByKotlinJs() {
                        internal lateinit var common: CommonAllTypesHandledByKotlinJs
                    
                        init {
                            common = CommonAllTypesHandledByKotlinJs()}
                    
                        public var myBoolean: Boolean
                            get() = common.myBoolean
                            set(`value`) {
                                common.myBoolean = value
                            }
                    
                        public var myByte: Byte
                            get() = common.myByte
                            set(`value`) {
                                common.myByte = value
                            }
                    
                        public var myChar: Char
                            get() = common.myChar
                            set(`value`) {
                                common.myChar = value
                            }
                    
                        public var myShort: Short
                            get() = common.myShort
                            set(`value`) {
                                common.myShort = value
                            }
                    
                        public var myInt: Int
                            get() = common.myInt
                            set(`value`) {
                                common.myInt = value
                            }
                    
                        public var myFloat: Float
                            get() = common.myFloat
                            set(`value`) {
                                common.myFloat = value
                            }
                    
                        public var myDouble: Double
                            get() = common.myDouble
                            set(`value`) {
                                common.myDouble = value
                            }
                    
                        public var myString: String
                            get() = common.myString
                            set(`value`) {
                                common.myString = value
                            }
                    
                        public var myBooleanArray: BooleanArray
                            get() = common.myBooleanArray
                            set(`value`) {
                                common.myBooleanArray = value
                            }
                    
                        public var myByteArray: ByteArray
                            get() = common.myByteArray
                            set(`value`) {
                                common.myByteArray = value
                            }
                    
                        public var myCharArray: CharArray
                            get() = common.myCharArray
                            set(`value`) {
                                common.myCharArray = value
                            }
                    
                        public var myShortArray: ShortArray
                            get() = common.myShortArray
                            set(`value`) {
                                common.myShortArray = value
                            }
                    
                        public var myIntArray: IntArray
                            get() = common.myIntArray
                            set(`value`) {
                                common.myIntArray = value
                            }
                    
                        public var myFloatArray: FloatArray
                            get() = common.myFloatArray
                            set(`value`) {
                                common.myFloatArray = value
                            }
                    
                        public var myDoubleArray: DoubleArray
                            get() = common.myDoubleArray
                            set(`value`) {
                                common.myDoubleArray = value
                            }
                    
                        internal constructor(common: CommonAllTypesHandledByKotlinJs) : this() {
                            this.common = common
                        }
                    }
                    
                    public fun CommonAllTypesHandledByKotlinJs.exportAllTypesHandledByKotlinJs():
                            AllTypesHandledByKotlinJs = AllTypesHandledByKotlinJs(this)
                    
                    public fun AllTypesHandledByKotlinJs.importAllTypesHandledByKotlinJs():
                            CommonAllTypesHandledByKotlinJs = this.common
                """.trimIndent()
            )
        )
    }

    @Test
    fun from_Long_to_Double() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustom.KustomExport

            @KustomExport
            class MyLongClass {
                var myLong: Long
                var myLongArray: LongArray
            }
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/MyLongClass.kt",
                content = """
                    package foo.bar.js

                    import kotlin.Array
                    import kotlin.Double
                    import kotlin.js.JsExport
                    import foo.bar.MyLongClass as CommonMyLongClass

                    @JsExport
                    public class MyLongClass() {
                        internal lateinit var common: CommonMyLongClass

                        init {
                            common = CommonMyLongClass()}

                        public var myLong: Double
                            get() = common.myLong.toDouble()
                            set(`value`) {
                                common.myLong = value.toLong()
                            }

                        public var myLongArray: Array<Double>
                            get() = common.myLongArray.map { it.toDouble() }.toTypedArray()
                            set(`value`) {
                                common.myLongArray = value.map { it.toLong() }.toLongArray()
                            }

                        internal constructor(common: CommonMyLongClass) : this() {
                            this.common = common
                        }
                    }

                    public fun CommonMyLongClass.exportMyLongClass(): MyLongClass = MyLongClass(this)

                    public fun MyLongClass.importMyLongClass(): CommonMyLongClass = this.common
                """.trimIndent()
            )
        )
    }

    @Test
    fun nullables() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustom.KustomExport

            @KustomExport
            class Nullables {
                var myNullableString: String?
                var myNullableArray: Array<String>?
                var myArrayOfNullables: Array<String?>
                var myNullableArrayOfNullables: Array<String?>?
            }
            """,
            ExpectedOutputFile(
                path = "foo/bar/js/Nullables.kt",
                content = """
                package foo.bar.js

                import kotlin.Array
                import kotlin.String
                import kotlin.js.JsExport
                import foo.bar.Nullables as CommonNullables

                @JsExport
                public class Nullables() {
                    internal lateinit var common: CommonNullables

                    init {
                        common = CommonNullables()}

                    public var myNullableString: String?
                        get() = common.myNullableString
                        set(`value`) {
                            common.myNullableString = value
                        }

                    public var myNullableArray: Array<String>?
                        get() = common.myNullableArray
                        set(`value`) {
                            common.myNullableArray = value
                        }

                    public var myArrayOfNullables: Array<String?>
                        get() = common.myArrayOfNullables
                        set(`value`) {
                            common.myArrayOfNullables = value
                        }

                    public var myNullableArrayOfNullables: Array<String?>?
                        get() = common.myNullableArrayOfNullables
                        set(`value`) {
                            common.myNullableArrayOfNullables = value
                        }

                    internal constructor(common: CommonNullables) : this() {
                        this.common = common
                    }
                }

                public fun CommonNullables.exportNullables(): Nullables = Nullables(this)

                public fun Nullables.importNullables(): CommonNullables = this.common
                """.trimIndent()
            )
        )
    }

    @Test
    fun arrays() {
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
                    "Arrays.kt",
                    """
                    package foo.bar

                    import pokemon.Pikachu
                    import deezer.kustom.KustomExport
        
                    @KustomExport
                    class Arrays {
                        var myBasicArray: Array<String>
                        var myCustomArray: Array<Pikachu>
                        var myArrayOfNullables: Array<Unit?>
                        var myNullableArrayOfNullables: Array<String?>?
                    }
                    """
                )
            ),
            expectedOutputFiles = listOf(
                ExpectedOutputFile(
                    path = "foo/bar/js/Arrays.kt",
                    content = """
                    package foo.bar.js

                    import kotlin.Array
                    import kotlin.String
                    import kotlin.Unit
                    import kotlin.js.JsExport
                    import pokemon.js.Pikachu
                    import pokemon.js.exportPikachu
                    import pokemon.js.importPikachu
                    import foo.bar.Arrays as CommonArrays
                    import pokemon.Pikachu as CommonPikachu

                    @JsExport
                    public class Arrays() {
                        internal lateinit var common: CommonArrays

                        init {
                            common = CommonArrays()}

                        public var myBasicArray: Array<String>
                            get() = common.myBasicArray
                            set(`value`) {
                                common.myBasicArray = value
                            }

                        public var myCustomArray: Array<Pikachu>
                            get() = common.myCustomArray.map { it.exportPikachu() }.toTypedArray()
                            set(`value`) {
                                common.myCustomArray = value.map { it.importPikachu() }.toTypedArray()
                            }

                        public var myArrayOfNullables: Array<Unit?>
                            get() = common.myArrayOfNullables
                            set(`value`) {
                                common.myArrayOfNullables = value
                            }

                        public var myNullableArrayOfNullables: Array<String?>?
                            get() = common.myNullableArrayOfNullables
                            set(`value`) {
                                common.myNullableArrayOfNullables = value
                            }

                        internal constructor(common: CommonArrays) : this() {
                            this.common = common
                        }
                    }

                    public fun CommonArrays.exportArrays(): Arrays = Arrays(this)

                    public fun Arrays.importArrays(): CommonArrays = this.common
                    """.trimIndent()
                )
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
                
                public fun CommonSachaInterface.exportSachaInterface(): SachaInterface = (this as?
                        ImportedSachaInterface)?.exported ?: ExportedSachaInterface(this)
                
                public fun SachaInterface.importSachaInterface(): CommonSachaInterface = (this as?
                        ExportedSachaInterface)?.common ?: ImportedSachaInterface(this)
                    """.trimIndent()
                )
            )
        )
    }
}
