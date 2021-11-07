package sample.generics

import deezer.kustom.KustomExport

@KustomExport
interface GenericsBase {
    val b: String
}

@KustomExport
class KmpGenericsBase : GenericsBase {
    override val b: String = "KmpGenericsBase"
}

@KustomExport
interface InterfaceGenerics<T : GenericsBase> {
    fun generateBase(): T
    fun check(input: T): String
}
/*

@KustomExport
class ClassGenerics : InterfaceGenerics<KmpGenericsBase> {
    override fun generateBase(): KmpGenericsBase = KmpGenericsBase()

    override fun check(input: KmpGenericsBase): String {
        if (input is KmpGenericsBase) return "KmpGenericsBase"
        if (input is GenericsBase) return "GenericsBase"
        return "nothing"
    }
}*/
