import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import Samples from '@kustom/Samples'

runTest("SimpleClass", () : void => {
    var simpleClass = new Samples.sample._class.simple.js.SimpleClass()
    assert(simpleClass.simpleValue == 42, "can retrieve value")
})