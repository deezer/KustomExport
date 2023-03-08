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

import com.squareup.kotlinpoet.*
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
    suppress: (String) -> Unit = {}
): TypeName {
    val returns = if (import) {
        returnType.concreteTypeName
    } else {
        returnType.exportedTypeName
    }
    return if (!import && isSuspend) {
        // https://youtrack.jetbrains.com/issue/KT-57192/KJS-IR-JsExport-PromiseUnit-wrongly-produces-non-exportable-type-warning
        if (returns == UNIT) {
            suppress("NON_EXPORTABLE_TYPE")
        }
        returns.asCoroutinesPromise()
    } else returns
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
    fb.returns(returnType(import) { fb.suppress(it) })

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
                functionName = if (import) funExportedName else name,
                returnsUnit = returnType.concreteTypeName == UNIT
            ).asCode()
        )
    }
    return fb.build()
}

fun FunctionDescriptor.buildWrappingFunctionBody(
    import: Boolean,
    receiver: String?,
    functionName: String,
    returnsUnit: Boolean
): FormatString {
    var body = "".toFormatString()
    val indent = if (!import && isSuspend) {
        body += "@OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)\n"
        body += "return %T.%M·{\n".toFormatString(coroutinesGlobalScope, coroutinesPromiseFunc)
        INDENTATION
    } else ""
    if (import && isSuspend) {
        body += "val abortController = %T()\n".toFormatString(abortController)
        body += "val abortSignal = abortController.signal\n"
        body += "%M.%M.invokeOnCompletion { abortController.abort() }\n".toFormatString(
            coroutinesContext, coroutinesContextJob
        )
    }
    if (!import && isSuspend) {
        body += "${indent}abortSignal.onabort = { %M.%M.cancel() }\n".toFormatString(coroutinesContext, coroutinesContextJob)
        body += "${indent}if (abortSignal.aborted) { %M.%M.cancel() }\n".toFormatString(coroutinesContext, coroutinesContextJob)
    }

    var params = parameters.fold(FormatString("")) { acc, item ->
        acc + "${indent}$INDENTATION${item.name} = ".toFormatString() + item.portMethod(!import) + ",\n"
    }
    if (import && isSuspend) {
        params += FormatString("${indent}${INDENTATION}abortSignal = abortSignal")
    }

    //TODO: Opti : could save the local "result" variable here
    val callStr = if (receiver == null) functionName else "$receiver.$functionName"
    body += if (returnsUnit) "${indent}$callStr(" else "${indent}val result = $callStr("
    body += if (parameters.isNotEmpty()) "\n" else ""
    body += params
    body += if (parameters.isNotEmpty()) "${indent})" else ")"

    body += if (import && isSuspend) "${indent}.%M()\n".toFormatString(coroutinesAwait) else "\n".toFormatString()

    if (!returnsUnit) {
        body += if (import || !isSuspend) "${indent}return " else indent
        body += returnType.portMethod(import, "result".toFormatString())
        body += "\n"
    }

    if (!import && isSuspend) {
        body += "}"
    }
    return body
}

fun overrideGetterSetter(
    prop: PropertyDescriptor,
    target: String,
    import: Boolean,
    isClassOpen: Boolean,
    forceOverride: Boolean, // true for interface
): PropertySpec {
    val fieldName = prop.name
    val fieldClass = when {
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
                ("return·".toFormatString() + getterMappingMethod).asCode()
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
