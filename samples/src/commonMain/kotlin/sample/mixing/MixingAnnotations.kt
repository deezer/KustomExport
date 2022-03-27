package sample.mixing

import deezer.kustomexport.KustomExport

@JsExport
open class JsExported {
    val data = 12
}

@KustomExport
open class KustomExported {
    val data = 12
}

@KustomExport
data class MixingAnnotations(val jsExported: JsExported, val kustomExported: KustomExported) {
    fun makeJ(): JsExported = JsExported()
    fun makeK(): KustomExported = KustomExported()
    fun consumeJ(j: JsExported) = println("Consumed $j")
    fun consumeK(k: KustomExported) = println("Consumed $k")
}
