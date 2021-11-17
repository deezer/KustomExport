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

package ex

import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.js.JsExport
import sample.type_alias.TypeAliasInterface as CommonTypeAliasInterface

@JsExport
public external interface TypeAliasInterface<T> {
    public fun fooBar(input: Double): String
}

private class ImportedTypeAliasInterface(
    internal val exported: sample.type_alias.TypeAliasInterface<Double>
) : CommonTypeAliasInterface<Long> {
    public override fun fooBar(input: Long): String {
        val result = exported.fooBar(
                input = input.toDouble()
        )
        return result
    }
}

private class ExportedTypeAliasInterface(
    internal val common: CommonTypeAliasInterface<Long>
) : sample.type_alias.TypeAliasInterface<Double> {
    public override fun fooBar(input: Double): String {
        val result = common.fooBar(
                input = input.toLong()
        )
        return result
    }
}

public fun CommonTypeAliasInterface<Long>.exportTypeAliasInterface(): sample.type_alias.TypeAliasInterface<Double> = (this as?
    ImportedTypeAliasInterface)?.exported ?: ExportedTypeAliasInterface(this)

public fun sample.type_alias.TypeAliasInterface<Double>.importTypeAliasInterface(): CommonTypeAliasInterface<Long> = (this as?
    ExportedTypeAliasInterface)?.common ?: ImportedTypeAliasInterface(this)
