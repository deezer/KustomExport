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
            import deezer.kustom.KustomExport

            @KustomExport
            class BasicClass
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/BasicClass.kt",
                content = """
        package foo.bar.js

        import kotlin.js.JsExport
        import foo.bar.BasicClass as CommonBasicClass
        
        @JsExport
        public class BasicClass() {
            internal lateinit var common: CommonBasicClass
        
            init {
                common = CommonBasicClass()}
        
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
            import deezer.kustom.KustomExport

            @KustomExport
            class BasicClass(val id: String)
    """,
            ExpectedOutputFile(
                path = "foo/bar/js/BasicClass.kt",
                content = """
        package foo.bar.js

        import kotlin.String
        import kotlin.js.JsExport
        import foo.bar.BasicClass as CommonBasicClass
        
        @JsExport
        public class BasicClass(
            id: String
        ) {
            internal lateinit var common: CommonBasicClass
        
            init {
                if (id != deezer.kmp.dynamicNull) {
                    common = CommonBasicClass(
                        id = id
                    )
                }}
        
            public val id: String
                get() = common.id
        
            internal constructor(common: CommonBasicClass) : this(id = deezer.kmp.dynamicNull) {
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
            
            import deezer.kustom.KustomExport
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
        import kotlin.js.Any
        import kotlin.js.JsExport
        import foo.bar.BasicClass as CommonBasicClass
        
        @JsExport
        public class BasicClass() : Any {
            internal lateinit var common: CommonBasicClass
        
            init {
                common = CommonBasicClass()}
        
            public val str: String
                get() = common.str
        
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
