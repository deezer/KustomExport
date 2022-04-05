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

package sample._value

import deezer.kustomexport.KustomExport

@KustomExport
value class NotEmptyString(internal val str: String) {
    init {
        require(str.isNotEmpty())
    }
}

@KustomExport
value class PositiveLong(internal val value: Long) {
    init {
        require(value >= 0)
    }

    // Functions will NOT be exported (please create an issue if you're interested in this feature).
    fun double(): Float = value * 2f
}

@KustomExport
class ValueClassConsumer {

    var nes: NotEmptyString = NotEmptyString("default")

    fun consume(nes: NotEmptyString): String {
        return nes.str
    }

    fun consume(pl: PositiveLong): String {
        return pl.value.toString()
    }
}