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

package sample.type_alias

import deezer.kustom.KustomExport

// Not exportable due to generics unresolvable
interface TypeAliasInterface<Template> {
    fun fooBar(input: Template) = "fooBar $input"
    fun fooBars(inputs: List<Template>) =
        inputs.joinToString(prefix = "[[", postfix = "]]", separator = " | ") { it.toString() }
}

class TypeAliasInterfaceDefault<T> : TypeAliasInterface<T>

@KustomExport
class TypeAliasConsumer {
    fun consume(typeAlias: TypeAliasInterface<Long>) =
        "consumed " + typeAlias.fooBar(123L)

    fun create(): TypeAliasInterface<Long> = TypeAliasInterfaceDefault()
}


// Trick to export interface with generics type: specific typealias!
// Here we can generate the interface wrapper because type is defined
// (You should not use generics on TypeAlias.)
@KustomExport
typealias TypeAliasLong = TypeAliasInterface<Long>
