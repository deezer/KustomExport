/*
 * Copyright 2022 Deezer.
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

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.UNIT
import deezer.kustomexport.compiler.js.TopLevelFunctionDescriptor
import deezer.kustomexport.compiler.js.jsExport
import deezer.kustomexport.compiler.js.jsPackage

fun TopLevelFunctionDescriptor.transform() = transformFunction(this)

fun transformFunction(origin: TopLevelFunctionDescriptor): FileSpec {
    val function = origin.function
    val commonFunctionName = "common${function.name.replaceFirstChar { it.titlecase() }}"
    val functionMemberName = MemberName(origin.packageName, function.name)
    val jsClassPackage = origin.packageName.jsPackage()

    val funBuilder = FunSpec.builder(function.name)
    funBuilder
        .addAnnotation(jsExport)
        .returns(function.returnType(false) { funBuilder.suppress(it) })
        .addParameters(function.parameters, false, function.isSuspend)
    funBuilder.addCode(
        function.buildWrappingFunctionBody(
            import = false,
            receiver = null,
            functionName = commonFunctionName,
            returnsUnit = function.returnType.concreteTypeName == UNIT
        ).asCode()
    )

    return FileSpec.builder(jsClassPackage, function.name)
        .addAliasedImport(functionMemberName, commonFunctionName)
        .addFunction(funBuilder.build())
        .build()
}