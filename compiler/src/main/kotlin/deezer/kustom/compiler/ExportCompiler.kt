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
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import deezer.kustom.compiler.js.ClassDescriptor
import deezer.kustom.compiler.js.EnumDescriptor
import deezer.kustom.compiler.js.InterfaceDescriptor
import deezer.kustom.compiler.js.SealedClassDescriptor
import deezer.kustom.compiler.js.pattern.`class`.transform
import deezer.kustom.compiler.js.pattern.`interface`.transform
import deezer.kustom.compiler.js.pattern.enum.transform
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
            resolver.getSymbolsWithAnnotation(
                annotationName = environment.options["annotation"] ?: "deezer.kustom.KustomExport",
                inDepth = true
            )
        } catch (e: Exception) {
            devLog("WTF? ${e.message} // ${e.stackTraceToString()}")
            return emptyList()
        }

        val passId = Random.nextLong()
        devLog("passId: $passId - symbols: ${symbols.count()} - first: ${symbols.firstOrNull()?.location}")

        symbols
            //.filter { it is KSClassDeclaration || it is KSTypeAlias /*&& it.validate()*/ }
            .forEach {
                devLog("----- Symbol $it")
                it.accept(ExportVisitor(), Unit)
                //it.accept(LoggerVisitor(environment), Unit)
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
            //devLog("----- visitClassDeclaration $classDeclaration - classKind = ${classDeclaration.classKind}")
            when (val descriptor = parseClass(classDeclaration)) {
                is ClassDescriptor -> descriptor.transform()
                    .writeCode(environment, classDeclaration.containingFile!!)
                is SealedClassDescriptor -> descriptor.transform()
                    .writeCode(environment, classDeclaration.containingFile!!)
                is EnumDescriptor -> descriptor.transform()
                    .writeCode(environment, classDeclaration.containingFile!!)
                is InterfaceDescriptor -> descriptor.transform()
                    .writeCode(environment, classDeclaration.containingFile!!)
                null -> {
                    // Cannot parse this class, parsing error already reported on the parser
                }
            }
        }

        override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit) {
            Logger.warn("visitTypeAlias !!!")
            val target = (typeAlias.type.element?.parent as? KSTypeReference)?.resolve() ?: return
            Logger.warn("visitTypeAlias resolved")
            // targetClassDeclaration is templated
            val targetClassDeclaration = target.declaration as? KSClassDeclaration ?: return


            Logger.warn(typeAlias.toString() + " = " + typeAlias.name.asString()) // TypeAliasLong (probably name.asString() too)

            //val resolver2 =
            //(typeAlias.type.resolve().declaration as KSClassDeclaration).typeParameters.toTypeParameterResolver()

            // Contains "Template" list
            val targetTypeParameters = targetClassDeclaration.typeParameters
            val targetTypeNames = typeAlias.type.element?.typeArguments
                ?.map { it.type!!.resolve().toClassName() }
                ?.mapIndexed { index, className -> targetTypeParameters[index].name.asString() to className }
                ?: return
            if (targetTypeParameters.size != targetTypeNames.size) return
            /*
            val map: Map<String, TypeVariableName> = targetTypeParameters.mapIndexed { index, ksTypeParameter ->
                ksTypeParameter.name.asString() to TypeVariableName(
                    ksTypeParameter.name.asString(),
                    targetTypeNames[index]
                )
            }.toMap()

            // Creating custom parent type resolver
            val parentTypeParameters = object : TypeParameterResolver {
                override val parametersMap: Map<String, TypeVariableName> = map

                override operator fun get(index: String): TypeVariableName = map[index] ?: error("nah")
            }*/
            val descriptor = parseClass(targetClassDeclaration, targetTypeNames)
            Logger.warn("TypeAlias parsed to $descriptor")
            when (descriptor) {
                is ClassDescriptor -> descriptor.transform()
                    .writeCode(environment, typeAlias.containingFile!!)
                is SealedClassDescriptor -> descriptor.transform()
                    .writeCode(environment, typeAlias.containingFile!!)
                is EnumDescriptor -> descriptor.transform()
                    .writeCode(environment, typeAlias.containingFile!!)
                is InterfaceDescriptor -> descriptor.transform()
                    .writeCode(environment, typeAlias.containingFile!!)
            }

            /*
            val decl = typeAlias.type.resolve().declaration
            Logger.warn(decl.packageName.asString()) //
            Logger.warn(decl.simpleName.asString()) // TypeAliasInterface
            Logger.warn("decl.typeParameters=" + decl.typeParameters.joinToString { it.packageName.asString() + " - " + it.simpleName.asString() + "(${it.qualifiedName?.asString()})" })
            Logger.warn("typeAlias.typeParameters=" + typeAlias.typeParameters.joinToString { it.packageName.asString() + " - " + it.simpleName.asString() + "(${it.qualifiedName?.asString()})" })
            //Logger.error("DONE")
            */
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
