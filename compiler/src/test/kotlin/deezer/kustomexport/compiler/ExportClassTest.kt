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

class ExportClassTest {

    @Test
    fun classWithNoVal() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustomexport.KustomExport

            @KustomExport
            class BasicClass
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/BasicClass.kt",
                content = """
                package foo.bar.js
                
                import kotlin.Suppress
                import kotlin.js.JsExport
                import foo.bar.BasicClass as CommonBasicClass
                
                @JsExport
                public class BasicClass() {
                    internal var common: CommonBasicClass
                
                    init {
                        common = CommonBasicClass()
                    }
                
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    internal constructor(common: CommonBasicClass) : this() {
                        this.common = common
                    }
                }
                
                public fun CommonBasicClass.exportBasicClass(): BasicClass = BasicClass(this)
                
                public fun BasicClass.importBasicClass(): CommonBasicClass = this.common
                """.trimIndent()
            )
        )
    }

    @Test
    fun classWithOneVal() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustomexport.KustomExport

            @KustomExport
            class BasicClass(val id: String)
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/BasicClass.kt",
                content = """
                package foo.bar.js
                
                import deezer.kustomexport.dynamicCastTo
                import deezer.kustomexport.dynamicNull
                import kotlin.String
                import kotlin.Suppress
                import kotlin.js.JsExport
                import foo.bar.BasicClass as CommonBasicClass
                
                @JsExport
                public class BasicClass(
                    id: String
                ) {
                    internal lateinit var common: CommonBasicClass
                
                    init {
                        if (id != dynamicNull) {
                            common = CommonBasicClass(
                                id = id,
                            )
                        }
                    }
                
                    public val id: String
                        get() = common.id
                
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    internal constructor(common: CommonBasicClass) :
                            this(id = dynamicNull?.dynamicCastTo<String>()) {
                        this.common = common
                    }
                }
                
                public fun CommonBasicClass.exportBasicClass(): BasicClass = BasicClass(this)
                
                public fun BasicClass.importBasicClass(): CommonBasicClass = this.common
                """.trimIndent()
            )
        )
    }

    @Test
    fun classWithoutOurAnnotation() {
        assertCompilationOutput(
            """
            package foo.bar
            import another.Export

            @KustomExport
            class BasicClass(val id: String)
    """
        )
    }

    @Test
    fun classGeneratedShouldKeepSuperTypes() {
        assertCompilationOutput(
            """
            package foo.bar
            
            import deezer.kustomexport.KustomExport
            import kotlin.Any

            @KustomExport
            class BasicClass: Any {
                override val str: String
            }
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/BasicClass.kt",
                content = """
                package foo.bar.js
                
                import kotlin.String
                import kotlin.Suppress
                import kotlin.js.JsExport
                import foo.bar.BasicClass as CommonBasicClass
                
                @JsExport
                public class BasicClass() {
                    internal var common: CommonBasicClass
                
                    init {
                        common = CommonBasicClass()
                    }
                
                    public val str: String
                        get() = common.str
                
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    internal constructor(common: CommonBasicClass) : this() {
                        this.common = common
                    }
                }
                
                public fun CommonBasicClass.exportBasicClass(): BasicClass = BasicClass(this)
                
                public fun BasicClass.importBasicClass(): CommonBasicClass = this.common
                """.trimIndent()
            )
        )
    }
}
