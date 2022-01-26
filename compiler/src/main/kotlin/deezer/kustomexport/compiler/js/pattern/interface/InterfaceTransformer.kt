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

package deezer.kustomexport.compiler.js.pattern.`interface`

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import deezer.kustomexport.compiler.Logger
import deezer.kustomexport.compiler.js.FunctionDescriptor
import deezer.kustomexport.compiler.js.InterfaceDescriptor
import deezer.kustomexport.compiler.js.MethodNameDisambiguation
import deezer.kustomexport.compiler.js.PropertyDescriptor
import deezer.kustomexport.compiler.js.jsExport
import deezer.kustomexport.compiler.js.jsPackage
import deezer.kustomexport.compiler.js.mapping.INDENTATION
import deezer.kustomexport.compiler.js.pattern.asClassName
import deezer.kustomexport.compiler.js.pattern.buildWrappingFunction
import deezer.kustomexport.compiler.js.pattern.overrideGetterSetter
import deezer.kustomexport.compiler.js.pattern.packageName
import deezer.kustomexport.compiler.js.pattern.simpleName
import java.util.Locale

fun InterfaceDescriptor.transform() = transformInterface(this)

fun transformInterface(origin: InterfaceDescriptor): FileSpec {
    val originalClass = origin.asTypeName()

    val delegateName = origin.classSimpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }
    val jsClassPackage = origin.packageName.jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, origin.exportedClassSimpleName)

    val importedClass = ClassName(jsClassPackage, "Imported${origin.exportedClassSimpleName}")
    val exportedClass = ClassName(jsClassPackage, "Exported${origin.exportedClassSimpleName}")

    return FileSpec.builder(jsClassPackage, origin.exportedClassSimpleName)
        .addAliasedImport(origin.asClassName, "Common${origin.classSimpleName}")
        .addType(
            TypeSpec.interfaceBuilder(origin.exportedClassSimpleName)
                .addModifiers(KModifier.EXTERNAL)
                .addAnnotation(jsExport)
                .also { builder ->
                    origin.supers.forEach { supr ->
                        val superType = supr.origin.exportedTypeName
                        if (superType is ClassName || superType is ParameterizedTypeName) {
                            builder.addSuperinterface(superType)
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
                exportedClassSimpleName = origin.exportedClassSimpleName,
                import = true,
                properties = origin.properties,
                functions = origin.functions,
            )
        )
        .addType(
            buildWrapperClass(
                delegateName = "common",
                originalClass = originalClass,
                exportedClassSimpleName = origin.exportedClassSimpleName,
                import = false,
                properties = origin.properties,
                functions = origin.functions,
            )
        )
        .addFunction(
            FunSpec.builder("export${origin.exportedClassSimpleName}")
                .receiver(originalClass)
                .returns(jsExportedClass)
                .addStatement(
                    "return·(this·as?·%T)?.exported·?: %T(this)",
                    importedClass, exportedClass
                )
                .build()
        )
        .addFunction(
            FunSpec.builder("import${origin.exportedClassSimpleName}")
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
    exportedClassSimpleName: String,
): TypeSpec {
    val jsClassPackage = originalClass.packageName().jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, exportedClassSimpleName)/*.let {
        if (typeParametersMap.isNotEmpty()) {
            it.parameterizedBy(typeParametersMap.map { (_, exportedTp) -> exportedTp })
        } else it
    }*/
    val wrapperPrefix = if (import) "Imported" else "Exported"
    val wrapperClass =
        ClassName(jsClassPackage, wrapperPrefix + exportedClassSimpleName)
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
