package sample._class.sealed

import deezer.kustom.KustomExport

//@KustomExport
sealed class SealedParent(val ctorParam: String) {
    abstract val field: Int
    val hardcoded: Long = 42
    fun computeFoo() = 12345
}
// Should generate a class wrapper with 'protected' constructor (package visibility, broken with package erasure?)

//@KustomExport
class SealedChild1(override val field: Int, ctorParam: String) : SealedParent(ctorParam) {
    val child1Field: String = "child1"
}

//@KustomExport
class SealedChild2(override var field: Int) : SealedParent("forced child 2 ctor value") {
    val child2Field: String = "child2"
}

@KustomExport
class SealedClassConsumer {
    fun consume(sealed: SealedParent): String {
        return when (sealed) {
            is SealedChild1 -> "child1:${sealed.child1Field}"
            is SealedChild2 -> "child2:${sealed.child2Field}"
        }
    }
}