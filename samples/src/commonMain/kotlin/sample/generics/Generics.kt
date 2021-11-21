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
        KustomGenerics("NameNotHonoredYet", GenericsStuff::class, arrayOf(Long::class)),
        KustomGenerics("NameNotHonoredYet", GenericsInterface::class, arrayOf(Long::class)),
    ]
)

package sample.generics

import deezer.kustom.KustomExport
import deezer.kustom.KustomExportGenerics
import deezer.kustom.KustomGenerics
import sample._class.data.DataClass

interface GenericsStuff<Template>

// Not exportable due to generics unresolvable
interface GenericsInterface<Template> {
    fun fooBar(input: Template) = "fooBar $input"
    fun fooBars(inputs: List<Template>) =
        inputs.joinToString(prefix = "[[", postfix = "]]", separator = " | ") { it.toString() }

    fun addListener(listener: (Long, GenericsStuff<Template>) -> Unit, default: GenericsStuff<Template>)

    // Class from different package (not really type alias related?)
    fun baz() = DataClass("baz data")
}

// Concrete classes with generics cannot be generated without failing the compilation
// TODO: https://github.com/google/ksp/issues/731
/*
class GenericsInterfaceDefault<T> : GenericsInterface<T> {
    override fun addListener(listener: (Long, T) -> Unit, default: T) {
        listener(33, default)
    }
}
 */

@KustomExport
class GenericsConsumer {
    fun consume(typeAlias: GenericsInterface<Long>) =
        "consumed " + typeAlias.fooBar(123L) + " / " + typeAlias.fooBars(listOf(1, 2, 3))

    fun create(): GenericsInterface<Long> = TODO()//TypeAliasInterfaceDefault()
}

// Trick to export interface with generics type: specific typealias!
// Here we can generate the interface wrapper because type is defined
// (You should not use generics on TypeAlias.)
//@KustomExport
//typealias TypeAliasLong = TypeAliasInterface<Long>
// Unfortunately, typealias are not properly handled by KotlinJs rn
