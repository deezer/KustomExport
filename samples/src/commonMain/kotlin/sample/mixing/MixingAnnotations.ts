import { runTest } from "../shared_ts/RunTest"
import { assertEquals, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("MixingAnnotations", () : void => {
    var j = new sample.mixing.JsExported()
    assertEquals(12, j.data, "Retrieve data from a @JsExport class")
    var k = new sample.mixing.js.KustomExported()
    assertEquals(12, k.data, "Retrieve data from a @KustomExport class")

    var mixer = new sample.mixing.js.MixingAnnotations(j, k)
    var jj = mixer.makeJ()
    var kk = mixer.makeK()

    assertEqualsQuiet("Consumed class JsExported - 12", mixer.consumeJ(j), "Consumed JsExported from Kotlin")
    assertEqualsQuiet("Consumed class KustomExported - 12", mixer.consumeK(k), "Consumed KustomExported from Kotlin")
})
