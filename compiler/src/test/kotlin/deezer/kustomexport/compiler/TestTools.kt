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
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.intellij.lang.annotations.Language
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.fail

data class InputFile(val path: String, @Language("kotlin") val content: String)
data class ExpectedOutputFile(val path: String, @Language("kotlin") val content: String)

@KotlinPoetKspPreview
fun assertCompilationOutput(@Language("kotlin") fileContent: String, vararg files: ExpectedOutputFile) {
    assertCompilationOutput(listOf(InputFile("Main.kt", fileContent)), files.asList())
}

@KotlinPoetKspPreview
fun assertCompilationOutput(inputFiles: List<InputFile>, expectedOutputFiles: List<ExpectedOutputFile>) {
    // Requires to add the annotation here
    val kustomExport = SourceFile.kotlin(
        name = "KustomExport.kt",
        contents = """
            package deezer.kustom
            internal annotation class KustomExport
        """.trimIndent()
    )

    val compilation = compile(inputFiles.map { SourceFile.kotlin(it.path, it.content) } + kustomExport)
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
            fail(
                "Expected a file at path $targetPath but nothing was there.\n" +
                    "Files:\n" +
                    compilation.kspSourcesDir.walkTopDown().joinToString("\n")
            )
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
