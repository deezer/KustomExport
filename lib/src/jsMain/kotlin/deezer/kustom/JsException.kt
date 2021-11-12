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

// See documentation in Exception.import
@file:Suppress("NON_EXPORTABLE_TYPE")

// package kotlin cannot be used, so we can't enforce code consistency and need to make it explicit in processor too.
package deezer.kustom

import kotlin.Exception as CommonException
import kotlin.IllegalArgumentException as CommonIllegalArgumentException
import kotlin.IllegalStateException as CommonIllegalStateException
import kotlin.RuntimeException as CommonRuntimeException

// TODO : Error

/**
 * Cannot define this file in jsMain, Kotlin Js-IR limitation?
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
open class Exception(val message: String, val stackTrace: String) {
    // "import": Cannot export common type BUT here we use a Typescript keyword 'import' to avoid
    // typescript method generation, see https://youtrack.jetbrains.com/issue/KT-38262
    // The resulting typescript (with 1.6.0-RC):
    /*
        class Exception {
            constructor(message: string, stackTrace: string);
            readonly message: string;
            readonly stackTrace: string;
            /* ErrorDeclaration: Name is a reserved word */
        }
     */
    // This trick should not be used everywhere, but for Exceptions it's useful
    // to make a method ignored from typescript code generation and offers a nicer API.
    // Hope to have an annotation like @JsIgnore and avoid this trick.
    open fun import() = CommonException(message, Throwable(stackTrace))
}

fun CommonException.export() = Exception(message ?: "", stackTraceToString())

@JsExport
class RuntimeException(message: String, stackTrace: String) : Exception(message, stackTrace) {
    override fun import() = CommonRuntimeException(message, Throwable(stackTrace))
}

fun CommonRuntimeException.export() = RuntimeException(message ?: "", stackTraceToString())

@JsExport
class IllegalArgumentException(message: String, stackTrace: String) : Exception(message, stackTrace) {
    override fun import() = CommonIllegalArgumentException(message, Throwable(stackTrace))
}

fun CommonIllegalArgumentException.export() = IllegalArgumentException(message ?: "", stackTraceToString())

@JsExport
class IllegalStateException(message: String, stackTrace: String) : Exception(message, stackTrace) {
    override fun import() = CommonIllegalStateException(message, Throwable(stackTrace))
}

//fun IllegalStateException.import() = CommonIllegalStateException(message, Throwable(stackTrace))
fun CommonIllegalStateException.export() = IllegalStateException(message ?: "", stackTraceToString())

// TODO : RuntimeException
// TODO : IllegalArgumentException
// TODO : IllegalStateException
// TODO : IndexOutOfBoundsException
// TODO : ConcurrentModificationException
// TODO : UnsupportedOperationException
// TODO : NumberFormatException
// TODO : NullPointerException
// TODO : ClassCastException
// TODO : AssertionError
// TODO : NoSuchElementException
// TODO : ArithmeticException
// TODO : NoWhenBranchMatchedException
// TODO : UninitializedPropertyAccessException
