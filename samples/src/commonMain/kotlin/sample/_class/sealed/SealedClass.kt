package sample._class.sealed

import deezer.kustom.KustomExportSamples

@KustomExportSamples
sealed class SealedParent(val ctorParam: Long) {
    abstract val prop: Int
    val hardcoded: Long = 42
    fun computeFoo() = 12345
}
// Should generate a class wrapper with 'protected' constructor (package visibility, broken with package erasure?)

@KustomExportSamples
class SealedChild1(override val prop: Int, ctorParam: Long) : SealedParent(ctorParam) {
    val child1Field: String = "child1"
    fun special() = "$prop-$ctorParam-$child1Field"
}

@KustomExportSamples
class SealedChild2(override var prop: Int) : SealedParent(94949) {
    val child2Field: String = "child2"
}

@KustomExportSamples
class SealedClassConsumer {
    fun consume(sealed: SealedParent): String {
        return when (sealed) {
            is SealedChild1 -> "child1:${sealed.child1Field}"
            is SealedChild2 -> "child2:${sealed.child2Field}"
        }
    }
}
