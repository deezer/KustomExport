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

import com.squareup.kotlinpoet.CodeBlock

class FormatString(val format: String, vararg args: Any?) {
    val argsArray: Array<out Any?> = args

    operator fun plus(other: FormatString): FormatString =
        FormatString(format + other.format, *arrayOf(*argsArray, *other.argsArray))

    operator fun plus(others: Collection<FormatString>) =
        others.fold(this) { acc, formatString -> acc + formatString }

    operator fun plus(other: String): FormatString =
        FormatString(format + other, *argsArray)

    fun asCode() = CodeBlock.of(format, *argsArray)

    fun eq(str: String) = argsArray.isEmpty() && format == str

    override fun toString(): String {
        error("You cannot format the string yourself, it's Kotlinpoet responsibility. " +
            "You may want to use `format`, `argsArray`, or `toCodeBlock()` instead.")
    }
}

fun String.eq(formatString: FormatString) = formatString.argsArray.isEmpty() && formatString.format == this

fun String.toFormatString(vararg args: Any?) = FormatString(this, *args)
