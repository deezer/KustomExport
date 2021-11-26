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

package deezer.kustom.compiler.js.pattern.`interface`

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.js.FunctionDescriptor
import deezer.kustom.compiler.js.InterfaceDescriptor
import deezer.kustom.compiler.js.MethodNameDisambiguation
import deezer.kustom.compiler.js.PropertyDescriptor
import deezer.kustom.compiler.js.jsExport
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.mapping.INDENTATION
import deezer.kustom.compiler.js.pattern.buildWrappingFunction
import deezer.kustom.compiler.js.pattern.overrideGetterSetter
import deezer.kustom.compiler.js.pattern.packageName
import deezer.kustom.compiler.js.pattern.simpleName
import java.util.Locale

fun InterfaceDescriptor.transform() = transformInterface(this)

fun transformInterface(origin: InterfaceDescriptor): FileSpec {
    val originalClass = origin.asTypeName()

    val delegateName = origin.classSimpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }
    val jsClassPackage = origin.packageName.jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, origin.classSimpleName)

    val importedClass = ClassName(jsClassPackage, "Imported${origin.classSimpleName}")
    val exportedClass = ClassName(jsClassPackage, "Exported${origin.classSimpleName}")

    return FileSpec.builder(jsClassPackage, origin.classSimpleName)
        .addAliasedImport(origin.asClassName, "Common${origin.classSimpleName}")
        //.autoImport(origin, origin.concreteTypeParameters)
        .addType(
            TypeSpec.interfaceBuilder(origin.classSimpleName) // ClassName(jsClassPackage, origin.classSimpleName).parameterizedBy(origin.generics.values.first()))
                /*.also { b ->
                    typeParametersMap.map { (_, exported) ->
                        b.addTypeVariable(TypeVariableName(exported))
                    }
                }*/
                .addModifiers(KModifier.EXTERNAL)
                .addAnnotation(jsExport)
                .also { builder ->
                    origin.supers.forEach { supr ->
                        val superType = supr.origin.concreteTypeName
                        if (superType is ClassName) {
                            val superClassName = ClassName(superType.packageName.jsPackage(), superType.simpleName)
                            builder.addSuperinterface(superClassName)
                        } else {
                            Logger.error("ClassTransformer - ${origin.classSimpleName} superTypes - ClassName($jsClassPackage, $superType)")
                        }
                    }

                    origin.properties.filter { it.isOverride.not() }.forEach { prop ->
                        val modifiers = if (prop.isOverride) listOf(KModifier.OVERRIDE) else emptyList()
                        builder.addProperty(
                            PropertySpec.builder(prop.name, prop.type.exportedTypeName, modifiers)
                                .mutable(prop.isMutable)
                                .build()
                        )
                    }

                    val mnd = MethodNameDisambiguation()
                    origin.functions
                        .filter { !it.isOverride }
                        .forEach { func ->
                            builder.addFunction(
                                func.buildWrappingFunction(
                                    body = false,
                                    import = false,
                                    delegateName = delegateName,
                                    mnd = mnd,
                                    isClassOpen = false, // already an interface, so "open" is not required
                                )
                            )
                        }
                }
                .build()
        )
        .addType(
            buildWrapperClass(
                delegateName = "exported",
                originalClass = originalClass,
                import = true,
                properties = origin.properties,
                functions = origin.functions,
            )
        )
        .addType(
            buildWrapperClass(
                delegateName = "common", // delegateName,
                originalClass = originalClass,
                import = false,
                properties = origin.properties,
                functions = origin.functions,
            )
        )
        .addFunction(
            FunSpec.builder("export${origin.classSimpleName}")
                .receiver(originalClass)
                .returns(jsExportedClass)
                .addStatement(
                    "return·(this·as?·%T)?.exported·?: %T(this)",
                    importedClass, exportedClass
                )
                .build()
        )
        .addFunction(
            FunSpec.builder("import${origin.classSimpleName}")
                .receiver(jsExportedClass)
                .returns(originalClass)
                .addStatement(
                    "return·(this·as?·%T)?.common·?: %T(this)",
                    exportedClass, importedClass
                )
                .build()
        )
        .indent(INDENTATION)
        .build()
}

private fun buildWrapperClass(
    delegateName: String,
    originalClass: TypeName,
    import: Boolean,
    properties: List<PropertyDescriptor>,
    functions: List<FunctionDescriptor>,
): TypeSpec {
    val jsClassPackage = originalClass.packageName().jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, originalClass.simpleName())/*.let {
        if (typeParametersMap.isNotEmpty()) {
            it.parameterizedBy(typeParametersMap.map { (_, exportedTp) -> exportedTp })
        } else it
    }*/
    val wrapperPrefix = if (import) "Imported" else "Exported"
    val wrapperClass =
        ClassName(jsClassPackage, wrapperPrefix + originalClass.simpleName())
    val delegatedClass = if (import) jsExportedClass else originalClass
    val superClass = if (import) originalClass else jsExportedClass

    return TypeSpec.classBuilder(wrapperClass)
        .addModifiers(KModifier.PRIVATE)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(delegateName, delegatedClass, KModifier.INTERNAL)
                .build()
        )
        .addProperty(PropertySpec.builder(delegateName, delegatedClass).initializer(delegateName).build())
        .addSuperinterface(superClass)
        .also { builder ->
            properties.forEach { prop ->
                // forceOverride = true because only used by interface right now
                builder.addProperty(
                    overrideGetterSetter(
                        prop,
                        delegateName,
                        import,
                        forceOverride = true,
                        isClassOpen = false, // already an interface, so "open" is not required
                    )
                )
            }

            val mnd = MethodNameDisambiguation()
            functions.forEach { func ->
                builder.addFunction(
                    func.buildWrappingFunction(
                        body = true,
                        import = import,
                        delegateName = delegateName,
                        mnd = mnd,
                        forceOverride = true,
                        isClassOpen = false, // already an interface, so "open" is not required
                    )
                )
            }
        }
        .build()
}
