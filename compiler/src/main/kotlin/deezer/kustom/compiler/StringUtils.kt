package deezer.kustom.compiler

fun shortNamesForIndex(index: Int): String {
    fun letterForIndex(index: Int): Char = 'a' + index
    val letter = letterForIndex(index % 26)
    return if (index >= 26) {
        shortNamesForIndex((index / 26) - 1) + letter
    } else {
        letter.toString()
    }
}
