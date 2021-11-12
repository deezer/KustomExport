package sample._exception

import deezer.kustom.KustomExport

@KustomExport
class ExceptionBuilder {
    fun buildException(msg: String) = Exception(msg)
    fun buildRuntimeException(msg: String) = RuntimeException(msg)
    fun buildIllegalArgumentException(msg: String) = IllegalArgumentException(msg)
    fun buildIllegalStateException(msg: String) = IllegalStateException(msg)
}

@KustomExport
class ExceptionConsumer {
    fun consume(e: Exception): String {
        return when (e) {
            is IllegalStateException -> "IllegalStateException:${e.message}"
            is IllegalArgumentException -> "IllegalArgumentException:${e.message}"
            is RuntimeException -> "RuntimeException:${e.message}"
            is Exception -> "Exception:${e.message}"
            else -> "Not an exception!!!"
        }
    }
}
