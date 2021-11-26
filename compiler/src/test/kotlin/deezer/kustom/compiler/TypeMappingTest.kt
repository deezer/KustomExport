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

package deezer.kustom.compiler

import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import org.junit.Test

@KotlinPoetKspPreview
class TypeMappingTest {

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
                import kotlin.Suppress
                import kotlin.js.JsExport
                import foo.bar.AllTypesHandledByKotlinJs as CommonAllTypesHandledByKotlinJs
                
                @JsExport
                public class AllTypesHandledByKotlinJs() {
                    internal var common: CommonAllTypesHandledByKotlinJs
                
                    init {
                        common = CommonAllTypesHandledByKotlinJs()
                    }
                
                    public var myBoolean: Boolean
                        get() = common.myBoolean
                        set(setValue) {
                            common.myBoolean = setValue
                        }
                
                    public var myByte: Byte
                        get() = common.myByte
                        set(setValue) {
                            common.myByte = setValue
                        }
                
                    public var myChar: Char
                        get() = common.myChar
                        set(setValue) {
                            common.myChar = setValue
                        }
                
                    public var myShort: Short
                        get() = common.myShort
                        set(setValue) {
                            common.myShort = setValue
                        }
                
                    public var myInt: Int
                        get() = common.myInt
                        set(setValue) {
                            common.myInt = setValue
                        }
                
                    public var myFloat: Float
                        get() = common.myFloat
                        set(setValue) {
                            common.myFloat = setValue
                        }
                
                    public var myDouble: Double
                        get() = common.myDouble
                        set(setValue) {
                            common.myDouble = setValue
                        }
                
                    public var myString: String
                        get() = common.myString
                        set(setValue) {
                            common.myString = setValue
                        }
                
                    public var myBooleanArray: BooleanArray
                        get() = common.myBooleanArray
                        set(setValue) {
                            common.myBooleanArray = setValue
                        }
                
                    public var myByteArray: ByteArray
                        get() = common.myByteArray
                        set(setValue) {
                            common.myByteArray = setValue
                        }
                
                    public var myCharArray: CharArray
                        get() = common.myCharArray
                        set(setValue) {
                            common.myCharArray = setValue
                        }
                
                    public var myShortArray: ShortArray
                        get() = common.myShortArray
                        set(setValue) {
                            common.myShortArray = setValue
                        }
                
                    public var myIntArray: IntArray
                        get() = common.myIntArray
                        set(setValue) {
                            common.myIntArray = setValue
                        }
                
                    public var myFloatArray: FloatArray
                        get() = common.myFloatArray
                        set(setValue) {
                            common.myFloatArray = setValue
                        }
                
                    public var myDoubleArray: DoubleArray
                        get() = common.myDoubleArray
                        set(setValue) {
                            common.myDoubleArray = setValue
                        }
                
                    @Suppress("UNNECESSARY_SAFE_CALL")
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
                import kotlin.Suppress
                import kotlin.collections.toLongArray
                import kotlin.collections.toTypedArray
                import kotlin.js.JsExport
                import foo.bar.MyLongClass as CommonMyLongClass
                
                @JsExport
                public class MyLongClass() {
                    internal var common: CommonMyLongClass
                
                    init {
                        common = CommonMyLongClass()
                    }
                
                    public var myLong: Double
                        get() = common.myLong.toDouble()
                        set(setValue) {
                            common.myLong = setValue.toLong()
                        }
                
                    public var myLongArray: Array<Double>
                        get() = common.myLongArray.map { it.toDouble() }.toTypedArray()
                        set(setValue) {
                            common.myLongArray = setValue.map { it.toLong() }.toLongArray()
                        }
                
                    @Suppress("UNNECESSARY_SAFE_CALL")
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
                import kotlin.Suppress
                import kotlin.js.JsExport
                import foo.bar.Nullables as CommonNullables
                
                @JsExport
                public class Nullables() {
                    internal var common: CommonNullables
                
                    init {
                        common = CommonNullables()
                    }
                
                    public var myNullableString: String?
                        get() = common.myNullableString
                        set(setValue) {
                            common.myNullableString = setValue
                        }
                
                    public var myNullableArray: Array<String>?
                        get() = common.myNullableArray
                        set(setValue) {
                            common.myNullableArray = setValue
                        }
                
                    public var myArrayOfNullables: Array<String?>
                        get() = common.myArrayOfNullables
                        set(setValue) {
                            common.myArrayOfNullables = setValue
                        }
                
                    public var myNullableArrayOfNullables: Array<String?>?
                        get() = common.myNullableArrayOfNullables
                        set(setValue) {
                            common.myNullableArrayOfNullables = setValue
                        }
                
                    @Suppress("UNNECESSARY_SAFE_CALL")
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
                    import kotlin.Suppress
                    import kotlin.Unit
                    import kotlin.collections.toTypedArray
                    import kotlin.js.JsExport
                    import pokemon.js.Pikachu
                    import pokemon.js.exportPikachu
                    import pokemon.js.importPikachu
                    import foo.bar.Arrays as CommonArrays
                    
                    @JsExport
                    public class Arrays() {
                        internal var common: CommonArrays
                    
                        init {
                            common = CommonArrays()
                        }
                    
                        public var myBasicArray: Array<String>
                            get() = common.myBasicArray
                            set(setValue) {
                                common.myBasicArray = setValue
                            }
                    
                        public var myCustomArray: Array<Pikachu>
                            get() = common.myCustomArray.map { it.exportPikachu() }.toTypedArray()
                            set(setValue) {
                                common.myCustomArray = setValue.map { it.importPikachu() }.toTypedArray()
                            }
                    
                        public var myArrayOfNullables: Array<Unit?>
                            get() = common.myArrayOfNullables
                            set(setValue) {
                                common.myArrayOfNullables = setValue
                            }
                    
                        public var myNullableArrayOfNullables: Array<String?>?
                            get() = common.myNullableArrayOfNullables
                            set(setValue) {
                                common.myNullableArrayOfNullables = setValue
                            }
                    
                        @Suppress("UNNECESSARY_SAFE_CALL")
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
                    
                    @JsExport
                    public external interface SachaInterface {
                        public var pika: Pikachu
                    }
                    
                    private class ImportedSachaInterface(
                        internal val exported: SachaInterface
                    ) : CommonSachaInterface {
                        public override var pika: pokemon.Pikachu
                            get() = exported.pika.importPikachu()
                            set(setValue) {
                                exported.pika = setValue.exportPikachu()
                            }
                    }
                    
                    private class ExportedSachaInterface(
                        internal val common: CommonSachaInterface
                    ) : SachaInterface {
                        public override var pika: Pikachu
                            get() = common.pika.exportPikachu()
                            set(setValue) {
                                common.pika = setValue.importPikachu()
                            }
                    }
                    
                    public fun CommonSachaInterface.exportSachaInterface(): SachaInterface =
                            (this as? ImportedSachaInterface)?.exported ?: ExportedSachaInterface(this)
                    
                    public fun SachaInterface.importSachaInterface(): CommonSachaInterface =
                            (this as? ExportedSachaInterface)?.common ?: ImportedSachaInterface(this)
                    """.trimIndent()
                )
            )
        )
    }

    @Test
    fun lambda() {
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
                    "Lambdas.kt",
                    """
                    package foo
                    import deezer.kustom.KustomExport
                    import pokemon.Pikachu
        
                    @KustomExport
                    class Lambdas {
                        var floatToUnit: (Float) -> Unit = {}
                        var complexFun: (index: Int, pika: Pikachu, Long) -> Pikachu = { _, pika, _ -> pika }
                        fun returnParam(block: (index: Int, pika: Pikachu, Long) -> Pikachu): (Int, Pikachu, Long) -> Pikachu {
                            return block
                        }
                    }
                    """.trimIndent()
                )
            ),
            expectedOutputFiles = listOf(
                ExpectedOutputFile(
                    path = "foo/js/Lambdas.kt",
                    content = """
                    package foo.js

                    import kotlin.Double
                    import kotlin.Float
                    import kotlin.Int
                    import kotlin.Long
                    import kotlin.Suppress
                    import kotlin.Unit
                    import kotlin.js.JsExport
                    import pokemon.js.Pikachu
                    import pokemon.js.exportPikachu
                    import pokemon.js.importPikachu
                    import foo.Lambdas as CommonLambdas
                    
                    @JsExport
                    public class Lambdas() {
                        internal var common: CommonLambdas
                    
                        init {
                            common = CommonLambdas()
                        }
                    
                        public var floatToUnit: (Float) -> Unit
                            get() = { a: Float -> 
                                common.floatToUnit(a)
                            }
                            set(setValue) {
                                common.floatToUnit = { a: Float -> 
                                    setValue(a)
                                }
                            }
                    
                        public var complexFun: (
                            Int,
                            Pikachu,
                            Double
                        ) -> pokemon.Pikachu
                            get() = { a: Int, b: Pikachu, c: Double -> 
                                common.complexFun(
                                    a,
                                    b.importPikachu(),
                                    c.toLong()).exportPikachu()
                            }
                            set(setValue) {
                                common.complexFun = { a: Int, b: pokemon.Pikachu, c: Long -> 
                                    setValue(
                                        a,
                                        b.exportPikachu(),
                                        c.toDouble()).importPikachu()
                                }
                            }
                    
                        @Suppress("UNNECESSARY_SAFE_CALL")
                        internal constructor(common: CommonLambdas) : this() {
                            this.common = common
                        }
                    
                        public fun returnParam(block: (
                            Int,
                            Pikachu,
                            Double
                        ) -> pokemon.Pikachu): (
                            Int,
                            Pikachu,
                            Double
                        ) -> pokemon.Pikachu {
                            val result = common.returnParam(
                                block = { a: Int, b: pokemon.Pikachu, c: Long -> 
                                block(
                                    a,
                                    b.exportPikachu(),
                                    c.toDouble()).importPikachu()
                            },
                            )
                            return { a: Int, b: Pikachu, c: Double -> 
                                result(
                                    a,
                                    b.importPikachu(),
                                    c.toLong()).exportPikachu()
                            }
                        }
                    }
                    
                    public fun CommonLambdas.exportLambdas(): Lambdas = Lambdas(this)
                    
                    public fun Lambdas.importLambdas(): CommonLambdas = this.common
                    """.trimIndent()
                )
            )
        )
    }
}
