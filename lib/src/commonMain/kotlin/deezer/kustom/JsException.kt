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

// package kotlin cannot be used, so we can't enforce code consistency and need to make it explicit in processor too.
package deezer.kustom

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
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
class Exception(val stackTrace: String)

fun Exception.import() = CommonException(stackTrace)
fun CommonException.export() = Exception(message + "\n" + stackTraceToString())

@JsExport
class RuntimeException(val stackTrace: String)

fun RuntimeException.import() = CommonRuntimeException(stackTrace)
fun CommonRuntimeException.export() = RuntimeException(message + "\n" + stackTraceToString())

@JsExport
class IllegalArgumentException(val stackTrace: String)

fun IllegalArgumentException.import() = CommonIllegalArgumentException(stackTrace)
fun CommonIllegalArgumentException.export() = IllegalArgumentException(message + "\n" + stackTraceToString())

@JsExport
class IllegalStateException(val stackTrace: String)

fun IllegalStateException.import() = CommonIllegalStateException(stackTrace)
fun CommonIllegalStateException.export() = IllegalStateException(message + "\n" + stackTraceToString())

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
