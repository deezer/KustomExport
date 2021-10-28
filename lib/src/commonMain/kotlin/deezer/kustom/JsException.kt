// package kotlin cannot be used, so we can't enforce code consistency and need to make it explicit in processor too.
package deezer.kustom

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.Exception as CommonException

/**
 * Cannot define this file in jsMain, Kotlin Js-IR limitation?
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class Exception(val stackTrace: String)

fun Exception.import() = CommonException(stackTrace)
fun CommonException.export() = Exception(message + "\n" + stackTraceToString())
