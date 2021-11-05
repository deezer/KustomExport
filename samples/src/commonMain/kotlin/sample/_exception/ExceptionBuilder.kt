package sample._exception

import deezer.kustom.KustomExport

@KustomExport
class ExceptionBuilder {
    fun buildException(msg: String) = Exception(msg)
    fun buildIllegalStateException(msg: String) = IllegalStateException(msg)
}

@KustomExport
class ExceptionConsumer {
    fun consume(e: Exception) {
        when (e) {
            is IllegalStateException -> println("IllegalStateException")
            is Exception -> println("Exception")
            else -> println("Not an exception!!!")
        }
    }
}
