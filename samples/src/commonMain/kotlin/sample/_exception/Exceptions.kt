package sample._exception

import deezer.kustomexport.KustomExport

@KustomExport
class MyEx(val cause: IllegalStateException = IllegalStateException("hello"))

@KustomExport
class ExceptionConsumer {
    fun consumeMessage(ex: MyEx): String {
        return "MyEx.cause.message=${ex.cause.message}"
    }

    fun consumeStackTrace(ex: MyEx): String {
        return "MyEx.cause.stackTraceToString=${ex.cause.stackTraceToString()}"
    }
}
