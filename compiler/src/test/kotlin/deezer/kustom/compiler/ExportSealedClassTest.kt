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
class ExportSealedClassTest {
    @Test
    fun foo() {
        assertCompilationOutput(
            inputFiles = listOf(
                InputFile(
                    path = "SealedParent.kt",
                    content =
                    """
                    package foo.bar
        
                    import deezer.kustom.KustomExport
        
                    @KustomExport
                    sealed class SealedParent(val ctorParam: Long) {
                        abstract val field: Long
                    }
                    @KustomExport
                    class SealedChild1() : SealedParent(1) {
                    }
        
                    @KustomExport
                    class SealedChild2(override var field: Long) : SealedParent(2) {
                        val childField: String = "childField"
                        fun asName() = "SealedChild-\$\{field}-\$\{ctorParam}-\$\{childField}"
                    }
                    """.trimIndent()
                )
            ),
            expectedOutputFiles = listOf(
                ExpectedOutputFile(
                    path = "foo/bar/js/SealedParent.kt",
                    content = """
                    package foo.bar.js

                    import kotlin.Double
                    import kotlin.js.JsExport
                    import foo.bar.SealedChild1 as CommonSealedChild1
                    import foo.bar.SealedChild2 as CommonSealedChild2
                    import foo.bar.SealedParent as CommonSealedParent
                    
                    @JsExport
                    public sealed class SealedParent() {
                      public abstract val `field`: Double
                    
                      public abstract val ctorParam: Double
                    }
                    
                    public fun CommonSealedParent.exportSealedParent(): SealedParent = when (this) {
                      is CommonSealedChild1 -> exportSealedChild1()
                      is CommonSealedChild2 -> exportSealedChild2()
                    }
                    
                    public fun SealedParent.importSealedParent(): CommonSealedParent = when (this) {
                      is SealedChild1 -> importSealedChild1()
                      is SealedChild2 -> importSealedChild2()
                    }
                    """.trimIndent()
                ),
                ExpectedOutputFile(
                    path = "foo/bar/js/SealedChild1.kt",
                    content = """
                    package foo.bar.js
                    
                    import foo.bar.js.exportSealedParent
                    import foo.bar.js.importSealedParent
                    import kotlin.Double
                    import kotlin.js.JsExport
                    import foo.bar.SealedChild1 as CommonSealedChild1
                    import foo.bar.SealedParent as CommonSealedParent
                    
                    @JsExport
                    public class SealedChild1() : SealedParent() {
                        internal lateinit var common: CommonSealedChild1
                    
                        init {
                            common = CommonSealedChild1()}
                    
                        public override val `field`: Double
                            get() = common.field.toDouble()
                    
                        public override val ctorParam: Double
                            get() = common.ctorParam.toDouble()
                    
                        internal constructor(common: CommonSealedChild1) : this() {
                            this.common = common
                        }
                    }
                    
                    public fun CommonSealedChild1.exportSealedChild1(): SealedChild1 = SealedChild1(this)
                    
                    public fun SealedChild1.importSealedChild1(): CommonSealedChild1 = this.common
                    """.trimIndent()
                ),
                ExpectedOutputFile(
                    path = "foo/bar/js/SealedChild2.kt",
                    content = """
                    package foo.bar.js
                    
                    import foo.bar.js.exportSealedParent
                    import foo.bar.js.importSealedParent
                    import kotlin.Double
                    import kotlin.String
                    import kotlin.js.JsExport
                    import foo.bar.SealedChild2 as CommonSealedChild2
                    import foo.bar.SealedParent as CommonSealedParent
                    
                    @JsExport
                    public class SealedChild2(
                        `field`: Double
                    ) : SealedParent() {
                        internal lateinit var common: CommonSealedChild2
                    
                        init {
                            if (field != deezer.kustom.dynamicNull) {
                                common = CommonSealedChild2(
                                    field = field.toLong()
                                )
                            }}
                    
                        public val childField: String
                            get() = common.childField
                    
                        public override var `field`: Double
                            get() = common.field.toDouble()
                            set(setValue) {
                                common.field = setValue.toLong()
                            }
                    
                        public override val ctorParam: Double
                            get() = common.ctorParam.toDouble()
                    
                        internal constructor(common: CommonSealedChild2) : this(field = deezer.kustom.dynamicNull) {
                            this.common = common
                        }
                    
                        public fun asName(): String {
                            val result = common.asName()
                            return result
                        }
                    }
                    
                    public fun CommonSealedChild2.exportSealedChild2(): SealedChild2 = SealedChild2(this)
                    
                    public fun SealedChild2.importSealedChild2(): CommonSealedChild2 = this.common
                    """.trimIndent()
                )
            )
        )
    }
}
