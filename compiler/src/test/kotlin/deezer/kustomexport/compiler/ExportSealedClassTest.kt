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

import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import org.junit.Test

@KotlinPoetKspPreview
class ExportSealedClassTest {
    @Test
    fun basicSealedClass() {
        assertCompilationOutput(
            inputFiles = listOf(
                InputFile(
                    path = "Sealeds.kt",
                    content =
                    """
                    package foo.bar
        
                    import deezer.kustomexport.KustomExport
        
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
                    
                    import kotlin.Double
                    import kotlin.Suppress
                    import kotlin.js.JsExport
                    import foo.bar.SealedChild1 as CommonSealedChild1
                    
                    @JsExport
                    public class SealedChild1() : SealedParent() {
                        internal var common: CommonSealedChild1
                    
                        init {
                            common = CommonSealedChild1()
                        }
                    
                        public override val `field`: Double
                            get() = common.field.toDouble()
                    
                        public override val ctorParam: Double
                            get() = common.ctorParam.toDouble()
                    
                        @Suppress("UNNECESSARY_SAFE_CALL")
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
                    
                    import deezer.kustomexport.dynamicCastTo
                    import deezer.kustomexport.dynamicNull
                    import kotlin.Double
                    import kotlin.String
                    import kotlin.Suppress
                    import kotlin.js.JsExport
                    import foo.bar.SealedChild2 as CommonSealedChild2
                    
                    @JsExport
                    public class SealedChild2(
                        `field`: Double
                    ) : SealedParent() {
                        internal lateinit var common: CommonSealedChild2
                    
                        init {
                            if (field != dynamicNull) {
                                common = CommonSealedChild2(
                                    field = field.toLong(),
                                )
                            }
                        }
                    
                        public val childField: String
                            get() = common.childField
                    
                        public override var `field`: Double
                            get() = common.field.toDouble()
                            set(setValue) {
                                common.field = setValue.toLong()
                            }
                    
                        public override val ctorParam: Double
                            get() = common.ctorParam.toDouble()
                    
                        @Suppress("UNNECESSARY_SAFE_CALL")
                        internal constructor(common: CommonSealedChild2) :
                                this(field = dynamicNull?.dynamicCastTo<Double>()) {
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

    @Test
    fun sealedOfSealedClassInheritingException() {
        assertCompilationOutput(
            inputFiles = listOf(
                InputFile(
                    path = "Sealeds.kt",
                    content =
                    """
                    package foo.bar
        
                    import deezer.kustomexport.KustomExport
                    import java.lang.IllegalStateException // Or else it's interpreted as a typealias and fail in the toClassName() koktlinpoet ext
        
                    @KustomExport
                    sealed class SealedParent(val ctorParam: Long): IllegalStateException("I'm sealed") {
                        abstract val field: Long
                    }
                    @KustomExport
                    class SealedChild(override var field: Long) : SealedParent(1) {
                        val goo: Long = 12L
                    }

                    @KustomExport
                    sealed class SealedLvl2() : SealedParent(4) {
                        abstract fun baz(input: SealedParent): SealedParent
                    }
        
        
                    @KustomExport
                    class SealedChild2(override var field: Long) : SealedLvl2() {
                        val childField: String = "childField"
                        override fun baz(input: SealedParent): SealedParent = input
                    }
                    """.trimIndent()
                )
            ),
            expectedOutputFiles = listOf(
                ExpectedOutputFile(
                    path = "foo/bar/js/SealedParent.kt",
                    content = """
                    package foo.bar.js
                    
                    import java.lang.js.IllegalStateException
                    import kotlin.Double
                    import kotlin.js.JsExport
                    import foo.bar.SealedChild as CommonSealedChild
                    import foo.bar.SealedLvl2 as CommonSealedLvl2
                    import foo.bar.SealedParent as CommonSealedParent
                    
                    @JsExport
                    public sealed class SealedParent() : IllegalStateException() {
                      public abstract val `field`: Double
                    
                      public abstract val ctorParam: Double
                    }
                    
                    public fun CommonSealedParent.exportSealedParent(): SealedParent = when (this) {
                      is CommonSealedChild -> exportSealedChild()
                      is CommonSealedLvl2 -> exportSealedLvl2()
                    }
                    
                    public fun SealedParent.importSealedParent(): CommonSealedParent = when (this) {
                      is SealedChild -> importSealedChild()
                      is SealedLvl2 -> importSealedLvl2()
                    }
                    """.trimIndent()
                ),
                ExpectedOutputFile(
                    path = "foo/bar/js/SealedChild.kt",
                    content = """
                    package foo.bar.js
                    
                    import deezer.kustomexport.dynamicCastTo
                    import deezer.kustomexport.dynamicNull
                    import kotlin.Double
                    import kotlin.String
                    import kotlin.Suppress
                    import kotlin.Throwable
                    import kotlin.js.JsExport
                    import foo.bar.SealedChild as CommonSealedChild
                    
                    @JsExport
                    public class SealedChild(
                        `field`: Double
                    ) : SealedParent() {
                        internal lateinit var common: CommonSealedChild
                    
                        init {
                            if (field != dynamicNull) {
                                common = CommonSealedChild(
                                    field = field.toLong(),
                                )
                            }
                        }
                    
                        public val goo: Double
                            get() = common.goo.toDouble()
                    
                        public override var `field`: Double
                            get() = common.field.toDouble()
                            set(setValue) {
                                common.field = setValue.toLong()
                            }
                    
                        public override val ctorParam: Double
                            get() = common.ctorParam.toDouble()
                    
                        public override val cause: Throwable?
                            get() = common.cause
                    
                        public override val message: String?
                            get() = common.message
                    
                        @Suppress("UNNECESSARY_SAFE_CALL")
                        internal constructor(common: CommonSealedChild) :
                                this(field = dynamicNull?.dynamicCastTo<Double>()) {
                            this.common = common
                        }
                    }
                    
                    public fun CommonSealedChild.exportSealedChild(): SealedChild = SealedChild(this)
                    
                    public fun SealedChild.importSealedChild(): CommonSealedChild = this.common
                    """.trimIndent()
                ),
                ExpectedOutputFile(
                    path = "foo/bar/js/SealedLvl2.kt",
                    content = """
                    package foo.bar.js
                    
                    import kotlin.js.JsExport
                    import foo.bar.SealedChild2 as CommonSealedChild2
                    import foo.bar.SealedLvl2 as CommonSealedLvl2
                    
                    @JsExport
                    public sealed class SealedLvl2() : SealedParent() {
                      public abstract fun baz(input: SealedParent): SealedParent
                    }
                    
                    public fun CommonSealedLvl2.exportSealedLvl2(): SealedLvl2 = when (this) {
                      is CommonSealedChild2 -> exportSealedChild2()
                    }
                    
                    public fun SealedLvl2.importSealedLvl2(): CommonSealedLvl2 = when (this) {
                      is SealedChild2 -> importSealedChild2()
                    }
                    """.trimIndent()
                ),
                ExpectedOutputFile(
                    path = "foo/bar/js/SealedChild2.kt",
                    content = """
                    package foo.bar.js
                    
                    import deezer.kustomexport.dynamicCastTo
                    import deezer.kustomexport.dynamicNull
                    import kotlin.Double
                    import kotlin.String
                    import kotlin.Suppress
                    import kotlin.Throwable
                    import kotlin.js.JsExport
                    import foo.bar.SealedChild2 as CommonSealedChild2
                    
                    @JsExport
                    public class SealedChild2(
                        `field`: Double
                    ) : SealedLvl2() {
                        internal lateinit var common: CommonSealedChild2
                    
                        init {
                            if (field != dynamicNull) {
                                common = CommonSealedChild2(
                                    field = field.toLong(),
                                )
                            }
                        }
                    
                        public val childField: String
                            get() = common.childField
                    
                        public var `field`: Double
                            get() = common.field.toDouble()
                            set(setValue) {
                                common.field = setValue.toLong()
                            }
                    
                        public override val ctorParam: Double
                            get() = common.ctorParam.toDouble()
                    
                        public override val cause: Throwable?
                            get() = common.cause
                    
                        public override val message: String?
                            get() = common.message
                    
                        @Suppress("UNNECESSARY_SAFE_CALL")
                        internal constructor(common: CommonSealedChild2) :
                                this(field = dynamicNull?.dynamicCastTo<Double>()) {
                            this.common = common
                        }
                    
                        public override fun baz(input: SealedParent): SealedParent {
                            val result = common.baz(
                                input = input.importSealedParent(),
                            )
                            return result.exportSealedParent()
                        }
                    }
                    
                    public fun CommonSealedChild2.exportSealedChild2(): SealedChild2 = SealedChild2(this)
                    
                    public fun SealedChild2.importSealedChild2(): CommonSealedChild2 = this.common
                    """.trimIndent()
                ),
            )
        )
    }
}
