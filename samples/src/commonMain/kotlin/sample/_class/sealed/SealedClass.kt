package sample._class.sealed

import deezer.kustomexport.KustomExport

@KustomExport
sealed class SealedParent(val ctorParam: Long) {
    abstract val prop: Int
    val hardcoded: Long = 42
    fun computeFoo() = 12345
}
// Should generate a class wrapper with 'protected' constructor (package visibility, broken with package erasure?)

@KustomExport
class SealedChild1(override val prop: Int, ctorParam: Long) : SealedParent(ctorParam) {
    val child1Field: String = "child1"
    fun special() = "$prop-$ctorParam-$child1Field"
}

@KustomExport
class SealedChild2(override var prop: Int) : SealedParent(94949) {
    val child2Field: String = "child2"
}

@KustomExport
sealed class ExtendedSealedParent(val foo: String) : SealedParent(4141)

@KustomExport
class ExtendedSealedChild(foo: String) : ExtendedSealedParent(foo) {
    override val prop: Int = 3 // TODO: KSP not detecting that as an override property when defined in ctor?
    val bar: Int = 9090
}

@KustomExport
class SealedClassConsumer {
    fun consume(sealed: SealedParent): String {
        return when (sealed) {
            is SealedChild1 -> "child1:${sealed.child1Field}"
            is SealedChild2 -> "child2:${sealed.child2Field}"
            is ExtendedSealedChild -> "extChild:${sealed.bar}"
        }
    }
}
