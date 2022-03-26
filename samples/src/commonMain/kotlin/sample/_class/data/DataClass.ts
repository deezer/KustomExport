import { runTest } from "../../shared_ts/RunTest"
import { assertEquals, assertEqualsQuiet } from "../../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("DataClass", () : void => {
    // Default values are used from Kotlin, but are not available on Ts
    var factory = new sample._class.data.js.DataClassFactory()
    var fromTs = new sample._class.data.js.DataClass("data", 2)
    var fromKotlin = factory.create()
    assertEquals(JSON.stringify(fromTs), JSON.stringify(fromKotlin), "created from Kotlin and Typescript are equals")
    assertEqualsQuiet(fromTs.data1, "data", "can read data1")
    assertEqualsQuiet(fromTs.data2, 2, "can read data2")
})
