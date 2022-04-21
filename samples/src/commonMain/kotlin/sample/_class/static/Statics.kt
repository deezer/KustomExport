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

@file:KustomExportGenerics(
    exportGenerics = [
        //KustomGenerics(StaticGenericFactory::class, arrayOf(Long::class)),
    ]
)

package sample._class.static

import deezer.kustomexport.KustomExport
import deezer.kustomexport.KustomExportGenerics

fun createString() = "string from factory method"

@KustomExport
object StaticFactory {
    fun create(stuff: Long) = "string from factory object stuff=$stuff"
}



/**
 * NOT SUPPORTED: Currently KustomExport cannot handle generics functions.
 *
 * When an annotation like `KustomGenerics(StaticGenericFactory::class, arrayOf(Long::class))` is found,
 * it resolves the class type parameters with the given parameter (here [Long]), but the class doesn't need it.
 * The generic functions can actually be used with multiple types so if we want to support it, we should handle
 * multiple custom types for each method, generating as many functions as needed, on the wrappers.
 * Also be able to split generics from the typed class and from the typed methods.
 *
 * ```kotlin
 * class Foo<T : Any> {
 *   fun <T, V> bar(t: T, v: V) { // T from here is not the T from the class
 *   }
 * }
 * Foo<Int>().bar("a", "b") // Here "T" is a Int at the class level, and a String at the method level.
 * ```
 * So it can be tedious to provide a good solution for that (waiting for more adopters, not sure if we need
 * this level of tooling right now...)
 */
/*
object StaticGenericFactory {
fun <T> create(): GenericsImpl<T> {
return GenericsImpl<T>()
}
}
 */