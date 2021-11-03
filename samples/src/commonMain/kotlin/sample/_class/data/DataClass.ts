import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import Samples from '@kustom/Samples'

runTest("DataClass", () : void => {
    // Default values are used from Kotlin, but are not available on Ts
    var fromTs = new Samples.sample._class.data.js.DataClass("data", 2)
    var fromKotlin = Samples.sample._class.data.js.singletonDataClass
    assert(JSON.stringify(fromTs) == JSON.stringify(fromKotlin), "created from Kotlin and Typescript are equals")
})
