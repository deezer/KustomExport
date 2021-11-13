package sample.generics

import deezer.kustom.KustomExportSamples

@KustomExportSamples
interface GenericsBase {
    val b: String
}

@KustomExportSamples
class KmpGenericsBase : GenericsBase {
    override val b: String = "KmpGenericsBase"
}

@KustomExportSamples
interface InterfaceGenerics<T : GenericsBase> {
    fun generateBase(): T
    fun check(input: T): String
}
/*

@KustomExportSamples
class ClassGenerics : InterfaceGenerics<KmpGenericsBase> {
    override fun generateBase(): KmpGenericsBase = KmpGenericsBase()

    override fun check(input: KmpGenericsBase): String {
        if (input is KmpGenericsBase) return "KmpGenericsBase"
        if (input is GenericsBase) return "GenericsBase"
        return "nothing"
    }
}*/
