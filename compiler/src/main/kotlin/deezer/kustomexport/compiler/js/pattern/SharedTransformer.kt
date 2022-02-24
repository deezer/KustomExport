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
import com.squareup.kotlinpoet.TypeName
import deezer.kustomexport.compiler.js.FormatString
import deezer.kustomexport.compiler.js.FunctionDescriptor
import deezer.kustomexport.compiler.js.MethodNameDisambiguation
import deezer.kustomexport.compiler.js.ParameterDescriptor
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

enum class OverrideMode { FORCE_OVERRIDE, FORCE_NO_OVERRIDE, AUTO }

fun FunctionDescriptor.returnType(
    import: Boolean,
): TypeName {
    val returns = if (import) {
        returnType.concreteTypeName
    } else {
        returnType.exportedTypeName
    }
    return if (!import && isSuspend) returns.asCoroutinesPromise() else returns
}

fun FunSpec.Builder.addParameters(
    params: List<ParameterDescriptor>,
    import: Boolean,
    isSuspend: Boolean
): FunSpec.Builder {
    params.forEach { param ->
        if (import) {
            addParameter(param.name, param.type.concreteTypeName)
        } else {
            addParameter(param.name, param.type.exportedTypeName)
        }
    }
    if (!import && isSuspend) {
        addParameter("abortSignal", abortSignal)
    }
    return this
}

//TODO: Rework and cut that spaghetti design (too much params / different use cases)
fun FunctionDescriptor.buildWrappingFunction(
    body: Boolean,
    import: Boolean,
    delegateName: String?,
    mnd: MethodNameDisambiguation,
    isClassOpen: Boolean,
    overrideMode: OverrideMode = OverrideMode.AUTO, // TODO: rework that shortcut for testing...
): FunSpec {
    val funExportedName = mnd.getMethodName(this)

    val fb = FunSpec.builder(if (!import) funExportedName else name)
    if (isClassOpen) fb.addModifiers(KModifier.OPEN)
    fb.returns(returnType(import))

    if (overrideMode != OverrideMode.FORCE_NO_OVERRIDE &&
        (overrideMode == OverrideMode.FORCE_OVERRIDE || isOverride)
    ) {
        fb.addModifiers(KModifier.OVERRIDE)
    }
    if (import && isSuspend) {
        fb.addModifiers(KModifier.SUSPEND)
    }

    fb.addParameters(parameters, import, isSuspend)

    if (body) {

        fb.addCode(
            buildWrappingFunctionBody(
                import = import,
                receiver = delegateName,
                functionName = if (import) funExportedName else name
            ).asCode()
        )
    }
    return fb.build()
}

fun FunctionDescriptor.buildWrappingFunctionBody(
    import: Boolean,
    receiver: String?,
    functionName: String
): FormatString {
    var body = "".toFormatString()
    if (!import && isSuspend) {
        body += "return %T.%M·{\n".toFormatString(coroutinesGlobalScope, coroutinesPromiseFunc)
    }
    if (import && isSuspend) {
        body += "val abortController = %T()\n".toFormatString(abortController)
        body += "val abortSignal = abortController.signal\n"
        body += "%M.%M.invokeOnCompletion { abortController.abort() }\n".toFormatString(
            coroutinesContext, coroutinesContextJob
        )
    }
    if (!import && isSuspend) {
        body += "abortSignal.onabort = { %M.%M.cancel() }\n".toFormatString(coroutinesContext, coroutinesContextJob)
    }

    var params = parameters.fold(FormatString("")) { acc, item ->
        acc + "$INDENTATION${item.name} = ".toFormatString() + item.portMethod(!import) + ",\n"
    }
    if (import && isSuspend) {
        params += FormatString("${INDENTATION}abortSignal = abortSignal")
    }

    //TODO: Opti : could save the local "result" variable here
    val callStr = if (receiver == null) "$functionName" else "$receiver.$functionName"
    body += "val result = $callStr("
    body += if (parameters.isNotEmpty()) "\n" else ""
    body += params
    body += if (parameters.isNotEmpty()) ")\n" else ")\n"

    body += if (import || !isSuspend) "return·" else ""
    body += returnType.portMethod(import, "result".toFormatString())
    body += if (import && isSuspend) ".%M()".toFormatString(coroutinesAwait) else "".toFormatString()

    if (!import && isSuspend) body += "\n}"
    return body
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
