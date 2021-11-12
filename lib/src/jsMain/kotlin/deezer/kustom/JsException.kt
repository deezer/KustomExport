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
@file:OptIn(ExperimentalJsExport::class)
// package kotlin cannot be used, so we can't enforce code consistency and need to make it explicit in processor too.
package deezer.kustom

import kotlin.ArithmeticException as CommonArithmeticException
import kotlin.AssertionError as CommonAssertionError
import kotlin.ClassCastException as CommonClassCastException
import kotlin.ConcurrentModificationException as CommonConcurrentModificationException
import kotlin.Error as CommonError
import kotlin.Exception as CommonException
import kotlin.IllegalArgumentException as CommonIllegalArgumentException
import kotlin.IllegalStateException as CommonIllegalStateException
import kotlin.IndexOutOfBoundsException as CommonIndexOutOfBoundsException
import kotlin.NoSuchElementException as CommonNoSuchElementException
import kotlin.NullPointerException as CommonNullPointerException
import kotlin.NumberFormatException as CommonNumberFormatException
import kotlin.RuntimeException as CommonRuntimeException
import kotlin.UnsupportedOperationException as CommonUnsupportedOperationException

/**
 * Cannot define this file in jsMain, Kotlin Js-IR limitation?
 */
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
open class Error(val message: String, val stackTrace: String) {
    open fun import() = CommonError(message, Throwable(stackTrace))
}

fun CommonError.export() = Error(message ?: "", stackTraceToString())

@JsExport
open class RuntimeException(message: String, stackTrace: String) : Exception(message, stackTrace) {
    override fun import() = CommonRuntimeException(message, Throwable(stackTrace))
}

fun CommonRuntimeException.export() = RuntimeException(message ?: "", stackTraceToString())

@JsExport
open class IllegalArgumentException(message: String, stackTrace: String) : RuntimeException(message, stackTrace) {
    override fun import() = CommonIllegalArgumentException(message, Throwable(stackTrace))
}

fun CommonIllegalArgumentException.export() = IllegalArgumentException(message ?: "", stackTraceToString())

@JsExport
class IllegalStateException(message: String, stackTrace: String) : RuntimeException(message, stackTrace) {
    override fun import() = CommonIllegalStateException(message, Throwable(stackTrace))
}

fun CommonIllegalStateException.export() = IllegalStateException(message ?: "", stackTraceToString())

@JsExport
class IndexOutOfBoundsException(message: String, stackTrace: String) : RuntimeException(message, stackTrace) {
    override fun import() = CommonIndexOutOfBoundsException(message + "\n" + stackTrace)
}

fun CommonIndexOutOfBoundsException.export() = IndexOutOfBoundsException(message ?: "", stackTraceToString())

@JsExport
class ConcurrentModificationException(message: String, stackTrace: String) : RuntimeException(message, stackTrace) {
    override fun import() = CommonConcurrentModificationException(message, Throwable(stackTrace))
}

fun CommonConcurrentModificationException.export() =
    ConcurrentModificationException(message ?: "", stackTraceToString())

@JsExport
class UnsupportedOperationException(message: String, stackTrace: String) : RuntimeException(message, stackTrace) {
    override fun import() = CommonUnsupportedOperationException(message, Throwable(stackTrace))
}

fun CommonUnsupportedOperationException.export() = UnsupportedOperationException(message ?: "", stackTraceToString())

@JsExport
class NumberFormatException(message: String, stackTrace: String) : IllegalArgumentException(message, stackTrace) {
    override fun import() = CommonNumberFormatException(message + "\n" + stackTrace)
}

fun CommonNumberFormatException.export() = NumberFormatException(message ?: "", stackTraceToString())

@JsExport
class NullPointerException(message: String, stackTrace: String) : RuntimeException(message, stackTrace) {
    override fun import() = CommonNullPointerException(message + "\n" + stackTrace)
}

fun CommonNullPointerException.export() = NullPointerException(message ?: "", stackTraceToString())

@JsExport
class ClassCastException(message: String, stackTrace: String) : RuntimeException(message, stackTrace) {
    override fun import() = CommonClassCastException(message + "\n" + stackTrace)
}

fun CommonClassCastException.export() = ClassCastException(message ?: "", stackTraceToString())

@JsExport
class AssertionError(message: String, stackTrace: String) : Error(message, stackTrace) {
    override fun import() = CommonAssertionError(message, Throwable(stackTrace))
}

fun CommonAssertionError.export() = AssertionError(message ?: "", stackTraceToString())

@JsExport
class NoSuchElementException(message: String, stackTrace: String) : RuntimeException(message, stackTrace) {
    override fun import() = CommonNoSuchElementException(message + "\n" + stackTrace)
}

fun CommonNoSuchElementException.export() = NoSuchElementException(message ?: "", stackTraceToString())

@JsExport
class ArithmeticException(message: String, stackTrace: String) : RuntimeException(message, stackTrace) {
    override fun import() = CommonArithmeticException(message + "\n" + stackTrace)
}

fun CommonArithmeticException.export() = ArithmeticException(message ?: "", stackTraceToString())
