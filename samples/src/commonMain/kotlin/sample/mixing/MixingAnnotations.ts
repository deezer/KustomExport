import { runTest } from "../shared_ts/RunTest"
import { assertEquals, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("MixingAnnotations", () : void => {
    var j = new sample.mixing.JsExported()
    console.log(j)
    assertEquals(12, j.data, "hey")
    var k = new sample.mixing.js.KustomExported()
    console.log(k)
    assertEquals(12, k.data, "hey")

    var mixer = new sample.mixing.js.MixingAnnotations(j, k)
    console.log(mixer)
    var jj = mixer.makeJ()
    console.log(jj)
    var kk = mixer.makeK()
    console.log(kk)

    mixer.consumeJ(j)
    mixer.consumeK(k)
})