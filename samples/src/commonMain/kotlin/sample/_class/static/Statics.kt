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
//import sample.generics.GenericsImpl

fun createString() = "string from factory method"

@KustomExport
object StaticFactory {
    fun create(stuff: Long) = "string from factory object stuff=$stuff"
}

/* See generic
object StaticGenericFactory {
    fun <T> create(): GenericsImpl<T> {
        return GenericsImpl<T>()
    }
}
 */