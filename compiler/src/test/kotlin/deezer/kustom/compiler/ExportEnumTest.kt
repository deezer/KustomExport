package deezer.kustom.compiler

import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import org.junit.Test

@KotlinPoetKspPreview
class ExportEnumTest {

    @Test
    fun basicEnum() {
        assertCompilationOutput(
            """
            package foo.bar
            
            import deezer.kmp.Export

            @Export
            enum class Season {
                SPRING,
                SUMMER,
                AUTUMN,
                WINTER
            }
    """, ExpectedOutputFile(
                path = "foo/bar/js/Season.kt",
                content = """
        package foo.bar.js

        import kotlin.String
        import kotlin.js.JsExport
        import foo.bar.Season as CommonSeason

        @JsExport
        public class Season internal constructor(
            internal val `value`: CommonSeason
        ) {
            public val name: String = value.name
        }

        public fun Season.`import`() = value

        public fun CommonSeason.export() = Season(this)

        @JsExport
        public object Seasons {
            public val SPRING: Season = CommonSeason.SPRING.export()

            public val SUMMER: Season = CommonSeason.SUMMER.export()

            public val AUTUMN: Season = CommonSeason.AUTUMN.export()

            public val WINTER: Season = CommonSeason.WINTER.export()
        }
    """.trimIndent()
            )
        )
    }
}