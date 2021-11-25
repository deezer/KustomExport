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

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import deezer.kustom.compiler.js.FormatString
import deezer.kustom.compiler.js.FunctionDescriptor
import deezer.kustom.compiler.js.MethodNameDisambiguation
import deezer.kustom.compiler.js.PropertyDescriptor
import deezer.kustom.compiler.js.mapping.INDENTATION
import deezer.kustom.compiler.js.toFormatString

fun FunctionDescriptor.buildWrappingFunction(
    body: Boolean,
    import: Boolean,
    delegateName: String,
    mnd: MethodNameDisambiguation,
    isClassOpen: Boolean,
    forceOverride: Boolean = false, // TODO: rework that shortcut for testing...
): FunSpec {
    val funExportedName = mnd.getMethodName(this)

    val fb = FunSpec.builder(if (!import) funExportedName else name)
    if (isClassOpen) fb.addModifiers(KModifier.OPEN)
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
        val params = parameters.fold(FormatString("")) { acc, item ->
            acc + "$INDENTATION${item.name} = ".toFormatString() + item.portMethod(!import) + ",\n"
        }
        //TODO: Opti : could save the local "result" variable here
        fb.addCode(
            ("val result = $delegateName.$funcName(".toFormatString() +
                (if (parameters.isNotEmpty()) "\n" else "") +
                params +
                (if (parameters.isNotEmpty()) ")\n" else ")\n")).asCode()
        )

        fb.addCode(("return ".toFormatString() + returnType.portMethod(import, "result".toFormatString())).asCode())
    }
    return fb.build()
}

fun overrideGetterSetter(
    prop: PropertyDescriptor,
    target: String,
    import: Boolean,
    isClassOpen: Boolean,
    forceOverride: Boolean // true for interface
): PropertySpec {
    val fieldName = prop.name
    val fieldClass = if (import) prop.type.concreteTypeName else prop.type.exportedTypeName
    val setterValueClass = if (import) prop.type.exportedTypeName else prop.type.concreteTypeName

    val getterMappingMethod = prop.type.portMethod(import, "$target.$fieldName".toFormatString())

    val builder = PropertySpec.builder(fieldName, fieldClass)
    if (forceOverride || prop.isOverride) builder.addModifiers(KModifier.OVERRIDE)
    if (isClassOpen) builder.addModifiers(KModifier.OPEN)

    builder.getter(
        FunSpec.getterBuilder()
            .addCode(("return ".toFormatString() + getterMappingMethod).asCode())
            .build()
    )
    if (prop.isMutable) {
        val setValName = "setValue" // fieldName
        val setterMappingMethod = prop.type.portMethod(!import, setValName.toFormatString())

        builder
            .mutable()
            .setter(
                FunSpec.setterBuilder()
                    .addParameter(setValName, setterValueClass)
                    .addCode(("$target.$fieldName = ".toFormatString() + setterMappingMethod).asCode())
                    .build()
            )
    }
    return builder.build()
}
