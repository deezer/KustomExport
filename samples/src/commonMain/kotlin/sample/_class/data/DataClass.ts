import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("DataClass", () : void => {
    // Default values are used from Kotlin, but are not available on Ts
    var fromTs = new sample._class.data.js.DataClass("data", 2)
    var fromKotlin = sample._class.data.js.singletonDataClass
    assert(JSON.stringify(fromTs) == JSON.stringify(fromKotlin), "created from Kotlin and Typescript are equals")
})
