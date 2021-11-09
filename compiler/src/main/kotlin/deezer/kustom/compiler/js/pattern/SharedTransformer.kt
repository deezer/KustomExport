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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import deezer.kustom.compiler.CompilerArgs
import deezer.kustom.compiler.js.FunctionDescriptor
import deezer.kustom.compiler.js.MethodNameDisambiguation
import deezer.kustom.compiler.js.PropertyDescriptor
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.mapping.TypeMapping
import deezer.kustom.compiler.js.mapping.TypeMapping.exportedType

fun FunctionDescriptor.buildWrappingFunction(
    body: Boolean,
    import: Boolean,
    delegateName: String,
    mnd: MethodNameDisambiguation,
    forceOverride: Boolean = false, // TODO: rework that shortcut for testing...
    typeParametersMap: List<Pair<TypeVariableName, TypeVariableName>> = emptyList()
): FunSpec {
    val funExportedName = mnd.getMethodName(this)

    val fb = FunSpec.builder(if (!import) funExportedName else name)
    if (returnType is TypeVariableName) {
        if (import) {
            fb.returns(returnType)
        } else {
            fb.returns(typeParametersMap.first { (origin, exported) -> returnType == origin }.second)
        }
    } else
        fb.returns(if (import) returnType else exportedType(returnType))

    if (forceOverride || isOverride) {
        fb.addModifiers(KModifier.OVERRIDE)
    }

    parameters.forEach { param ->
        if (param.type is TypeVariableName) {
            if (import) {
                fb.addParameter(param.name, param.type)
            } else {
                fb.addParameter(param.name, typeParametersMap.first { (origin, _) -> param.type == origin }.second)
            }
        } else {
            fb.addParameter(param.name, if (import) param.type else exportedType(param.type))
        }
    }

    if (body) {
        val funcName = if (import) funExportedName else name
        fb.addStatement(
            "val result = $delegateName.$funcName(" +
                (if (parameters.isNotEmpty()) "\n" else "") +
                parameters.joinToString(",\n", transform = {
                    it.name + " = " +
                        (if (import) TypeMapping.exportMethod(it.name, it.type)
                        else TypeMapping.importMethod(it.name, it.type)) +
                        (if (it.type is TypeVariableName) {
                            " as " + if (import) typeParametersMap.first().second.name else typeParametersMap.first().first.name
                        } else "")

                }) +
                (if (parameters.isNotEmpty()) "" else ")")
        )
        if (parameters.isNotEmpty())
            fb.addStatement(")")
        fb.addStatement(
            "return " +
                (if (import) TypeMapping.importMethod("result", returnType)
                else TypeMapping.exportMethod("result", returnType)) +
                (if (returnType is TypeVariableName) {
                    " as " + if (import) typeParametersMap.first().first.name else typeParametersMap.first().second.name
                } else "")
        )
    }
    return fb.build()
}

fun buildWrapperClass(
    delegateName: String,
    originalClass: TypeName,
    typeParameters: Map<String, TypeVariableName>,
    import: Boolean,
    properties: List<PropertyDescriptor>,
    functions: List<FunctionDescriptor>,
): TypeSpec {
    val typeParametersMap = typeParameters.map { (_, value) ->
        value to TypeVariableName("__" + value.name, value.bounds.map { exportedType(it) })
    }
    val allTypeParameters = typeParametersMap.flatMap { (origin, exported) -> listOf(origin, exported) }

    val jsClassPackage = if (CompilerArgs.erasePackage) "" else originalClass.packageName().jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, originalClass.simpleName()).let {
        if (typeParameters.isNotEmpty()) {
            it.parameterizedBy(typeParametersMap.map { (_, exportedTp) -> exportedTp })
        } else it
    }
    val wrapperPrefix = if (import) "Imported" else "Exported"
    val wrapperClass =
        ClassName(jsClassPackage, wrapperPrefix + originalClass.simpleName())
    val delegatedClass = if (import) jsExportedClass else originalClass
    val superClass = if (import) originalClass else jsExportedClass


    return TypeSpec.classBuilder(wrapperClass)
        .addModifiers(KModifier.PRIVATE)
        .also { b ->
            allTypeParameters.forEach {
                b.addTypeVariable(it)
            }
        }
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
                builder.addProperty(overrideGetterSetter(prop, delegateName, import, forceOverride = true))
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
                        typeParametersMap = typeParametersMap,
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
    val exportedType = TypeMapping.exportedType(prop.type)
    val fieldClass = if (import) prop.type else exportedType
    val setterValueClass = if (import) exportedType else prop.type

    val getterMappingMethod =
        if (import) TypeMapping.importMethod(
            "$target.$fieldName",
            prop.type
        ) else TypeMapping.exportMethod("$target.$fieldName", prop.type)
    val setterMappingMethod =
        if (import) TypeMapping.exportMethod(fieldName, prop.type) else TypeMapping.importMethod(fieldName, prop.type)

    val modifiers = if (forceOverride || prop.isOverride) listOf(KModifier.OVERRIDE) else emptyList()

    return PropertySpec.builder(fieldName, fieldClass, modifiers)
        // .getter(FunSpec.getterBuilder().addCode("$target.$fieldName").build())
        // One-line version `get() = ...` is less verbose
        .getter(
            FunSpec.getterBuilder()
                .addStatement("return $getterMappingMethod")
                .build()
        )
        .also { builder ->
            if (prop.isMutable) {
                builder
                    .mutable()
                    .setter(
                        FunSpec.setterBuilder()
                            .addParameter(fieldName, setterValueClass)
                            .addStatement("$target.$fieldName = $setterMappingMethod")
                            .build()
                    )
            }
        }
        .build()
}
