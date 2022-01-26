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
        KustomGenerics(GenericsStuff::class, arrayOf(Long::class)),
        KustomGenerics(GenericsStuff::class, arrayOf(Float::class), "GenericsStuffFloat"),
        KustomGenerics(GenericsInterface::class, arrayOf(Long::class), "GenericsInterface"),
        KustomGenerics(GenericsInterface::class, arrayOf(Float::class), "GenericsInterfaceFloat"),
        KustomGenerics(SuperGenericsInterface::class, arrayOf(Float::class, Int::class), "SuperGenericsInterfaceFloat"),
        KustomGenerics(GenericsImpl::class, arrayOf(Long::class), "GenericsImpl"),
        KustomGenerics(GenericsImpl::class, arrayOf(Float::class), "GenericsImplFloat"),
    ]
)

package sample.generics

import deezer.kustomexport.KustomExport
import deezer.kustomexport.KustomExportGenerics
import deezer.kustomexport.KustomGenerics
import sample._class.data.DataClass

interface GenericsStuff<Template>

// Not exportable due to generics unresolvable
interface GenericsInterface<Template> {
    var bar: GenericsStuff<Template>?
    fun fooBar(input: Template?) = "fooBar $input"
    fun fooBars(inputs: List<Template>) =
        inputs.joinToString(prefix = "[[", postfix = "]]", separator = " | ") { it.toString() }

    fun addListener(listener: (Long, GenericsStuff<Template>) -> Unit, default: GenericsStuff<Template>)

    // Class from different package (not really type alias related?)
    fun baz() = DataClass("baz data")
}

interface SuperGenericsInterface<Template, SomethingElse> : GenericsInterface<Template> {
    val superFoo: Template
}

class GenericsImpl<Template> {
    var bar: Template? = null
}

@KustomExport
class GenericsFactory {
    fun buildCLong(value: Long) = GenericsImpl<Long>().apply { bar = value }
    fun buildCFloat(value: Float) = GenericsImpl<Float>().apply { bar = value }
}

@KustomExport
class GenericsConsumer {
    fun consumeILong(generic: GenericsInterface<Long>) =
        "consumed " + generic.fooBar(123L) + " / " + generic.fooBars(listOf(1, 2, 3))

    fun consumeIFloat(generic: GenericsInterface<Float>) =
        "consumed " + generic.fooBar(123f) + " / " + generic.fooBars(listOf(1.1f, 2.2f, 3.3f))

    fun consumeCLong(generic: GenericsImpl<Long>) = "consumed " + generic.bar
    fun consumeCFloat(generic: GenericsImpl<Float>) = "consumed " + generic.bar
}

// Trick to export interface with generics type: specific typealias!
// Here we can generate the interface wrapper because type is defined
// (You should not use generics on TypeAlias.)
//@KustomExport
//typealias TypeAliasLong = TypeAliasInterface<Long>
// Unfortunately, typealias are not properly handled by KotlinJs rn
