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

class ExportEnumTest {

    @Test
    fun basicEnum() {
        assertCompilationOutput(
            """
            package foo.bar
            
            import deezer.kustomexport.KustomExport

            @KustomExport
            enum class Season {
                SPRING,
                SUMMER,
                AUTUMN,
                WINTER
            }
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/Season.kt",
                content = """
                package foo.bar.js
                
                import kotlin.Array
                import kotlin.String
                import kotlin.js.JsExport
                import foo.bar.Season as CommonSeason
                
                @JsExport
                public class Season internal constructor(
                    internal val common: CommonSeason
                ) {
                    public val name: String = common.name
                }
                
                @JsExport
                public fun Season_values(): Array<Season> = arrayOf(Season_SPRING, Season_SUMMER, Season_AUTUMN,
                        Season_WINTER)
                
                @JsExport
                public fun Season_valueOf(name: String): Season? {
                    if (name == Season_SPRING.name)
                        return Season_SPRING
                
                    if (name == Season_SUMMER.name)
                        return Season_SUMMER
                
                    if (name == Season_AUTUMN.name)
                        return Season_AUTUMN
                
                    if (name == Season_WINTER.name)
                        return Season_WINTER
                
                    return null
                }
                
                public fun Season.importSeason(): CommonSeason = common
                
                public fun CommonSeason.exportSeason(): Season = Season_valueOf(this.name)!!
                
                @JsExport
                public val Season_SPRING: Season = Season(CommonSeason.SPRING)
                
                @JsExport
                public val Season_SUMMER: Season = Season(CommonSeason.SUMMER)
                
                @JsExport
                public val Season_AUTUMN: Season = Season(CommonSeason.AUTUMN)
                
                @JsExport
                public val Season_WINTER: Season = Season(CommonSeason.WINTER)
                """.trimIndent()
            )
        )
    }
}
