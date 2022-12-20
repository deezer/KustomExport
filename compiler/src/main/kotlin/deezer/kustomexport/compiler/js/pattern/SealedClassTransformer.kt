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

package deezer.kustomexport.compiler.js.pattern

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import deezer.kustomexport.compiler.js.SealedClassDescriptor
import deezer.kustomexport.compiler.js.jsExport
import deezer.kustomexport.compiler.js.jsPackage
import deezer.kustomexport.compiler.js.mapping.INDENTATION

fun SealedClassDescriptor.transform() = transformSealedClass(this)

fun transformSealedClass(origin: SealedClassDescriptor): FileSpec {
    val jsClassPackage = origin.packageName.jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, origin.classSimpleName)

    // Impossible to get the value from a constructor to another. Ex:
    // class Foo(): Bar(33) // Cannot retrieve 33 as it's only available at runtime
    // So instead we create an empty constructor and all properties are abstract
    val ctorParams = origin.constructorParams.map {
        ParameterSpec(it.name, it.type.exportedTypeName)
    }
    val ctor = FunSpec.constructorBuilder()
//        .addParameters(ctorParams)
        .build()

    var properties = origin.properties
        .filter { !it.isOverride } // we're already inheriting the base class so method is already visible
        .map { p ->
            PropertySpec.builder(p.name, p.type.exportedTypeName)
                .addModifiers(KModifier.ABSTRACT)
                .build()
        }

    val functions = origin.functions.map {
        val params = it.parameters.map { p ->
            ParameterSpec.builder(p.name, p.type.exportedTypeName)
                .build()
        }
        val funSpec = FunSpec.builder(it.name)
            .addModifiers(KModifier.ABSTRACT)
            .addParameters(params)
            .returns(it.returnType.exportedTypeName)
        if (it.isOverride) {
            funSpec.addModifiers(KModifier.OVERRIDE)
        }
        funSpec.build()
    }

    val exportFunSpec = FunSpec.builder("export${origin.classSimpleName}")
        .receiver(origin.asClassName)
        .returns(jsExportedClass)
        .beginControlFlow("return·when·(this)")
        .also {
            origin.subClasses.forEach { subClass ->
                it.addStatement("$INDENTATION${INDENTATION}is %T -> export${subClass.classSimpleName}()", subClass.asClassName)
            }
            it.addStatement("$INDENTATION${INDENTATION}else -> error(\"Cannot export \$this\")")
            if (origin.subClasses.isEmpty()) {
                it.addComment("TODO: no subclasses found, bad configuration?")
            }
        }
        .endControlFlow()
        .build()

    val importFunSpec = FunSpec.builder("import${origin.classSimpleName}")
        .receiver(jsExportedClass)
        .returns(origin.asClassName)
        .beginControlFlow("return·when·(this)")
        .also {
            origin.subClasses.forEach { subClass ->
                val exportedSubClass = ClassName(subClass.packageName.jsPackage(), subClass.classSimpleName)
                it.addStatement("$INDENTATION${INDENTATION}is %T -> import${subClass.classSimpleName}()", exportedSubClass)
            }
            it.addStatement("$INDENTATION${INDENTATION}else -> error(\"Cannot export \$this\")")
            if (origin.subClasses.isEmpty()) {
                it.addComment("TODO: no subclasses found, bad configuration?")
            }
        }
        .endControlFlow()
        .build()

    return FileSpec.builder(jsClassPackage, origin.classSimpleName)
        .addAliasedImport(origin.asClassName, "Common${origin.classSimpleName}")
        .also {
            origin.subClasses.forEach { subClass ->
                it.addAliasedImport(subClass.asClassName, "Common${subClass.classSimpleName}")
            }
        }
        .addType(
            TypeSpec.classBuilder(origin.classSimpleName)
                .addAnnotation(jsExport)
                .addModifiers(KModifier.ABSTRACT) // Not SEALED, because https://youtrack.jetbrains.com/issue/KT-39193
                .also { b ->
                    origin.supers.forEach { supr ->
                        if (supr.parameters == null) {
                            b.addSuperinterface(supr.origin.exportedTypeName)
                        } else {
                            b.superclass(supr.origin.exportedTypeName)
                            b.addSuperclassConstructorParameter(
                                CodeBlock.of(supr.parameters.joinToString { it.name + " = " + it.name })
                            )
                        }
                    }
                }
                .primaryConstructor(ctor)
                .addProperties(properties)
                .addFunctions(functions)
                .build()
        )
        .addFunction(exportFunSpec)
        .addFunction(importFunSpec)
        .build()
}
