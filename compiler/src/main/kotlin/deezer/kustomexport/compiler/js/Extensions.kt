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

package deezer.kustomexport.compiler.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import deezer.kustomexport.compiler.CompilerArgs

val jsExport = ClassName("kotlin.js", "JsExport")

val dynamicCastTo = MemberName("deezer.kustomexport", "dynamicCastTo")
val dynamicNull = MemberName("deezer.kustomexport", "dynamicNull")
val dynamicString = MemberName("deezer.kustomexport", "dynamicString")
val dynamicNotString = MemberName("deezer.kustomexport", "dynamicNotString")

fun String.jsPackage() = if (CompilerArgs.erasePackage) "" else "$this.js"
