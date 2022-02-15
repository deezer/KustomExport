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

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import deezer.kustomexport.compiler.js.FormatString
import deezer.kustomexport.compiler.js.FunctionDescriptor
import deezer.kustomexport.compiler.js.MethodNameDisambiguation
import deezer.kustomexport.compiler.js.PropertyDescriptor
import deezer.kustomexport.compiler.js.abortController
import deezer.kustomexport.compiler.js.abortSignal
import deezer.kustomexport.compiler.js.asCoroutinesPromise
import deezer.kustomexport.compiler.js.coroutinesAwait
import deezer.kustomexport.compiler.js.coroutinesContext
import deezer.kustomexport.compiler.js.coroutinesContextJob
import deezer.kustomexport.compiler.js.coroutinesGlobalScope
import deezer.kustomexport.compiler.js.coroutinesPromiseFunc
import deezer.kustomexport.compiler.js.mapping.INDENTATION
import deezer.kustomexport.compiler.js.toFormatString

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
    val returns = if (import) {
        returnType.concreteTypeName
    } else {
        returnType.exportedTypeName
    }
    fb.returns(if (!import && isSuspend) returns.asCoroutinesPromise() else returns)

    if (forceOverride || isOverride) {
        fb.addModifiers(KModifier.OVERRIDE)
    }
    if (import && isSuspend) {
        fb.addModifiers(KModifier.SUSPEND)
    }

    parameters.forEach { param ->
        if (import) {
            fb.addParameter(param.name, param.type.concreteTypeName)
        } else {
            fb.addParameter(param.name, param.type.exportedTypeName)
        }
    }
    if (!import && isSuspend) {
        fb.addParameter("abortSignal", abortSignal)
    }

    if (body) {
        if (!import && isSuspend) {
            fb.addCode("return %T.%M·{\n", coroutinesGlobalScope, coroutinesPromiseFunc)
        }
        if (import && isSuspend) {
            fb.addStatement("val abortController = %T()", abortController)
            fb.addStatement("val abortSignal = abortController.signal")
            fb.addStatement(
                "%M.%M.invokeOnCompletion { abortController.abort() }",
                coroutinesContext,
                coroutinesContextJob
            )
        }
        if (!import && isSuspend) {
            fb.addStatement("abortSignal.onabort = { %M.%M.cancel() }", coroutinesContext, coroutinesContextJob)
        }

        val funcName = if (import) funExportedName else name
        var params = parameters.fold(FormatString("")) { acc, item ->
            acc + "$INDENTATION${item.name} = ".toFormatString() + item.portMethod(!import) + ",\n"
        }
        if (import && isSuspend) {
            params += FormatString("${INDENTATION}abortSignal = abortSignal")
        }

        //TODO: Opti : could save the local "result" variable here
        fb.addCode(
            ("val result = $delegateName.$funcName(".toFormatString() +
                (if (parameters.isNotEmpty()) "\n" else "") +
                params +
                (if (parameters.isNotEmpty()) ")\n" else ")\n")).asCode()
        )

        fb.addCode(
            ((if (import || !isSuspend) "return·" else "").toFormatString() +
                returnType.portMethod(import, "result".toFormatString()) +
                (if (import && isSuspend) ".%M()".toFormatString(coroutinesAwait) else "".toFormatString())).asCode()
        )

        if (!import && isSuspend) fb.addCode("\n}")
    }
    return fb.build()
}

fun overrideGetterSetter(
    prop: PropertyDescriptor,
    target: String,
    import: Boolean,
    isClassOpen: Boolean,
    forceOverride: Boolean, // true for interface
    isClassThrowable: Boolean = false,
): PropertySpec {
    val fieldName = prop.name
    val isExceptionMessage = isClassThrowable && fieldName == "message"
    val fieldClass = when {
        isExceptionMessage -> STRING // Not nullable
        import -> prop.type.concreteTypeName
        else -> prop.type.exportedTypeName
    }
    val setterValueClass = if (import) prop.type.exportedTypeName else prop.type.concreteTypeName

    val getterMappingMethod = prop.type.portMethod(import, "$target.$fieldName".toFormatString())

    val builder = PropertySpec.builder(fieldName, fieldClass)
    if (forceOverride || prop.isOverride) builder.addModifiers(KModifier.OVERRIDE)
    if (isClassOpen) builder.addModifiers(KModifier.OPEN)

    builder.getter(
        FunSpec.getterBuilder()
            .addCode(
                ("return·".toFormatString() + getterMappingMethod + if (isExceptionMessage) " ?: \"\"" else "").asCode()
            )
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
