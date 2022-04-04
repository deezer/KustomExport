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
    fun consumeJ(j: JsExported) = "Consumed ${j::class} - ${j.data}"
    fun consumeK(k: KustomExported) = "Consumed ${k::class} - ${k.data}"
}
