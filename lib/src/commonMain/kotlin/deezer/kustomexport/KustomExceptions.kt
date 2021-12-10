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

// KSP issue is limiting JS generation at the moment
// https://github.com/google/ksp/issues/728
// As a hack, we generate JS facade for KotlinMetadata instead, so we define this expect/actual pattern
// Eventually we should move these classes in jsMain.

// See documentation in Exception.import
@file:Suppress("NON_EXPORTABLE_TYPE")
@file:OptIn(ExperimentalJsExport::class)
// package kotlin cannot be used, so we can't enforce code consistency and need to make it explicit in processor too.
package deezer.kustomexport

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.ArithmeticException as CommonArithmeticException
import kotlin.ClassCastException as CommonClassCastException
import kotlin.ConcurrentModificationException as CommonConcurrentModificationException
import kotlin.Exception as CommonException
import kotlin.IllegalArgumentException as CommonIllegalArgumentException
import kotlin.IllegalStateException as CommonIllegalStateException
import kotlin.IndexOutOfBoundsException as CommonIndexOutOfBoundsException
import kotlin.NoSuchElementException as CommonNoSuchElementException
import kotlin.NullPointerException as CommonNullPointerException
import kotlin.NumberFormatException as CommonNumberFormatException
import kotlin.RuntimeException as CommonRuntimeException
import kotlin.UnsupportedOperationException as CommonUnsupportedOperationException

// Because KSP for JS (kspJs) is not able to resolve types for now
// We need to define a expect/actual to reuse the 'Error'
expect open class BaseExceptionAny constructor(message: String?, cause: Throwable?) {
    open val message: String?
    open val cause: Throwable?
}

/**
 * Cannot define this file in jsMain, Kotlin Js-IR limitation?
 */
@JsExport
open class Exception(message: String? = null, cause: Throwable? = null) : BaseExceptionAny(message, cause) {

    // "import": Cannot export common type BUT here we use a Typescript keyword 'import' to avoid
    // typescript method generation, see https://youtrack.jetbrains.com/issue/KT-38262
    // The resulting typescript (with 1.6.0-RC):
    /*
    class Exception {
        constructor(message: String? = null, stackTrace: String? = null);
        readonly message: string;
        readonly stackTrace: string;
        /* ErrorDeclaration: Name is a reserved word */
    }
     */
    // This trick should not be used everywhere, but for Exceptions it's useful
    // to make a method ignored from typescript code generation and offers a nicer API.
    // Hope to have an annotation like @JsIgnore and avoid this trick.
    open fun import() = CommonException(message, cause)
}

fun CommonException.export() = Exception(message, cause)

/*
@JsExport
open class Error(message: String? = null, cause: Throwable? = null) : BaseExceptionAny(message, cause) {
    open fun import() = CommonError(message, cause)
}

fun CommonError.export() = Error(message, cause)
*/

@JsExport
open class RuntimeException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    override fun import() = CommonRuntimeException(message, cause)
}

fun CommonRuntimeException.export() = RuntimeException(message, cause)

@JsExport
open class IllegalArgumentException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    override fun import() = CommonIllegalArgumentException(message, cause)
}

fun CommonIllegalArgumentException.export() = IllegalArgumentException(message, cause)

@JsExport
open class IllegalStateException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    override fun import() = CommonIllegalStateException(message, cause)
}

fun CommonIllegalStateException.export() = IllegalStateException(message, cause)

@JsExport
open class IndexOutOfBoundsException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    override fun import() = CommonIndexOutOfBoundsException(message + "\n" + cause)
}

fun CommonIndexOutOfBoundsException.export() = IndexOutOfBoundsException(message, cause)

@JsExport
open class ConcurrentModificationException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    override fun import() = CommonConcurrentModificationException(message + "\n" + cause)
}

fun CommonConcurrentModificationException.export() =
    ConcurrentModificationException(message, cause)

@JsExport
open class UnsupportedOperationException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    override fun import() = CommonUnsupportedOperationException(message, cause)
}

fun CommonUnsupportedOperationException.export() = UnsupportedOperationException(message, cause)

@JsExport
open class NumberFormatException(message: String? = null, cause: Throwable? = null) :
    IllegalArgumentException(message, cause) {
    override fun import() = CommonNumberFormatException(message + "\n" + cause)
}

fun CommonNumberFormatException.export() = NumberFormatException(message, cause)

@JsExport
open class NullPointerException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    override fun import() = CommonNullPointerException(message + "\n" + cause)
}

fun CommonNullPointerException.export() = NullPointerException(message, cause)

@JsExport
open class ClassCastException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    override fun import() = CommonClassCastException(message + "\n" + cause)
}

fun CommonClassCastException.export() = ClassCastException(message, cause)

/*
@JsExport
open class AssertionError(message: String? = null, cause: Throwable? = null) : Error(message, cause) {
    override fun import() = CommonAssertionError(message + "\n" + cause)
}
fun CommonAssertionError.export() = AssertionError(message, cause)
*/

@JsExport
open class NoSuchElementException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    override fun import() = CommonNoSuchElementException(message + "\n" + cause)
}

fun CommonNoSuchElementException.export() = NoSuchElementException(message, cause)

@JsExport
open class ArithmeticException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    override fun import() = CommonArithmeticException(message + "\n" + cause)
}

fun CommonArithmeticException.export() = ArithmeticException(message, cause)
