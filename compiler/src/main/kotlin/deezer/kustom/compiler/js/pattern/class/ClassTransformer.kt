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

package deezer.kustom.compiler.js.pattern.`class`

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.js.ALL_KOTLIN_EXCEPTIONS
import deezer.kustom.compiler.js.ClassDescriptor
import deezer.kustom.compiler.js.MethodNameDisambiguation
import deezer.kustom.compiler.js.dynamicCastTo
import deezer.kustom.compiler.js.jsExport
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.mapping.INDENTATION
import deezer.kustom.compiler.js.pattern.asClassName
import deezer.kustom.compiler.js.pattern.autoImport
import deezer.kustom.compiler.js.pattern.buildWrappingFunction
import deezer.kustom.compiler.js.pattern.overrideGetterSetter

fun ClassDescriptor.transform() = transformClass(this)

fun transformClass(origin: ClassDescriptor): FileSpec {
    val originalClass = origin.asTypeName()

    val jsClassPackage = origin.packageName.jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, origin.classSimpleName)

    // Primary constructor should respect the original class signature, and creates a 'common' instance.
    // Kotlin forces the 2nd ctor to call the 1st one, problematic in this case.
    // Tricking the primary constructor to avoid creating a common when it's not required.
    // For that, the 2nd constructor pass dynamic values as parameters for the 1st ctor.
    // The 2nd constructor also assigns 'common', but it's called later so can't be used as marker.
    // So in the init block we check if the params injected are actually the dynamic one
    // so we can determine if it's the 1st ctor (need to create instance) or the 2nd (no need).
    // If there is no ctor params, then we can't avoid a duplicated common object creation.
    val firstCtorParam = origin.constructorParams.firstOrNull()
    val ctorDyn = when {
        firstCtorParam == null -> null
        !firstCtorParam.type.concreteTypeName.isNullable -> "deezer.kustom.dynamicNull"
        firstCtorParam.type.concreteTypeName != STRING -> "deezer.kustom.dynamicString"
        else -> "deezer.kustom.dynamicNotString"
    }

    if (origin.concreteTypeParameters.isNotEmpty()) {
        Logger.error("ClassTransformer - ${origin.classSimpleName} superTypes - generics=${origin.concreteTypeParameters}")
    }

    // The field containing the 'common' instance have to be internal to not be exported BUT be visible from extension method.
    // Due to that limitation, when class is open, we need to define a "random" name to avoid conflicts.
    val commonFieldName = if (origin.isOpen) "common_" + origin.classIdHash else "common"

    return FileSpec.builder(jsClassPackage, origin.classSimpleName)
        .addAliasedImport(origin.asClassName(), "Common${origin.classSimpleName}")
        .autoImport(origin, origin.concreteTypeParameters)
        .addImport(ClassName("deezer", "kustom"), "dynamicCastTo")
        .addType(
            TypeSpec.classBuilder(origin.classSimpleName)
                .addAnnotation(jsExport)
                .addModifiers(if (origin.isOpen) listOf(KModifier.OPEN) else emptyList())
                .primaryConstructor(
                    /**
                     * Primary constructor have to contain the commonMain instance,
                     * but it cannot be exported
                     */
                    FunSpec.constructorBuilder()
                        .also { b ->
                            origin.constructorParams.forEach {
                                b.addParameter(ParameterSpec(it.name, it.type.exportedTypeName))
                            }
                        }
                        .build()
                )
                .addFunction(
                    FunSpec.constructorBuilder()
                        .addModifiers(KModifier.INTERNAL)
                        .callThisConstructor(
                            CodeBlock.of(
                                origin.constructorParams.joinToString { "${it.name}·=·$ctorDyn?.%M<%T>()" },
                                *origin.constructorParams.flatMap { listOf(dynamicCastTo, it.type.exportedTypeName) }
                                    .toTypedArray()
                            )
                        )
                        .addParameter(ParameterSpec("common", originalClass))
                        .addStatement("this.$commonFieldName = common")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder(commonFieldName, originalClass, KModifier.INTERNAL)
                        .also { if (origin.constructorParams.isNotEmpty()) it.addModifiers(KModifier.LATEINIT) }
                        .mutable(true) // because lateinit
                        .build()
                )
                .addInitializerBlock(
                    CodeBlock.of(
                        if (firstCtorParam == null) {
                            "$commonFieldName = Common${origin.classSimpleName}()\n"
                        } else {
                            """
                            |if (${firstCtorParam.name} != $ctorDyn) {
                            |${INDENTATION}$commonFieldName = Common${origin.classSimpleName}(${
                                (
                                    origin.constructorParams.joinToString(
                                        ",\n$INDENTATION$INDENTATION",
                                        prefix = "\n$INDENTATION$INDENTATION",
                                        postfix = "\n"
                                    ) { it.name + "·=·" + it.importedMethod }
                                    )
                            }$INDENTATION)
                            |}
                            |
                            """.trimMargin()
                        }
                    )
                )
                .also { b ->
                    origin.supers.forEach { supr ->
                        if (supr.parameters == null) {
                            b.addSuperinterface(supr.origin.exportedTypeName)
                        } else {
                            // sealed and exceptions are not wrapped as the other classes
                            if (supr.isSealed || supr.origin.concreteTypeName in ALL_KOTLIN_EXCEPTIONS) {
                                b.superclass(supr.origin.exportedTypeName)
                                b.addSuperclassConstructorParameter(
                                    CodeBlock.of(supr.parameters.joinToString { it.name + " = " + it.name })
                                )
                            } else {
                                // other wrappers have an additional constructor by us, we can trick to avoid RAM usage
                                b.superclass(supr.origin.exportedTypeName)
                                b.addSuperclassConstructorParameter(
                                    CodeBlock.of(
                                        "common = %M.%M<${supr.origin.concreteTypeName.asClassName().canonicalName}>()",
                                        MemberName("deezer.kustom", "dynamicNull"),
                                        MemberName("deezer.kustom", "dynamicCastTo"),
                                    )
                                )
                            }
                        }
                    }
                }
                .also { b ->
                    origin.properties
                        // Don't export fields only present in super implementation
                        // .filterNot { p -> origin.supers.any { s -> s.parameters?.any { it.name == p.name } ?: false } }
                        .forEach {
                            b.addProperty(
                                overrideGetterSetter(
                                    it,
                                    commonFieldName,
                                    import = false,
                                    forceOverride = false,
                                    isClassOpen = origin.isOpen,
                                )
                            )
                        }
                }
                .also { b ->

                    val mnd = MethodNameDisambiguation()
                    origin.functions.forEach { func ->
                        b.addFunction(
                            func.buildWrappingFunction(
                                body = true,
                                import = false,
                                delegateName = commonFieldName,
                                mnd = mnd,
                                isClassOpen = origin.isOpen
                            )
                        )
                    }
                }
                .build()
        )
        .addFunction(
            FunSpec.builder("export${origin.classSimpleName}")
                .receiver(originalClass)
                .returns(jsExportedClass)
                .addStatement("return ${origin.classSimpleName}(this)")
                .build()
        )
        .addFunction(
            FunSpec.builder("import${origin.classSimpleName}")
                .receiver(jsExportedClass)
                .returns(originalClass)
                .addStatement("return this.$commonFieldName")
                .build()
        )
        .indent(INDENTATION)
        .build()
}
