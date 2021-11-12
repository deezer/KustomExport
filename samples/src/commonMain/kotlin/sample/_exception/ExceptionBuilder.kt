package sample._exception

import deezer.kustom.KustomExport

@KustomExport
class ExceptionBuilder {
    fun buildException(msg: String) = Exception(msg)
    fun buildRuntimeException(msg: String) = RuntimeException(msg)
    fun buildIllegalArgumentException(msg: String) = IllegalArgumentException(msg)
    fun buildIllegalStateException(msg: String) = IllegalStateException(msg)
    fun buildIndexOutOfBoundsException(msg: String) = IndexOutOfBoundsException(msg)
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

    fun consume2(e: Exception): String {
        if (e is Exception) {
            println("is Exception")
        }
        if (e is IllegalArgumentException) {
            println("is IllegalArgumentException")
        }
        if (e is IndexOutOfBoundsException) {
            println("is IndexOutOfBoundsException")
        }
        return "IndexOutOfBoundsException:${e.message}"
    }
}
