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

package deezer.kustom.compiler.js.pattern

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import deezer.kustom.compiler.js.FunctionDescriptor
import deezer.kustom.compiler.js.MethodNameDisambiguation
import deezer.kustom.compiler.js.PropertyDescriptor
import deezer.kustom.compiler.js.jsPackage

fun FunctionDescriptor.buildWrappingFunction(
    body: Boolean,
    import: Boolean,
    delegateName: String,
    mnd: MethodNameDisambiguation,
    forceOverride: Boolean = false, // TODO: rework that shortcut for testing...
): FunSpec {
    val funExportedName = mnd.getMethodName(this)

    val fb = FunSpec.builder(if (!import) funExportedName else name)
        .addModifiers(KModifier.OPEN) // Allow inheritance of wrapped classes
    if (import) {
        fb.returns(returnType.concreteTypeName)
    } else {
        fb.returns(returnType.exportedTypeName)
    }

    if (forceOverride || isOverride) {
        fb.addModifiers(KModifier.OVERRIDE)
    }

    parameters.forEach { param ->
        if (import) {
            fb.addParameter(param.name, param.type.concreteTypeName)
        } else {
            fb.addParameter(param.name, param.type.exportedTypeName)
        }
    }

    if (body) {
        val funcName = if (import) funExportedName else name
        fb.addStatement(
            "val result = $delegateName.$funcName(" +
                (if (parameters.isNotEmpty()) "\n" else "") +
                parameters.joinToString(",\n", transform = {
                    it.name + " = " + it.portMethod(!import)
                }) +
                (if (parameters.isNotEmpty()) "" else ")")
        )
        if (parameters.isNotEmpty())
            fb.addStatement(")")

        fb.addStatement("return " + returnType.portMethod(import, "result"))
    }
    return fb.build()
}

fun buildWrapperClass(
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
        /*.also { b ->
            allTypeParameters.forEach {
                b.addTypeVariable(it)
            }
        }*/
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
                        forceOverride = true
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
                    )
                )
            }
        }
        .build()
}

fun overrideGetterSetter(
    prop: PropertyDescriptor,
    target: String,
    import: Boolean,
    forceOverride: Boolean // true for interface
): PropertySpec {
    val fieldName = prop.name
    val fieldClass = if (import) prop.type.concreteTypeName else prop.type.exportedTypeName
    val setterValueClass = if (import) prop.type.exportedTypeName else prop.type.concreteTypeName

    val isStackTraceException = prop.name == "stackTrace" // Forbidden word! :/
    val getterMappingMethod =
        if (isStackTraceException) "$target.stackTraceToString()"
        else prop.type.portMethod(import, "$target.$fieldName")

    val modifiers = if (forceOverride || prop.isOverride) listOf(KModifier.OVERRIDE) else emptyList()
    val builder = PropertySpec.builder(fieldName, fieldClass, modifiers)
        .addModifiers(KModifier.OPEN) // Allow inheritance for wrapper classes
    // .getter(FunSpec.getterBuilder().addCode("$target.$fieldName").build())
    // One-line version `get() = ...` is less verbose
    // .initializer(fieldName)

    builder.getter(
        FunSpec.getterBuilder()
            .addStatement("return $getterMappingMethod")
            .build()
    )
    if (prop.isMutable) {
        val setValName = "setValue" // fieldName
        val setterMappingMethod = prop.type.portMethod(!import, setValName)

        builder
            .mutable()
            .setter(
                FunSpec.setterBuilder()
                    .addParameter(setValName, setterValueClass)
                    .addStatement("$target.$fieldName = $setterMappingMethod")
                    .build()
            )
    }
    return builder.build()
}
