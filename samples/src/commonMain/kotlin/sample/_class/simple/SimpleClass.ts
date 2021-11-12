import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("SimpleClass", () : void => {
    var simpleClass = new sample._class.simple.js.SimpleClass()
    assert(simpleClass.simpleValue == 42, "can retrieve value")
})