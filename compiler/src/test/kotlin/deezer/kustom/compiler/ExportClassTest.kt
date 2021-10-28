package deezer.kustom.compiler

import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import org.junit.Test

@KotlinPoetKspPreview
class ExportClassTest {

    @Test
    fun classWithNoVal() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kmp.Export

            @Export
            class BasicClass
    """, ExpectedOutputFile(
                path = "foo/bar/js/BasicClass.kt",
                content = """
        package foo.bar.js

        import kotlin.js.JsExport
        import foo.bar.BasicClass as CommonBasicClass
        
        @JsExport
        public class BasicClass()
        
        public fun CommonBasicClass.export() = BasicClass(
        
        )
        
        public fun BasicClass.`import`() = CommonBasicClass(
        
        )
    """.trimIndent()
            )
        )
    }

    @Test
    fun classWithOneVal() {
        assertCompilationOutput(
            """
            package foo.bar
            import deezer.kmp.Export

            @Export
            class BasicClass(val id: String)
    """, ExpectedOutputFile(
                path = "foo/bar/js/BasicClass.kt",
                content = """
        package foo.bar.js

        import kotlin.String
        import kotlin.js.JsExport
        import foo.bar.BasicClass as CommonBasicClass
        
        @JsExport
        public class BasicClass(
            public val id: String
        )
        
        public fun CommonBasicClass.export() = BasicClass(
            id
        )
        
        public fun BasicClass.`import`() = CommonBasicClass(
            id
        )
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

            @Export
            class BasicClass(val id: String)
    """
        )
    }


    @Test
    fun classGeneratedShouldKeepSuperTypes() {
        assertCompilationOutput(
            """
            package foo.bar
            
            import deezer.kmp.Export
            import kotlin.Any

            @Export
            class BasicClass: Any {
                override val str: String
            }
    """, ExpectedOutputFile(
                path = "foo/bar/js/BasicClass.kt",
                content = """
        package foo.bar.js

        import kotlin.String
        import kotlin.js.JsExport
        import foo.bar.BasicClass as CommonBasicClass
        
        @JsExport
        public class BasicClass(
            public override val str: String
        ) : Any
        
        public fun CommonBasicClass.export() = BasicClass(
            str
        )
        
        public fun BasicClass.`import`() = CommonBasicClass(
            str
        )
    """.trimIndent()
            )
        )
    }

}