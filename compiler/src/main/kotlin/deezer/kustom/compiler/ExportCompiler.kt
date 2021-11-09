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

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import deezer.kustom.compiler.js.ClassDescriptor
import deezer.kustom.compiler.js.EnumDescriptor
import deezer.kustom.compiler.js.InterfaceDescriptor
import deezer.kustom.compiler.js.pattern.`class`.transform
import deezer.kustom.compiler.js.pattern.enum.transform
import deezer.kustom.compiler.js.pattern.`interface`.transform
import deezer.kustom.compiler.js.pattern.parseClass
import kotlin.random.Random

// Trick to share the Logger everywhere without injecting the dependency everywhere
internal lateinit var sharedLogger: KSPLogger

internal object Logger : KSPLogger by sharedLogger

internal object CompilerArgs {
    var erasePackage: Boolean = false
}

@KotlinPoetKspPreview
class ExportCompiler(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    init {
        sharedLogger = environment.logger
    }

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        CompilerArgs.erasePackage = environment.options["erasePackage"] == "true"
        val symbols = try {
            resolver.getSymbolsWithAnnotation(environment.options["annotation"] ?: "deezer.kustom.KustomExport")
        } catch (e: Exception) {
            devLog("WTF? ${e.message} // ${e.stackTraceToString()}")
            return emptyList()
        }

        val passId = Random.nextLong()
        devLog("passId: $passId - symbols: ${symbols.count()} - first: ${symbols.firstOrNull()?.location}")

        // ------------------------------------------------------------------------------------------------
        // Hack to avoid compilation on Android/iOS : create a dummy file, then check generated file path
        // https://github.com/google/ksp/issues/641
        /*
        symbols.firstOrNull()?.accept(
            object : KSVisitorVoid() {
                // For some reasons on KSP 1.5.31-1.0 not using the Visitor pattern lead to gradle freeze
                override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
                    devLog("Creating the placeholder...")
                    FileSpec.builder("", "placeholder")
                        .build().writeCode(environment)
                }
            },
            Unit
        )
        val generatedPath = environment.codeGenerator.generatedFile.firstOrNull().toString()
        val isJsBuild = generatedPath.contains("/jsMain/")
        // Please don't ask why there is multiple possible values here, I've no clue, but it's working
        val isUnitTest =
            generatedPath.contains("/T/junit") || generatedPath.contains("/T/Kotlin-Compilation") || generatedPath == "null"
        devLog("isJsBuild=$isJsBuild isUnitTest=$isUnitTest generatedPath=$generatedPath")
        if (!isUnitTest && !isJsBuild) return emptyList() // Disable compilation
        */
        // ------------------------------------------------------------------------------------------------

        symbols
            .filter { it is KSClassDeclaration /*&& it.validate()*/ }
            .forEach {
                devLog("----- Symbol $it")
                it.accept(ExportVisitor(), Unit)
            }

        /*return symbols.filter { !it.validate() }.toList()
            .also { list ->
                list.forEach {
                    devLog("Cannot handle $it $it.")
                }
            }*/
        return emptyList()
    }

    @KotlinPoetKspPreview
    inner class ExportVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            devLog("----- visitClassDeclaration $classDeclaration - classKind = ${classDeclaration.classKind}")
            when (val descriptor = parseClass(classDeclaration)) {
                is ClassDescriptor -> descriptor.transform()
                    .writeCode(environment, classDeclaration.containingFile!!)
                is EnumDescriptor -> descriptor.transform()
                    .writeCode(environment, classDeclaration.containingFile!!)
                is InterfaceDescriptor -> descriptor.transform()
                    .writeCode(environment, classDeclaration.containingFile!!)
            }
        }
    }

    fun devLog(msg: String) {
        println(msg) // for unit tests
        environment.logger.warn(msg) // when compiling other modules
    }
}

@KotlinPoetKspPreview
class ExportCompilerProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        ExportCompiler(environment)
}
