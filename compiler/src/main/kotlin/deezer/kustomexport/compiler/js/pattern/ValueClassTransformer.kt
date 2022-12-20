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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeAliasSpec
import deezer.kustomexport.compiler.js.ValueClassDescriptor
import deezer.kustomexport.compiler.js.jsPackage
import deezer.kustomexport.compiler.js.toFormatString

fun ValueClassDescriptor.transform() = transformValueClass(this)

fun transformValueClass(origin: ValueClassDescriptor): FileSpec {
    val jsClassPackage = origin.packageName.jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, origin.classSimpleName)

    val exportFunSpec = FunSpec.builder("export${origin.classSimpleName}")
        .receiver(origin.asClassName)
        .returns(jsExportedClass)
        .addCode(
            ("return·".toFormatString() +
                origin.inlinedType.type.exportedMethod("this.${origin.inlinedType.name}".toFormatString())
                ).asCode()
        )
        .build()
    val importFunSpec = FunSpec.builder("import${origin.classSimpleName}")
        .receiver(jsExportedClass)
        .returns(origin.asClassName)
        .addCode(
            ("return·%T(".toFormatString(origin.asClassName) +
                origin.inlinedType.type.importedMethod("this".toFormatString())
                + ")"
                ).asCode()
        )
        .build()

    return FileSpec.builder(jsClassPackage, origin.classSimpleName)
        .addAliasedImport(origin.asClassName, "Common${origin.classSimpleName}")
        .addTypeAlias(
            TypeAliasSpec.builder(origin.classSimpleName, origin.inlinedType.type.exportedTypeName).build()
        )
        .addFunction(exportFunSpec)
        .addFunction(importFunSpec)
        .build()
}