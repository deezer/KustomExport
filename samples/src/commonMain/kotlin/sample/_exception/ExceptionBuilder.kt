package sample._exception

import deezer.kustom.KustomExportSamples

@KustomExportSamples
class ExceptionBuilder {
    fun buildArithmeticException(msg: String) = ArithmeticException(msg)
    fun buildAssertionError(msg: String) = AssertionError(msg)
    fun buildClassCastException(msg: String) = ClassCastException(msg)
    fun buildConcurrentModificationException(msg: String) = ConcurrentModificationException(msg)
    fun buildError(msg: String) = Error(msg)
    fun buildException(msg: String) = Exception(msg)
    fun buildIllegalArgumentException(msg: String) = IllegalArgumentException(msg)
    fun buildIllegalStateException(msg: String) = IllegalStateException(msg)
    fun buildIndexOutOfBoundsException(msg: String) = IndexOutOfBoundsException(msg)
    fun buildNoSuchElementException(msg: String) = NoSuchElementException(msg)
    fun buildNullPointerException(msg: String) = NullPointerException(msg)
    fun buildNumberFormatException(msg: String) = NumberFormatException(msg)
    fun buildRuntimeException(msg: String) = RuntimeException(msg)
    fun buildUnsupportedOperationException(msg: String) = UnsupportedOperationException(msg)
}

@KustomExportSamples
class ExceptionConsumer {
    fun consume(e: Exception): String {
        return when (e) {
            is ArithmeticException -> "ArithmeticException=${e.message}"
            is AssertionError -> "AssertionError=${e.message}"
            is ClassCastException -> "ClassCastException=${e.message}"
            is ConcurrentModificationException -> "ConcurrentModificationException=${e.message}"
            is NumberFormatException -> "NumberFormatException=${e.message}"
            is IllegalStateException -> "IllegalStateException=${e.message}"
            is IndexOutOfBoundsException -> "IndexOutOfBoundsException=${e.message}"
            is NoSuchElementException -> "NoSuchElementException=${e.message}"
            is NullPointerException -> "NullPointerException=${e.message}"
            is UnsupportedOperationException -> "UnsupportedOperationException=${e.message}"

            is IllegalArgumentException -> "IllegalArgumentException=${e.message}"

            is RuntimeException -> "RuntimeException=${e.message}"

            is Error -> "Error=${e.message}"
            is Exception -> "Exception=${e.message}"

            else -> "Not an exception!!!"
        }
    }
}
