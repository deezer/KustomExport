// package kotlin cannot be used, so we can't enforce code consistency and need to make it explicit in processor too.
package deezer.kustom

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.Exception as CommonException
import kotlin.RuntimeException as CommonRuntimeException
import kotlin.IllegalArgumentException as CommonIllegalArgumentException
import kotlin.IllegalStateException as CommonIllegalStateException

//TODO : Error

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

//TODO : RuntimeException
//TODO : IllegalArgumentException
//TODO : IllegalStateException
//TODO : IndexOutOfBoundsException
//TODO : ConcurrentModificationException
//TODO : UnsupportedOperationException
//TODO : NumberFormatException
//TODO : NullPointerException
//TODO : ClassCastException
//TODO : AssertionError
//TODO : NoSuchElementException
//TODO : ArithmeticException
//TODO : NoWhenBranchMatchedException
//TODO : UninitializedPropertyAccessException