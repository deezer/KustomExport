package sample._common

import deezer.kustom.KustomExport

@KustomExport
class BasicTypeMapping {
    val bool: Boolean = true
    val byte: Byte = 0x42
    val char: Char = 'c'
    val short: Short = 0x7F7F
    val int: Int = 0x7F4251
    val float: Float = 12345.678f
    val double: Double = 123456789.123456789
    val string: String = "magic"
    val booleanArray: BooleanArray = BooleanArray(5) { it % 2 == 0 }
    val byteArray: ByteArray = ByteArray(5) { it.toByte() }
    val shortArray: ShortArray = ShortArray(5) { it.toShort() }
    val intArray: IntArray = IntArray(5) { it }
    val floatArray: FloatArray = FloatArray(5) { it.toFloat() }
    val doubleArray: DoubleArray = DoubleArray(5) { it.toDouble() }
    val charArray: CharArray = CharArray(5) { it.toChar() }

    // ...
    val any: Any = Any()
    val unit: Unit = Unit
}