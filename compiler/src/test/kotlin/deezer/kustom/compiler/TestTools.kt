package deezer.kustom.compiler

import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.fail

data class InputFile(val path: String, val content: String)
data class ExpectedOutputFile(val path: String, val content: String)

@KotlinPoetKspPreview
fun assertCompilationOutput(fileContent: String, vararg files: ExpectedOutputFile) {
    assertCompilationOutput(listOf(InputFile("Main.kt", fileContent)), files.asList())
}

@KotlinPoetKspPreview
fun assertCompilationOutput(inputFiles: List<InputFile>, expectedOutputFiles: List<ExpectedOutputFile>) {
    val compilation = compile(inputFiles.map { SourceFile.kotlin(it.path, it.content) })
    expectedOutputFiles.forEach { expectedFile ->
        val targetPath = compilation.kspSourcesDir.path + "/kotlin/" + expectedFile.path
        val path = Path(targetPath)
        if (path.exists()) {
            // Java8:
            val generatedContent = Files.readAllLines(path).joinToString("\n")
            // Java11: (also includes last empty line = closer to actual output)
            // val generatedContent = Files.readString(path)
            assertEquals(expectedFile.content, generatedContent)
        } else {
            println("--------")
            println(compilation.kspSourcesDir.walkTopDown().joinToString())
            println("--------")
            fail("Expected a file at path $targetPath but nothing was there.")
        }
    }
}

@KotlinPoetKspPreview
private fun compile(
    sourceFiles: List<SourceFile>,
    expectedExitCode: KotlinCompilation.ExitCode = KotlinCompilation.ExitCode.OK
): KotlinCompilation {
    val compilation = prepareCompilation(sourceFiles)
    val result = compilation.compile()
    assertEquals(expectedExitCode, result.exitCode, result.messages)
    return compilation
}

@KotlinPoetKspPreview
private fun prepareCompilation(sourceFiles: List<SourceFile>): KotlinCompilation {
    return KotlinCompilation()
        .apply {
            inheritClassPath = true // Required so that the compiled code also see the annotation
            symbolProcessorProviders = listOf(ExportCompilerProvider())
            sources = sourceFiles
        }
}
