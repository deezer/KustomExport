package deezer.kustom.compiler

import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import org.junit.Test

@KotlinPoetKspPreview
class GenericsInterfaceTest {

    @Test
    fun genericsInterface() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kustom.KustomExport

            open class GenericsBase

            @KustomExport
            interface InterfaceGenerics<T : GenericsBase> {
                fun generateBase(): T
                fun check(input: T): String
            }
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/InterfaceGenerics.kt",
                content = """
                    
                """.trimIndent()
            )
        )
    }
}
